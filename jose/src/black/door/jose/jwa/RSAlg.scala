package black.door.jose.jwa

import java.nio.charset.StandardCharsets
import java.security.Signature

import black.door.jose.jwk.RsaPublicKey

sealed case class RSAlg(hashBits: Int) extends SignatureAlgorithm {
  val alg          = s"RS$hashBits"
  val jcaSignature = Signature.getInstance(s"SHA${hashBits}withRSA")

  val validate = {
    case (key: RsaPublicKey, header, signingInput, signature) if header.alg == alg =>
      val veri = jcaSignature
      veri.initVerify(key.toJcaPublicKey)
      veri.update(signingInput.getBytes(StandardCharsets.US_ASCII))
      veri.verify(signature)
  }

  val sign = PartialFunction.empty
}

object RSAlgs {
  val RS256 = RSAlg(256)
  val RS384 = RSAlg(384)
  val RS512 = RSAlg(512)

  val all = List(RS256, RS384, RS512)
}
