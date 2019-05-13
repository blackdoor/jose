package black.door.jose.json.playjson.jwt

import java.time.Instant

import black.door.jose.jwt.Claims
import black.door.jose.json.playjson._
import play.api.libs.json._

trait JwtJsonSupport {

  private val unregisteredObjectKey = "unregistered"

  implicit val unitReads  = Reads[Unit](_ => JsSuccess(Unit))
  implicit val unitWrites = OWrites[Unit](_ => JsObject.empty)

  implicit private[this] val instantFormat = Format[Instant](
    implicitly[Reads[Long]].map(Instant.ofEpochSecond),
    Writes[Instant](inst => JsNumber(inst.getEpochSecond))
  )

  private val unitClaimsReads = Json.reads[Claims[Unit]]
  private val unregisteredInjector = Reads(
    _.validate[JsObject]
      .map(
        jsObj =>
          if (jsObj.keys.contains(unregisteredObjectKey)) jsObj
          else jsObj + (unregisteredObjectKey, JsNull)
      )
  )

  implicit def claimsReads[A](implicit unregisteredReads: Reads[A]): Reads[Claims[A]] =
    Reads { js =>
      for {
        unitClaims   <- unitClaimsReads.reads(js)
        unregistered <- unregisteredReads.reads(js)
      } yield unitClaims.copy(unregistered = unregistered)
    }.composeWith(unregisteredInjector)

  private def preClaimsWrites[A: Writes] = Json.writes[Claims[A]]

  implicit def claimsWrites[A: OWrites]: OWrites[Claims[A]] =
    preClaimsWrites[A]
      .transform { jsObj: JsObject =>
        (jsObj - unregisteredObjectKey) ++ (jsObj \ unregisteredObjectKey).as[JsObject]
      }

  implicit def claimsSerializer[C](implicit w: Writes[Claims[C]]) =
    jsonSerializer[Claims[C]]

  implicit def claimsDeserializer[C](implicit r: Reads[Claims[C]]) =
    jsonDeserializer[Claims[C]]
}

object JwtJsonSupport extends JwtJsonSupport
