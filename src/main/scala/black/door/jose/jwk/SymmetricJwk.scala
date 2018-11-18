package black.door.jose.jwk

import java.security.SecureRandom

trait SymmetricJwk extends Jwk {
  def k: Array[Byte]
  val kty = "oct"
}

case class OctJwk(
                   k: Array[Byte],
                   use: Option[String] = None,
                   alg: Option[String] = None,
                   key_ops: Option[Seq[String]] = None,
                   kid: Option[String] = None
                 ) extends SymmetricJwk

object OctJwk {

  /**
    * @param len key length in bits
    */
  def generate(len: Int) = {
    val random = new SecureRandom
    val k = new Array[Byte](len / 8)
    random.nextBytes(k)
    OctJwk(k)
  }
}

