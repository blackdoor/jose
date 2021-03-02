package black.door.jose

import black.door.jose.json.circe
import black.door.jose.jwt.Claims
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}
import io.circe.{Decoder, Encoder}

class CirceJwtSpec extends JwtSpec with circe.JsonSupport {

  implicit val decoder: Decoder[MyCustomClaimsClass] = deriveDecoder
  implicit val encoder: Encoder[MyCustomClaimsClass] = deriveEncoder

  val payloadUnitSerializer = implicitly[ByteSerializer[Claims[Unit]]]

  val payloadCustomSerializer =
    implicitly[ByteSerializer[Claims[MyCustomClaimsClass]]]

  val payloadUnitDeserializer = implicitly[ByteDeserializer[Claims[Unit]]]

  val payloadCustomDeserializer =
    implicitly[ByteDeserializer[Claims[MyCustomClaimsClass]]]
}
