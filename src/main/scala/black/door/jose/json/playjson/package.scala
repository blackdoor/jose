package black.door.jose.json

import java.nio.charset.StandardCharsets

import scala.language.higherKinds
import black.door.jose.{Mapper, Unmapper}
import cats.Functor
import play.api.libs.json._
import cats.syntax.functor._

package object playjson {
  def jsonSerializer[A](implicit writes: Writes[A]): Mapper[A, Array[Byte]] =
    a => writes.writes(a).toString.getBytes(StandardCharsets.UTF_8)
  def jsonDeserializer[A](implicit reads: Reads[A]): Unmapper[Array[Byte], A] =
    bytes => Json.parse(bytes).validate[A].asEither.left.map(_.toString)

  implicit val unitReads = Reads[Unit](_ => JsSuccess(Unit))
  implicit val unitWrites = OWrites[Unit](_ => JsObject.empty)

  private[playjson] def integratedWrites[A[_], B: OWrites](key: String, preWrites: OWrites[A[B]]) =
    preWrites
      .transform { jsObj: JsObject =>
        (jsObj - key) ++ (jsObj \ key).as[JsObject]
      }

  private[playjson] def integratedReads[A[_]: Functor, B: Reads](key: String, unitReads: Reads[A[Unit]]): Reads[A[B]] = {
    val unregisteredInjector = Reads(_.validate[JsObject]
      .map ( jsObj =>
        if(jsObj.keys.contains(key)) jsObj
        else jsObj + (key, JsNull)
      )
    )

    Reads { js =>
      for {
        unit <- unitReads.reads(js)
        unregistered <- implicitly[Reads[B]].reads(js)
      } yield unit.map(_ => unregistered)
    }.compose(unregisteredInjector)
  }
}
