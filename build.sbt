lazy val commonSettings = Seq(
  scalaVersion := Dependencies.Versions.scala,
  organization := "io.logging4s",
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
    name := "core"
  )

lazy val root = (project in file("."))
  .aggregate(core)
