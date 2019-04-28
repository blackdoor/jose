// build.sc
import mill._
import mill.scalalib.publish.{Developer, License, PomSettings, VersionControl}
import scalalib._

val devInfo = Developer("kag0", "Nathan Fischer", "https://github.com/kag0", Some("blackdoor"), Some("https://github.com/blackdoor"))

trait BaseModule extends ScalaModule {
  def scalaVersion = "2.12.8"
  def scalacOptions = Seq("-Xfatal-warnings", "-feature", "-unchecked", "-deprecation")
  def publishVersion = T.input(T.ctx().env("PUBLISH_VERSION"))
}

object jose extends BaseModule with PublishModule { root =>

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
    def moduleDeps = List(json.play)

    def ivyDeps = Agg(
      ivy"org.scalatest::scalatest:3.0.7",
      ivy"com.nimbusds:nimbus-jose-jwt:7.1"
    )
    def testFrameworks = List("org.scalatest.tools.Framework")
  }

  object json extends BaseModule {

    object circe extends BaseModule with PublishModule {
      lazy val circeVersion = "0.11.1"

      def moduleDeps = List(jose)
      def ivyDeps = Agg(
        ivy"io.circe::circe-core:$circeVersion",
        ivy"io.circe::circe-generic:$circeVersion",
        ivy"io.circe::circe-parser:$circeVersion"
      )

      def pomSettings = root.pomSettings().copy(description = "Circe JSON support for blackdoor jose")
    }

    object play extends BaseModule with PublishModule {

      def moduleDeps = List(jose)
      def ivyDeps = Agg(ivy"com.typesafe.play::play-json:2.7.3")

      def pomSettings = root.pomSettings().copy(description = "Play JSON support for blackdoor jose")
    }
  }
}