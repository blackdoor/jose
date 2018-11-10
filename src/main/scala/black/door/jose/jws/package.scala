package black.door.jose

import black.door.jose.jwk._

package object jws {

  // (key, headerSerializer, header, payload)
  type InputSigner = PartialFunction[(Jwk, JwsHeader, String), Array[Byte]]

  private[jws] def doKeyAndHeaderPlayNice(key: Jwk, header: JwsHeader) =
    key.isValidFor(header.alg) &&
    key.alg.forall(_ == header.alg) &&
    header.kid.forall(hid => key.kid.forall(_ == hid)) &&
    key.use.forall(_ == "sig")

  // (key, header, signingInput, signature)
  type SignatureValidator = PartialFunction[(Jwk, JwsHeader, String, Array[Byte]), Boolean]
}
