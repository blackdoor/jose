package black.door.jose.json.ninny

import java.security.KeyException
import java.util.Base64

import black.door.jose.jwk._
import io.github.kag0.ninny.ast.{JsonObject, JsonString}
import io.github.kag0.ninny._

import scala.collection.compat.immutable._
import scala.collection.immutable.IndexedSeq
import scala.language.implicitConversions
import scala.util.{Failure, Try}

trait JwkJsonSupport {

  private def idxToArray(idx: IndexedSeq[Byte]): Array[Byte] = idx match {
    case wrapped: ArraySeq[Byte] => wrapped.unsafeArray.asInstanceOf[Array[Byte]]
    case other                   => other.toArray
  }

  private val base64Decoder = Base64.getUrlDecoder
  private val base64Encoder = Base64.getUrlEncoder.withoutPadding

  implicit private val bigIntFromJson =
    indexedBytesFromJson.map(bytes => BigInt(idxToArray(bytes)))

  implicit private val bigIntToJson =
    indexedBytesToJson.contramap[BigInt](i => ArraySeq.unsafeWrapArray(i.toByteArray))

  val p256KeyPairFromJson   = FromJson.auto[P256KeyPair]
  val p256PublicKeyFromJson = FromJson.auto[P256PublicKey]

  implicit private lazy val indexedBytesFromJson: FromJson[IndexedSeq[Byte]] =
    FromJson.fromSome {
      case JsonString(encoded) => Try(ArraySeq.unsafeWrapArray(base64Decoder.decode(encoded)))
      case _ =>
        Failure(new IllegalArgumentException("Binary value was not a base64url string"))
    }

  implicit private lazy val indexedBytesToJson =
    ToJson((bytes: IndexedSeq[Byte]) =>
      JsonString(base64Encoder.encodeToString(idxToArray(bytes)))
    )

  implicit val rsaJwkFromJson = FromJson.auto[RsaPublicKey]
  implicit val octJwkFromJson = FromJson.auto[OctJwk]

  implicit val jwkFromJson: FromJson[Jwk] = FromJson.fromSome {
    case obj: JsonObject =>
      obj / "kty" match {
        case Some(JsonString("EC"))  => obj.to[EcJwk]
        case Some(JsonString("RSA")) => obj.to[RsaPublicKey]
        case Some(JsonString("oct")) => obj.to[OctJwk]
        case Some(_) => Failure(new KeyException("Expected JSON string for key type"))
        case None    => Failure(new KeyException("JWK missing key type"))
      }
    case other =>
      Failure(
        new KeyException(s"Expected JSON object for JWK, got ${other.getClass.getSimpleName}")
      )
  }

  implicit private val p256KeyPairToJson   = ToJson.auto[P256KeyPair]
  implicit private val p256PublicKeyToJson = ToJson.auto[P256PublicKey]

  implicit val ecJwkToJson: ToSomeJsonObject[EcJwk] = k =>
    (k match {
      case key: P256KeyPair   => key.toSomeJson
      case key: P256PublicKey => key.toSomeJson
      case _                  => ???
    }) ++ obj("kty" -> "EC", "crv" -> "P-256")

  implicit val rsaJwkToJson: ToSomeJsonObject[RsaPublicKey] =
    ToJson.auto[RsaPublicKey].toSome(_) + ("kty" -> "RSA")

  implicit val octJwkToJson: ToSomeJsonObject[OctJwk] =
    ToJson.auto[OctJwk].toSome(_) + ("kty" -> "oct")

  implicit val ecJwkFromJson: FromJson[EcJwk] = FromJson.fromSome {
    case obj: JsonObject =>
      obj / "crv" match {
        case Some(JsonString("P-256")) =>
          obj / "d" match {
            case Some(_) => p256KeyPairFromJson.from(obj)
            case None    => p256PublicKeyFromJson.from(obj)
          }
        case Some(_) => Failure(new KeyException("JWK does not have a supported curve"))
        case None    => Failure(new KeyException("JWK does not have a defined curve"))
      }
    case other =>
      Failure(
        new KeyException(s"Expected JSON object for JWK, got ${other.getClass.getSimpleName}")
      )
  }

  implicit val jwkToJson: ToSomeJsonObject[Jwk] = {
    case key: EcJwk        => key.toSomeJson
    case key: RsaPublicKey => key.toSomeJson
    case key: OctJwk       => key.toSomeJson
  }
}
object JwkJsonSupport extends JwkJsonSupport
