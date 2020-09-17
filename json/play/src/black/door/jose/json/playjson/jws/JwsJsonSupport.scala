package black.door.jose.json.playjson.jws

import black.door.jose.jws.JwsHeader
import play.api.libs.json.Json
import black.door.jose.json.playjson.jwk.JwkJsonSupport._
import black.door.jose.json.playjson._

trait JwsJsonSupport {
  implicit val headerWrites = Json.writes[JwsHeader]
  implicit val headerReads = Json.reads[JwsHeader]
  implicit val headerSerializer = jsonSerializer[JwsHeader]
  implicit val headerDeserializer = jsonDeserializer[JwsHeader]
}
