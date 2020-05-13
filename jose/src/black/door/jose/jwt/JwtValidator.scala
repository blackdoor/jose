package black.door.jose.jwt

import java.time.{Clock, Instant}

import scala.concurrent.{ExecutionContext, Future}
import scala.language.implicitConversions

object JwtValidator {

  def combine[C](validators: Seq[JwtValidator[C]])(implicit ex: ExecutionContext) =
    validators.fold(fromSync(PartialFunction.empty))(_ orElse _)

  def fromSync[C](validator: PartialFunction[Jwt[C], String]): JwtValidator[C] =
    validator.lift.andThen(Future.successful)

  private def iatMessage(maybeIat: Option[Instant]) =
    maybeIat.map(iat => s"It was issued at $iat.").getOrElse("")

  def defaultValidator(clock: Clock = Clock.systemDefaultZone) = {
    val now = Instant.now(clock)
    JwtValidator.fromSync[Any] {
      case Jwt(_, claims) if claims.exp.exists(_.isBefore(now)) =>
        s"Token expired at ${claims.exp.get}.${iatMessage(claims.iat)} It is now $now."
      case Jwt(_, claims) if claims.nbf.exists(_.isAfter(now)) =>
        s"Token will not be valid until ${claims.nbf.getOrElse("fail")}.${iatMessage(claims.iat)} It is now $now."
      case Jwt(_, claims) if claims.iat.exists(iat => claims.exp.exists(_.isBefore(iat))) =>
        "Token was never valid, it expired before it was issued"
    }
  }

  implicit class JwtValidatorOps[C](val v: JwtValidator[C]) extends AnyVal {

    def orElse(next: JwtValidator[C])(implicit ex: ExecutionContext): JwtValidator[C] =
      jwt =>
        v(jwt).flatMap {
          case None => next(jwt)
          case some => Future.successful(some)
        }
  }

  def empty[C] = fromSync[C](PartialFunction.empty)
}

trait Check {

  def iss[C](check: String => Boolean, required: Boolean = true) =
    JwtValidator.fromSync[C] {
      case jwt if !jwt.claims.iss.exists(check) || (required && jwt.claims.iss.isEmpty) =>
        "Issuer invalid"
    }

  def sub[C](check: String => Boolean, required: Boolean = true) =
    JwtValidator.fromSync[C] {
      case jwt if !jwt.claims.sub.exists(check) || (required && jwt.claims.sub.isEmpty) =>
        "Subject invalid"
    }

  def aud[C](check: String => Boolean, required: Boolean = true) =
    JwtValidator.fromSync[C] {
      case jwt if !jwt.claims.aud.exists(check) || (required && jwt.claims.aud.isEmpty) =>
        "Audience invalid"
    }
}
object Check extends Check
