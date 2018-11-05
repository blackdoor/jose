package pkg.jwt

import java.time.{Clock, Instant}

import scala.concurrent.{ExecutionContext, Future}

object JwtValidator {
  def combine(validators: Seq[JwtValidator])(implicit ex: ExecutionContext) =
    validators.fold(fromSync(PartialFunction.empty))(_ orElse _)

  def fromSync(validator: PartialFunction[Jwt, String]): JwtValidator = validator.lift.andThen(Future.successful)

  def defaultValidator(clock: Clock = Clock.systemDefaultZone): JwtValidator = {
    val now = Instant.now(clock)
    JwtValidator.fromSync {
      case Jwt(_, claims) if claims.exp.exists(_.isBefore(now)) => "Token has expired"
      case Jwt(_, claims) if claims.nbf.exists(_.isAfter(now)) => s"Token will not be valid until ${claims.nbf.getOrElse("fail")}"
    }
  }

  implicit class JwtValidatorOps(val v: JwtValidator) extends AnyVal {
    def orElse(next: JwtValidator)(implicit ex: ExecutionContext): JwtValidator = jwt => v(jwt).flatMap {
      case None => next(jwt)
      case some => Future.successful(some)
    }
  }
}

trait Check {
  def iss(check: String => Boolean) =
    JwtValidator.fromSync { case jwt if !jwt.claims.iss.exists(check) => "Issuer invalid"}
  def sub(check: String => Boolean) =
    JwtValidator.fromSync { case jwt if !jwt.claims.sub.exists(check) => "Subject invalid"}
  def aud(check: String => Boolean) =
    JwtValidator.fromSync { case jwt if !jwt.claims.aud.exists(check) => "Audience invalid"}
}
object Check extends Check
