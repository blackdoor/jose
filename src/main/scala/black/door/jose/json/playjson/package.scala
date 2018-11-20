package black.door.jose.json

import java.nio.charset.StandardCharsets

import black.door.jose.Mapper
import play.api.libs.json.{Json, Reads, Writes}

package object playjson {
  def jsonSerializer[A](implicit writes: Writes[A]): Mapper[A, Array[Byte]] =
    a => Right(writes.writes(a).toString.getBytes(StandardCharsets.UTF_8))
  def jsonDeserializer[A](implicit reads: Reads[A]): Mapper[Array[Byte], A] =
    bytes => Json.parse(bytes).validate[A].asEither.left.map(_.toString)
}
