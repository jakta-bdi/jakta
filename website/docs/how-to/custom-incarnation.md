---
sidebar_position: 7
---

# Write your own incarnation

The engine is generic over the belief and goal types, so you can use any Kotlin types for them.
An [incarnation](../explanation/incarnations/index.md) is just that choice, plus a few helper functions that make
triggers and guards pleasant to write. This guide builds a tiny one, where beliefs and goals are *facts* such as
`friend(bob)`.

## 1. Choose the representation

```kotlin
data class Fact(val name: String, val args: List<Any>) {
    override fun toString() = "$name(${args.joinToString()})"
}

fun fact(name: String, vararg args: Any) = Fact(name, args.toList())
```

Beliefs and goals are compared with `equals` (e.g. when a belief is removed), so data classes are a good fit.

## 2. Write trigger helpers

A trigger receives the goal or belief as `this` and returns `null` if the plan is not relevant, or the **context**
passed to the guard and the body. Here, a fact matches by name and arity, and its arguments become the context:

```kotlin
fun Fact.matches(name: String, arity: Int): List<Any>? =
    args.takeIf { this.name == name && args.size == arity }
```

## 3. Write guard helpers

A guard runs in a `GuardScope`, which exposes the agent's `beliefs` and the plan `context`.
It returns the context (possibly refined) if the plan is applicable, `null` otherwise:

```kotlin
fun <Context : Any> GuardScope<Fact, Context>.believes(belief: Fact): Context? =
    context.takeIf { belief in beliefs }
```

## 4. Use it

```kotlin
val greeter = agent<Fact, Fact, Any> {
    embodiedAs { Any() }
    believes {
        +fact("friend", "bob")
    }
    hasInitialGoals {
        !fact("greet", "bob")
        !fact("greet", "eve")
    }
    hasPlanLibrary {
        adding.goal {
            matches("greet", 1)
        } onlyWhen {
            believes(fact("friend", context[0]))
        } triggers {
            agent.print("Hello, ${context[0]}!")
        }
        failing.goal {
            matches("greet", 1)
        } triggers {
            agent.print("I don't greet strangers like ${context[0]}")
            node.terminateNode()
        }
    }
}

fun main(): Unit = runBlocking {
    mas(NodeBuilders.baseNode()) {
        node { withAgents(greeter) }
    }.runLocally()
}
```

Output:

```
Hello, bob!
I don't greet strangers like eve
```

Greeting `eve` has a relevant plan, but its guard fails: no plan is applicable, the goal fails, and the failure plan
handles it.

<details>
<summary>Imports</summary>

```kotlin
import it.unibo.jakta.dsl.agent
import it.unibo.jakta.dsl.mas
import it.unibo.jakta.dsl.mas.runLocally
import it.unibo.jakta.dsl.node.NodeBuilders
import it.unibo.jakta.dsl.plan.triggers
import it.unibo.jakta.plan.GuardScope
import kotlinx.coroutines.runBlocking
```

</details>

## 5. Package it

To reuse the incarnation, put the types and helpers in their own module, as
[`jakta-string-incarnation`](https://github.com/jakta-bdi/jakta/tree/main/jakta-string-incarnation) does:
expose `jakta-api` and `jakta-dsl` to your users, and depend on `jakta-core` for the implementation.

```kotlin
kotlin {
    sourceSets {
        commonMain.dependencies {
            api("it.unibo.jakta:jakta-api:<VERSION>")
            api("it.unibo.jakta:jakta-dsl:<VERSION>")
            implementation("it.unibo.jakta:jakta-core:<VERSION>")
        }
    }
}
```

Applications using the incarnation will still declare `jakta-core` to access the DSL entry points (`mas`, `agent`, ...).

For a richer example, with unification, logic variables in plans and message protocols, look at
[`jakta-prolog-incarnation`](https://github.com/jakta-bdi/jakta/tree/main/jakta-prolog-incarnation).
