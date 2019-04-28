package black.door.jose.json.circe

import black.door.jose.json.circe.jwk.JwkJsonSupport
import black.door.jose.json.circe.jwt.JwtJsonSupport
import black.door.jose.json.circe.jws.JwsJsonSupport

trait JsonSupport extends JwtJsonSupport with JwsJsonSupport with JwkJsonSupport
object JsonSupport extends JsonSupport
