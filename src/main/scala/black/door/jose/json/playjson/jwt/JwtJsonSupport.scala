package black.door.jose.json.playjson.jwt

import black.door.jose.jwt.Claims
import play.api.libs.json.{Json => PJson}
import black.door.jose.json.playjson._

trait JwtJsonSupport {
  implicit val claimsFormat = PJson.format[Claims]
  implicit val claimsSerializer = jsonSerializer[Claims]
  implicit val claimsDeserializer = jsonDeserializer[Claims]
}
object JwtJsonSupport extends JwtJsonSupport