package pkg

import scala.concurrent.Future

package object jwt {
  type JwtValidator = Jwt => Future[Option[String]]
}
