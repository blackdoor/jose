package object pkg {
  type Mapper[A, B] = A => Either[String, B]
}
