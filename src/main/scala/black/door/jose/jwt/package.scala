package black.door.jose

import scala.concurrent.Future

package object jwt {
  type JwtValidator[HP] = Jwt[HP] => Future[Option[String]]
}
