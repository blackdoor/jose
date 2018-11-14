package black.door.jose.jws

import cats.data.EitherT
import cats.implicits._
import black.door.jose.jwk.Jwk

import scala.concurrent.{ExecutionContext, Future}

trait KeyResolver[A] {
  def resolve(header: JwsHeader, payload: A): EitherT[Future, String, Jwk]
}
object KeyResolver {
  implicit def fromSingleKey[A](key: Jwk)(implicit ex: ExecutionContext): KeyResolver[A] =
    (header: JwsHeader, payload: A) => EitherT.rightT[Future, String](key)
}