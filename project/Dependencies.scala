import sbt._

object Dependencies {

  object Versions {
    val scala = "3.2.1"

    val janino         = "3.1.9"
    val scalaLogging   = "3.9.5"
    val logback        = "1.4.5"
    val logbackEncoder = "7.2"

    val cats       = "2.9.0"
    val catsEffect = "3.4.5"
    val circe      = "0.14.3"
    val jsoniter   = "2.20.2"
    val playJson   = "2.10.0-RC7"
    val sprayJson  = "1.3.6"
    val json4s     = "4.0.6"
    val argonaut   = "6.3.8"

    val scalatest         = "3.2.15"
    val catsEffectTesting = "1.4.0"
  }

  object Logging {
    val janino         = "org.codehaus.janino"         % "janino"                   % Versions.janino
    val scalaLogging   = "com.typesafe.scala-logging" %% "scala-logging"            % Versions.scalaLogging
    val logbackClassic = "ch.qos.logback"              % "logback-classic"          % Versions.logback
    val logbackEncoder = "net.logstash.logback"        % "logstash-logback-encoder" % Versions.logbackEncoder

    val all: Seq[ModuleID] = Seq(janino, scalaLogging, logbackClassic, logbackEncoder)
  }

  object Cats {
    val cats       = "org.typelevel" %% "cats-core"   % Versions.cats
    val catsEffect = "org.typelevel" %% "cats-effect" % Versions.catsEffect

    val all: Seq[ModuleID] = Seq(cats, catsEffect)

  }

  object Json {
    val circeCore    = "io.circe" %% "circe-core"    % Versions.circe
    val circeParser  = "io.circe" %% "circe-parser"  % Versions.circe
    val circeGeneric = "io.circe" %% "circe-generic" % Versions.circe

    val circe: Seq[ModuleID] = Seq(circeCore, circeParser, circeGeneric)

    val jsoniterCore   = "com.github.plokhotnyuk.jsoniter-scala" %% "jsoniter-scala-core"   % Versions.jsoniter
    val jsoniterMacros = "com.github.plokhotnyuk.jsoniter-scala" %% "jsoniter-scala-macros" % Versions.jsoniter % "provided"

    val jsoniter: Seq[ModuleID] = Seq(jsoniterCore, jsoniterMacros)

    val playJson = "com.typesafe.play" %% "play-json" % Versions.playJson

    val sprayJson = "io.spray" %% "spray-json" % Versions.sprayJson

    val json4s = "org.json4s" %% "json4s-native" % Versions.json4s

    val argonaut = "io.argonaut" %% "argonaut" % Versions.argonaut
  }

  object Testing {
    val scalatest   = "org.scalatest" %% "scalatest"                     % Versions.scalatest         % Test
    val scalactic   = "org.scalactic" %% "scalactic"                     % Versions.scalatest         % Test
    val catsEffects = "org.typelevel" %% "cats-effect-testing-scalatest" % Versions.catsEffectTesting % Test

    val core: Seq[ModuleID] = Seq(scalatest, scalactic)
    val all: Seq[ModuleID]  = core :+ catsEffects
  }

}
