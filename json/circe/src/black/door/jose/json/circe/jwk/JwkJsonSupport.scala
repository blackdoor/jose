package black.door.jose.json.circe.jwk

import java.util.Base64

import black.door.jose.jwk.{EcJwk, Jwk, OctJwk, P256KeyPair, P256PublicKey, RsaPublicKey}
import io.circe.{Decoder, DecodingFailure, Encoder, Json}
import io.circe.generic.semiauto
import io.circe.syntax._
import io.circe.generic.auto._

import scala.collection.immutable.IndexedSeq

trait JwkJsonSupport {
  implicit val p256KeyPairDecoder: Decoder[P256KeyPair]     = semiauto.deriveDecoder
  implicit val p256PublicKeyDecoder: Decoder[P256PublicKey] = semiauto.deriveDecoder
  implicit val p256KeyPairEncoder: Encoder[P256KeyPair]     = semiauto.deriveEncoder
  implicit val p256PublicKeyEncoder: Encoder[P256PublicKey] = semiauto.deriveEncoder

  implicit val indexedBytesEncoder: Encoder[IndexedSeq[Byte]] = Encoder.instance { bytes =>
    Json.fromString(Base64.getUrlEncoder.withoutPadding.encodeToString(bytes.toArray))
  }

  implicit val indexedBytesDecoder: Decoder[IndexedSeq[Byte]] = Decoder.instance { c =>
    c.as[String] match {
      case Right(value) => Right(Base64.getUrlDecoder.decode(value).toIndexedSeq)
      case Left(_) =>
        Left(DecodingFailure("Binary value was not a base64url string", c.history))
    }
  }

  implicit val bigIntEncoder: Encoder[BigInt] = Encoder.instance { int =>
    Json.fromString(Base64.getUrlEncoder.withoutPadding.encodeToString(int.toByteArray))
  }

  implicit val bigIntDecoder: Decoder[BigInt] = Decoder.instance { c =>
    c.as[String] match {
      case Right(value) => Right(BigInt(Base64.getUrlDecoder.decode(value)))
      case Left(_) =>
        Left(DecodingFailure("BigInt value was not a base64url string", c.history))
    }
  }

  implicit val octJwkEncoder: Encoder[OctJwk] =
    semiauto.deriveEncoder[OctJwk].mapJsonObject(_.add("kty", Json.fromString("oct")))

  implicit val ecJwkEncoder: Encoder[EcJwk] = new Encoder[EcJwk] {

    final def apply(ecJwk: EcJwk): Json = {
      val json = ecJwk match {
        case key: P256KeyPair   => key.asJson
        case key: P256PublicKey => key.asJson
        case key                => key.asJson
      }
      json.deepMerge(
        Json.obj(("crv", Json.fromString("P-256")), ("kty", Json.fromString("EC")))
      )
    }
  }

  implicit val ecJwkDecoder: Decoder[EcJwk] = Decoder.instance { c =>
    c.downField("crv").as[String] match {
      case Right(value) if value == "P-256" =>
        c.downField("d").as[String] match {
          case Right(_) => c.as[P256KeyPair]
          case Left(_)  => c.as[P256PublicKey]
        }
      case Right(_) => Left(DecodingFailure("JWK does not have a supported curve", c.history))
      case Left(_)  => Left(DecodingFailure("JWK does not have a defined curve", c.history))
    }
  }

  implicit val rsaPublicKeyEncoder: Encoder[RsaPublicKey] =
    semiauto.deriveEncoder[RsaPublicKey].mapJsonObject(_.add("kty", Json.fromString("RSA")))

  implicit val jwkDecoder: Decoder[Jwk] = Decoder.instance { c =>
    c.downField("kty").as[String] match {
      case Right(value) if value == "EC"  => c.as[EcJwk]
      case Right(value) if value == "RSA" => c.as[RsaPublicKey]
      case Right(value) if value == "oct" => c.as[OctJwk]
      case Right(_) =>
        Left(DecodingFailure("JWK does not have a supported key type", c.history))
      case Left(_) => Left(DecodingFailure("JWK does not have a defined key type", c.history))
    }
  }

  implicit val jwkEncoder: Encoder[Jwk] = Encoder.instance {
    case d: EcJwk        => d.asJson
    case d: RsaPublicKey => d.asJson
    case d: OctJwk       => d.asJson
  }
}
