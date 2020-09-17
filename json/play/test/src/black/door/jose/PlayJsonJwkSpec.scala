package black.door.jose

import black.door.jose.json.playjson
import black.door.jose.jwk.Jwk
import play.api.libs.json.Json

class PlayJsonJwkSpec extends JwkSpec with playjson.JsonSupport {
  def jwkSerializer   = key => Right(Json.toJson(key).toString)
  def jwkDeserializer = json => Json.parse(json).validate[Jwk].asEither.left.map(_.toString)
}
