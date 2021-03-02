package black.door.jose.jwk

import black.door.jose.StringDeserializer

trait Jwk {
  def kty: String
  def use: Option[String]
  def key_ops: Option[Seq[String]]
  def alg: Option[String]
  def kid: Option[String]

  def withAlg(alg: Option[String]): Jwk
}

object Jwk {
  def parse(json: String)(implicit parser: StringDeserializer[Jwk]) = parser(json)
}
