package black.door.jose

import black.door.jose.json.ninny.JsonSupport
import black.door.jose.jwt.Claims

class NinnyJsonJwtSpec extends JwtSpec with JsonSupport {

  val payloadUnitSerializer   = implicitly[ByteSerializer[Claims[Unit]]]
  val payloadUnitDeserializer = implicitly[ByteDeserializer[Claims[Unit]]]

  import io.github.kag0.ninny.Auto._
  val payloadCustomDeserializer = implicitly[ByteDeserializer[Claims[MyCustomClaimsClass]]]
  val payloadCustomSerializer   = implicitly[ByteSerializer[Claims[MyCustomClaimsClass]]]
}
