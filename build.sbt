lazy val commonSettings = Seq(
  scalaVersion := Dependencies.Versions.scala,
  organization := "com.github.logging4s",
  libraryDependencies ++= Dependencies.Testing.core,
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
    name := "core",
    libraryDependencies ++= Dependencies.Logging.all
  )

lazy val cats = (project in file("cats"))
  .settings(commonSettings)
  .settings(
    name := "cats",
    libraryDependencies ++= Dependencies.Cats.all :+ Dependencies.Testing.catsEffects
  )
  .dependsOn(core)

lazy val circe = (project in file("json/circe"))
  .settings(commonSettings)
  .settings(
    name := "circe",
    libraryDependencies ++= Dependencies.Json.circe
  )
  .dependsOn(core)

lazy val jsoniter = (project in file("json/jsoniter"))
  .settings(commonSettings)
  .settings(
    name := "jsoniter",
    libraryDependencies ++= Dependencies.Json.jsoniter
  )
  .dependsOn(core)

lazy val playJson = (project in file("json/play"))
  .settings(commonSettings)
  .settings(
    name := "play",
    libraryDependencies += Dependencies.Json.playJson
  )
  .dependsOn(core)

lazy val sprayJson = (project in file("json/spray"))
  .settings(commonSettings)
  .settings(
    name := "spray",
    libraryDependencies += Dependencies.Json.sprayJson
  )
  .dependsOn(core)

lazy val json4s = (project in file("json/json4s"))
  .settings(commonSettings)
  .settings(
    name := "json4s",
    libraryDependencies += Dependencies.Json.json4s
  )
  .dependsOn(core)

lazy val json = (project in file("json"))
  .settings(commonSettings)
  .aggregate(
    circe,
    jsoniter,
    playJson,
    sprayJson,
    json4s
  )

lazy val root = (project in file("."))
  .aggregate(
    core,
    cats,
    json
  )
