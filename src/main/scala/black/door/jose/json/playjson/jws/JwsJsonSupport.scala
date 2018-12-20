package black.door.jose.json.playjson.jws

import black.door.jose.jws.JwsHeader
import play.api.libs.json._
import black.door.jose.json.playjson._
import black.door.jose.json.playjson.jwk.JwkJsonSupport

trait JwsJsonSupport extends JwkJsonSupport {

  private val unregisteredObjectKey = "unregistered"

  private val unitHeaderReads = Json.reads[JwsHeader[Unit]]

  private def preHeaderWrites[HP: Writes]= Json.writes[JwsHeader[HP]]

  implicit def headerWrites[HP: OWrites] =
    integratedWrites(unregisteredObjectKey, preHeaderWrites[HP])

  implicit def headerReads[HP: Reads]: Reads[JwsHeader[HP]] =
    integratedReads(unregisteredObjectKey, unitHeaderReads)

  implicit def headerSerializer[HP](implicit writes: Writes[JwsHeader[HP]]) = jsonSerializer[JwsHeader[HP]]
  implicit def headerDeserializer[HP](implicit reads: Reads[JwsHeader[HP]]) = jsonDeserializer[JwsHeader[HP]]
}
