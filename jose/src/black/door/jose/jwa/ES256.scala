package black.door.jose.jwa

import java.nio.charset.StandardCharsets
import java.security.Signature

import black.door.jose.adopted.DerTools
import black.door.jose.jwk.{EcPrivateKey, EcPublicKey}

object ES256 extends SignatureAlgorithm {
  val alg = "ES256"

  private def jcaSignature = Signature.getInstance("SHA256withECDSA")

  val validate = {
    case (key: EcPublicKey, header, signingInput, signature) if header.alg == alg =>
      val veri = jcaSignature
      veri.initVerify(key.toJcaPublicKey)
      veri.update(signingInput.getBytes(StandardCharsets.US_ASCII))
      veri.verify(DerTools.transcodeSignatureToDER(signature))
  }

  val sign = {
    case (key: EcPrivateKey, header, signingInput) if header.alg == alg =>
      val sig     = jcaSignature
      val javaKey = key.toJcaPrivateKey
      sig.initSign(javaKey)
      sig.update(signingInput.getBytes(StandardCharsets.US_ASCII))
      DerTools.transcodeSignatureToConcat(
        sig.sign(),
        javaKey.getParams.getCurve.getField.getFieldSize / 4
      )
  }
}
