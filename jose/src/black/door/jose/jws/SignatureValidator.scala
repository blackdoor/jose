package black.door.jose.jws

import java.security.KeyException

object SignatureValidator {

  val keyHeaderPreValidator: SignatureValidator = {
    case (key, header, _, _) if !doKeyAndHeaderPlayNice(key, header) =>
      throw new KeyException("The JWK that was resolved is not valid for this type of JWS")
    case (key, _, _, _) if !key.key_ops.forall(_.contains("verify")) =>
      throw new KeyException(
        s"The JWK that was resolved is not valid for signature verification (only ${key.key_ops.get.mkString(",")})"
      )
    case (key, _, _, _) if key.use.exists(_ != "sig") =>
      throw new KeyException(
        s"The JWK that was provided is not valid for signature use (only ${key.use.get})"
      )
  }
}
