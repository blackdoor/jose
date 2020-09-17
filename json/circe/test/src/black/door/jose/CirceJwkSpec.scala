package black.door.jose

import black.door.jose.json.circe
import black.door.jose.jwk.Jwk
import io.circe.Decoder
import io.circe.parser._
import io.circe.syntax._

class CirceJwkSpec extends JwkSpec with circe.JsonSupport {
  def jwkSerializer   = key => Right(key.asJson.dropNullValues.noSpaces)
  def jwkDeserializer = json => decode(json)(Decoder[Jwk]).left.map(_.toString)
}
