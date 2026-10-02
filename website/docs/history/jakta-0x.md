---
sidebar_label: JaKtA 0.x (deprecated)
sidebar_position: 2
slug: /jakta-0x
---

# JaKtA 0.x (deprecated)

:::danger[Deprecated]
JaKtA 0.x is no longer maintained and is **not compatible** with JaKtA 1.x.
New projects should use the current version: start from [Getting Started](../tutorials/index.mdx).
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

The same agent in 1.x is shown in [Writing a simple agent](../tutorials/hello-world.md).

## Architecture

JaKtA 0.x was made of three layered modules:

1. the **DSL** (`jakta-dsl`), defining the syntax of the language;
2. the **BDI interpreter** (`jakta-bdi`), running agents and environments regardless of the syntax used to define them;
3. the **concurrency management** module (`jakta-state-machine`), handling runtime, concurrency and scheduling.

Keeping the DSL apart from the interpreter meant that other syntaxes, such as a Jason parser or a Scala DSL,
could in principle run on the same engine. JaKtA 1.x keeps this separation between `jakta-dsl` and `jakta-core`.

## Beliefs

Beliefs were always [2P-Kt](https://tuprolog.github.io/2p-kt/) Prolog facts and rules, written with its Kotlin DSL,
and the agent could reason over them with Prolog unification and resolution:

```kotlin
agent("moon walker") {
    beliefs {
        fact { "path"("location1", "location2") }
        fact { "path"("location2", "location3") }
        rule { "reachable"(X, Y) impliedBy "path"(X, Y) }
        rule { "reachable"(X, Z) impliedBy "path"(X, Y) and "reachable"(Y, Z) }
    }
}
```

Each belief carried its **source**: `self` for the agent's own knowledge, `percept` for what it perceived in the
environment, or the name of the agent that sent it in a message. Plans selected beliefs by source with
`.fromSelf` and `.fromPercept`. In plan bodies, `+belief` and `-belief` added and removed beliefs, and
`update(belief)` replaced one.

## Goals

Goals were either **achievement** goals, satisfied by running a plan, or **test** goals, answered first from the
belief base. Initial goals were declared in the `goals { }` block:

```kotlin
agent("Robot") {
    goals {
        test("batteryLevel")
        achieve("chargeBattery")
    }
}
```

In plan bodies, `achieve(g)` and `test(g)` pursued a sub-goal within the same intention, and `spawn(g)` started
a new intention for it.

## Plans

Plans followed the Jason model: a **triggering event** decides whether the plan is relevant, an optional
**context** (`onlyIf`) checked against the belief base by logic resolution decides whether it is applicable,
and a **body** (`then`) lists the operations to run. A prefix `+` meant the addition of a goal or belief, and
`-` its failure or deletion:

```kotlin
plans {
    +achieve("goal"(X)) onlyIf { "guard"(X) } then {
        achieve("goal"(X))
        test("goal"(Y))
        spawn("goal"(Z))
        +"belief"(A)
        update("belief"(C))
        execute("action"(X, Y, Z))
    }
    -achieve("goal"(X)) then { /* on failure */ }
    +test("goal"(Y)) onlyIf { "guard"(Y) } then { /* ... */ }
    +"temperature"(X).fromPercept onlyIf { X greaterThan 30 } then { /* ... */ }
}
```

## Actions

Plan bodies could not change agent or environment state directly: they invoked actions, which declared their
changes as **side effects** through a restricted API.

- **Internal actions** changed the agent: `addBelief`, `removeBelief`, `addIntention`, `removeIntention`,
  `addEvent`, `removeEvent`, `addPlan`, `removePlan`, `stopAgent`, `sleepAgent`, `pauseAgent`.
- **External actions** changed the environment: `addAgent`, `removeAgent`, `addData`, `removeData`,
  `updateData`, `sendMessage`, `broadcastMessage`.

Actions were defined inline in an `actions { }` block, or as reusable objects:

```kotlin
object Print : AbstractInternalAction("print", 2) {
    override fun action(request: InternalRequest) {
        println("[" + request.agent.name + "] " + request.arguments.joinToString(" "))
    }
}

agent("name") {
    actions { action(Print) }
    plans {
        +achieve("greet") then { execute("print"("Hello World!")) }
    }
}
```

The standard library offered four internal actions: `print`, `fail` (fail the current plan and select another one),
`stop` (stop the agent) and `pause(ms)`. There were no standard external actions.

In 1.x, plan bodies are plain Kotlin and capabilities are [skills](../explanation/basic-concepts/skills.md).

## Environment

All agents of a MAS shared one environment, which agents could read only as perceptions and change only through
external actions. The default environment had no actions, and could be extended with an `environment { actions { } }`
block. Custom environments extended `EnvironmentImpl` and turned their state into beliefs with a `percept()` method:

```kotlin
class TemperatureEnvironment : EnvironmentImpl(externalActions = emptyMap(), perception = Perception.empty()) {
    override fun percept(): BeliefBase =
        BeliefBase.of(Belief.fromPerceptSource(Struct.of("temperature", Numeric.of(15))))
    // copy(...) omitted
}

mas {
    environment(TemperatureEnvironment())
    agent("reactiveAgent") { /* ... */ }
}.start()
```

Agents could also coordinate by **stigmergy**, leaving data in the environment for others to perceive.
In 1.x, the environment is replaced by [nodes](../explanation/nodes.md).

## Communication

Messages were sent by an external action calling `sendMessage`, with one of two performatives:
`Tell`, which the receiver got as a belief annotated with the sender as source, and `Achieve`, which it got as a goal
to pursue, without knowing who sent it:

```kotlin
mas {
    environment {
        actions {
            action("send", 2) {
                val receiver: String = argument<Atom>(0).value
                val payload: Struct = argument(1)
                sendMessage(receiver, Message(this.sender, Tell, payload))
            }
        }
    }
    agent("sender") {
        goals { achieve("sendMessage") }
        plans {
            +achieve("sendMessage") then { execute("send"("receiver", "handleMessage"("Hello!"))) }
        }
    }
    agent("receiver") {
        plans {
            +"handleMessage"("source"(S), X) then { execute("print"("Received message: ", X)) }
        }
    }
}.start()
```

In 1.x, the Prolog incarnation adds `untell`, `unachieve` and `askOne`, and annotates achieve goals with their sender too: see [KQML messaging](../explanation/incarnations/prolog/kqml.md).

## From 0.x to 1.x

| JaKtA 0.x | JaKtA 1.x |
|---|---|
| JVM only | Kotlin Multiplatform: JVM, JS, native |
| Beliefs and goals always Prolog terms | Pluggable [incarnations](../explanation/incarnations/index.md): Prolog, strings, your own types |
| `mas { ... }.start()` | `mas(NodeBuilders.baseNode()) { node { ... } }.runLocally()` |
| `beliefs { fact { } ; rule { } }` | `believes { +initialBelief { } ; +inferenceRule { } }` |
| `goals { achieve(...) ; test(...) }` | `hasInitialGoals { !initialGoal { } }`; test goals are replaced by guards (`satisfies`) |
| `+achieve(g) onlyIf { } then { }` | `adding.goal { matchingGoal { g } } onlyWhen { } triggers { }` |
| `execute("print"(...))`, `actions { action(...) { } }` | Plain Kotlin in plan bodies, and [skills](../explanation/basic-concepts/skills.md) through context parameters |
| `environment { }`, `.fromPercept` beliefs | [Nodes](../explanation/nodes.md): perceptions published on the node, turned into beliefs by each agent |
| Messages sent through environment actions | `MessagingSkill` and [KQML messaging](../explanation/incarnations/prolog/kqml.md) |
| Custom concurrency models | A coroutine-based engine: each intention is a coroutine, see the [execution model](../explanation/execution-model.md) |

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
