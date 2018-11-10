package black.door.jose.jwt

import java.security.KeyException
import java.util.concurrent.{Executors, TimeUnit}

import cats.data.{EitherT, OptionT}
import cats.implicits._
import black.door.jose.Mapper
import black.door.jose.jwk.Jwk
import black.door.jose.jws._

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

  def validate(
                   compact: String,
                   keyResolver: KeyResolver,
                   jwtValidator: JwtValidator = JwtValidator.defaultValidator(),
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
        OptionT(jwtValidator(jwt)).toLeft(jwt)
      }.value
  }

  private val sadSpasticLittleEc = ExecutionContext.fromExecutorService(Executors.newCachedThreadPool)

  def validateSync(compact: String,
                   keyResolver: KeyResolver,
                   jwtValidator: JwtValidator = JwtValidator.defaultValidator(),
                   signatureValidator: SignatureValidator = SignatureValidator.javaSignatureValidator
                  )
                  (
                    implicit payloadDeserializer: Mapper[Array[Byte], Claims],
                    headerDeserializer: Mapper[Array[Byte], JwsHeader]
                  ) = Await.result(
    validate(compact, keyResolver, jwtValidator, signatureValidator)(payloadDeserializer, headerDeserializer, sadSpasticLittleEc),
    Duration(1, TimeUnit.SECONDS)
  )
}
