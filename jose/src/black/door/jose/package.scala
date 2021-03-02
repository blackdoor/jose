package black.door

package object jose {

  type ByteSerializer[-A]    = A => Array[Byte]
  type StringSerializer[-A]  = A => String
  type ByteDeserializer[A]   = Array[Byte] => Either[String, A]
  type StringDeserializer[A] = String => Either[String, A]

  implicit val byteDeserializer: ByteDeserializer[Array[Byte]] = Right(_)
}
