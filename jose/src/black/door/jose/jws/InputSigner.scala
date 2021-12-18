package black.door.jose.jws

import java.security.KeyException

object InputSigner {

  val keyHeaderPreSigner: InputSigner = {
    case (key, header, _) if !doKeyAndHeaderPlayNice(key, header) =>
      throw new KeyException(
        "The JWK that was provided is not valid for the headers on the JWS"
      )
    case (key, _, _) if !key.key_ops.forall(_.contains("sign")) =>
      throw new KeyException(
        s"The JWK that was provided is not valid for signature operations (only ${key.key_ops.get.mkString(",")})"
      )
    case (key, _, _) if key.use.exists(_ != "sig") =>
      throw new KeyException(
        s"The JWK that was provided is not valid for signature use (only ${key.use.get})"
      )
  }
}
