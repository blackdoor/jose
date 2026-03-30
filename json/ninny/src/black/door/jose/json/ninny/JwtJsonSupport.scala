package black.door.jose.json.ninny

import black.door.jose.{ByteDeserializer, ByteSerializer}
import black.door.jose.jwt._
import nrktkt.ninny.ast.JsonObject
import nrktkt.ninny.{FromJson, ToJson, ToSomeJson, ToSomeJsonObject}
import java.time.Instant

trait JwtJsonSupport {

  implicit def claimsToJson[C: ToSomeJsonObject]: ToSomeJsonObject[Claims[C]] =
    a => {
      val js = ToJson.auto[Claims[C]].toSome(a)
      js - "unregistered" ++ (js / "unregistered")
        .collect { case obj: JsonObject => obj }
        .getOrElse(JsonObject(Map.empty))
    }

  private def _claimsFromJson[A: FromJson]:FromJson[Claims[A]] = FromJson.forProduct8("iss", "sub", "aud", "exp", "nbf", "iat", "jti", "unregistered") (
    (iss: Option[String], sub: Option[String], aud: Option[String], exp: Option[Instant], nbf: Option[Instant], iat: Option[Instant], jti: Option[String], unregistered: A) =>
      Claims(iss, sub, aud, exp, nbf, iat, jti, unregistered)
  )

  implicit def claimsFromJson[C: FromJson]: FromJson[Claims[C]] =
    FromJson.fromSome[Claims[C]] { js =>

      for {
        registered   <- _claimsFromJson[Option[Unit]].from(js)
        unregistered <- js.to[C]
      } yield registered.copy(unregistered = unregistered)
    }

  implicit def claimsSerializer[C](
      implicit to: ToSomeJson[Claims[C]]
    ): ByteSerializer[Claims[C]] =
    jsonSerializer[Claims[C]]

  implicit def claimsDeserializer[C](
      implicit from: FromJson[Claims[C]]
    ): ByteDeserializer[Claims[C]] =
    jsonDeserializer[Claims[C]]
}
