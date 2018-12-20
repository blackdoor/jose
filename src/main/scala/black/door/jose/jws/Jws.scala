package black.door.jose.jws

import java.util.Base64

import black.door.jose.{Mapper, Unmapper}
import black.door.jose.jwa.{SignatureAlgorithm, SignatureAlgorithms}
import black.door.jose.jwk.Jwk
import cats.Functor
import cats.data.EitherT
import cats.implicits._
import com.typesafe.scalalogging.Logger

import scala.collection.immutable.Seq
import scala.concurrent.{ExecutionContext, Future}
import scala.util.Try

trait Jws[+HeaderParameters, Payload] {
  def header: JwsHeader[HeaderParameters]
  def payload: Payload

  private lazy val logger = Logger(classOf[Jws[HeaderParameters, Payload]])

  def sign(key: Jwk, algorithms: Seq[SignatureAlgorithm] = SignatureAlgorithms.all)
          (
            implicit headerSerializer: Mapper[JwsHeader[HeaderParameters], Array[Byte]],
            payloadSerializer: Mapper[Payload, Array[Byte]]
          ): String = {
    val encoder = Base64.getUrlEncoder.withoutPadding
    val headerCompact = encoder.encodeToString(headerSerializer(header))
    val payloadCompact = encoder.encodeToString(payloadSerializer(payload))
    val signingInput = s"$headerCompact.$payloadCompact"
    val signerTuple = (key, header, signingInput)
    val definedAlgs = algorithms.filter(_.sign.isDefinedAt(signerTuple))

    val oddAlgs = definedAlgs.filterNot(_.alg.toLowerCase == header.alg.toLowerCase)
    if (oddAlgs.nonEmpty) logger.warn(
      s"Signing algorithms ${oddAlgs.map(_.alg).mkString(", ")} " +
      s"reported being applicable for ${header.alg}. This may indicate an improperly implemented SignatureAlgorithm."
    )

    InputSigner.keyHeaderPreSigner.orElse(
      definedAlgs
        .map(_.sign)
        .reduceOption(_ orElse _)
        .getOrElse(PartialFunction.empty)
    )
      .andThen(encoder.encodeToString)
      .andThen(signature => s"$signingInput.$signature")(signerTuple)
  }
}

case class GenericJws[HeaderParameters, Payload](header: JwsHeader[HeaderParameters], payload: Payload) extends Jws[HeaderParameters, Payload]

case class JwsHeader[+UnregisteredParameters](
                      alg: String,
                      jku: Option[String] = None,
                      jwk: Option[Jwk] = None,
                      kid: Option[String] = None,
                      typ: Option[String] = None,
                      cty: Option[String] = None,
                      crit: Option[Seq[String]] = None,
                      unregistered: UnregisteredParameters = ()
                    )
object JwsHeader {
  implicit val functor = new Functor[JwsHeader] {
    def map[A, B](fa: JwsHeader[A])(f: A => B) = fa.copy(unregistered = f(fa.unregistered))
  }
}

object Jws {
  private lazy val logger = Logger(Jws.getClass)

  def apply[HP, PL](header: JwsHeader[HP], payload: PL) = GenericJws(header, payload)

  // return (signingInput, header, payload, signature)
  private def parse[HP, PL](compact: String)
                      (
                        implicit payloadDeserializer: Unmapper[Array[Byte], PL],
                        headerDeserializer: Unmapper[Array[Byte], JwsHeader[HP]]
                      ): Either[String, (String, JwsHeader[HP], PL, Array[Byte])] =
    for {
      arr <- {
        val arr = compact.split('.')
        if(arr.length == 3) Right(arr) else Left("Compact JWS did not have three parts")
      }
      (headerC, payloadC, signatureC) = (arr(0), arr(1), arr(2))
      signingInput = s"$headerC.$payloadC"
      decoder = Base64.getUrlDecoder
      headerBytes <- Try(decoder.decode(headerC)).toEither.left.map(_.getMessage)
      header <- headerDeserializer(headerBytes)
      payloadBytes <- Try(decoder.decode(payloadC)).toEither.left.map(_.getMessage)
      payload <- payloadDeserializer(payloadBytes)
      signature <- Try(decoder.decode(signatureC)).toEither.left.map(_.getMessage)
    } yield (signingInput, header, payload, signature)

  def validate[HP, PL](compact: String, keyResolver: KeyResolver[PL], algorithms: Seq[SignatureAlgorithm] = SignatureAlgorithms.all)
                 (
                   implicit payloadDeserializer: Unmapper[Array[Byte], PL],
                   headerDeserializer: Unmapper[Array[Byte], JwsHeader[HP]],
                   ec: ExecutionContext
                 ): Future[Either[String, Jws[HP, PL]]] = (
    for {
      tup <- EitherT.fromEither[Future](parse[HP, PL](compact))
      (signingInput, header, payload, signature) = tup
      key <- keyResolver.resolve(header, payload)
      validatorTuple = (key, header, signingInput, signature)
      definedAlgs = algorithms.filter(_.validate.isDefinedAt(validatorTuple))

      _ = {
        val oddAlgs = definedAlgs.filterNot(_.alg.toLowerCase == header.alg.toLowerCase)
        if (oddAlgs.nonEmpty) logger.warn(
          s"Signing algorithms ${oddAlgs.map(_.alg).mkString(", ")} " +
          s"reported being applicable for ${header.alg}. This may indicate an improperly implemented SignatureAlgorithm."
        )
      }

      jws <- EitherT.fromOption[Future](
        SignatureValidator.keyHeaderPreValidator.orElse(
          definedAlgs
            .map(_.validate)
            .reduceOption(_ orElse _)
            .getOrElse(PartialFunction.empty)
        )
          .andThen(if(_) None else Some("Signature was invalid"))
          .applyOrElse[(Jwk, JwsHeader[HP], String, Array[Byte]), Option[String]](
            (key, header, signingInput, signature),
            _ => Some("Algorithm not supported")
        ),
        Jws[HP, PL](header, payload) // todo optional type params there?
      ).swap
    } yield jws
    ).value
}
