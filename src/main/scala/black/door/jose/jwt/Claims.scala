package black.door.jose.jwt

import java.time.Instant

case class Claims[Private](
                   iss: Option[String] = None,
                   sub: Option[String] = None,
                   aud: Option[String] = None,
                   exp: Option[Instant] = None,
                   nbf: Option[Instant] = None,
                   iat: Option[Instant] = None,
                   jti: Option[String] = None,
                   privateClaims: Private
                 )

object StandardClaims {
  def apply(
             iss: Option[String] = None,
             sub: Option[String] = None,
             aud: Option[String] = None,
             exp: Option[Instant] = None,
             nbf: Option[Instant] = None,
             iat: Option[Instant] = None,
             jti: Option[String] = None,
           ): StandardClaims = Claims (iss, sub, aud, exp, nbf, iat, jti, Unit)
}