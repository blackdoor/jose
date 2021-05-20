package black.door.jose.jwt

import black.door.jose.jwa.{SignatureAlgorithm, SignatureAlgorithms}
import black.door.jose.jwk.Jwk
import black.door.jose.jws._
import black.door.jose.{ByteDeserializer, ByteSerializer}
import cats.data.{EitherT, OptionT}
import cats.implicits._

import java.security.KeyException
import java.util.concurrent.TimeUnit
import scala.collection.immutable.Seq
import scala.concurrent.duration.Duration
import scala.concurrent.{Await, ExecutionContext, Future}

case class Jwt[+UnregisteredClaims](header: JwsHeader, claims: Claims[UnregisteredClaims])
    extends Jws[Claims[UnregisteredClaims]] {
  def payload = claims
}

object Jwt {

  @throws[KeyException]
  def sign[PC](
      claims: Claims[PC],
      key: Jwk,
      algorithms: Seq[SignatureAlgorithm] = SignatureAlgorithms.all
    )(
      implicit headerSerializer: ByteSerializer[JwsHeader],
      payloadSerializer: ByteSerializer[Claims[PC]]
    ) = {
    val alg = key.alg.getOrElse(
      throw new KeyException(
        "Jwk must have a defined alg to use Jwt.sign. Alternatively, create a Jwt with an explicit JwsHeader."
      )
    )
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
    * @tparam C unregistered claims type
    * @return
    */
  def validate[C](
      compact: String,
      keyResolver: KeyResolver[Claims[C]],
      jwtValidator: JwtValidator[C] = JwtValidator.empty,
      fallbackJwtValidator: JwtValidator[C] = JwtValidator.defaultValidator(),
      algorithms: Seq[SignatureAlgorithm] = SignatureAlgorithms.all
    )(
      implicit payloadDeserializer: ByteDeserializer[Claims[C]],
      headerDeserializer: ByteDeserializer[JwsHeader],
      ec: ExecutionContext
    ): Future[Either[String, Jwt[C]]] =
    EitherT(Jws.validate[Claims[C]](compact, keyResolver, algorithms)).flatMap { jws =>
      val jwt = Jwt(jws.header, jws.payload)
      OptionT(jwtValidator.orElse(fallbackJwtValidator).apply(jwt)).toLeft(jwt)
    }.value

  def validate(compact: String) = new UnitValidation(compact)

  protected class UnitValidation(protected val compact: String) extends TypedValidation[Unit] {
    self =>

    def apply[A] = new TypedValidation[A] {
      protected def compact = self.compact
    }
  }

  private val sameThreadExecutionContext = new ExecutionContext {
    def execute(runnable: Runnable)     = runnable.run()
    def reportFailure(cause: Throwable) = cause.printStackTrace()
  }

  sealed trait TypedValidation[C] {
    protected def compact: String

    def using(
        keyResolver: KeyResolver[Claims[C]],
        jwtValidator: JwtValidator[C] = JwtValidator.empty,
        fallbackJwtValidator: JwtValidator[C] = JwtValidator.defaultValidator(),
        algorithms: Seq[SignatureAlgorithm] = SignatureAlgorithms.all
      )(
        implicit payloadDeserializer: ByteDeserializer[Claims[C]],
        headerDeserializer: ByteDeserializer[JwsHeader]
      ) = new Using(keyResolver, jwtValidator, fallbackJwtValidator, algorithms)

    protected class Using(
        keyResolver: KeyResolver[Claims[C]],
        jwtValidator: JwtValidator[C],
        fallbackJwtValidator: JwtValidator[C],
        algorithms: Seq[SignatureAlgorithm]
      )(
        implicit payloadDeserializer: ByteDeserializer[Claims[C]],
        headerDeserializer: ByteDeserializer[JwsHeader]
      ) {

      def now = Await.result(
        validate(compact, keyResolver, jwtValidator, fallbackJwtValidator, algorithms)(
          payloadDeserializer,
          headerDeserializer,
          sameThreadExecutionContext
        ),
        Duration(30, TimeUnit.SECONDS)
      )

      def async(implicit ec: ExecutionContext) =
        validate(compact, keyResolver, jwtValidator, fallbackJwtValidator, algorithms)
    }
  }
}
