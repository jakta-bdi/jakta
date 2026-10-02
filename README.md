<!-- markdownlint-disable-next-line first-line-heading -->
| <img src="website/static/img/logo.svg" alt="JaKtA logo" width="100"> | <h1>JaKtA</h1> |
|:-:|:-:|

[![DOI](https://zenodo.org/badge/DOI/10.5281/zenodo.10945579.svg)](https://doi.org/10.5281/zenodo.10945579)

JaKtA is a BDI (Belief-Desire-Intention) agent-oriented programming framework, built as an internal DSL
for [Kotlin Multiplatform](https://kotlinlang.org/docs/multiplatform.html).
Agents, their beliefs, goals and plans are written side by side with ordinary Kotlin code.

- **Generic knowledge representation**: beliefs and goals can be Prolog terms, plain strings, or your own Kotlin types.
- **Generic plan matching**: how triggers and guards match is defined by the chosen
  [incarnation](https://jakta-bdi.github.io/docs/explanation/incarnations), from Prolog unification to any Kotlin function.
- **Coroutine-based engine**: each intention is a coroutine, and plan bodies are plain `suspend` Kotlin code.
- **Multiplatform**: runs on the JVM, in JavaScript (browser and Node.js) and natively.

[![CI/CD](https://github.com/jakta-bdi/jakta/actions/workflows/dispatcher.yml/badge.svg)](https://github.com/jakta-bdi/jakta/actions/workflows/dispatcher.yml)
[![codecov](https://codecov.io/gh/jakta-bdi/jakta/branch/main/graph/badge.svg?token=ACIA7DKGT1)](https://codecov.io/gh/jakta-bdi/jakta)
[![GitHub issues](https://img.shields.io/github/issues-raw/jakta-bdi/jakta?style=plastic)](https://github.com/jakta-bdi/jakta/issues)
[![GitHub closed issues](https://img.shields.io/github/issues-closed/jakta-bdi/jakta)](https://github.com/jakta-bdi/jakta/issues?q=is%3Aissue+is%3Aclosed)
[![GitHub pull requests](https://img.shields.io/github/issues-pr-raw/jakta-bdi/jakta?style=plastic)](https://github.com/jakta-bdi/jakta/pulls)
[![GitHub](https://img.shields.io/github/license/jakta-bdi/jakta?style=plastic)](/LICENSE)
[![GitHub release (latest SemVer including pre-releases)](https://img.shields.io/github/v/release/jakta-bdi/jakta?include_prereleases&style=plastic)](https://github.com/jakta-bdi/jakta/releases)
[![GitHub release date](https://img.shields.io/github/release-date/jakta-bdi/jakta)](https://github.com/jakta-bdi/jakta/releases)
[![GitHub contributors](https://img.shields.io/github/contributors/jakta-bdi/jakta)](https://github.com/jakta-bdi/jakta/graphs/contributors)
[![GitHub last commit](https://img.shields.io/github/last-commit/jakta-bdi/jakta)](https://github.com/jakta-bdi/jakta/commits/main)
[![Codacy Badge](https://app.codacy.com/project/badge/Grade/e19ca8dfa53649eba21b6d01fb67c9b6)](https://app.codacy.com/gh/jakta-bdi/jakta/dashboard?utm_source=gh&utm_medium=referral&utm_content=&utm_campaign=Badge_grade)
![GitHub Code Size](https://img.shields.io/github/languages/code-size/jakta-bdi/jakta)
![GitHub Repo Size](https://img.shields.io/github/repo-size/jakta-bdi/jakta)
![GitHub Release Downloads](https://img.shields.io/github/downloads/jakta-bdi/jakta/total)
![GitHub Languages](https://img.shields.io/github/languages/count/jakta-bdi/jakta)

## Import JaKtA in your project

JaKtA is published on [Maven Central](https://central.sonatype.com/namespace/it.unibo.jakta) (and on npm under `@jakta`).
Add `jakta-core` and an [incarnation](https://jakta-bdi.github.io/docs/explanation/incarnations), e.g. the Prolog one:

```kotlin
dependencies {
    implementation("it.unibo.jakta:jakta-core:<VERSION>")
    implementation("it.unibo.jakta:jakta-prolog-incarnation:<VERSION>")
}
```

| Module | Description |
|---|---|
| [`jakta-api`](jakta-api) | Representation-agnostic contracts (agents, events, plans, nodes) |
| [`jakta-dsl`](jakta-dsl) | DSL builder interfaces |
| [`jakta-core`](jakta-core) | Engine implementation and DSL entry points (`mas`, `node`, `agent`) |
| [`jakta-prolog-incarnation`](jakta-prolog-incarnation) | Beliefs and goals as [2P-Kt](https://tuprolog.github.io/2p-kt/) Prolog terms, KQML messaging |
| [`jakta-string-incarnation`](jakta-string-incarnation) | Beliefs and goals as plain strings |
| [`alchemist-jakta-incarnation`](alchemist-jakta-incarnation) | Run JaKtA agents in [Alchemist](https://alchemistsimulator.github.io/) simulations |

## Hello world

```kotlin
val helloWorldAgent = agent<String, String, Any> {
    embodiedAs { Any() }
    hasInitialGoals {
        !"sayHello"
    }
    hasPlanLibrary {
        adding.goal {
            takeIf { it == "sayHello" }
        } triggers {
            agent.print("Hello, world!")
            node.terminateNode()
        }
    }
}

fun main(): Unit = runBlocking {
    mas(NodeBuilders.baseNode()) {
        node { withAgents(helloWorldAgent) }
    }.runLocally()
}
```

## Documentation

- User documentation: <https://jakta-bdi.github.io/>
- API reference of the latest release: <https://jakta-bdi.github.io/api/> (older versions on [javadoc.io](https://javadoc.io/doc/it.unibo.jakta))

## Usage examples

Runnable examples live in [`examples`](examples):

```bash
./gradlew :examples:hello-world:run
./gradlew :examples:blocksworld:run
```

## Citation

If you want to cite this work, you can follow [citation instructions](https://github.com/jakta-bdi/jakta/blob/main/CITATION).

## Contributors

<a href="https://github.com/jakta-bdi/jakta/graphs/contributors">
  <img src="https://contributors-img.web.app/image?repo=jakta-bdi/jakta" alt="JaKtA contributors" />
</a>
