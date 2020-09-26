package black.door.jose

import black.door.jose.json.ninny.JsonSupport
import black.door.jose.jwt.Claims
import io.github.kag0.ninny.Auto._

class NinnyJsonJwtSpec extends JwtSpec with JsonSupport {

  val payloadUnitSerializer     = implicitly[Mapper[Claims[Unit], Array[Byte]]]
  val payloadUnitDeserializer   = implicitly[Mapper[Array[Byte], Claims[Unit]]]
  val payloadCustomDeserializer = implicitly[Mapper[Array[Byte], Claims[MyCustomClaimsClass]]]
  val payloadCustomSerializer   = implicitly[Mapper[Claims[MyCustomClaimsClass], Array[Byte]]]
}
