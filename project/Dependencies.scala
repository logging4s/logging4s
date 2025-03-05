import sbt.*

object Dependencies {

  object Versions {
    val scala3 = "3.6.3"

    val janino         = "3.1.12"
    val scalaLogging   = "3.9.5"
    val logback        = "1.5.17"
    val logbackEncoder = "8.0"

    val cats               = "2.13.0"
    val catsEffect2        = "2.5.5"
    val catsEffect2Testing = "0.5.4"
    val catsEffect3        = "3.5.7"
    val catsEffectTesting  = "1.6.0"

    val zio        = "2.1.16"
    val zioPrelude = "1.0.0-RC39"

    val kyo = "0.16.2"

    val circe     = "0.14.10"
    val jsoniter  = "2.33.2"
    val playJson  = "3.0.4"
    val sprayJson = "1.3.6"
    val json4s    = "4.0.7"
    val argonaut  = "6.3.10"
    val borer     = "1.15.0"
    val upickle   = "4.1.0"
    val weepickle = "1.9.1"
    val zioJson   = "0.7.38"

    val scalatest = "3.2.19"
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

    val catsEffect2        = "org.typelevel"  %% "cats-effect"                   % Versions.catsEffect2
    val catsEffect2Testing = "com.codecommit" %% "cats-effect-testing-scalatest" % Versions.catsEffect2Testing

    val catsEffect3        = "org.typelevel" %% "cats-effect"                   % Versions.catsEffect3
    val catsEffect3Kernel  = "org.typelevel" %% "cats-effect-kernel"            % Versions.catsEffect3
    val catsEffect3Testing = "org.typelevel" %% "cats-effect-testing-scalatest" % Versions.catsEffectTesting % Test
  }

  object Zio {
    val zio        = "dev.zio" %% "zio"         % Versions.zio
    val zioPrelude = "dev.zio" %% "zio-prelude" % Versions.zioPrelude

    val all: Seq[ModuleID] = Seq(zio, zioPrelude)
  }

  object Kyo {
    val kyoCore = "io.getkyo" %% "kyo-core" % Versions.kyo
    val kyoData = "io.getkyo" %% "kyo-data" % Versions.kyo

    val all: Seq[ModuleID] = Seq(kyoCore, kyoData)
  }

  object Json {
    val circe: Seq[ModuleID] = Seq(
      "io.circe" %% "circe-core",
      "io.circe" %% "circe-parser",
      "io.circe" %% "circe-generic",
    ).map(_ % Versions.circe)

    val jsoniterCore            = "com.github.plokhotnyuk.jsoniter-scala" %% "jsoniter-scala-core"   % Versions.jsoniter
    val jsoniterMacros          = "com.github.plokhotnyuk.jsoniter-scala" %% "jsoniter-scala-macros" % Versions.jsoniter % "provided"
    val jsoniter: Seq[ModuleID] = Seq(jsoniterCore, jsoniterMacros)

    val playJson = "org.playframework" %% "play-json" % Versions.playJson

    val sprayJson = "io.spray" %% "spray-json" % Versions.sprayJson

    val json4s = "org.json4s" %% "json4s-native" % Versions.json4s

    val argonaut = "io.argonaut" %% "argonaut" % Versions.argonaut

    val upickle   = "com.lihaoyi"     %% "upickle"      % Versions.upickle
    val weepickle = "com.rallyhealth" %% "weepickle-v1" % Versions.weepickle

    val borer: ModuleID = "io.bullet" %% "borer-core" % Versions.borer

    val zioJson: ModuleID = "dev.zio" %% "zio-json" % Versions.zioJson
  }

  object Testing {
    val scalatest = "org.scalatest" %% "scalatest" % Versions.scalatest % Test
    val scalactic = "org.scalactic" %% "scalactic" % Versions.scalatest % Test

    val all: Seq[ModuleID] = Seq(scalatest, scalactic)
  }

}
