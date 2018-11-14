name := "jose"

version := "0.1.0"

scalaVersion := "2.12.7"

coverageEnabled := true

libraryDependencies += "org.typelevel" %% "cats-core" % "1.4.0"
libraryDependencies += "com.typesafe.play" %% "play-json" % "2.6.10"
libraryDependencies += "com.nimbusds" % "nimbus-jose-jwt" % "6.0" % Test
libraryDependencies += "org.scalatest" %% "scalatest" % "3.0.5" % Test

enablePlugins(SiteScaladocPlugin)
siteSubdirName in SiteScaladoc := "api/latest"
enablePlugins(GhpagesPlugin)
git.remoteRepo := "git@github.com:blackdoor/jose.git"