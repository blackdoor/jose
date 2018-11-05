package pkg.jws

import java.nio.charset.StandardCharsets
import java.security.KeyException

import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec
import pkg.adopted.DerTools
import pkg.jwk.{HsJwk, JavaEcPrivateKey, SymmetricJwk}

object InputSigner {

  val keyHeaderPreSigner: InputSigner = {
    case (key, header, _) if !doKeyAndHeaderPlayNice(key, header) =>
      throw new KeyException("The JWK that was provided is not valid for the headers on the JWS")
    case (key, _, _) if !key.key_ops.forall(_.contains("sign")) =>
      throw new KeyException(s"The JWK that was provided is not valid for signature operations (only ${key.key_ops.get.mkString(",")})")
  }

  val javaInputSigner: InputSigner =
    {
      case (key: JavaEcPrivateKey, _, signingInput) =>
        val sig = key.javaSignature
        val javaKey = key.toJavaPrivateKey
        sig.initSign(javaKey)
        sig.update(signingInput.getBytes(StandardCharsets.US_ASCII))
        DerTools.transcodeSignatureToConcat(sig.sign(), javaKey.getParams.getCurve.getField.getFieldSize / 4)
      case (key: SymmetricJwk, header, signingInput) =>
        val algorithm = HsJwk.javaAlgorithm(header.alg)

        val mac = Mac.getInstance(algorithm)
        mac.init(new SecretKeySpec(key.k, algorithm))
        mac.doFinal(signingInput.getBytes(StandardCharsets.US_ASCII))
    }
}