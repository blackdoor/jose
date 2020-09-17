package black.door.jose

import black.door.jose.json.circe
import black.door.jose.jwt.Claims
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}
import io.circe.{Decoder, Encoder}

class CirceJwtSpec extends JwtSpec with circe.JsonSupport {

  implicit val decoder: Decoder[MyCustomClaimsClass] = deriveDecoder
  implicit val encoder: Encoder[MyCustomClaimsClass] = deriveEncoder

  val payloadUnitSerializer = implicitly[Mapper[Claims[Unit], Array[Byte]]]

  val payloadCustomSerializer =
    implicitly[Mapper[Claims[MyCustomClaimsClass], Array[Byte]]]

  val payloadUnitDeserializer = implicitly[Mapper[Array[Byte], Claims[Unit]]]

  val payloadCustomDeserializer =
    implicitly[Mapper[Array[Byte], Claims[MyCustomClaimsClass]]]
}
