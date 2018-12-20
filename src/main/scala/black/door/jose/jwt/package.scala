package black.door.jose

import scala.concurrent.Future

package object jwt {

  type JwtValidator[UnregisteredClaims] = Jwt[Any, UnregisteredClaims] => Future[Option[String]]
}
