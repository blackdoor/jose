name := "jose"

version := "0.1"

scalaVersion := "2.12.7"

libraryDependencies += "org.typelevel" %% "cats-core" % "1.4.0"
libraryDependencies += "com.typesafe.play" %% "play-json" % "2.6.10"
libraryDependencies += "com.nimbusds" % "nimbus-jose-jwt" % "6.0" % Test
libraryDependencies += "org.scalatest" %% "scalatest" % "3.0.5" % Test
