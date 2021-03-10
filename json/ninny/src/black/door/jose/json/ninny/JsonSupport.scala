package black.door.jose.json.ninny

import io.github.kag0.ninny.ToSomeJsonObject
import io.github.kag0.ninny.ast.JsonObject

trait JsonSupport extends JwkJsonSupport with JwsJsonSupport with JwtJsonSupport {
  implicit val unitToCustomClaim: ToSomeJsonObject[Unit] = _ => JsonObject(Map.empty)
}

object JsonSupport extends JsonSupport
