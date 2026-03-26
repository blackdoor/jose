package black.door.jose

import black.door.jose.json.ninny.JsonSupport
import black.door.jose.jwt.Claims
import nrktkt.ninny.{ToJson, ToSomeJsonObject}
import nrktkt.ninny.FromJson

class NinnyJsonJwtSpec extends JwtSpec with JsonSupport {

  val payloadUnitSerializer   = implicitly[ByteSerializer[Claims[Unit]]]
  val payloadUnitDeserializer = implicitly[ByteDeserializer[Claims[Unit]]]

  implicit val customClaimsToJsonObject: ToSomeJsonObject[MyCustomClaimsClass] =
    ToJson.auto[MyCustomClaimsClass].toSome(_)
  implicit val customClaimsFromJson = FromJson.auto[MyCustomClaimsClass]
  val payloadCustomDeserializer = implicitly[ByteDeserializer[Claims[MyCustomClaimsClass]]]
  val payloadCustomSerializer   = implicitly[ByteSerializer[Claims[MyCustomClaimsClass]]]
}
