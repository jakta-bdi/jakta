---
sidebar_position: 2
---

# Define your Prolog vocabulary in Kotlin

Writing `"needs"(D, S)` by hand in every plan spreads the same functor, arity and atoms across the code, and a typo
does not raise any error: the guard just never holds (see [caveats](../../explanation/incarnations/prolog/caveats.md#guards-and-queries)).
Define the terms the agents use once, in Kotlin, and let the compiler check every use.
The snippets follow the kitchen of the [`contract-net`](https://github.com/jakta-bdi/jakta/tree/main/examples/contract-net)
example, whose `Beliefs.kt` is a complete vocabulary file.

## Name atoms once

Atoms used as goals or beliefs become constants of type `Atom` (from `it.unibo.tuprolog.core`):

```kotlin
val open = Atom.of("open")

hasInitialGoals { !initialGoal { open } }
hasPlanLibrary {
    prologPlan {
        adding.goal { matchingGoal { open } } triggers {
            // ...
        }
    }
}
```

## Write a function per predicate

A function that builds the `Struct` fixes the functor and the arity. Taking `Term` arguments, the same function
works with variables in patterns and guards, and with values in beliefs and goals:

```kotlin
fun needs(dish: Term, station: Term): Struct = Struct.of("needs", dish, station)
fun cook(dish: Term): Struct = Struct.of("cook", dish)

prologPlan {
    adding.goal {
        matchingGoal { cook(D) }
    } onlyWhen {
        satisfies { needs(D, S) }
    } triggers {
        agent.print("Cooking ", D, " at ", S)
    }
}
```

The functions build plain 2P-Kt terms, so they also work outside plans, where there is no DSL scope:
`Fact.of(needs(...))` is a belief that a [perception handler](./perceptions.md) can add.

## Map Kotlin types to atoms

Enums map naturally to atoms. Give the predicate an overload with Kotlin types for the cases where the values are
known:

```kotlin
enum class Dish { STEAK, SALAD }
enum class Station { GRILL, CHOPPING_BOARD }

fun Enum<*>.toAtom(): Atom = Atom.of(name.lowercase())

fun needs(dish: Dish, station: Station): Struct = needs(dish.toAtom(), station.toAtom())

believes {
    +initialBelief { needs(Dish.STEAK, Station.GRILL) }
    +initialBelief { needs(Dish.SALAD, Station.CHOPPING_BOARD) }
}
```

Lowercase names keep the atoms unquoted, and the variables of a pattern can be mixed with typed values:
`satisfies { needs(D, Station.GRILL.toAtom()) }`.

## Read terms back as Kotlin values

In a plan body, read a binding with [`value()`](../../explanation/incarnations/prolog/2p-kt.md#reading-values-in-kotlin)
and turn it back into the Kotlin type with a function next to the vocabulary:

```kotlin
fun dishNamed(name: String): Dish = enumValueOf(name.uppercase())

// in a body
val dish = dishNamed(D.value())
```

## Keep the vocabulary in one file

Put the atoms, the predicate functions and the conversions in a single file, shared by the agents, the perception
handlers and any Kotlin code that reads the beliefs. It becomes the one place where Kotlin values become terms and
back, so a predicate cannot be spelled two ways, and renaming one is a refactoring.

:::warning
The functions fix names and arities, not types: `needs(Dish.STEAK.toAtom(), Dish.SALAD.toAtom())` compiles.
Whether a term means what the plans expect is still decided at run time, by unification.
:::

:::info
Functions can also build whole patterns and rules: the [Tic-tac-toe](../../showcase/tictactoe.md) showcase generates
the patterns its agents look for, for any board size.
:::
