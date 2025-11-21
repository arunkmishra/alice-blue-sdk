import xerial.sbt.Sonatype._
val scala3Version = "3.3.1"
ThisBuild / versionScheme := Some("early-semver")

lazy val root = project
  .in(file("."))
  .enablePlugins(xerial.sbt.Sonatype)
  .settings(
    name := "alice-blue-sdk",
    version := "0.1.0",
    organization := "io.github.arunkmishra",
    description := "Alice Blue Scala SDK",
    
    licenses := List("Apache-2.0" -> url("http://www.apache.org/licenses/LICENSE-2.0.txt")),
    homepage := Some(url("https://github.com/arunkmishra/alice-blue-sdk")),
    
    developers := List(
      Developer(
        "arunkmishra",
        "Arun Kumar Mishra",
        "arunkmishra4@gmail.com",
        url("https://github.com/arunkmishra")
      )
    ),
    
    scmInfo := Some(
      ScmInfo(
        url("https://github.com/arunkmishra/alice-blue-sdk"),
        "scm:git:git@github.com:arunkmishra/alice-blue-sdk.git"
      )
    ),
    
    pomIncludeRepository := { _ => false },
    publishTo := sonatypePublishToBundle.value,
    sonatypeCredentialHost := "central.sonatype.com",
    publishMavenStyle := true,

    scalaVersion := scala3Version,

    libraryDependencies ++= Seq(
      "dev.zio" %% "zio" % "2.0.19",
      "dev.zio" %% "zio-streams" % "2.0.19",
      "dev.zio" %% "zio-json" % "0.6.2",
      "com.softwaremill.sttp.client3" %% "zio" % "3.9.1",
      "com.softwaremill.sttp.client3" %% "zio-json" % "3.9.1",
      "dev.zio" %% "zio-logging" % "2.1.15",
      "dev.zio" %% "zio-logging-slf4j" % "2.1.15",
      "ch.qos.logback" % "logback-classic" % "1.4.11",
      "dev.zio" %% "zio-test" % "2.0.19" % Test,
      "dev.zio" %% "zio-test-sbt" % "2.0.19" % Test
    )
  )
