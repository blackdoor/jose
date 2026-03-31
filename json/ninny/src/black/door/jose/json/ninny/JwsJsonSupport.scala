package black.door.jose.json.ninny

import black.door.jose.jws.JwsHeader
import JwkJsonSupport._
import black.door.jose.ByteSerializer
import black.door.jose.ByteDeserializer

trait JwsJsonSupport {

  import nrktkt.ninny.Auto._

  implicit val headerSerializer: ByteSerializer[JwsHeader]   = jsonSerializer[JwsHeader]
  implicit val headerDeserializer: ByteDeserializer[JwsHeader] = jsonDeserializer[JwsHeader]
}
