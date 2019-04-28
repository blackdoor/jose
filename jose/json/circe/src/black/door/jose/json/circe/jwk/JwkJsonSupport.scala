package black.door.jose.json.circe.jwk

import black.door.jose.jwk.{EcJwk, Jwk, P256KeyPair, P256PublicKey}
import io.circe.{Decoder, DecodingFailure}
import io.circe.generic.semiauto._

object JwkJsonSupport {
  implicit val p256KeyPairDecoder: Decoder[P256KeyPair] = deriveDecoder
  implicit val p256PublicKeyDecoder: Decoder[P256PublicKey] = deriveDecoder

  implicit val ecJwkDecoder: Decoder[EcJwk] = Decoder.instance { c =>
    c.downField("crv").as[String] match {
      case Right(value) if value == "P-256" =>
        c.downField("d").as[String] match {
          case Right(_) => c.as[P256KeyPair]
          case Left(_) => c.as[P256PublicKey]
        }
      case Right(_) => Left(DecodingFailure("JWK does not have a supported curve", c.history))
      case Left(_) => Left(DecodingFailure("JWK does not have a defined curve", c.history))
    }
  }

  implicit val jwkDecoder: Decoder[Jwk] = Decoder.instance { c =>
    c.downField("kty").as[String] match {
      case Right(value) if value == "EC" => c.as[EcJwk]
      case Right(_) => Left(DecodingFailure("JWK does not have a supported key type", c.history))
      case Left(_) => Left(DecodingFailure("JWK does not have a defined key type", c.history))
    }
  }
}
