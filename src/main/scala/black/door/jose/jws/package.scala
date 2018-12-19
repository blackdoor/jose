package black.door.jose

import black.door.jose.jwk._

package object jws {

  // (key, header, payload)
  type InputSigner = PartialFunction[(Jwk, JwsHeader[_], String), Array[Byte]]

  private[jose] def doKeyAndHeaderPlayNice(key: Jwk, header: JwsHeader[Any]) =
    key.alg.forall(_ == header.alg) &&
    header.kid.forall(hid => key.kid.forall(_ == hid))

  // (key, header, signingInput, signature)
  type SignatureValidator = PartialFunction[(Jwk, JwsHeader[_], String, Array[Byte]), Boolean]
}
