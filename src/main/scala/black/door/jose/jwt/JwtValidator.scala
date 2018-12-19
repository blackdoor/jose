package black.door.jose.jwt

import java.time.{Clock, Instant}

import scala.concurrent.{ExecutionContext, Future}

object JwtValidator {
  def combine[HP](validators: Seq[JwtValidator[HP]])(implicit ex: ExecutionContext) =
    validators.fold(fromSync(PartialFunction.empty))(_ orElse _)

  def fromSync[HP](validator: PartialFunction[Jwt[HP], String]): JwtValidator[HP] = validator.lift.andThen(Future.successful)

  def defaultValidator[HP](clock: Clock = Clock.systemDefaultZone): JwtValidator[HP] = {
    val now = Instant.now(clock)
    JwtValidator.fromSync {
      case Jwt(_, claims) if claims.exp.exists(_.isBefore(now)) => s"Token expired at ${claims.exp.get}. It was issued at ${claims.iat}."
      case Jwt(_, claims) if claims.nbf.exists(_.isAfter(now)) => s"Token will not be valid until ${claims.nbf.getOrElse("fail")}. It was issued at ${claims.iat}."
      case Jwt(_, claims) if claims.iat.exists(iat => claims.exp.exists(_.isBefore(iat))) => "Token was never valid, it expired before it was issued"
    }
  }

  implicit class JwtValidatorOps[HP](val v: JwtValidator[HP]) extends AnyVal {
    def orElse[HP2 >: HP](next: JwtValidator[HP2])(implicit ex: ExecutionContext): JwtValidator[HP2] = jwt => v(jwt).flatMap {
      case None => next(jwt)
      case some => Future.successful(some)
    }
  }

  def empty[HP]: JwtValidator[HP] = fromSync(PartialFunction.empty)
}

trait Check {
  def iss(check: String => Boolean, required: Boolean = true) =
    JwtValidator.fromSync[Any] { case jwt if !jwt.claims.iss.exists(check) || (required && jwt.claims.iss.isEmpty) => "Issuer invalid" }
  def sub(check: String => Boolean, required: Boolean = true) =
    JwtValidator.fromSync[Any] { case jwt if !jwt.claims.sub.exists(check) || (required && jwt.claims.sub.isEmpty) => "Subject invalid" }
  def aud(check: String => Boolean, required: Boolean = true) =
    JwtValidator.fromSync[Any] { case jwt if !jwt.claims.aud.exists(check) || (required && jwt.claims.aud.isEmpty) => "Audience invalid" }
}
object Check extends Check
