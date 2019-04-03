// build.sc
import mill._, scalalib._

val scala12 = "2.12.8"

object jose extends ScalaModule {
  def scalaVersion = scala12

  def ivyDeps = Agg(
    ivy"org.typelevel::cats-core:1.4.0",
    ivy"com.typesafe.play::play-json:2.6.10",
    ivy"com.typesafe.scala-logging::scala-logging:3.9.0",
  )

  object test extends Tests {
    def moduleDeps = List(playjson)

    def ivyDeps = Agg(
      ivy"org.scalatest::scalatest:3.0.7",
      ivy"com.nimbusds:nimbus-jose-jwt:6.0"
    )
    def testFrameworks = List("org.scalatest.tools.Framework")
  }
}

object playjson extends ScalaModule {
  def scalaVersion = scala12

  def moduleDeps = List(jose)
}

