package black.door.jose.jwk

import java.security.SecureRandom
import scala.collection.immutable._

case class OctJwk(
    k: IndexedSeq[Byte],
    use: Option[String] = None,
    alg: Option[String] = None,
    key_ops: Option[Seq[String]] = None,
    kid: Option[String] = None
  ) extends Jwk {
  val kty = "oct"

  def withAlg(alg: Option[String]) = copy(alg = alg)
}

object OctJwk {

  /** @param len
    *   key length in bits
    */
  def generate(len: Int) = {
    val random = new SecureRandom
    val k      = new Array[Byte](len / 8)
    random.nextBytes(k)
    OctJwk(k.toIndexedSeq)
  }
}
