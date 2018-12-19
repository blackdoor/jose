package black.door.jose.jws

import cats.data.EitherT
import cats.implicits._
import black.door.jose.jwk.Jwk

import scala.concurrent.{ExecutionContext, Future}

trait KeyResolver[Par, Pay] {
  def resolve(header: JwsHeader[Par], payload: Pay): EitherT[Future, String, Jwk]
}
object KeyResolver {
  implicit def fromSingleKey[Par, Pay](key: Jwk)(implicit ex: ExecutionContext) = new KeyResolver[Par, Pay] {
    def resolve(header: JwsHeader[Par], payload: Pay) = EitherT.rightT(key)
  }
}