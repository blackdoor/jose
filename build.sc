import mill._
import mill.define.{Segment, Segments}
import mill.scalalib.publish.{Developer, License, PomSettings, VersionControl}
import scalalib._

val devInfo = Developer(
  "kag0",
  "Nathan Fischer",
  "https://github.com/nrktkt",
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
    millModuleSegments.value.filterNot(_.isInstanceOf[Segment.Cross])
      .map(_.pathSegments.head)
      .mkString("-")

  trait Test extends ScalaTests with TestModule.ScalaTest {
    override def scalacOptions = super.scalacOptions().filterNot(_ == "-Xfatal-warnings")

    def ivyDeps = Agg(
      mvn"org.scalatest::scalatest:3.2.15",
      mvn"com.nimbusds:nimbus-jose-jwt:9.30",
      mvn"org.slf4j:slf4j-simple:2.0.17"
    )
  }

}

object jose extends Cross[JoseModule](`2.12`, `2.13`, `3`)

trait JoseModule extends BaseModule with PublishModule with Cross.Module[String] {
  def crossScalaVersion = crossValue

  def ivyDeps = Agg(
    mvn"org.typelevel::cats-core:2.7.0",
    mvn"com.typesafe.scala-logging::scala-logging:3.9.5"
  )

  object test extends Test
}

object json extends Module {

  trait JsonModule extends BaseModule {
    def jsonModuleName: String

    def artifactName = "jose-" + super.artifactName()

    def pomSettings =
      super.pomSettings().copy(description = s"$jsonModuleName JSON support for blackdoor jose")

    object test extends Test {
      override def moduleDeps = super.moduleDeps ++ Seq(jose(crossScalaVersion).test)
    }

  }

  object circe extends Cross[CirceModule](`2.12`, `2.13`, `3`)

  trait CirceModule extends JsonModule
      with PublishModule with Cross.Module[String] {
    def crossScalaVersion = crossValue
    def jsonModuleName = "Circe"
    def moduleDeps        = List(jose(crossScalaVersion))
    lazy val circeVersion = "0.14.3"

    def ivyDeps = Agg(
      mvn"io.circe::circe-core:$circeVersion",
      mvn"io.circe::circe-generic:$circeVersion",
      mvn"io.circe::circe-parser:$circeVersion"
    )

  }

  object play extends Cross[PlayModule](`2.12`, `2.13`)

  trait PlayModule extends JsonModule
      with PublishModule with Cross.Module[String] {
    def crossScalaVersion = crossValue
    def jsonModuleName = "Play"
    def ivyDeps    = Agg(mvn"com.typesafe.play::play-json:2.10.8")
    def moduleDeps = List(jose(crossScalaVersion))
  }

  object ninny extends Cross[NinnyModule](`2.13`)

  trait NinnyModule extends JsonModule
      with PublishModule with Cross.Module[String] {
    def crossScalaVersion = crossValue
    def jsonModuleName = "ninny"
    override def scalacOptions = super.scalacOptions().filterNot(_ == "-Xfatal-warnings")

    def ivyDeps    = Agg(mvn"tk.nrktkt::ninny:0.7.2")
    def moduleDeps = List(jose(crossScalaVersion))
  }

}

object docs extends ScalaModule {
  def scalaVersion = `2.13`
  def moduleDeps   = List(json.ninny(`2.13`))
}
