package black.door.jose.json.playjson.jws

import black.door.jose.jws.JwsHeader
import play.api.libs.json.Json
import black.door.jose.json.playjson.jwk.JwkJsonSupport._
import black.door.jose.json.playjson._
import play.api.libs.json.OWrites
import play.api.libs.json.Reads
import black.door.jose.ByteSerializer
import black.door.jose.ByteDeserializer

trait JwsJsonSupport {
  implicit val headerWrites: OWrites[JwsHeader]        = Json.writes[JwsHeader]
  implicit val headerReads: Reads[JwsHeader]          = Json.reads[JwsHeader]
  implicit val headerSerializer: ByteSerializer[JwsHeader]   = jsonSerializer[JwsHeader] 
  implicit val headerDeserializer: ByteDeserializer[JwsHeader] = jsonDeserializer[JwsHeader]
}
