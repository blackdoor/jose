package black.door.jose.jwk

trait Jwk {
  def kty: String
  def use: Option[String]
  def key_ops: Option[Seq[String]]
  def alg: Option[String]
  def kid: Option[String]
}
