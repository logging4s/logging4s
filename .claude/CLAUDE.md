# logging4s — project context

Structured logging for Scala 3. `core` is backend/runtime/JSON-library-agnostic; everything else wires in
through a `given`. Author: Alexandr Oshlakov (shadowsmind.dev@gmail.com), GitHub `logging4s/logging4s`,
Maven Central `org.logging4s`. Written for and used in production at betby.com, and handed off to
onlinetours.ru. **This is an actively developed, ongoing project, not a one-off** — the user explicitly
does not want it treated as finished after a release; keep proposing improvements, prefer durable designs
over quick hacks, and don't wait to be re-asked for the standing workflow below.

Sibling project by the same author/style: `mongo4s` (usually checked out at `../mongo4s` next to this
repo) — effect-agnostic MongoDB client. When asked to make logging4s match "the same as mongo4s" for
CI/docs/community files, read mongo4s's actual files rather than guessing; there's also a
`scala-oss-library-launch` skill distilled from that exact work.

## Architecture / module map

| Path | What it is |
| --- | --- |
| `core` | `Loggable[A]` / `Logging[F[_]]` / `LoggableValue`, opaque `ValueKey`/`JsonString`/`PlainString`, `Level`, `Position`, `LogMessage`, derivation (`derives Loggable`, `fromEncoders`, `deriving` builder), the log interpolator, `LoggableEncodingConfig` |
| `backend/logback` | `LoggingFactory` for logback; opt-in `Logging4sEncoder` (own nested-JSON encoder, no Jackson) alongside the default LogstashEncoder path |
| `backend/log4j2` | `LoggingFactory` for log4j2; real nested JSON via `LoggableMapMessage` + stock `JsonTemplateLayout` |
| `backend/slf4j` | bare `org.slf4j.Logger` facade — no structured-JSON guarantee, user's responsibility |
| `backend/console` | standalone stdout/stderr backend, core + HOCON only, no logging framework underneath; the only backend that filters by logger name itself (`logging4s.console.levels`) |
| `runtime/{cats,zio,kyo,rapid}` | `given Delay[F]` per effect runtime |
| `json/{circe,jsoniter,play,spray,json4s,argonaut,borer,upickle,weepickle,fabric,zio}` | `given JsonEncoder[A]` bridged from each library's own codec typeclass |
| `examples` | runnable end-to-end examples (cats+logback, zio, console, context, no-effect) |
| `benchmarks` | JMH — derivation paths (`derived`/`fromEncoders`/`make`), the 6 backend/encoder configs, and `LevelGateBench` (what a suppressed record costs) |
| `docs/index.html` | the project site, served by **GitHub Pages from `main` / `/docs`**, custom domain `logging4s.org` (CNAME already in `docs/`). Self-contained: all CSS inline, logo copied locally into `docs/` (no CDN, no external image host) |

Scala versions: everything on the LTS release except `runtime/kyo` and `runtime/rapid`, which pin to the
latest Scala 3 release (their own deps need it) — sbt handles the cross-version dependency transparently.

## Design decisions worth knowing (the *why*, not just the *what*)

- **`Logging[F]` has exactly three abstract members** — `emit(level, message, cause, values)(using Position)`,
  `enabled(level)`, `unit`. The twenty per-level overloads are `final` and forward to `emit`. Before 3.0 all
  twenty were abstract and every backend reimplemented them (~350 lines of copy-paste), which is also how
  `withContext(...).info("msg")` came to silently drop the context in three of four backends. A new backend
  implements three methods; a test double implements three methods.
- **`LoggableValue` is a `sealed trait`, not a case class.** `Deferred[A]` holds the value plus its `Loggable`
  and renders in `lazy val`s; `Rendered` is the pre-rendered variant that `LoggableValue(key, plain, json)`
  still builds. The `lazy val`s are load-bearing, not an optimization: logback reads `json` twice (once for
  the logstash marker, once in `Logging4sEncoder` via `LoggableValuesMarker`), so `def` would double-render.
  Keys stay eager, which is what lets merging/dedup/key-normalization run without forcing a render.
- **Level gating lives in two places, deliberately.** The interpolator macro emits
  `if logger.enabled(lvl) then logger.emit(...) else logger.unit`, and backends *also* re-check inside `emit`.
  The macro guard is what makes a suppressed `debug"…"` cost nothing (it also defers the interpolated
  expressions themselves); the in-`emit` check is what keeps semantics right. **Caveat to keep in mind:**
  `enabled` is sampled when the `F[Unit]` is *constructed*, so an action built under a disabled level and run
  later stays suppressed. The reverse direction is safe. This is a consequence of `Delay` having no `flatMap` —
  there is no way to sequence "check then emit" inside `F`.
- **Opaque types `ValueKey`/`JsonString`/`PlainString`** (in `core/core.scala`) are the field types of
  `LoggableValue` and the return types of `Loggable.key`/`json`/`plain` and `JsonEncoder`/`PlainEncoder`.
  Forces callers through `JsonString.quoted(...)` (escapes) instead of hand-rolled string interpolation —
  this is what fixed a real JSON-escaping bug class.
- **`Loggable` has exactly four creation paths, and all four are symmetric**: derive the key from the
  type name, or pass one explicitly as the first argument — `derived[A]`/`derived[A](key)`,
  `fromEncoders[A]`/`fromEncoders[A](key)`, `make[A](json,plain)`/`make[A](key)(json,plain)`,
  `deriving[A]`/`deriving[A](key)`. There used to be a fifth, `make[A: JsonEncoder: PlainEncoder](key)` —
  it was removed because it was structurally identical to `fromEncoders`. `make`'s functions return
  `JsonString`/`PlainString`, not raw `String`. The method **must** stay named `derived` (not `derive`) —
  the `derives Loggable` clause calls `Loggable.derived[A]` by that exact name.
- **`derives`** assembles JSON itself via a Mirror-driven macro (no codec needed). **`fromEncoders`**
  delegates `json` verbatim to an existing `JsonEncoder[A]` (faster, and log JSON matches wire JSON) —
  prefer this whenever a codec already exists. Both can coexist per type.
- **Four combinators on `Loggable`, all `final`**: `rename`, `contramap`, `mapPlain`, `redacted`.
- **`Loggable[Throwable]` alone does not cover `IllegalStateException`** — `Loggable` is invariant, and the
  interpolator summons by the argument's *static* type. So the instance is
  `given LoggableThrowable[E <: Throwable]` living in a `private[core] trait LoggableLowPriority` that the
  companion extends; the low-priority placement is what lets a user's own instance for a specific exception
  win. `LoggableContainer[T, C[*]]` sits there too, so the explicit `List`/`Seq`/`Set`/`Array` instances beat
  it without relying on specificity rules.
- **The log message never contains the values.** Message is static text; values render separately via
  `LogMessage.render` into `message: key -> (value)` and are attached as fields. Do not add Serilog/logstage
  placeholder templating — this was proposed and explicitly rejected. The static message already *is* the
  aggregation key.
- **Duplicate keys resolve by specificity, except within one call.** A call-site value overrides a context
  value, a later `withContext` overrides an earlier one (`LoggableValue.mergeByKey` / `keepLastByKey`). Only
  duplicates passed in a *single* call are suffixed `k`, `k_2` — those are two values the caller deliberately
  passed, so merging them would lose data.
- **`Position` is captured for every call site**, via an inline given macro, threaded as `(using Position)`
  through `emit` and all twenty overloads, and emitted as a `source` field (`includeSourcePosition`, default
  on). It goes to **structured fields only** — never into the message text, except console-plain, which has
  no field channel. Putting it in the message was tried and reverted; tests pin this.
- **`syntax` is a package, not a flat object**: `LoggableSyntax` + `LoggingSyntax` traits, `object all`
  aggregates both. Users must `import logging4s.core.syntax.all.*` — a bare `import syntax.*` on the
  package can't flatten trait extensions, and `export all.*` at the package top level gives a cyclic
  reference (verified, dead end). This is the working idiom (same shape as cats).
- **`LoggableEncodingConfig`** (package `logging4s.core.config`) is one app-wide `given` — key-name style,
  tuple/collection rendering, and since 3.0 `includeSourcePosition`. Threaded two ways: baked into compound
  Loggables at summon time (`jsonTupleAsArray`/`plainTupleStyle`), or captured by the backend at
  `Logging.create` (`keyNameStyle`/`plainValuesStyle`/`includeSourcePosition`) — never as a `using` param on
  `Logging.create` itself. Consequence for tests: a local `given LoggableEncodingConfig` inside a test body
  does NOT reach the logger, because the factory resolved its config when the logger was created — pass the
  config into whatever helper builds the logger instead.
- **`StructuredJson.Builder`** (core) is the shared incremental JSON writer used by both the console
  renderer and `Logging4sEncoder`. It exists because the encoder's fields come from three different
  sources (Scala envelope, a `java.util.Map` MDC, a recursive `java.util.List` marker walk) — any single
  `line(fields, values)` call would still force a merge into one collection first. The `Builder` appends
  each source directly with zero intermediate collections, iterating Java collections natively
  (`.forEach`) instead of `scala.jdk.CollectionConverters`.
- **log4j2 nested JSON without a custom plugin.** `LoggableMapMessage extends StringMapMessage` (which
  implements `MultiformatMessage`); overriding `getFormattedMessage(Array[String])` to splice entry
  values as raw JSON is enough for a *stock* `JsonTemplateLayout`'s `message` resolver to render real
  nested objects. A custom `EventResolver` plugin was considered and rejected — it needs log4j2's Java
  annotation processor to generate `Log4j2Plugins.dat`, which doesn't run under scalac.
- **`Logging4sEncoder` (logback) is additive, not a replacement.** `MarkerHelper` appends a private
  `LoggableValuesMarker` alongside the existing logstash marker, so a stock `LogstashEncoder` and the
  opt-in `Logging4sEncoder` both work off the same call — switching encoders is a one-line XML change,
  never a breaking one.
- **No tinylog backend, and it won't be added** — `tinylog`'s `LogEntry.message` is always a stringified
  `Object` before any `Writer` sees it (unlike log4j2's `Message`, which survives to the appender intact).
  The only structured channel tinylog offers is `ThreadContext` (per-call MDC/thread-local), which the
  user already rejected for log4j2 specifically because fiber-based runtimes (cats-effect/ZIO/kyo) can
  migrate a logical task across OS threads between `put` and the log call, corrupting or leaking fields.
  A backend that *can* silently corrupt structured data under concurrency is worse than no backend.
- **Benchmarks measure throughput (ops/s, `Mode.Throughput`), not `ns/op`** — changed on request, more
  intuitive to read. They compare logging4s's **own** backend/encoder configs against each other, never
  against other logging libraries (no third-party baseline exists in the harness) — don't let README/site
  wording drift into implying "we beat other loggers." Numbers on this Windows dev box are noisy (error
  bars up to ±50%, run-to-run swings up to ~2×) — only the extremes and "own encoder ≥ LogstashEncoder"
  are stable across runs; say so explicitly rather than presenting a precise ranking. `JsonDerivationBench`
  was still on `AverageTime` until 3.0 — both harnesses are on throughput now.
- **`LevelGateBench` is the exception to "numbers here are noisy"** — its error bars are ±0.2–2.5%. It
  measures a suppressed `debug`: 1.5M ops/s with pre-rendered values (the 2.x behaviour), 387M with lazy
  values, 791M with lazy values + the macro guard; the enabled path is unchanged at ~406–409k. The two
  disabled rows are JIT-optimistic in absolute terms (nothing escapes, so escape analysis removes the
  wrapper allocations) — quote the orders of magnitude, not the nanoseconds.

## Standing conventions — apply without being re-asked

- **Never add comments to Scala source** (`//` above defs or trailing) — the user considers them
  non-idiomatic here and strips them out. Names/types/structure carry the meaning; explanations go in
  chat, not in the diff. This applies to main *and* test sources.
- **After non-trivial changes:** `sbt scalafmtAll compile testFull "examples / Compile / compile"` before
  calling it done. Verify empirically — real test runs over claims. `testFull` is an sbt 2 built-in and it is
  the one to use: plain `test` consults sbt 2's action cache and will silently replay old results instead of
  re-running suites you just changed. Add `"benchmarks / Compile / compile"` too — it is not in the root
  aggregate either.
- **Commit messages are one terse line**, imperative, ending in `;` (e.g. "up logback to 1.6.3, add
  compile/test in github actions, improve docs and site;") — no multi-paragraph bodies. Only commit when
  asked; the user commits/pushes/tags on their own schedule (commit implies push implies release, which
  they treat as a deliberate, slower step).
- **GitHub release notes** follow a fixed template — reproduce verbatim, don't invent a new shape:
  ```
  Logging4s <version>
  ================

  What's Changed:
  ---------------

  *   <item>
  *   <last item, ending in a semicolon>;
  ```
  Bullets use `*   ` (asterisk + three spaces); only the final bullet gets the trailing `;`.

## Release process

Publishing to Maven Central uses **sbt 2.0's built-in Sonatype Central Portal support** — there is no
`sbt-sonatype` plugin in `project/plugins.sbt`; `sonaUpload`/`sonaRelease`/`localStaging` are native sbt 2
keys/commands. **Run only `sonaRelease`** — it uploads and publishes in one shot. Running
`sonaUpload; sonaRelease` creates *two* Central Portal deployments per version (an orphan `VALIDATED` one
that `sonaUpload` leaves behind, plus the real `PUBLISHED` one from `sonaRelease`) — harmless, but tidy up
by dropping the orphan if it bothers you. Credentials live at `~/.sbt/sonatype_credentials`. After a
deployment shows `PUBLISHED`, `repo1.maven.org` mirroring and central.sonatype.com search indexing lag
another 30 min – a few hours.

## Windows / sbt toolchain gotchas

Load the `windows-sbt-jvm-toolchain` skill whenever sbt misbehaves on this machine — it's a distilled
runbook (JAVA_HOME vs PATH, `UnsupportedClassVersionError`, the native client hanging silently, the
`AccessDeniedException` file-lock on `packageBin`, `given` syntax errors). Confirmed in this repo
specifically:
- **Two independent sbt servers on the same project directory fight over `target/` files on Windows** and
  produce a persistent (not transient) `AccessDeniedException` on `packageBin` that plain retries don't
  clear — check for and close every other terminal/IDE window that ever ran `sbt` here before assuming
  it's the usual transient lock. `sbt shutdownall` on the stray server fixes it immediately.
- The server registration directory is `%LOCALAPPDATA%\sbt\cache\v2\proc\<pid>.json` (not
  `%LOCALAPPDATA%\sbt\v2\proc`) — a per-project pointer to whichever server is "active" lives at
  `<project>/project/target/active.json`. If that pointer goes stale (points at a server that's already
  been shut down), a bare `sbt` can fail silently instead of falling through to start a fresh one —
  deleting `active.json` (safe, just a cache pointer) can be the actual fix when `--jvm-client` alone
  doesn't help. **This is the most common symptom in practice:** a bare `sbt "…"` that prints the boot
  banner and exits with no output at all, or prints nothing whatsoever.
- **`exportJars := false` (in `commonSettings`) is the actual cure for the `packageBin` lock.** sbt 2 puts
  the *jar* on the inter-project classpath by default, so a test classloader holds `logging4s-core_3-*.jar`
  open and the next `packageBin` can't replace it. With `exportJars := false` the classpath uses `classes/`
  and the whole class of failure disappears. sbt 2.0.8 did **not** fix it on its own — this setting did.

## Repo hygiene / OSS presentation

Set up to match `mongo4s`'s standard (see the `scala-oss-library-launch` skill for the full checklist):
`.github/workflows/ci.yml` (JDK 21 — kept at 21 rather than mongo4s's 25 deliberately, low-risk choice;
runs `scalafmtCheckAll`, `compile`, `testFull`, then `examples`/`benchmarks` compile — the last step matters
because neither is in the root aggregate and they used to break unnoticed), `CONTRIBUTING.md`/
`CODE_OF_CONDUCT.md`/`SECURITY.md` at the repo root, README badges (CI/Maven Central/Scala 3/License) + a
`## Contributing` section, and `docs/index.html` mirroring the README's badges/hero. No
Testcontainers/Docker-dependent tests exist here, so there's no separate integration workflow (unlike mongo4s).

**MiMa** is wired up (`sbt-mima-plugin` 1.1.6 — 1.1.4 has no sbt-2 build; `mimaPreviousArtifacts := Set.empty`
in every skip-publish module, because `publish / skip` is a *task* in sbt 2 and a setting cannot depend on
one). It is **deliberately not a CI step yet**: `previousRelease` is still `2.0.1`, so it reports the 41
intentional 3.0 breaks and would sit red. Flip both together right after 3.0.0 ships.

`scalacOptions` carry `-deprecation`, `-feature`, `-Wunused:all`, `-Wnonunit-statement`. The last one is
filtered out of `Test` (`Test / scalacOptions ~= …`) because ScalaTest's non-final assertions trip it on
nearly every test — and note that in sbt 2 `Test / scalacOptions` inherits it even when you only add it to
`Compile`, so the filter is the working approach.

**A real CSS bug worth remembering if the site hero ever looks broken again:** `docs/index.html`'s hero
`<img>` for the logo must carry `class="logo"`, with the sizing rule scoped to `header.hero .logo` — a
bare `header.hero img` selector also matches the badge `<img>`s (they're nested in the same `<header>`)
and, by CSS specificity, silently overrides `.badges img`'s intended small size. This exact bug shipped
once; mongo4s's `docs/index.html` already had it right (`.logo` class) — when copying patterns from
mongo4s, copy the actual class scoping, not just the pixel values.

## Current status (accurate as of the last session that touched this file)

- **Published on Maven Central:** `v1.0.1`, `v2.0.0`, `v2.0.1` (tags exist for all three; 2.0.0 was the
  big breaking rewrite — opaque types, unified `Loggable` creation, derivation, interpolator, console
  backend, own logback encoder; 2.0.1 followed with a logback bump + the CI/docs/OSS-hygiene work above).
- **3.0.0 is committed but NOT released** (`build.sbt version := "3.0.0"`; fifteen commits ending at
  `954c8f6`). Remaining: release notes, tag `v3.0.0`, `sonaRelease`, then `previousRelease := "3.0.0"` plus
  a `mimaReportBinaryIssues` CI step. MiMa reports **41 breaks** — 31 core, 9 console, 1 log4j2 — all
  intentional and all listed in the README's "From 2.x to 3.0" section, which is the release-notes source.
  What 3.0 contains: five real bug fixes (empty-value crash in logback, invalid JSON for `None`/`Unit`,
  double evaluation in `Delay[ThrowableEither]` and in the interpolator, `ValueKey.combine()` on empty),
  the `emit` refactor, lazy `LoggableValue` + level gating, `Position`/`source`, per-logger console levels,
  key-merge semantics for context, `Loggable` for `Throwable`/`java.time`/`Array`, `mapK`, `mapPlain`, a
  key-normalization cache, MiMa, and a ScalaCheck "output is always parseable JSON" property spec.
- Working tree is otherwise close to clean day-to-day; routine dependency-version bumps in
  `project/Dependencies.scala` are common and usually fine to commit on their own.
- The user works from two machines with separate Claude sessions (this one, and a work PC also used for
  `betby.commons`, a separate closed-source project) — no memory is shared between machines; treat this
  file as the portable, git-tracked source of truth that survives both machines and cleared chats. Claude's
  own private session memory for this project path carries additional day-to-day narrative detail beyond
  what's condensed here, but isn't guaranteed to be available in every environment — don't rely on it
  being there.

## Planned for 4.0 (agreed, deliberately deferred)

Both items came out of reading how zio-logging, izumi logstage, tofu-logging and kyo's `Log` solve the same
problems. They are **decisions, not tasks** — each changes a core abstraction, so start them at the top of a
session, not as a tail-end addition.

- **`LogRenderer`-style abstraction over the target representation (tofu's model).** Today `Loggable` is
  hard-wired to exactly two renderings, `json` and `plain`. tofu instead has one `logFields` plus a swappable
  `LogRenderer`, so the same value can render into JSON, into a flat MDC map, into OpenTelemetry attributes,
  into CBOR. For us a third representation currently means changing `Loggable` and every instance across all
  eleven `json/*` modules. This is the thing to fix *before* anything OTel-shaped is ever attempted.
- **Async writing through a bounded channel (kyo-logging-slf4j's model).** Logging is synchronous inside
  `Delay[F].delay` today. kyo dispatches through a bounded background channel so the calling fiber never
  blocks on I/O. Worth it for high-throughput services, but it brings its own questions — backpressure
  policy, what happens to buffered records at shutdown, and whether it stays runtime-agnostic without
  pulling in fs2/zio-streams (which the console backend deliberately avoided).

Also parked, smaller, no owner yet:

- **Typed context annotations with a `combine` function (zio's `LogAnnotation[A]`).** 3.0 implements the
  common case — last-writer-wins — but not accumulation (append to a list, sum a counter). A general
  `combine` needs the annotation to keep its type, which `LoggableValue` erases; it therefore belongs with
  the `LogRenderer` work, not before it.
- **The doubled space when an interpolation hole sits mid-message** (`info"created $user with $n"` →
  `"created  with  retries"`). Off-idiom usage — holes belong at the end after a separator — and the fix is
  one line of whitespace collapsing. Do **not** "fix" it by introducing message templating; see the design
  note above.
- **log4j2's deprecated `LoggerConfig.createLogger`** (used in one test and one benchmark). The replacement
  `newBuilder()` is an F-bounded Java generic (`<B extends Builder<B>>`) that Scala 3 infers as `Nothing` and
  cannot call — javac gets there via raw types, which Scala has no equivalent for. Left deprecated on purpose;
  don't burn time re-attempting it without a new idea.
