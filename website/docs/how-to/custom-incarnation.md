---
sidebar_position: 7
---

# Write your own incarnation

The engine is generic over the belief and goal types, so you can use any Kotlin types for them.
An [incarnation](../explanation/incarnations/index.md) is just that choice, plus a few helper functions that make
triggers and guards pleasant to write. This guide builds a tiny one where beliefs and goals are plain `Int`s:
an agent that follows the [Collatz sequence](https://en.wikipedia.org/wiki/Collatz_conjecture) of a number, halving
it when it is even and tripling it and adding one when it is odd, until it gets back to a number it has already seen.
The [`custom-incarnation`](https://github.com/jakta-bdi/jakta/tree/main/examples/custom-incarnation) example also
has a second agent with numbers as beliefs and goals, a prime sieve whose belief base is the primes it found so far.
The complete program is the [`custom-incarnation`](https://github.com/jakta-bdi/jakta/tree/main/examples/custom-incarnation) example; run it with `./gradlew :examples:custom-incarnation:run`.

## 1. Choose the representation

A goal is a number to explore, and a belief is a number already explored:

```kotlin
fun collatz(start: Int) = agent<Int, Int, Any> {
    embodiedAs { Any() }
    hasInitialGoals {
        !start
    }
    // ...
}
```

Beliefs and goals are compared with `equals` (e.g. when a belief is removed), so any type with a meaningful
`equals` fits: numbers, strings, data classes.

## 2. Write trigger helpers

A trigger receives the goal or belief as `this` and returns `null` if the plan is not relevant, or the **context**
passed to the guard and the body. Here, a number is relevant to a plan by its parity, and becomes the context itself:

```kotlin
private fun Int.even(): Int? = takeIf { it > 0 && it % 2 == 0 }

private fun Int.odd(): Int? = takeIf { it > 0 && it % 2 == 1 }
```

## 3. Write guard helpers

A guard runs in a `GuardScope`, which exposes the agent's `beliefs` and the plan `context`.
It returns the context (possibly refined) if the plan is applicable, `null` otherwise.
Here, the guards ask whether the number was already explored:

`GuardScope` comes from `it.unibo.jakta.plan.GuardScope`.

```kotlin
private fun GuardScope<Int, Int>.explored(): Int? = context.takeIf { it in beliefs }

private fun GuardScope<Int, Int>.unexplored(): Int? = context.takeIf { it !in beliefs }
```

## 4. Use it in plans

The helpers make triggers and guards read like the representation. An unexplored number is remembered, and the
next number of the sequence becomes a sub-goal:

```kotlin
hasPlanLibrary {
    adding.goal {
        even()
    } onlyWhen {
        unexplored()
    } triggers {
        agent.believe(context)
        agent.print("$context is even: halving it")
        agent.achieve(context / 2)
    }
    adding.goal {
        odd()
    } onlyWhen {
        unexplored()
    } triggers {
        agent.believe(context)
        agent.print("$context is odd: tripling it and adding one")
        agent.achieve(3 * context + 1)
    }
}
```

`achieve` comes from `it.unibo.jakta.agent.achieve`.

## 5. Stop at a number already seen

A number already explored means the sequence loops: for 10, it goes 10, 5, 16, 8, 4, 2, 1 and back to 4.
A plan relevant to any positive number, applicable only when it was explored, stops the agent there:

```kotlin
adding.goal {
    takeIf { it > 0 }
} onlyWhen {
    explored()
} triggers {
    agent.print("Back to $context: the sequence loops, as the conjecture says")
    node.terminateNode()
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
