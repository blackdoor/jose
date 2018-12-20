package black.door.jose.json

import java.nio.charset.StandardCharsets

import black.door.jose.{Mapper, Unmapper}

import scala.util.Try

package object common {
  implicit val byteUnmapper: Unmapper[Array[Byte], Array[Byte]] = Unmapper.fromMapper(identity)
  implicit val stringSerializer: Mapper[String, Array[Byte]] = _.getBytes(StandardCharsets.UTF_8)
  implicit val stringDeserializer: Unmapper[Array[Byte], String] = bytes =>
    Try(new String(bytes, StandardCharsets.UTF_8))
      .toEither
      .left
      .map(_.getMessage)
}
