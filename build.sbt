import Dependencies.Versions

lazy val commonSettings = Seq(
  organization           := "org.logging4s",
  organizationName       := "Logging4s",
  homepage               := Some(uri("https://logging4s.org/")),
  description            := "Structural logging for Scala 3 via slf4j and logback",
  version                := "0.19.0",
  versionScheme          := Some("semver-spec"),
  scalaVersion           := Versions.scalaLTS,
  parallelExecution      := true,
  publishMavenStyle      := true,
  Test / publishArtifact := false,
  licenses               := List(License.Apache2),
  pomIncludeRepository   := { _ => false },
  publishTo              := localStaging.value,
  scmInfo                := Some(
    ScmInfo(
      uri("https://github.com/logging4s/logging4s"),
      "git@github.com:logging4s/logging4s.git",
    )
  ),
  developers             := List(
    Developer(
      "shadowsmind",
      "Alexandr Oshlakov",
      "shadowsmind.dev@gmail.com",
      uri("https://github.com/shadowsmind"),
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
  )

lazy val logback = project
  .in(file("backend/logback"))
  .settings(commonSettings)
  .settings(
    name := "logging4s-logback",
    libraryDependencies ++= Dependencies.Logback.all,
    libraryDependencies += Dependencies.Logback.jacksonDatabind,
  )
  .dependsOn(core)

lazy val log4j2 = project
  .in(file("backend/log4j2"))
  .settings(commonSettings)
  .settings(
    name := "logging4s-log4j2",
    libraryDependencies ++= Dependencies.Log4j2.all,
  )
  .dependsOn(core)

lazy val slf4j = project
  .in(file("backend/slf4j"))
  .settings(commonSettings)
  .settings(
    name := "logging4s-slf4j",
    libraryDependencies ++= Dependencies.Slf4j.all,
  )
  .dependsOn(core)
  .dependsOn(logback % Test)

lazy val backend = project
  .in(file("backend"))
  .settings(commonSettings)
  .settings(
    publish / skip := true
  )
  .aggregate(
    logback,
    log4j2,
    slf4j,
  )

lazy val `cats-core` = project
  .in(file("runtime/cats/core"))
  .settings(commonSettings)
  .settings(
    name := "logging4s-cats-core",
    libraryDependencies += Dependencies.Cats.catsCore,
  )
  .dependsOn(core)

lazy val `cats-effect-2` = project
  .in(file("runtime/cats/ce-2"))
  .settings(commonSettings)
  .settings(
    name := "logging4s-ce-2",
    libraryDependencies ++= Seq(
      Dependencies.Cats.catsEffect2,
      Dependencies.Cats.catsEffect2Testing,
    )
  )
  .dependsOn(`cats-core`)
  .dependsOn(logback % Test)

lazy val `cats-effect-3` = project
  .in(file("runtime/cats/ce-3"))
  .settings(commonSettings)
  .settings(
    name := "logging4s-ce-3",
    libraryDependencies ++= Seq(
      Dependencies.Cats.catsEffect3Kernel,
      Dependencies.Cats.catsEffect3Testing,
    )
  )
  .dependsOn(`cats-core`)
  .dependsOn(logback % Test)

lazy val cats = project
  .in(file("runtime/cats"))
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
  .in(file("runtime/zio"))
  .settings(commonSettings)
  .settings(
    name := "logging4s-zio",
    libraryDependencies ++= Dependencies.Zio.all,
  )
  .dependsOn(core)
  .dependsOn(logback % Test)

lazy val kyo = project
  .in(file("runtime/kyo"))
  .settings(commonSettings)
  .settings(
    name         := "logging4s-kyo",
    scalaVersion := Versions.scalaLast,
    libraryDependencies ++= Dependencies.Kyo.all,
  )
  .dependsOn(core)
  .dependsOn(logback % Test)

lazy val rapid = project
  .in(file("runtime/rapid"))
  .settings(commonSettings)
  .settings(
    name         := "logging4s-rapid",
    scalaVersion := Versions.scalaLast,
    libraryDependencies ++= Dependencies.Rapid.all,
  )
  .dependsOn(core)
  .dependsOn(logback % Test)

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
    name := "logging4s-zio-json",
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

val runtime = project
  .in(file("runtime"))
  .settings(commonSettings)
  .settings(
    publish / skip := true
  )
  .aggregate(
    cats,
    zio,
    kyo,
    rapid,
  )

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
    libraryDependencies += Dependencies.Cats.catsEffect3,
  )
  .dependsOn(circe)
  .dependsOn(`cats-effect-3`)
  .dependsOn(zio)
  .dependsOn(logback)

lazy val logging4s = project
  .in(file("."))
  .settings(commonSettings)
  .settings(
    name           := "logging4s",
    publish / skip := true,
  )
  .aggregate(
    core,
    backend,
    runtime,
    json,
  )
