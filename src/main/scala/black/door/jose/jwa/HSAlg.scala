package black.door.jose.jwa
import java.nio.charset.StandardCharsets
import java.security.InvalidKeyException
import java.util

import black.door.jose.jwk.SymmetricJwk
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

sealed case class HSAlg(hashBits: Int) extends SignatureAlgorithm {
  val alg = s"HS$hashBits"
  val jcaAlgorithm = s"HmacSHA$hashBits"

  val validate = {
    case (key: SymmetricJwk, header, signingInput, signature) if alg == header.alg =>
      val mac = Mac.getInstance(jcaAlgorithm)
      mac.init(new SecretKeySpec(key.k, jcaAlgorithm))
      util.Arrays.equals(signature, mac.doFinal(signingInput.getBytes(StandardCharsets.US_ASCII)))
  }

  val sign = {
    case (key: SymmetricJwk, header, signingInput) if alg == header.alg =>
      if(key.k.length < hashBits / 8) throw new InvalidKeyException(s"$alg keys must be $hashBits bits (was ${key.k.length * 8})")
      val mac = Mac.getInstance(jcaAlgorithm)
      mac.init(new SecretKeySpec(key.k, jcaAlgorithm))
      mac.doFinal(signingInput.getBytes(StandardCharsets.US_ASCII))
  }
}

object HSAlgs {
  val HS256 = HSAlg(256)
  val HS384 = HSAlg(384)
  val HS512 = HSAlg(512)

  val all = List(HS256, HS384, HS512)
}