import sbt._

object Dependencies {

  object Versions {
    val scala212 = "2.12.17"
    val scala213 = "2.13.10"
    val scala3   = "3.2.2"

    val janino         = "3.1.9"
    val scalaLogging   = "3.9.5"
    val logback        = "1.4.5"
    val logbackEncoder = "7.2"

    val cats              = "2.9.0"
    val catsEffect2       = "2.5.5"
    val catsEffect3       = "3.4.6"
    val catsEffectTesting = "1.4.0"

    val zio        = "2.0.6"
    val zioPrelude = "1.0.0-RC16"

    val circe     = "0.14.4"
    val jsoniter  = "2.20.7"
    val playJson  = "2.10.0-RC7"
    val sprayJson = "1.3.6"
    val json4s    = "4.0.6"
    val argonaut  = "6.3.8"

    val scalatest = "3.2.15"
  }

  object Logging {
    val janino         = "org.codehaus.janino"         % "janino"                   % Versions.janino
    val scalaLogging   = "com.typesafe.scala-logging" %% "scala-logging"            % Versions.scalaLogging
    val logbackClassic = "ch.qos.logback"              % "logback-classic"          % Versions.logback
    val logbackEncoder = "net.logstash.logback"        % "logstash-logback-encoder" % Versions.logbackEncoder

    val all: Seq[ModuleID] = Seq(janino, scalaLogging, logbackClassic, logbackEncoder)
  }

  object Cats {
    val catsCore = "org.typelevel" %% "cats-core" % Versions.cats

    val catsEffect2 = "org.typelevel" %% "cats-effect" % Versions.catsEffect2

    val catsEffect3         = "org.typelevel" %% "cats-effect"                   % Versions.catsEffect3
    val catsEffect3Kernel   = "org.typelevel" %% "cats-effect-kernel"            % Versions.catsEffect3
    val catsEffect3Testing = "org.typelevel" %% "cats-effect-testing-scalatest" % Versions.catsEffectTesting % Test
  }

  object Zio {
    val zio        = "dev.zio" %% "zio"         % Versions.zio
    val zioPrelude = "dev.zio" %% "zio-prelude" % Versions.zioPrelude

    val all: Seq[ModuleID] = Seq(zio, zioPrelude)
  }

  object Json {
    val circeCore    = "io.circe" %% "circe-core"    % Versions.circe
    val circeParser  = "io.circe" %% "circe-parser"  % Versions.circe
    val circeGeneric = "io.circe" %% "circe-generic" % Versions.circe

    val circe: Seq[ModuleID] = Seq(circeCore, circeParser, circeGeneric)

    val jsoniterCore            = "com.github.plokhotnyuk.jsoniter-scala" %% "jsoniter-scala-core"   % Versions.jsoniter
    val jsoniterMacros          = "com.github.plokhotnyuk.jsoniter-scala" %% "jsoniter-scala-macros" % Versions.jsoniter % "provided"
    val jsoniter: Seq[ModuleID] = Seq(jsoniterCore, jsoniterMacros)

    val playJson = "com.typesafe.play" %% "play-json" % Versions.playJson

    val sprayJson = "io.spray" %% "spray-json" % Versions.sprayJson

    val json4s = "org.json4s" %% "json4s-native" % Versions.json4s

    val argonaut = "io.argonaut" %% "argonaut" % Versions.argonaut
  }

  object Testing {
    val scalatest = "org.scalatest" %% "scalatest" % Versions.scalatest % Test
    val scalactic = "org.scalactic" %% "scalactic" % Versions.scalatest % Test

    val all: Seq[ModuleID] = Seq(scalatest, scalactic)
  }

}
