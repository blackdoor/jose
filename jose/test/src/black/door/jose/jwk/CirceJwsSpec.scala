package black.door.jose.jwk

import black.door.jose.json.circe
import io.circe.Decoder
import io.circe.parser._
import io.circe.syntax._

class CirceJwsSpec extends JwkSpec with circe.JsonSupport {
  def jwkSerializer   = key => Right(key.asJson.dropNullValues.noSpaces)
  def jwkDeserializer = json => decode(json)(Decoder[Jwk]).left.map(_.toString)
}
