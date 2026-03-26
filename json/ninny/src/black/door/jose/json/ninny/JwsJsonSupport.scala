package black.door.jose.json.ninny

import black.door.jose.jws.JwsHeader
import JwkJsonSupport._

trait JwsJsonSupport {

  import nrktkt.ninny.Auto._

  implicit val headerSerializer   = jsonSerializer[JwsHeader]
  implicit val headerDeserializer = jsonDeserializer[JwsHeader]
}
