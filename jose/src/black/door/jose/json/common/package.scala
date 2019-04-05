package black.door.jose.json

import java.nio.charset.StandardCharsets

import black.door.jose.Mapper

import scala.util.Try

package object common {
  implicit val byteMapper: Mapper[Array[Byte], Array[Byte]] = Right(_)
  implicit val stringSerializer: Mapper[String, Array[Byte]] = _.getBytes(StandardCharsets.UTF_8)
  implicit val stringDeserializer: Mapper[Array[Byte], String] = bytes =>
    Try(new String(bytes, StandardCharsets.UTF_8))
      .toEither
      .left
      .map(_.getMessage)
}
