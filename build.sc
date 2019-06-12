// build.sc
import mill._
import mill.scalalib.publish.{Developer, License, PomSettings, VersionControl}
import scalalib._

val devInfo = Developer("kag0", "Nathan Fischer", "https://github.com/kag0", Some("blackdoor"), Some("https://github.com/blackdoor"))

trait BaseModule extends CrossScalaModule {
  def scalacOptions = Seq("-Xfatal-warnings", "-feature", "-unchecked", "-deprecation")
  def publishVersion = T.input(T.ctx().env("PUBLISH_VERSION"))
}

object jose extends Cross[JoseModule]("2.12.8", "2.13.0-RC2")
class JoseModule(val crossScalaVersion: String) extends BaseModule with PublishModule { root =>

  def ivyDeps = Agg(
    ivy"org.typelevel::cats-core:1.6.0",
    ivy"com.typesafe.scala-logging::scala-logging:3.9.2",
  )

  def pomSettings = PomSettings(
    description = "Extensible JOSE library for Scala.",
    organization = "black.door",
    url = "https://github.com/blackdoor/jose",
    licenses = List(License.Unlicense),
    versionControl = VersionControl.github("blackdoor", "jose"),
    developers = List(devInfo)
  )

  object test extends Tests {
    def moduleDeps = List(json.play(crossScalaVersion), json.circe(crossScalaVersion))

    def ivyDeps = Agg(
      ivy"org.scalatest::scalatest:3.0.7",
      ivy"com.nimbusds:nimbus-jose-jwt:7.1"
    )
    def testFrameworks = List("org.scalatest.tools.Framework")
  }

  object json extends Module {

    object circe extends Cross[CirceModule]("2.12.8", "2.13.0-RC2")
    class CirceModule(val crossScalaVersion: String) extends BaseModule with PublishModule {

      lazy val circeVersion = "0.12.0-M1"

      def moduleDeps = List(jose(crossScalaVersion))
      def ivyDeps = Agg(
        ivy"io.circe::circe-core:$circeVersion",
        ivy"io.circe::circe-generic:$circeVersion",
        ivy"io.circe::circe-parser:$circeVersion"
      )

      def pomSettings = root.pomSettings().copy(description = "Circe JSON support for blackdoor jose")
    }

    object play extends Cross[PlayModule]("2.12.8", "2.13.0-RC2")
    class PlayModule(val crossScalaVersion: String) extends BaseModule with PublishModule {

      def moduleDeps = List(jose(crossScalaVersion))
      def ivyDeps = Agg(ivy"com.typesafe.play::play-json:2.7.3")

      def pomSettings = root.pomSettings().copy(description = "Play JSON support for blackdoor jose")
    }
  }
}