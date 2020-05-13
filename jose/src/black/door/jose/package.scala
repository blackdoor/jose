package black.door

package object jose {
  type Mapper[-A, B] = A => Either[String, B]
}
