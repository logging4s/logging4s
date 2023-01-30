lazy val commonSettings = Seq(
  scalaVersion := Dependencies.Versions.scala3,
  organization := "org.logging4s",
  libraryDependencies ++= Dependencies.Testing.all,
  scalacOptions ++= Seq(
    "-source:future",
    "-Xmax-inlines",
    "200"
  ),
  developers   := List(
    Developer(
      "shadowsmind",
      "Alexandr Oshlakov",
      "shadowsmind.dev@gmail.com",
      url("https://github.com/shadowsmind")
    )
  )
)

lazy val core = (project in file("core"))
  .settings(commonSettings)
  .settings(
    name := "logging4s-core",
    libraryDependencies ++= Dependencies.Logging.all
  )

lazy val `cats-core` = (project in file("cats/core"))
  .settings(commonSettings)
  .settings(
    name := "logging4s-cats-core",
    libraryDependencies += Dependencies.Cats.catsCore
  )
  .dependsOn(core)

lazy val `cats-effect-2` = (project in file("cats/ce-2"))
  .settings(commonSettings)
  .settings(
    name := "logging4s-ce-2",
    libraryDependencies += Dependencies.Cats.catsEffect2
  )
  .dependsOn(`cats-core`)

lazy val `cats-effect-3` = (project in file("cats/ce-3"))
  .settings(commonSettings)
  .settings(
    name := "logging4s-ce-3",
    libraryDependencies ++= Seq(
      Dependencies.Cats.catsEffect3Kernel,
      Dependencies.Cats.catsEffect3Testing,
    )
  )
  .dependsOn(`cats-core`)

lazy val cats = (project in file("cats"))
  .settings(commonSettings)
  .aggregate(
    `cats-effect-2`,
    `cats-effect-3`
  )

lazy val zio = (project in file("zio"))
  .settings(commonSettings)
  .settings(
    name := "logging4s-zio",
    libraryDependencies ++= Dependencies.Zio.all
  )
  .dependsOn(core)

lazy val circe = (project in file("json/circe"))
  .settings(commonSettings)
  .settings(
    name := "logging4s-circe",
    libraryDependencies ++= Dependencies.Json.circe
  )
  .dependsOn(core)

lazy val jsoniter = (project in file("json/jsoniter"))
  .settings(commonSettings)
  .settings(
    name := "logging4s-jsoniter",
    libraryDependencies ++= Dependencies.Json.jsoniter
  )
  .dependsOn(core)

lazy val `play-json` = (project in file("json/play"))
  .settings(commonSettings)
  .settings(
    name := "logging4s-play-json",
    libraryDependencies += Dependencies.Json.playJson
  )
  .dependsOn(core)

lazy val `spray-json` = (project in file("json/spray"))
  .settings(commonSettings)
  .settings(
    name := "logging4s-spray-json",
    libraryDependencies += Dependencies.Json.sprayJson
  )
  .dependsOn(core)

lazy val json4s = (project in file("json/json4s"))
  .settings(commonSettings)
  .settings(
    name := "logging4s-json4s",
    libraryDependencies += Dependencies.Json.json4s
  )
  .dependsOn(core)

lazy val argonaut = (project in file("json/argonaut"))
  .settings(commonSettings)
  .settings(
    name := "logging4s-argonaut",
    libraryDependencies += Dependencies.Json.argonaut
  )
  .dependsOn(core)

lazy val json = (project in file("json"))
  .settings(commonSettings)
  .aggregate(
    circe,
    jsoniter,
    `play-json`,
    `spray-json`,
    json4s,
    argonaut
  )

lazy val examples = (project in file("examples"))
  .settings(commonSettings)
  .settings(
    name := "logging4s-examples",
    libraryDependencies += Dependencies.Cats.catsEffect3
  )
  .dependsOn(circe)
  .dependsOn(`cats-effect-3`)
  .dependsOn(zio)

lazy val logging4s = (project in file("."))
  .settings(commonSettings)
  .settings(
    name := "logging4s"
  )
  .aggregate(
    core,
    cats,
    zio,
    json
  )
