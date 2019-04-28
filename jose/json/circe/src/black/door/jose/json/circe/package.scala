package black.door.jose.json

import java.nio.charset.StandardCharsets

import black.door.jose.Mapper
import io.circe._
import io.circe.parser._
import io.circe.syntax._

package object circe {
  def jsonSerializer[A](implicit encoder: Encoder[A]): Mapper[A, Array[Byte]] =
    a => Right(a.asJson.dropNullValues.toString().getBytes(StandardCharsets.UTF_8))
  def jsonDeserializer[A](implicit decoder: Decoder[A]): Mapper[Array[Byte], A] =
    bytes => decode(new String(bytes, StandardCharsets.UTF_8)).left.map(_.toString)
}
