---
title: Prolog Incarnation
sidebar_label: Overview
---

# Prolog Incarnation

`jakta-prolog-incarnation` is the closest thing to Jason in JaKtA:
- beliefs are Prolog **facts and rules**;
- goals are Prolog **terms**;
- triggers and guards work by **unification**, so a plan can match `on(X, Y)` and use `X` and `Y` in its body.

The logic engine comes from [2P-Kt](https://tuprolog.github.io/2p-kt/), a Kotlin Multiplatform Prolog.

```kotlin
agent<PrologBelief, PrologGoal, Any> {
    embodiedAs { Any() }
    believes {
        +initialBelief { "parent"("abraham", "isaac") }
        +initialBelief { "parent"("isaac", "jacob") }
        +inferenceRule { "ancestor"(X, Y) impliedBy "parent"(X, Y) }
        +inferenceRule { "ancestor"(X, Y) impliedBy ("parent"(X, Z) and "ancestor"(Z, Y)) }
    }
    hasInitialGoals {
        !initialGoal { "introduce"("jacob") }
    }
    hasPlanLibrary {
        prologPlan {
            adding.goal {
                matchingGoal { "introduce"(P) }       // binds P = jacob
            } onlyWhen {
                satisfies { "ancestor"(A, P) }        // proves it on the beliefs, binds A
            } triggers {
                agent.print(P, " descends from ", A)  // jacob descends from isaac
            }
        }
    }
}
```

## Why Prolog?

- **Declarative knowledge.** Inference rules derive new facts, like `ancestor/2` above, so you don't have to
  store every consequence as a belief.
- **Pattern-matching plans.** A plan's trigger is a pattern, and one plan handles a whole family of goals.
- **Jason compatibility.** Belief sources (`[source(bob)]`) and KQML messaging work as they do in Jason,
  which makes porting AgentSpeak programs straightforward (see the [Blocks World](../../../getting-started/blocks-world.md) tutorial).

## In this section

- [2P-Kt integration and syntax](./2p-kt.md): what JaKtA takes from 2P-Kt, and a recap of its Kotlin DSL.
- [Prolog plans](./prolog-plans.md): what `prologPlan { }` means and how variables flow through a plan.
- [Beliefs, goals and annotations](./beliefs-and-goals.md): which function to use where, sources, and reading values.
- [Caveats](./caveats.md): the surprises to know about before you start.

For the full list of functions, see the [Prolog Incarnation Reference](../../../reference/prolog-incarnation.md).
