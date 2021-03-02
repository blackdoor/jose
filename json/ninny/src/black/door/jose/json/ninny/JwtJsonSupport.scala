package black.door.jose.json.ninny

import black.door.jose.{ByteDeserializer, ByteSerializer}
import black.door.jose.jwt._
import io.github.kag0.ninny.ast.JsonObject
import io.github.kag0.ninny.{FromJson, ToJson, ToSomeJson, ToSomeJsonObject}

trait JwtJsonSupport {

  implicit def claimsToJson[C: ToSomeJsonObject]: ToSomeJsonObject[Claims[C]] =
    a => {
      val js = ToJson.auto[Claims[C]].toSome(a)
      js - "unregistered" ++ js.unregistered.maybeJson
        .collect { case obj: JsonObject => obj }
        .getOrElse(JsonObject(Map.empty))
    }

  private def _claimsFromJson[A: FromJson] = FromJson.auto[Claims[A]]

  implicit def claimsFromJson[C: FromJson] =
    FromJson.fromSome[Claims[C]] { js =>
      implicit def from[A: FromJson] = _claimsFromJson[A]

      for {
        registered   <- js.to[Claims[Option[Unit]]]
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
