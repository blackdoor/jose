package black.door.jose.jwk

import black.door.jose.Unmapper

trait Jwk {
  def kty: String
  def use: Option[String]
  def key_ops: Option[Seq[String]]
  def alg: Option[String]
  def kid: Option[String]
}

object Jwk {
  def parse(json: String)(implicit parser: Unmapper[String, Jwk]) = parser(json)
}