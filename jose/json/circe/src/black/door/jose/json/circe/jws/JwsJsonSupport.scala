package black.door.jose.json.circe.jws

import black.door.jose.jws.JwsHeader
import io.circe.{Decoder, Encoder}
import io.circe.generic.semiauto._
import black.door.jose.json.circe.jwk._
import black.door.jose.json.circe._

trait JwsJsonSupport extends JwkJsonSupport {
  implicit val headerEncoder: Encoder[JwsHeader] = deriveEncoder
  implicit val headerDecoder: Decoder[JwsHeader] = deriveDecoder
  implicit val headerSerializer                  = jsonSerializer[JwsHeader]
  implicit val headerDeserializer                = jsonDeserializer[JwsHeader]
}
