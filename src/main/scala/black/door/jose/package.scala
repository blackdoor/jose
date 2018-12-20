package black.door

import scala.language.implicitConversions

package object jose {
  type Unmapper[A, B] = A => Either[String, B]
  type Mapper[-A, B] = A => B

  object Unmapper {
    def fromMapper[A, B](mapper: Mapper[A, B]): Unmapper[A, B] = mapper.andThen(Right(_))
  }
}
