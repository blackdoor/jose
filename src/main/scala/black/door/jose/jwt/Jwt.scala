package black.door.jose.jwt

import java.security.KeyException
import java.util.concurrent.{Executors, TimeUnit}

import cats.data.{EitherT, OptionT}
import cats.implicits._
import black.door.jose.Mapper
import black.door.jose.jwa.{SignatureAlgorithm, SignatureAlgorithms}
import black.door.jose.jwk.Jwk
import black.door.jose.jws._
import black.door.jose.jwt.JwtValidator.JwtValidatorOps

import scala.collection.immutable.Seq
import scala.concurrent.duration.Duration
import scala.concurrent.{Await, ExecutionContext, Future}

case class Jwt[PrivateClaims](header: JwsHeader, claims: Claims[PrivateClaims]) extends Jws[Claims[PrivateClaims]] {
  def payload = claims
}

object Jwt {
  @throws[KeyException]
  def sign[PC](claims: Claims[PC], key: Jwk, algorithms: Seq[SignatureAlgorithm] = SignatureAlgorithms.all)
          (implicit headerSerializer: Mapper[JwsHeader, Array[Byte]], payloadSerializer: Mapper[Claims[PC], Array[Byte]]) = {
    val alg = key.alg.getOrElse(throw new KeyException("Jwk must have a defined alg to use Jwt.sign. Alternatively, create a Jwt with an explicit JwsHeader."))
    Jwt(JwsHeader(alg, typ = Some("JWT"), kid = key.kid), claims).sign(key, algorithms)
  }

  /**
    *
    * @param compact
    * @param keyResolver
    * @param jwtValidator
    * @param fallbackJwtValidator A validator that runs if all the validations from jwtValidator pass.
    *                             By default this checks temporal claims.
    * @param algorithms
    * @param payloadDeserializer
    * @param headerDeserializer
    * @param ec
    * @return
    */
  def validate[C](
                compact: String,
                keyResolver: KeyResolver[Claims[C]],
                jwtValidator: JwtValidator[C] = JwtValidator.empty,
                fallbackJwtValidator: JwtValidator[C] = JwtValidator.defaultValidator(),
                algorithms: Seq[SignatureAlgorithm] = SignatureAlgorithms.all
              )
              (
                implicit payloadDeserializer: Mapper[Array[Byte], Claims[C]],
                headerDeserializer: Mapper[Array[Byte], JwsHeader],
                ec: ExecutionContext
              ): Future[Either[String, Jwt[C]]] = {
    EitherT(Jws.validate[Claims[C]](compact, keyResolver, algorithms))
      .flatMap { jws =>
        val jwt = Jwt(jws.header, jws.payload)
        OptionT(jwtValidator.orElse(fallbackJwtValidator).apply(jwt)).toLeft(jwt)
      }.value
  }

  private val sadSpasticLittleEc = ExecutionContext.fromExecutorService(Executors.newCachedThreadPool)

  /*
  def validateSync(compact: String,
                   keyResolver: KeyResolver[StandardClaims],
                   jwtValidator: JwtValidator[Unit] = JwtValidator.empty,
                   fallbackJwtValidator: JwtValidator[Unit] = JwtValidator.defaultValidator(),
                   algorithms: Seq[SignatureAlgorithm] = SignatureAlgorithms.all
                  )
                  (
                    implicit payloadDeserializer: Mapper[Array[Byte], StandardClaims],
                    headerDeserializer: Mapper[Array[Byte], JwsHeader]
                  ) = validateSync[Unit](compact, keyResolver, jwtValidator, fallbackJwtValidator, algorithms)
  */

  def validateSync[C](compact: String,
                   keyResolver: KeyResolver[Claims[C]],
                   jwtValidator: JwtValidator[C] = JwtValidator.empty[C],
                   fallbackJwtValidator: JwtValidator[C] = JwtValidator.defaultValidator[C](),
                   algorithms: Seq[SignatureAlgorithm] = SignatureAlgorithms.all
                  )
                  (
                    implicit payloadDeserializer: Mapper[Array[Byte], Claims[C]],
                    headerDeserializer: Mapper[Array[Byte], JwsHeader]
                  ) =
    Await.result(
      validate(compact, keyResolver, jwtValidator, fallbackJwtValidator, algorithms)
              (payloadDeserializer, headerDeserializer, sadSpasticLittleEc),
      Duration(1, TimeUnit.SECONDS)
    )

  object validate {

    def apply =
    

    class DoIt[C]

    private def doIt[C](
                  compact: String,
                  keyResolver: KeyResolver[Claims[C]],
                  jwtValidator: JwtValidator[C] = JwtValidator.empty,
                  fallbackJwtValidator: JwtValidator[C] = JwtValidator.defaultValidator(),
                  algorithms: Seq[SignatureAlgorithm] = SignatureAlgorithms.all
                )
                (
                  implicit payloadDeserializer: Mapper[Array[Byte], Claims[C]],
                  headerDeserializer: Mapper[Array[Byte], JwsHeader],
                  ec: ExecutionContext
                ): Future[Either[String, Jwt[C]]] = {
      EitherT(Jws.validate[Claims[C]](compact, keyResolver, algorithms))
        .flatMap { jws =>
          val jwt = Jwt(jws.header, jws.payload)
          OptionT(jwtValidator.orElse(fallbackJwtValidator).apply(jwt)).toLeft(jwt)
        }.value
    }
  }
}
