package black.door.jose.jwk

import black.door.jose.Mapper

trait Jwk {
  def kty: String
  def use: Option[String]
  def key_ops: Option[Seq[String]]
  def alg: Option[String]
  def kid: Option[String]
}

object Jwk {
  def parse(json: String)(implicit parser: Mapper[String, Jwk]) = parser(json)
}