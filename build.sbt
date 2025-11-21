val scala3Version = "3.3.1"

lazy val root = project
  .in(file("."))
  .settings(
    name := "alice-blue-sdk",
    version := "0.1.0-SNAPSHOT",

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
