package black.door.jose.json.playjson

import black.door.jose.json.playjson.jwk.JwkJsonSupport
import black.door.jose.json.playjson.jws.JwsJsonSupport
import black.door.jose.json.playjson.jwt.JwtJsonSupport

trait JsonSupport extends JwtJsonSupport with JwsJsonSupport with JwkJsonSupport
object JsonSupport extends JsonSupport