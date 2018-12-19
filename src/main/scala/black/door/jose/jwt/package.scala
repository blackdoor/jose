package black.door.jose

import scala.concurrent.Future

package object jwt {

  type RegisteredClaims = Claims[Unit]

  type JwtValidator[UnregisteredClaims] = Jwt[UnregisteredClaims] => Future[Option[String]]
}
