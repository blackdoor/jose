package black.door.jose.json.circe.jwt

import black.door.jose.jwt.Claims
import io.circe.{Decoder, Encoder, HCursor, Json}
import io.circe.syntax._
import black.door.jose.json.circe._
import io.circe.generic.semiauto._

trait JwtJsonSupport {
  private val unregisteredObjectKey = "unregistered"

  implicit val unitEncoder: Decoder[Unit] = new Decoder[Unit] {
    final def apply(c: HCursor): Decoder.Result[Unit] = Right(())
  }
  private implicit val claimsUnitDecoder: Decoder[Claims[Unit]] = deriveDecoder

  implicit def claimsEncoder[A: Encoder]: Encoder[Claims[A]] = deriveEncoder[Claims[A]].mapJson { j =>
    val unregisteredObjectKeyJson = j.withObject(_.key(unregisteredObjectKey).asJson)
    j.withObject(_.remove(unregisteredObjectKey).asJson).deepMerge(unregisteredObjectKeyJson)
  }

  implicit def claimsDecoder[A: Decoder]: Decoder[Claims[A]] = new Decoder[Claims[A]] {
    final def apply(c: HCursor): Decoder.Result[Claims[A]] = {
      val unregisteredObjectKeyExist = c.keys.exists(keys => keys.toList.contains(unregisteredObjectKey))
      val result = if (unregisteredObjectKeyExist) c
                    else c.withFocus(_.deepMerge(Map(unregisteredObjectKey -> Json.Null).asJson))

      for {
        unitClaims <- result.as[Claims[Unit]]
        unregistered <- c.as[A]
      } yield unitClaims.copy(unregistered = unregistered)
    }
  }

  implicit def claimsSerializer[C](implicit w: Encoder[Claims[C]]) = jsonSerializer[Claims[C]]
  implicit def claimsDeserializer[C](implicit r: Decoder[Claims[C]]) = jsonDeserializer[Claims[C]]
}
