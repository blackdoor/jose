package pkg.jws

import java.nio.charset.StandardCharsets
import java.security.KeyException
import java.util

import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec
import pkg.adopted.DerTools
import pkg.jwk.{HsJwk, JavaEcPublicKey, SymmetricJwk}

object SignatureValidator {
  val keyHeaderPreValidator: SignatureValidator = {
    case (key, header, _, _) if !doKeyAndHeaderPlayNice(key, header) =>
      throw new KeyException("The JWK that was resolved is not valid for this type of JWS")
    case (key, _, _, _) if !key.key_ops.forall(_.contains("verify")) =>
      throw new KeyException(s"The JWK that was resolved is not valid for signature verification (only ${key.key_ops.get.mkString(",")})")
  }

  val javaSignatureValidator: SignatureValidator = {
    case (key: JavaEcPublicKey, _, signingInput, signature) =>
      val veri = key.javaSignature
      veri.initVerify(key.toJavaPublicKey)
      veri.update(signingInput.getBytes(StandardCharsets.US_ASCII))
      veri.verify(DerTools.transcodeSignatureToDER(signature))
    case (key: SymmetricJwk, header, signingInput, signature) if HsJwk.validAlgs.contains(header.alg) =>
      val algorithm = HsJwk.javaAlgorithm(header.alg)
      val mac = Mac.getInstance(algorithm)
      mac.init(new SecretKeySpec(key.k, algorithm))
      util.Arrays.equals(signature, mac.doFinal(signingInput.getBytes(StandardCharsets.US_ASCII)))
  }
}
