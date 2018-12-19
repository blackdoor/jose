package black.door.jose

import scala.concurrent.Future

package object jwt {

  type StandardClaims = Claims[Unit]

  type JwtValidator[PrivateClaims] = Jwt[PrivateClaims] => Future[Option[String]]
}
