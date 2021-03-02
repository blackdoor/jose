package black.door.jose

import black.door.jose.json.ninny.JsonSupport
import black.door.jose.jwt.Claims
import io.github.kag0.ninny.Auto._

class NinnyJsonJwtSpec extends JwtSpec with JsonSupport {

  val payloadUnitSerializer     = implicitly[ByteSerializer[Claims[Unit]]]
  val payloadUnitDeserializer   = implicitly[ByteDeserializer[Claims[Unit]]]
  val payloadCustomDeserializer = implicitly[ByteDeserializer[Claims[MyCustomClaimsClass]]]
  val payloadCustomSerializer   = implicitly[ByteSerializer[Claims[MyCustomClaimsClass]]]
}
