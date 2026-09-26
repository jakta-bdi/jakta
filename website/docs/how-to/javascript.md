---
sidebar_position: 8
---

# Use JaKtA from Kotlin/JS

JaKtA is a Kotlin Multiplatform library: besides the JVM, it is compiled for JavaScript, both for the browser and
Node.js. The JS artifacts are published on Maven Central (e.g. `it.unibo.jakta:jakta-core-js`) and on npm
(e.g. [`@jakta/jakta-core`](https://www.npmjs.com/package/@jakta/jakta-core)).

JaKtA does not expose a JavaScript-specific API: agents are written in **Kotlin** and compiled to JS.

## Setup

In a Kotlin Multiplatform project with a `js` target, add the dependencies to `commonMain` (or `jsMain`);
Gradle picks the JS variant automatically:

```kotlin
plugins {
    kotlin("multiplatform") version "<KOTLIN_VERSION>"
}

repositories {
    mavenCentral()
}

kotlin {
    js {
        nodejs() // or browser()
        binaries.executable()
    }
    sourceSets {
        commonMain.dependencies {
            implementation("it.unibo.jakta:jakta-core:<VERSION>")
            implementation("it.unibo.jakta:jakta-prolog-incarnation:<VERSION>") // or another incarnation
        }
    }
}
```

`jakta-api`, `jakta-dsl`, `jakta-core`, `jakta-string-incarnation` and `jakta-prolog-incarnation` all support JS;
`alchemist-jakta-incarnation` is JVM-only.

## Write and run the MAS

The DSL is the same as on the JVM. The only difference is how the program starts: `runBlocking` does not exist on
JS, but Kotlin/JS supports a `suspend fun main()`:

```kotlin
val helloAgent = agent<String, String, Any> {
    embodiedAs { Any() }
    hasInitialGoals { !"hello" }
    hasPlanLibrary {
        adding.goal {
            takeIf { it == "hello" }
        } triggers {
            agent.print("Hello from Kotlin/JS!")
            node.terminateNode()
        }
    }
}

suspend fun main() {
    mas(NodeBuilders.baseNode()) {
        node { withAgents(helloAgent) }
    }.runLocally()
}
```

Run it with the tasks Kotlin/JS generates for executables, e.g. `./gradlew jsNodeDevelopmentRun`
(or `./gradlew jsBrowserDevelopmentRun` for a browser target).
