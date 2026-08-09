<img width="200" height="200" align="right" src="logos/logging4s_icon.png" alt="Logging4s logo"/>

# Logging4s

**Structured logging for Scala 3 — for any backend, any effect, any JSON library.**

[![Maven Central](https://img.shields.io/maven-central/v/org.logging4s/logging4s-core_3?color=blue)](https://central.sonatype.com/search?q=logging4s)
[![Scala 3](https://img.shields.io/badge/Scala-3-red)](https://www.scala-lang.org/)
[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://www.apache.org/licenses/LICENSE-2.0)

`Logging4s` turns your domain objects into **real, structured JSON logs** with almost no boilerplate. The `core` is a
tiny, backend-agnostic set of type classes; everything else — the logging backend, the effect runtime, the JSON
codec — is a small module you pick and mix. Bring what you already use, wire it with one import, and log typed values
instead of stringly-typed messages.

```scala
final case class User(id: Int, name: String) derives Loggable

logging.info("user created", User(1, "John"))
// {"message":"user created: user -> (id -> (1), name -> (John))",
//  "user":{"id":1,"name":"John"}}   ← a real nested object, not an escaped string
```

---

## Table of contents

* [Overview](#overview)
* [Getting started](#getting-started)
* [Defining `Loggable`](#defining-loggable)
* [Logging](#logging)
* [Configuration](#configuration)
* [Modules](#modules)
* [Adopters](#adopters)
* [Motivation](#motivation)

---

## Overview

Why Logging4s:

* **Structured-first.** On the `logback` backend, values are attached as genuine nested JSON (via logstash raw-JSON
  markers) — objects and arrays, not double-encoded strings.
* **Bring your own everything.** 3 backends × 4 effect runtimes × 11 JSON libraries, mix-and-match through imports —
  no lock-in, reuse the encoders and effects your app already has.
* **Zero-boilerplate or reuse your codecs.** `derives Loggable` renders any `case class`/`enum` out of the box, or
  `Loggable.fromEncoders` reuses your existing circe/jsoniter/… codec so the log JSON matches your wire JSON exactly.
* **Field-level control, no annotations.** Hide, mask, rename or unembed fields with **type-safe selectors**
  (`.hide(_.password)`) — works even on classes you don't own.
* **One app-wide output config.** Tune key naming and JSON/plain shapes once (`LoggableEncodingConfig`).
* **Scala 3 native.** `given`-based, opaque-typed, macro-derived — small surface, no runtime reflection.

## Getting started

Pick a **backend**, an **effect runtime**, and (optionally) a **JSON library**. For a plain `cats-effect` app on
`logback`:

```scala
libraryDependencies ++= Seq(
  "org.logging4s" %% "logging4s-cats"    % "2.0.0",
  "org.logging4s" %% "logging4s-logback" % "2.0.0"
)
```

```scala
import cats.effect.{IO, IOApp}

import logging4s.core.{Loggable, Logging}
import logging4s.cats.CatsInstances.given        // Delay[IO] + cats.Show/data bridges
import logging4s.logback.LogbackInstances.given  // the backend's LoggingFactory

final case class User(id: Int, name: String) derives Loggable

object Main extends IOApp.Simple:
  def run: IO[Unit] =
    for
      logging <- Logging.create[IO]("Main")
      _       <- logging.info("user created", User(1, "John"))
    yield ()
```

That's it — `derives Loggable` gives you the JSON and the plain rendering for free, `logback.xml` controls the output
format as usual. See [`./examples`](examples) for ZIO, Kyo and more.

## Defining `Loggable`

`Loggable[A]` is the single type class the whole library revolves around — it knows a value's **key**, its **JSON**
form and its **plain** (human) form. There are four ways to get one, from most to least automatic:

**1. Derive it structurally** — no JSON library required, we render the JSON ourselves:

```scala
final case class Point(x: Int, y: Int) derives Loggable
enum Color derives Loggable:
  case Red, Green, Blue
```

**2. Reuse an existing JSON codec** with `Loggable.fromEncoders` — the JSON comes **verbatim** from your codec (correct
escaping, your field names, untouched by our config), the plain form falls back to `cats.Show`/`zio.Debug`/… or a
structural rendering:

```scala
import io.circe.Encoder
import io.circe.generic.semiauto.deriveEncoder
import logging4s.core.Loggable
import logging4s.json.circe.CirceInstances.given

final case class User(id: Int, name: String)
object User:
  given Encoder[User]  = deriveEncoder
  given Loggable[User] = Loggable.fromEncoders   // JSON = circe, key = "user"
```

**3. Build one by hand** with an explicit key:

```scala
given Loggable[Money] = Loggable.make("money")(m => m.cents.toString, m => s"$$${m.cents / 100.0}")
```

**4. Adapt an existing instance** with `contramap` / `rename` / `redacted`:

```scala
given Loggable[UserId] = Loggable[Int].contramap(_.value, "user_id")
val secret             = Loggable[String].redacted()   // always logs "***"
```

### Hiding, masking and renaming fields

For derived case classes you often want per-field control — do it with a builder and **type-safe selectors** (no
annotations, works on third-party types):

```scala
import logging4s.core.Loggable
import logging4s.core.deriving.MaskMode

final case class Account(id: Int, email: String, password: String, address: Address)
object Account:
  given Loggable[Account] =
    Loggable.deriving[Account]
      .hide(_.password)                    // drop it entirely
      .mask(_.email)(MaskMode.KeepLast(4)) // ****...@x.com
      .rename(_.id, "account_id")          // custom key
      .unembed(_.address)                  // splice its fields into the parent object
      .derived
```

## Logging

Obtain a `Logging[F]` from `Logging.create` (or `createTry` / `createEither` / `createUnsafe` for non-effect code), then
log a message with any number of typed values:

```scala
for
  logging <- Logging.create[IO]("OrderService")
  _       <- logging.info("service started")
  _       <- logging.info("order placed", user, order)          // many values
  _       <- logging.error("payment failed", throwable, order)  // + a cause
  scoped   = logging.withContextValues(requestId.asLogValue("request_id"))
  _       <- scoped.info("handled")                             // context on every line
yield ()
```

Prefer an interpolator? Import the log syntax and bring a `given Logging[F]` into scope — keys are taken from the
variable names:

```scala
import logging4s.core.syntax.logging.*

info"order placed: $order"     // → logging.info("order placed", order.asLogValue("order"))
```

Value-building helpers (`asLogValue`, `mapPlain`, `withKey`) live in `logging4s.core.syntax.loggable.*`, and
`logging4s.core.syntax.all.*` brings both.

## Configuration

How values are rendered is controlled by a single `LoggableEncodingConfig` (package `logging4s.core.config`). It works
out of the box; to change the house style, put **one** `given` at the top of your application:

```scala
import logging4s.core.config.*

given LoggableEncodingConfig =
  LoggableEncodingConfig(
    keyNameStyle     = KeyNameStyle.CamelCase,
    plainValuesStyle = PlainValuesStyle.Logfmt
  )
```

| Knob | Default | Options → effect |
| --- | --- | --- |
| `jsonTupleAsArray` | `true` | `true` → `[1,"a"]`; `false` → `{"int":1,"string":"a"}` (object, e.g. for Elasticsearch) |
| `keyNameStyle` | `SnakeCase` | `AsIs` / `SnakeCase` / `KebabCase` / `CamelCase` / `PascalCase` — applied to **every** key |
| `plainTupleStyle` | `AsScala` | `(1, a)` / `[1, a]` / `1, a` / `{1, a}` |
| `plainValuesStyle` | `Arrow` | `Arrow` `k -> (v)`, `Logfmt` `k=v`, `Colon` `k: v`, `CurlyMap` `{k=v}`, … |

> `keyNameStyle` and `plainValuesStyle` are applied by the backend when it aggregates the log line; `jsonTupleAsArray`
> and `plainTupleStyle` are baked into compound `Loggable`s when they are summoned. One top-level `given` covers both.

**Default keys.** Built-in scalars key by their type name (`Loggable[Int]` → `int`, `Loggable[String]` → `string`);
date/time types use `time`, `FiniteDuration` uses `time_ms`; collections append a plural suffix
(`List[Int]` → `ints`); a derived case class keys by its decapitalized name (`User` → `user`).

## Modules

All modules are published for **Scala 3** under the `org.logging4s` group:

```scala
"org.logging4s" %% "logging4s-<module>" % "2.0.0"
```

**Core**

| Module | Description |
| --- | --- |
| `logging4s-core` | Type classes (`Loggable`, `JsonEncoder`, `PlainEncoder`) and the `Logging` interface. Backend-agnostic. |

**Backends** — pick one; import `logging4s.<backend>.<Backend>Instances.given`.

| Module | Notes |
| --- | --- |
| `logging4s-logback` | `slf4j` + `logback` + `logstash-encoder`. **Real nested JSON** via raw-JSON markers. |
| `logging4s-log4j2` | Log4j2's own API; values passed as a `MapMessage` argument (no MDC). |
| `logging4s-slf4j` | Bare `slf4j-api` 2.x fluent `addKeyValue`. Bring your own binding. |

**Effect runtimes** — pick one; import `logging4s.<runtime>.<Runtime>Instances.given`.

| Module | Effect | Plain rendering |
| --- | --- | --- |
| `logging4s-cats` | `cats-effect 3` `Sync` | `cats.Show` |
| `logging4s-zio` | `zio.Task` | `zio.prelude.Debug` |
| `logging4s-kyo` | `kyo.IO` | `kyo.Render` |
| `logging4s-rapid` | `rapid.Task` | built-in |

**JSON libraries** — optional; import `logging4s.json.<lib>.<Lib>Instances.given` to bridge a codec into `fromEncoders`/`make`.

| Module | Codec | | Module | Codec |
| --- | --- | --- | --- | --- |
| `logging4s-circe` | `io.circe.Encoder` | | `logging4s-upickle` | `upickle Writer` |
| `logging4s-jsoniter` | `jsoniter JsonValueCodec` | | `logging4s-weepickle` | `weepickle From` |
| `logging4s-play-json` | `play-json Writes` | | `logging4s-zio-json` | `zio-json JsonEncoder` |
| `logging4s-spray-json` | `spray-json JsonWriter` | | `logging4s-argonaut` | `argonaut EncodeJson` |
| `logging4s-json4s` | `json4s Formats` | | `logging4s-borer` | `borer Encoder` |
| `logging4s-fabric` | `fabric Json` | | | |

Every integration module exposes its `given`s as a named trait + companion object, so you can also mix several into a
single import for your app:

```scala
object instances extends LogbackInstances with CatsInstances with CirceInstances
```

## Adopters

Logging4s is used in production at:

<a href="https://betby.com">
  <picture>
    <source media="(prefers-color-scheme: dark)" srcset="logos/betby.svg"/>
    <img src="logos/betby-dark.svg" alt="Betby" height="56"/>
  </picture>
</a>

Using Logging4s? Open a PR to add your logo here.

## Motivation

A structured-logging library that targets **Scala 3** and plays nicely with the whole ecosystem of effect systems and
JSON libraries, rather than a single opinionated stack. Compared to the alternatives, Logging4s leans on the encoders
and effects you already have, sits on top of a real logging backend you already operate, and keeps its own surface
small.

## License

Apache 2.0 — see [LICENSE](LICENSE).
