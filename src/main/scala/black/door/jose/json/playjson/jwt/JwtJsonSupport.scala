package black.door.jose.json.playjson.jwt

import java.time.Instant

import black.door.jose.jwt.Claims
import black.door.jose.json.playjson._
import play.api.libs.json._
import play.api.libs.functional.syntax._

trait JwtJsonSupport {

  implicit val unitFormat: OFormat[Unit] = OFormat(
    Reads[Unit](_ => JsSuccess(Unit)),
    OWrites[Unit](_ => JsObject.empty)
  )

  val publicClaimsFormat =
    (__ \ "iss").formatNullable[String] and
    (__ \ "sub").formatNullable[String] and
    (__ \ "aud").formatNullable[String] and
    (__ \ "exp").formatNullable[Instant] and
    (__ \ "nbf").formatNullable[Instant] and
    (__ \ "iat").formatNullable[Instant] and
    (__ \ "jti").formatNullable[String] tupled

  // todo shapeless
  implicit def claimsReads[C](implicit r: Reads[C]): Reads[Claims[C]] = Reads(json =>
    for {
      privateClaims <- r.reads(json)
      (iss, sub, aud, exp, nbf, iat, jti) <- publicClaimsFormat.reads(json)
    } yield Claims(iss, sub, aud, exp, nbf, iat, jti, privateClaims)
  )

  implicit def claimsWrites[C](implicit w: OWrites[C]): Writes[Claims[C]] = Writes { claims =>
    val publicJson = publicClaimsFormat.writes((
      claims.iss,
      claims.sub,
      claims.aud,
      claims.exp,
      claims.nbf,
      claims.iat,
      claims.jti
    ))
    publicJson ++ w.writes(claims.privateClaims)
  }

  //implicit def claimsFormat[C: Format]: Format[Claims[C]] = Json.format[Claims[C]]
  implicit def claimsSerializer[C](implicit w: Writes[Claims[C]]) = jsonSerializer[Claims[C]]
  implicit def claimsDeserializer[C](implicit r: Reads[Claims[C]]) = jsonDeserializer[Claims[C]]
}
object JwtJsonSupport extends JwtJsonSupport