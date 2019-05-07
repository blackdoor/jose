package black.door.jose.jwt

import black.door.jose.Mapper
import black.door.jose.json.playjson
import play.api.libs.json.Json

class PlayJsonJwtSpec extends JwtSpec with playjson.JsonSupport {

  implicit def customFormat = Json.format[MyCustomClaimsClass]

  val payloadUnitSerializer = implicitly[Mapper[Claims[Unit], Array[Byte]]]

  val payloadCustomSerializer =
    implicitly[Mapper[Claims[MyCustomClaimsClass], Array[Byte]]]

  val payloadUnitDeserializer = implicitly[Mapper[Array[Byte], Claims[Unit]]]

  val payloadCustomDeserializer =
    implicitly[Mapper[Array[Byte], Claims[MyCustomClaimsClass]]]
}
