import sbt._

object Dependencies {

  object Versions {
    val scala = "3.2.1"

    val scalatest         = "3.2.14"
    val catsEffectTesting = "1.4.0"
  }

  object Testing {
    val scalatest   = "org.scalatest" %% "scalatest"                     % Versions.scalatest         % Test
    val scalactic   = "org.scalactic" %% "scalactic"                     % Versions.scalatest         % Test
    val catsEffects = "org.typelevel" %% "cats-effect-testing-scalatest" % Versions.catsEffectTesting % Test

    val core: Seq[ModuleID] = Seq(scalatest, scalactic)
    val all: Seq[ModuleID]  = core :+ catsEffects
  }

}
