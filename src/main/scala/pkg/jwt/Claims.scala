package pkg.jwt

import java.time.Instant

case class Claims(
                   iss: Option[String] = None,
                   sub: Option[String] = None,
                   aud: Option[String] = None,
                   exp: Option[Instant] = None,
                   nbf: Option[Instant] = None,
                   iat: Option[Instant] = None,
                   jti: Option[String] = None
                 )
