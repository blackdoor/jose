package black.door.jose.jwk

import java.security.SecureRandom

trait SymmetricJwk extends Jwk {
  def k: Array[Byte]
  val kty = "oct"
}

trait HsJwk extends SymmetricJwk {
  require(alg.forall(HsJwk.validAlgs.contains), "JWK algorithm must be a HMAC with SHA-2 function")
  private def keyLengthIsValid(_alg: String) = _alg match {
    case "HS256" => k.length == 32
    case "HS384" => k.length == 48
    case "HS512" => k.length == 64
  }
  require(alg.forall(keyLengthIsValid), "JWK oct key must be the same length as the HMAC algorithm used")

  val use = Some("sig")

  def isValidFor(algorithm: String) = HsJwk.validAlgs.contains(algorithm) && keyLengthIsValid(algorithm)
}

abstract case class OctJwk(
                            k: Array[Byte],
                            alg: Option[String] = None,
                  key_ops: Option[Seq[String]] = None,
                  kid: Option[String] = None
                          ) extends SymmetricJwk

object HsJwk {
  val validAlgs = List("HS256", "HS384", "HS512")

  def generate = {
    val random = new SecureRandom()
    val k = new Array[Byte](32)
    random.nextBytes(k)
    new OctJwk(k, Some("HS256")) with HsJwk
  }

  def javaAlgorithm(alg: String) = alg match {
    case "HS256" => "HmacSHA256"
    case "HS384" => "HmacSHA384"
    case "HS512" => "HmacSHA512"
  }
}

