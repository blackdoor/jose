package black.door.jose.json

import java.nio.charset.StandardCharsets.UTF_8
import black.door.jose.{ByteDeserializer, ByteSerializer}
import io.github.kag0.ninny.{FromJson, Json, ToSomeJson}

package object ninny {

  def jsonSerializer[A](implicit toJson: ToSomeJson[A]): ByteSerializer[A] =
    a => Json.render(toJson.toSome(a)).getBytes(UTF_8)

  def jsonDeserializer[A](implicit fromJson: FromJson[A]): ByteDeserializer[A] =
    bytes => Json.parse(new String(bytes, UTF_8)).to[A].toEither.left.map(_.toString)
}
