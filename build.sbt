name := "jose"

version := "0.1.2"

scalaVersion := "2.12.8"

libraryDependencies ++= Seq(
  "org.typelevel" %% "cats-core" % "1.4.0",
  "com.typesafe.play" %% "play-json" % "2.6.10",
  "com.typesafe.scala-logging" %% "scala-logging" % "3.9.0",
  "com.nimbusds" % "nimbus-jose-jwt" % "6.0" % Test,
  "org.scalatest" %% "scalatest" % "3.0.5" % Test
)

enablePlugins(SiteScaladocPlugin)
siteSubdirName in SiteScaladoc := s"api/${sys.env.getOrElse("RELEASE_TAG", "latest")}"
