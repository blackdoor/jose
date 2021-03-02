package black.door.jose.json

import black.door.jose.{ByteDeserializer, ByteSerializer}

import java.nio.charset.StandardCharsets
import scala.util.Try

package object common {
  implicit val byteMapper: ByteDeserializer[Array[Byte]] = Right(_)

  implicit val stringSerializer: ByteSerializer[String] =
    _.getBytes(StandardCharsets.UTF_8)

  implicit val stringDeserializer: ByteDeserializer[String] = bytes =>
    Try(new String(bytes, StandardCharsets.UTF_8)).toEither.left
      .map(_.getMessage)
}
