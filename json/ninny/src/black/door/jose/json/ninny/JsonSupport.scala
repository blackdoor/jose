package black.door.jose.json.ninny

import nrktkt.ninny.ToSomeJsonObject
import nrktkt.ninny.ast.JsonObject

trait JsonSupport extends JwkJsonSupport with JwsJsonSupport with JwtJsonSupport {
  implicit val unitToCustomClaim: ToSomeJsonObject[Unit] = _ => JsonObject(Map.empty)
}

object JsonSupport extends JsonSupport
