package black.door.jose.jwt

import java.security.KeyException
import java.util.concurrent.{Executors, TimeUnit}

import cats.data.{EitherT, OptionT}
import cats.implicits._
import black.door.jose.Mapper
import black.door.jose.jwk.Jwk
import black.door.jose.jws._
import black.door.jose.jwt.JwtValidator.JwtValidatorOps

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, ExecutionContext, Future}

case class Jwt(header: JwsHeader, claims: Claims) extends Jws[Claims] {
  def payload = claims
}

object Jwt {
  @throws[KeyException]
  def sign(claims: Claims, key: Jwk, signer: InputSigner = InputSigner.javaInputSigner)
          (implicit headerSerializer: Mapper[JwsHeader, Array[Byte]], payloadSerializer: Mapper[Claims, Array[Byte]]) = {
    val alg = key.alg.getOrElse(throw new KeyException("Jwk must have a defined alg to use Jwt.sign. Alternatively, create a Jwt with an explicit JwsHeader."))
    Jwt(JwsHeader(alg, typ = Some("JWT")), claims).sign(key, signer)
  }

  /**
    *
    * @param compact
    * @param keyResolver
    * @param jwtValidator
    * @param fallbackJwtValidator A validator that runs if all the validations from jwtValidator pass.
    *                             By default this checks temporal claims.
    * @param signatureValidator
    * @param payloadDeserializer
    * @param headerDeserializer
    * @param ec
    * @return
    */
  def validate(
                compact: String,
                keyResolver: KeyResolver[Claims],
                jwtValidator: JwtValidator = JwtValidator.empty,
                fallbackJwtValidator: JwtValidator = JwtValidator.defaultValidator(),
                signatureValidator: SignatureValidator = SignatureValidator.javaSignatureValidator
              )
              (
                implicit payloadDeserializer: Mapper[Array[Byte], Claims],
                headerDeserializer: Mapper[Array[Byte], JwsHeader],
                ec: ExecutionContext
              ): Future[Either[String, Jwt]] = {
    EitherT(Jws.validate[Claims](compact, keyResolver, signatureValidator))
      .flatMap { jws =>
        val jwt = Jwt(jws.header, jws.payload)
        OptionT(jwtValidator.orElse(fallbackJwtValidator).apply(jwt)).toLeft(jwt)
      }.value
  }

  private val sadSpasticLittleEc = ExecutionContext.fromExecutorService(Executors.newCachedThreadPool)

  def validateSync(compact: String,
                   keyResolver: KeyResolver[Claims],
                   jwtValidator: JwtValidator = JwtValidator.empty,
                   fallbackJwtValidator: JwtValidator = JwtValidator.defaultValidator(),
                   signatureValidator: SignatureValidator = SignatureValidator.javaSignatureValidator
                  )
                  (
                    implicit payloadDeserializer: Mapper[Array[Byte], Claims],
                    headerDeserializer: Mapper[Array[Byte], JwsHeader]
                  ) =
    Await.result(
      validate(compact, keyResolver, jwtValidator, fallbackJwtValidator, signatureValidator)(payloadDeserializer, headerDeserializer, sadSpasticLittleEc),
      Duration(1, TimeUnit.SECONDS)
    )
}
