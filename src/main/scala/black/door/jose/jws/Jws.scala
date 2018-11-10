package black.door.jose.jws

import java.util.Base64

import cats.data.EitherT
import cats.implicits._
import black.door.jose.Mapper
import black.door.jose.jwk.Jwk

import scala.collection.immutable.Seq
import scala.concurrent.{ExecutionContext, Future}
import scala.util.Try

trait Jws[A] {
  def header: JwsHeader
  def payload: A

  def sign(key: Jwk, signer: InputSigner = InputSigner.javaInputSigner)
          (
            implicit headerSerializer: Mapper[JwsHeader, Array[Byte]],
            payloadSerializer: Mapper[A, Array[Byte]]
          ): String = {
    val encoder = Base64.getUrlEncoder.withoutPadding
    val Right(headerCompact) = headerSerializer(header).map(encoder.encodeToString)
    val Right(payloadCompact) = payloadSerializer(payload).map(encoder.encodeToString)
    val signingInput = s"$headerCompact.$payloadCompact"
    InputSigner.keyHeaderPreSigner.orElse(signer)
      .andThen(encoder.encodeToString)
      .andThen(signature => s"$signingInput.$signature")(
      (
        key,
        header,
        signingInput
      )
    )
  }
}

case class GenericJws[A](header: JwsHeader, payload: A) extends Jws[A]

case class JwsHeader(
                      alg: String,
                      jku: Option[String] = None,
                      jwk: Option[Jwk] = None,
                      kid: Option[String] = None,
                      typ: Option[String] = None,
                      cty: Option[String] = None,
                      crit: Option[Seq[String]] = None
                    )

object Jws {
  def apply[A](header: JwsHeader, payload: A) = GenericJws(header, payload)

  // return (signingInput, header, payload, signature)
  private def parse[A](compact: String)
                      (
                        implicit payloadDeserializer: Mapper[Array[Byte], A],
                        headerDeserializer: Mapper[Array[Byte], JwsHeader]
                      ): Either[String, (String, JwsHeader, A, Array[Byte])] =
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

  def validate[A](compact: String, keyResolver: KeyResolver, validator: SignatureValidator = SignatureValidator.javaSignatureValidator)
                 (
                   implicit payloadDeserializer: Mapper[Array[Byte], A],
                   headerDeserializer: Mapper[Array[Byte], JwsHeader],
                   ec: ExecutionContext
                 ): Future[Either[String, Jws[A]]] = (
    for {
      tup <- EitherT.fromEither[Future](parse[A](compact))
      (signingInput, header, payload, signature) = tup
      key <- keyResolver.resolve(header, signature)
      jws <- EitherT.fromOption[Future](
        SignatureValidator.keyHeaderPreValidator.orElse(validator)
          .andThen(if(_) None else Some("Signature was invalid"))
          .applyOrElse[(Jwk, JwsHeader, String, Array[Byte]), Option[String]]((key, header, signingInput, signature), _ => Some("Algorithm not supported")),
        Jws[A](header, payload)
      ).swap
    } yield jws
    ).value
}
