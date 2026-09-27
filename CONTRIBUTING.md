# Contributing to logging4s

Thanks for taking the time to contribute! This document covers what you need to build the project,
run its tests, and where to make changes for common kinds of contributions.

## Building

**JDK 25 is required**, and that is what CI uses. It is not merely a preference: `runtime/kyo` expands
kyo's `Frame` macro inside the compiler, and those class files target Java 25, so an older JDK fails to
compile that module.

The project is built with [sbt](https://www.scala-sbt.org/) 2.x. Every module is on the Scala `3.9 LTS`
release — there is no longer a per-module Scala version to keep track of.

```bash
sbt compile             # compile everything
sbt testFull            # run unit tests for all modules
sbt scalafmtCheckAll    # verify formatting
sbt scalafmtAll         # fix formatting
```

Prefer `testFull` over `test`: sbt 2 consults its action cache for `test` and will replay a previous
run's results instead of re-running suites you just changed.

sbt 2 joins its arguments into a single command string, so several tasks must be one quoted argument
separated by `;` — `sbt "scalafmtCheckAll; testFull"`, never `sbt scalafmtCheckAll testFull`.

To run a single module's tests:

```bash
sbt core/test
sbt "logback/testOnly logging4s.logback.Logging4sEncoderSpec"
```

## Project layout

| Path | What lives there |
| --- | --- |
| `core` | `Loggable` / `Logging` / `LoggableValue`, the opaque `ValueKey` / `JsonString` / `PlainString` value types, derivation (`derives Loggable`, `fromEncoders`, the `deriving` field-policy builder), the log interpolator, and `LoggableEncodingConfig` |
| `backend/logback` | logback `LoggingFactory`, plus the opt-in `Logging4sEncoder` (nested JSON, no Jackson) |
| `backend/log4j2` | log4j2 `LoggingFactory`; real nested JSON via `JsonTemplateLayout` + `LoggableMapMessage` |
| `backend/slf4j` | bare `org.slf4j.Logger` `LoggingFactory` (facade — no structured-JSON guarantee) |
| `backend/console` | standalone stdout/stderr `LoggingFactory` — no third-party logging framework underneath |
| `runtime/cats`, `runtime/zio`, `runtime/kyo`, `runtime/rapid` | `given Delay[F]` per effect runtime |
| `json/circe`, `json/jsoniter`, `json/play`, `json/spray`, `json/json4s`, `json/argonaut`, `json/borer`, `json/upickle`, `json/weepickle`, `json/fabric`, `json/zio` | `given JsonEncoder[A]` bridged from each library's own codec typeclass |
| `examples` | runnable end-to-end examples, one per backend/runtime combination |
| `benchmarks` | JMH benchmarks comparing derivation paths and backend encoders (see the README's Benchmarks section) |

## Adding a new backend

A backend needs a `given LoggingFactory`:

```scala
trait LoggingFactory:
  def create[F[*]: Delay](name: String, context: LoggingContext): F[Logging[F]]
```

See `backend/console` for the smallest reference implementation — it wraps a `println` in
`Delay[F].delay { ... }` with no third-party logging framework underneath.

## Adding a new runtime

A runtime needs a `given Delay[F]` — one method, `delay[A](a: => A): F[A]`. See `runtime/cats` for the
reference implementation.

## Adding a new JSON codec bridge

A bridge needs a `given JsonEncoder[A]`:

```scala
trait JsonEncoder[A]:
  def encode(a: A): JsonString
```

built from whatever encoding typeclass the target library already exposes — see `json/circe` or
`json/jsoniter` for the pattern.

## Tests

- Unit tests cover `Loggable` derivation and instances, the syntax/interpolator, and `LoggingContext`
  directly — no backend required.
- Backends are verified against their **real** serialized output, not mocks: `backend/logback` tests
  attach a real `OutputStreamAppender` and parse the captured bytes with Jackson to assert genuine
  nested JSON (not a double-encoded string); `backend/log4j2` builds a real `JsonTemplateLayout`;
  `backend/console` redirects `System.out`/`System.err`. New backends or codec bridges should follow the
  same "assert on the real bytes" pattern rather than mocking.

## Code style

Formatting is enforced by [scalafmt](https://scalameta.org/scalafmt/) (`.scalafmt.conf`) and checked in
CI. Run `sbt scalafmtAll` before opening a PR. No comments in Scala source unless the *why* is genuinely
non-obvious — well-named identifiers should carry the *what*.

## Opening a pull request

- Keep PRs focused — one backend, one codec bridge, one runtime, or one bug fix per PR is easier to
  review than a mix.
- Add or extend tests for anything behavioral you change.
- `sbt "scalafmtCheckAll; testFull"` should pass locally before you push.

## Backend conformance

`core`'s test sources carry a shared `BackendConformance` suite (`logging4s.core.conformance`), and every backend
module depends on them via `core % "compile->compile;test->test"`. A backend implements one method — capture a record
and return its message plus its fields in order — and inherits the contract every backend must satisfy: context
merging, call-site override, key normalization, `source` placement, duplicate-key resolution, and reserved envelope
names.

Add a new backend's conformance spec before anything else; the suite exists because these invariants had quietly
diverged between the four backends, and no per-backend test noticed.

`slf4j` is deliberately outside the suite: it is a facade whose key-value pairs are stringly-typed by definition, so
its field values are not comparable with the other backends'.
