package black.door.jose.json.playjson.jwt

import black.door.jose.jwt.Claims
import black.door.jose.json.playjson._
import play.api.libs.json._

trait JwtJsonSupport {

  private val unregisteredObjectKey = "unregistered"

  implicit val unitReads = Reads[Unit](_ => JsSuccess(Unit))
  implicit val unitWrites = OWrites[Unit](_ => JsObject.empty)

  private val customInjector = Reads[JsValue](js => js.validate[JsObject].map(obj => obj + (unregisteredObjectKey, JsNull)))

  implicit def claimsReads[A: Reads]: Reads[Claims[A]] = Json.reads[Claims[A]]
    .compose(Reads(_.validate[JsObject]
      .map ( jsObj =>
        if(jsObj.keys.contains(unregisteredObjectKey)) jsObj
        else jsObj + (unregisteredObjectKey, JsNull)
      )
    ))

  private def preClaimsWrites[A: Writes] = Json.writes[Claims[A]]

  implicit def claimsWrites[A: OWrites]: OWrites[Claims[A]] = preClaimsWrites[A]
    .transform { jsObj: JsObject =>
      (jsObj - unregisteredObjectKey) ++ (jsObj \ unregisteredObjectKey).as[JsObject]
    }

  implicit def claimsSerializer[C](implicit w: Writes[Claims[C]]) = jsonSerializer[Claims[C]]
  implicit def claimsDeserializer[C](implicit r: Reads[Claims[C]]) = jsonDeserializer[Claims[C]]
}
object JwtJsonSupport extends JwtJsonSupport