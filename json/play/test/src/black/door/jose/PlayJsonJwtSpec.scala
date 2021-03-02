package black.door.jose

import black.door.jose.json.playjson
import black.door.jose.jwt.Claims
import play.api.libs.json.Json

class PlayJsonJwtSpec extends JwtSpec with playjson.JsonSupport {

  implicit def customFormat = Json.format[MyCustomClaimsClass]

  val payloadUnitSerializer = implicitly[ByteSerializer[Claims[Unit]]]

  val payloadCustomSerializer =
    implicitly[ByteSerializer[Claims[MyCustomClaimsClass]]]

  val payloadUnitDeserializer = implicitly[ByteDeserializer[Claims[Unit]]]

  val payloadCustomDeserializer =
    implicitly[ByteDeserializer[Claims[MyCustomClaimsClass]]]
}
