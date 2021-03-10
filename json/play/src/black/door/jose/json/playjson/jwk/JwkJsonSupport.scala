package black.door.jose.json.playjson.jwk

import java.util.Base64

import black.door.jose.jwk._
import play.api.libs.json._

import scala.collection.immutable.IndexedSeq

trait JwkJsonSupport {

  implicit private val bigIntFormat = Format[BigInt](
    Reads {
      case JsString(encoded) => JsSuccess(BigInt(Base64.getUrlDecoder.decode(encoded)))
      case _                 => JsError("BigInt value was not a base64url string")
    },
    Writes(int =>
      JsString(Base64.getUrlEncoder.withoutPadding.encodeToString(int.toByteArray))
    )
  )

  val p256KeyPairReads   = Json.reads[P256KeyPair]
  val p256PublicKeyReads = Json.reads[P256PublicKey]

  val ecJwkReads: Reads[EcJwk] = Reads(_.validate[JsObject] match {
    case error: JsError => error
    case JsSuccess(obj, path) =>
      obj \ "crv" match {
        case JsDefined(JsString("P-256")) =>
          obj \ "d" match {
            case JsDefined(_) => p256KeyPairReads.reads(obj)
            case _            => p256PublicKeyReads.reads(obj)
          }
        case JsDefined(_) => JsError(path \ "crv", "JWK does not have a supported curve")
        case _            => JsError(path \ "crv", "JWK does not have a defined curve")
      }
  })

  def ecJwkWrites =
    OWrites[EcJwk] {
      case key: P256KeyPair   => Json.writes[P256KeyPair].writes(key)
      case key: P256PublicKey => Json.writes[P256PublicKey].writes(key)
      case _                  => ???
    }.transform((js: JsObject) => js + ("crv", JsString("P-256")) + ("kty", JsString("EC")))

  def rsaJwkReads = Json.reads[RsaPublicKey]

  def rsaJwkWrites =
    Json.writes[RsaPublicKey].transform((js: JsObject) => js + ("kty", JsString("RSA")))

  implicit def indexedBytesFormat = Format[IndexedSeq[Byte]](
    Reads {
      case JsString(encoded) => JsSuccess(Base64.getUrlDecoder.decode(encoded).toIndexedSeq)
      case _                 => JsError("Binary value was not a base64url string")
    },
    Writes(bytes =>
      JsString(Base64.getUrlEncoder.withoutPadding.encodeToString(bytes.toArray))
    )
  )

  val octJwkFormat = OFormat[OctJwk](
    Json.reads[OctJwk],
    Json.writes[OctJwk].transform((js: JsObject) => js + ("kty", JsString("oct")))
  )

  implicit val jwkReads: Reads[Jwk] = Reads(_.validate[JsObject] match {
    case error: JsError => error
    case JsSuccess(obj, path) =>
      obj \ "kty" match {
        case JsDefined(JsString("EC"))  => ecJwkReads.reads(obj)
        case JsDefined(JsString("RSA")) => rsaJwkReads.reads(obj)
        case JsDefined(JsString("oct")) => octJwkFormat.reads(obj)
        case JsDefined(_)               => JsError(path \ "kty", "JWK does not have a supported key type")
        case _                          => JsError(path \ "kty", "JWK does not have a defined key type")
      }
  })

  implicit val jwkWrites: OWrites[Jwk] = OWrites[Jwk] {
    case key: EcJwk        => ecJwkWrites.writes(key)
    case key: RsaPublicKey => rsaJwkWrites.writes(key)
    case key: OctJwk       => octJwkFormat.writes(key)
  }
}

object JwkJsonSupport extends JwkJsonSupport
