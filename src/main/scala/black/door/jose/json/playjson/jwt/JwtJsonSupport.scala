package black.door.jose.json.playjson.jwt

import black.door.jose.jwt.Claims
import black.door.jose.json.playjson._
import play.api.libs.json._

trait JwtJsonSupport {

  private val unregisteredObjectKey = "unregistered"

  private val unitClaimsReads = Json.reads[Claims[Unit]]

  private def preClaimsWrites[A: Writes] = Json.writes[Claims[A]]

  implicit def claimsWrites[A: OWrites]: OWrites[Claims[A]] =
    integratedWrites(unregisteredObjectKey, preClaimsWrites[A])

  implicit def claimsReads[A: Reads]: Reads[Claims[A]] =
    integratedReads(unregisteredObjectKey, unitClaimsReads)

  implicit def claimsSerializer[C](implicit w: Writes[Claims[C]]) = jsonSerializer[Claims[C]]
  implicit def claimsDeserializer[C](implicit r: Reads[Claims[C]]) = jsonDeserializer[Claims[C]]
}
