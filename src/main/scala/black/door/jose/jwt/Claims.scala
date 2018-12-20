package black.door.jose.jwt

import java.time.Instant

case class Claims[UnregisteredClaims](
                            iss: Option[String] = None,
                            sub: Option[String] = None,
                            aud: Option[String] = None,
                            exp: Option[Instant] = None,
                            nbf: Option[Instant] = None,
                            iat: Option[Instant] = None,
                            jti: Option[String] = None,
                            unregistered: UnregisteredClaims = () // note: UnregisteredClaims cannot be an option type with some json marshallers
                 )