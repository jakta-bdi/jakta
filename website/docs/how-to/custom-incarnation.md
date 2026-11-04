---
sidebar_position: 7
---

# Write your own incarnation

The engine is generic over the belief and goal types, so you can use any Kotlin types for them.
An [incarnation](../explanation/incarnations/index.md) is just that choice, plus a few helper functions that make
triggers and guards pleasant to write. This guide builds a tiny one, where beliefs are *entries* of a key-value
memory, such as `friends = [bob]`, and goals are *tasks*, an action to perform on a target.
The complete program is the [`custom-incarnation`](https://github.com/jakta-bdi/jakta/tree/main/examples/custom-incarnation) example; run it with `./gradlew :examples:custom-incarnation:run`.

## 1. Choose the representation

```kotlin
private data class Entry(val key: String, val value: Any)

private data class Task(val action: String, val target: String)
```

Beliefs and goals are compared with `equals` (e.g. when a belief is removed), so data classes are a good fit.

## 2. Write trigger helpers

A trigger receives the goal or belief as `this` and returns `null` if the plan is not relevant, or the **context**
passed to the guard and the body. Here, a task matches by its action, and its target becomes the context:

```kotlin
private fun Task.targetOf(action: String): String? = target.takeIf { this.action == action }
```

## 3. Write guard helpers

A guard runs in a `GuardScope`, which exposes the agent's `beliefs` and the plan `context`.
It returns the context (possibly refined) if the plan is applicable, `null` otherwise.
Here, a helper reads the value stored under a key, if it has the expected type:

`GuardScope` comes from `it.unibo.jakta.plan.GuardScope`.

```kotlin
private inline fun <reified V> GuardScope<Entry, *>.valueOf(key: String): V? =
    beliefs.firstOrNull { it.key == key }?.value as? V
```

## 4. Use it in plans

The helpers make triggers and guards read like the representation:

```kotlin
hasPlanLibrary {
    adding.goal {
        targetOf("greet")
    } onlyWhen {
        context.takeIf { it in valueOf<Set<String>>("friends").orEmpty() }
    } triggers {
        agent.print("Hello, $context!")
    }
}
```

## 5. Handle the goals no plan applies to

With `believes { +Entry("friends", setOf("bob")) }`, the goal `Task("greet", "eve")` has a relevant plan whose
guard fails: no plan is applicable and the goal fails. A failure plan handles it:

```kotlin
failing.goal {
    targetOf("greet")
} triggers {
    agent.print("I don't greet strangers like $context")
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
