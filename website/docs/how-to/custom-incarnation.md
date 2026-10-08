---
sidebar_position: 7
---

# Write your own incarnation

The engine is generic over the belief and goal types, so you can use any Kotlin types for them.
An [incarnation](../explanation/incarnations/index.md) is just that choice, plus a few helper functions that make
triggers and guards pleasant to write. This guide builds a tiny one, where beliefs and goals are *facts* such as
`friend(bob)`.
The complete program is the [`custom-incarnation`](https://github.com/jakta-bdi/jakta/tree/main/examples/custom-incarnation) example; run it with `./gradlew :examples:custom-incarnation:run`.

## 1. Choose the representation

```kotlin
private data class Fact(val name: String, val args: List<Any>) {
    override fun toString() = "$name(${args.joinToString()})"
}

private fun fact(name: String, vararg args: Any) = Fact(name, args.toList())
```

Beliefs and goals are compared with `equals` (e.g. when a belief is removed), so data classes are a good fit.

## 2. Write trigger helpers

A trigger receives the goal or belief as `this` and returns `null` if the plan is not relevant, or the **context**
passed to the guard and the body. Here, a fact matches by name and arity, and its arguments become the context:

```kotlin
private fun Fact.matches(name: String, arity: Int): List<Any>? = args.takeIf { this.name == name && args.size == arity }
```

## 3. Write guard helpers

A guard runs in a `GuardScope`, which exposes the agent's `beliefs` and the plan `context`.
It returns the context (possibly refined) if the plan is applicable, `null` otherwise:

`GuardScope` comes from `it.unibo.jakta.plan.GuardScope`.

```kotlin
private fun <Context : Any> GuardScope<Fact, Context>.believes(belief: Fact): Context? =
    context.takeIf { belief in beliefs }
```

## 4. Use it in plans

The helpers make triggers and guards read like the representation:

```kotlin
hasPlanLibrary {
    adding.goal {
        matches("greet", 1)
    } onlyWhen {
        believes(fact("friend", context[0]))
    } triggers {
        agent.print("Hello, ${context[0]}!")
    }
}
```

## 5. Handle the goals no plan applies to

With `believes { +fact("friend", "bob") }`, the goal `greet(eve)` has a relevant plan whose guard fails:
no plan is applicable and the goal fails. A failure plan handles it:

```kotlin
failing.goal {
    matches("greet", 1)
} triggers {
    agent.print("I don't greet strangers like ${context[0]}")
}
```

## 6. Package it

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
