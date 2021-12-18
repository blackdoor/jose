package black.door.jose.jwk

import java.security.{PrivateKey, PublicKey}

sealed trait AsymmetricJwk extends Jwk

trait PublicJwk extends AsymmetricJwk {
  def toJcaPublicKey: PublicKey
}

trait PrivateJwk extends AsymmetricJwk {
  def toJcaPrivateKey: PrivateKey
}
