---
title: JaKtA 0.x (deprecated)
sidebar_label: JaKtA 0.x (deprecated)
---

# JaKtA 0.x (deprecated)

:::danger[Deprecated]
JaKtA 0.x is no longer maintained and is **not compatible** with JaKtA 1.x.
New projects should use the current version: start from [Getting Started](./getting-started/index.mdx).
:::

This page is kept as a historical reference. The [publications](/publications) from 2023 and 2024 describe
JaKtA 0.x, and their code follows its syntax, which no longer compiles with JaKtA 1.x.

## What 0.x looked like

A JaKtA 0.x agent was defined inside `mas { }`, with an `environment { }` block exposing actions,
and plans written as `+achieve(...) onlyIf { } then { }`:

```kotlin
fun main() {
    mas {
        agent("myAgent") {
            goals {
                achieve("sayHello")
            }
            plans {
                +achieve("sayHello") then {
                    execute("print"("Hello, World!"))
                }
            }
        }
    }.start()
}
```

The same agent in 1.x is shown in [Writing a simple agent](./getting-started/hello-world.md).

## From 0.x to 1.x

| JaKtA 0.x | JaKtA 1.x |
|---|---|
| JVM only | Kotlin Multiplatform: JVM, JS, native |
| Beliefs and goals always Prolog terms | Pluggable [incarnations](./explanation/incarnations/index.md): Prolog, strings, your own types |
| `mas { ... }.start()` | `mas(NodeBuilders.baseNode()) { node { ... } }.runLocally()` |
| `beliefs { fact { } ; rule { } }` | `believes { +initialBelief { } ; +inferenceRule { } }` |
| `goals { achieve(...) ; test(...) }` | `hasInitialGoals { !initialGoal { } }`; test goals are replaced by guards (`satisfies`) |
| `+achieve(g) onlyIf { } then { }` | `adding.goal { matchingGoal { g } } onlyWhen { } triggers { }` |
| `execute("print"(...))`, `actions { action(...) { } }` | Plain Kotlin in plan bodies, and [skills](./basic-concepts/skills.md) through context parameters |
| `environment { }`, `.fromPercept` beliefs | [Nodes](./explanation/nodes.md): perceptions published on the node, turned into beliefs by each agent |
| Messages sent through environment actions | `MessagingSkill` and [KQML messaging](./explanation/communication.md) |
| Custom concurrency models | A coroutine-based engine: each intention is a coroutine, see the [execution model](./explanation/execution-model.md) |

## Resources

- **Version:** the last 0.x release is **0.15.1**, on Maven Central as `it.unibo.jakta:jakta-dsl`, `jakta-bdi`
  and `jakta-state-machine`. `jakta-dsl` is also the name of a 1.x module, so pin `0.15.1` explicitly:
  `implementation("it.unibo.jakta:jakta-dsl:0.15.1")`.
- **Source code:** tag [`v0.15.1`](https://github.com/jakta-bdi/jakta/tree/v0.15.1). The later 0.15.x tags
  (up to `v0.15.187`) were never published because of issues with the Maven Central releases, and contain no relevant changes.
- **API docs:** [javadoc.io](https://javadoc.io/doc/it.unibo.jakta/jakta-dsl/0.15.1).
- **Old documentation:** the source of the 0.x version of this website is
  [archived on GitHub](https://github.com/jakta-bdi/jakta-bdi.github.io/tree/60a0e9b/docs).
- **Examples:** the [jakta-examples](https://github.com/jakta-bdi/jakta-examples) repository (archived).

To learn what changed and why, read the [JaKtA 1.0 announcement](/blog/jakta-1-0).
