// build.sc
import mill._
import mill.scalalib.publish.{Developer, License, PomSettings, VersionControl}
import scalalib._

val devInfo = Developer("kag0", "Nathan Fischer", "https://github.com/kag0", Some("blackdoor"), Some("https://github.com/blackdoor"))

object jose extends ScalaModule with PublishModule { root =>
  def scalaVersion = "2.12.8"

  def publishVersion = T.input(T.ctx().env("PUBLISH_VERSION"))

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

  object json extends ScalaModule {
    def scalaVersion = root.scalaVersion

    object play extends ScalaModule with PublishModule {
      def scalaVersion = root.scalaVersion

      def moduleDeps = List(jose)
      def ivyDeps = Agg(ivy"com.typesafe.play::play-json:2.7.3")

      def publishVersion = root.publishVersion

      def pomSettings = root.pomSettings().copy(description = "Play JSON support for blackdoor jose")
    }
  }
}