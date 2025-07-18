import Dependencies.Versions
import xerial.sbt.Sonatype.autoImport.sonatypeRepository
import xerial.sbt.Sonatype.GitHubHosting

lazy val commonSettings = Seq(
  organization           := "org.logging4s",
  organizationName       := "Logging4s",
  homepage               := Some(url("https://logging4s.org/")),
  description            := "Structural logging for Scala 3 via slf4j and logback",
  version                := "0.9.8",
  versionScheme          := Some("semver-spec"),
  scalaVersion           := Versions.scalaLTS,
  parallelExecution      := true,
  publishMavenStyle      := true,
  Test / publishArtifact := false,
  sonatypeTimeoutMillis  := 60 * 60 * 1000,
  sonatypeCredentialHost := "central.sonatype.com",
  sonatypeRepository     := "https://s01.oss.sonatype.org/service/local",
  sonatypeProjectHosting := Some(GitHubHosting("logging4s", "logging4s", "shadowsmind.dev@gmail.com")),
  licenses               := Seq("APL2" -> url("http://www.apache.org/licenses/LICENSE-2.0.txt")),
  publishTo              := sonatypePublishToBundle.value,
  scmInfo                := Some(
    ScmInfo(
      url("https://github.com/logging4s/logging4s"),
      "git@github.com:logging4s/logging4s.git",
    )
  ),
  developers             := List(
    Developer(
      "shadowsmind",
      "Alexandr Oshlakov",
      "shadowsmind.dev@gmail.com",
      url("https://github.com/shadowsmind"),
    )
  ),
  libraryDependencies ++= Dependencies.Testing.all,
  scalacOptions ++= Seq(
    "-encoding",
    "UTF-8",
    "-source:future",
    "-Wunused:all"
  ),
  credentials += Credentials(Path.userHome / ".sbt" / "sonatype_credentials"),
)

lazy val core = project
  .in(file("core"))
  .settings(commonSettings)
  .settings(
    name := "logging4s-core",
    libraryDependencies ++= Dependencies.Logging.all,
  )

lazy val `cats-core` = project
  .in(file("cats/core"))
  .settings(commonSettings)
  .settings(
    name := "logging4s-cats-core",
    libraryDependencies += Dependencies.Cats.catsCore,
  )
  .dependsOn(core)

lazy val `cats-effect-2` = project
  .in(file("cats/ce-2"))
  .settings(commonSettings)
  .settings(
    name := "logging4s-ce-2",
    libraryDependencies ++= Seq(
      Dependencies.Cats.catsEffect2,
      Dependencies.Cats.catsEffect2Testing,
    )
  )
  .dependsOn(`cats-core`)

lazy val `cats-effect-3` = project
  .in(file("cats/ce-3"))
  .settings(commonSettings)
  .settings(
    name := "logging4s-ce-3",
    libraryDependencies ++= Seq(
      Dependencies.Cats.catsEffect3Kernel,
      Dependencies.Cats.catsEffect3Testing,
    )
  )
  .dependsOn(`cats-core`)

lazy val cats = project
  .in(file("cats"))
  .settings(commonSettings)
  .settings(
    publish / skip := true
  )
  .aggregate(
    `cats-core`,
    `cats-effect-2`,
    `cats-effect-3`,
  )

lazy val zio = project
  .in(file("zio"))
  .settings(commonSettings)
  .settings(
    name := "logging4s-zio",
    libraryDependencies ++= Dependencies.Zio.all,
  )
  .dependsOn(core)

lazy val kyo = project
  .in(file("kyo"))
  .settings(commonSettings)
  .settings(
    name         := "logging4s-kyo",
    scalaVersion := Versions.scalaLast,
    libraryDependencies ++= Dependencies.Kyo.all,
  )
  .dependsOn(core)

lazy val circe = project
  .in(file("json/circe"))
  .settings(commonSettings)
  .settings(
    name := "logging4s-circe",
    libraryDependencies ++= Dependencies.Json.circe,
  )
  .dependsOn(core)

lazy val jsoniter = project
  .in(file("json/jsoniter"))
  .settings(commonSettings)
  .settings(
    name := "logging4s-jsoniter",
    libraryDependencies ++= Dependencies.Json.jsoniter,
  )
  .dependsOn(core)

lazy val `play-json` = project
  .in(file("json/play"))
  .settings(commonSettings)
  .settings(
    name := "logging4s-play-json",
    libraryDependencies += Dependencies.Json.playJson,
  )
  .dependsOn(core)

lazy val `spray-json` = project
  .in(file("json/spray"))
  .settings(commonSettings)
  .settings(
    name := "logging4s-spray-json",
    libraryDependencies += Dependencies.Json.sprayJson,
  )
  .dependsOn(core)

lazy val json4s = project
  .in(file("json/json4s"))
  .settings(commonSettings)
  .settings(
    name := "logging4s-json4s",
    libraryDependencies += Dependencies.Json.json4s,
  )
  .dependsOn(core)

lazy val argonaut = project
  .in(file("json/argonaut"))
  .settings(commonSettings)
  .settings(
    name := "logging4s-argonaut",
    libraryDependencies += Dependencies.Json.argonaut,
  )
  .dependsOn(core)

lazy val borer = project
  .in(file("json/borer"))
  .settings(commonSettings)
  .settings(
    name := "logging4s-borer",
    libraryDependencies ++= Dependencies.Json.borer,
  )
  .dependsOn(core)

lazy val upickle = project
  .in(file("json/upickle"))
  .settings(commonSettings)
  .settings(
    name := "logging4s-upickle",
    libraryDependencies += Dependencies.Json.upickle,
  )
  .dependsOn(core)

lazy val weepickle = project
  .in(file("json/weepickle"))
  .settings(commonSettings)
  .settings(
    name := "logging4s-weepickle",
    libraryDependencies += Dependencies.Json.weepickle,
  )
  .dependsOn(core)

lazy val `zio-json` = project
  .in(file("json/zio"))
  .settings(commonSettings)
  .settings(
    name := "logging4s-zio",
    libraryDependencies += Dependencies.Json.zioJson,
  )
  .dependsOn(core)

lazy val fabric = project
  .in(file("json/fabric"))
  .settings(commonSettings)
  .settings(
    name := "logging4s-fabric",
    libraryDependencies ++= Dependencies.Json.fabric,
  )
  .dependsOn(core)

lazy val json = project
  .in(file("json"))
  .settings(commonSettings)
  .settings(
    publish / skip := true
  )
  .aggregate(
    circe,
    jsoniter,
    `play-json`,
    `spray-json`,
    json4s,
    argonaut,
    borer,
    upickle,
    weepickle,
    `zio-json`,
    fabric,
  )

lazy val examples = project
  .in(file("examples"))
  .settings(commonSettings)
  .settings(
    name           := "logging4s-examples",
    publish / skip := true,
  )
  .dependsOn(circe)
  .dependsOn(`cats-effect-3`)
  .dependsOn(zio)

lazy val logging4s = project
  .in(file("."))
  .settings(commonSettings)
  .settings(
    name           := "logging4s",
    publish / skip := true,
  )
  .aggregate(
    core,
    cats,
    zio,
    kyo,
    json,
  )
