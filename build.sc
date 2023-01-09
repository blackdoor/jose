import mill._
import mill.define.{Segment, Segments}
import mill.scalalib.publish.{Developer, License, PomSettings, VersionControl}
import scalalib._

val devInfo = Developer(
  "kag0",
  "Nathan Fischer",
  "https://github.com/kag0",
  Some("blackdoor"),
  Some("https://github.com/blackdoor")
)

val `2.12` = "2.12.15"
val `2.13` = "2.13.7"
val `3`    = "3.1.0"

trait BaseModule extends CrossScalaModule {
  def scalacOptions = Seq("-Xfatal-warnings", "-feature", "-unchecked", "-deprecation")
  def publishVersion: T[String] = T.input(T.ctx().env("PUBLISH_VERSION"))

  def pomSettings: T[PomSettings] = PomSettings(
    description = "Extensible JOSE library for Scala.",
    organization = "black.door",
    url = "https://github.com/blackdoor/jose",
    licenses = List(License.Unlicense),
    versionControl = VersionControl.github("blackdoor", "jose"),
    developers = List(devInfo)
  )

  def artifactName =
    Segments(millModuleSegments.value.filterNot(_.isInstanceOf[Segment.Cross]): _*).parts
      .mkString("-")

  trait Test extends Tests with TestModule.ScalaTest {
    def scalacOptions = T(super.scalacOptions().filterNot(_ == "-Xfatal-warnings"))

    def ivyDeps = Agg(
      ivy"org.scalatest::scalatest:3.2.10",
      ivy"com.nimbusds:nimbus-jose-jwt:9.28"
    )
  }

}

object jose extends Cross[JoseModule](`2.12`, `2.13`, `3`)

class JoseModule(val crossScalaVersion: String) extends BaseModule with PublishModule {

  def ivyDeps = Agg(
    ivy"org.typelevel::cats-core:2.7.0",
    ivy"com.typesafe.scala-logging::scala-logging:3.9.5"
  )

  object test extends Test
}

object json extends Module {

  abstract class JsonModule(moduleName: String) extends BaseModule {
    def artifactName = "jose-" + super.artifactName()

    def pomSettings =
      super.pomSettings().copy(description = s"$moduleName JSON support for blackdoor jose")

    object test extends Test {
      def moduleDeps = super.moduleDeps :+ jose(crossScalaVersion).test
    }

  }

  object circe extends Cross[CirceModule](`2.12`, `2.13`, `3`)

  class CirceModule(val crossScalaVersion: String)
      extends JsonModule("Circe")
      with PublishModule {
    def moduleDeps        = List(jose(crossScalaVersion))
    lazy val circeVersion = "0.14.3"

    def ivyDeps = Agg(
      ivy"io.circe::circe-core:$circeVersion",
      ivy"io.circe::circe-generic:$circeVersion",
      ivy"io.circe::circe-parser:$circeVersion"
    )

  }

  object play extends Cross[PlayModule](`2.12`, `2.13`)

  class PlayModule(val crossScalaVersion: String)
      extends JsonModule("Play")
      with PublishModule {
    def ivyDeps    = Agg(ivy"com.typesafe.play::play-json:2.9.3")
    def moduleDeps = List(jose(crossScalaVersion))
  }

  object ninny extends Cross[NinnyModule](`2.12`, `2.13`)

  class NinnyModule(val crossScalaVersion: String)
      extends JsonModule("ninny")
      with PublishModule {

    def ivyDeps    = Agg(ivy"io.github.kag0::ninny:0.4.3")
    def moduleDeps = List(jose(crossScalaVersion))
  }

}

object docs extends ScalaModule {
  def scalaVersion = `2.13`
  def moduleDeps   = List(json.ninny(`2.13`))
}
