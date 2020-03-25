package black.door.jose.jws

import black.door.jose.jwk.Jwk
import cats.data.EitherT

import scala.concurrent.Future
import scala.language.implicitConversions

trait KeyResolver[A] {
  def resolve(header: JwsHeader, payload: A): EitherT[Future, String, Jwk]
}

object KeyResolver {

  implicit def fromSingleKey[A](key: Jwk): KeyResolver[A] =
    (_, _) => EitherT[Future, String, Jwk](Future.successful(Right(key)))

  implicit def fromKeys[A](keys: Seq[Jwk]): KeyResolver[A] =
    (header, _) =>
      EitherT(
        Future.successful(
          keys
            .collectFirst {
              case key if key.kid == header.kid => key
            }
            .toRight(s"no key found in set for kid ${header.kid}")
        )
      )
}
