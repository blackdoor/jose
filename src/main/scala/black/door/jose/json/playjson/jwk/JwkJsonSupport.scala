package black.door.jose.json.playjson.jwk

import java.util.Base64

import black.door.jose.jwk.{EcJwk, Jwk, P256KeyPair, P256PublicKey}
import play.api.libs.json._

trait JwkJsonSupport {

  private implicit val bigIntFormat = Format[BigInt] (
    Reads {
      case JsString(encoded) => JsSuccess(BigInt(Base64.getUrlDecoder.decode(encoded)))
      case _ => JsError("BigInt value was not a base64url string")
    },
    Writes(int => JsString(Base64.getUrlEncoder.withoutPadding.encodeToString(int.toByteArray)))
  )

  val p256KeyPairReads = Json.reads[P256KeyPair]
  val p256PublicKeyReads = Json.reads[P256PublicKey]

  val ecJwkReads: Reads[EcJwk] = Reads(_.validate[JsObject] match {
    case error: JsError => error
    case JsSuccess(obj, path) => obj \ "crv" match {
      case JsDefined(JsString("P-256")) => obj \ "d" match {
        case JsDefined(_) => p256KeyPairReads.reads(obj)
        case JsUndefined() => p256PublicKeyReads.reads(obj)
      }
      case JsDefined(_) => JsError(path \ "crv", "JWK does not have a supported curve")
      case JsUndefined() => JsError(path \ "crv", "JWK does not have a defined curve")
    }
  })

  implicit val jwkReads: Reads[Jwk] = Reads(_.validate[JsObject] match {
    case error: JsError => error
    case JsSuccess(obj, path) => obj \ "kty" match {
      case JsDefined(JsString("EC")) => ecJwkReads.reads(obj)
      case JsDefined(_) => JsError(path \ "kty", "JWK does not have a supported key type")
      case JsUndefined() => JsError(path \ "kty", "JWK does not have a defined key type")
    }
  })

  implicit def jwkWrites: Writes[Jwk] = Writes(_ => ???)
}