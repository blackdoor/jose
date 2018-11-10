package black.door.jose

import java.nio.charset.StandardCharsets

import black.door.jose.jwk.Jwk
import black.door.jose.jws.JwsHeader
import black.door.jose.jwt.Claims
import play.api.libs.json.{Reads, Writes, Json => PJson}

import scala.util.Try

object Json {
  implicit def jwkWrites: Writes[Jwk] = Writes(_ => ???)
  implicit val headerWrites = play.api.libs.json.Json.writes[JwsHeader]
  def jsonSerializer[A](implicit writes: Writes[A]): Mapper[A, Array[Byte]] =
    a => Right(writes.writes(a).toString.getBytes(StandardCharsets.UTF_8))

  implicit val jwkReads: Reads[Jwk] = Reads(json => ???)
  //implicit val stringSerializer: Mapper[String, Array[Byte]] = s => Right(s.getBytes(StandardCharsets.UTF_8))
  implicit val headerReads = play.api.libs.json.Json.reads[JwsHeader]
  def jsonDeserializer[A](implicit reads: Reads[A]): Mapper[Array[Byte], A] =
    bytes => play.api.libs.json.Json.parse(bytes).validate[A].asEither.left.map(_.toString)

  implicit val claimsFormat = PJson.format[Claims]

  implicit val headerSerializer = jsonSerializer[JwsHeader]
  implicit val headerDeserializer = jsonDeserializer[JwsHeader]
  implicit val byteSerializer: Mapper[Array[Byte], Array[Byte]] = Right(_)
  implicit val stringSerializer: Mapper[String, Array[Byte]] = _.getBytes(StandardCharsets.UTF_8)
  implicit val stringDeserializer: Mapper[Array[Byte], String] = bytes =>
      Try(new String(bytes, StandardCharsets.UTF_8))
        .toEither
        .left
        .map(_.getMessage)
  implicit val claimsSerializer = jsonSerializer[Claims]
  implicit val claimsDeserializer = jsonDeserializer[Claims]
}
