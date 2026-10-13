---
sidebar_position: 1
---

# Write guards and inference rules

These snippets live in a Prolog agent (`agent<PrologBelief, PrologGoal, Any> { }`), with plans wrapped in
`prologPlan { }` (see [Prolog plans](../../explanation/incarnations/prolog/prolog-plans.md) for why).

## Derive facts with inference rules

Declare rules next to the facts in `believes { }`; guards and queries prove them on demand:

```kotlin
believes {
    +initialBelief { "parent"("abraham", "isaac") }
    +initialBelief { "parent"("isaac", "jacob") }
    +inferenceRule { "ancestor"(X, Y) impliedBy "parent"(X, Y) }
    +inferenceRule { "ancestor"(X, Y) impliedBy ("parent"(X, Z) and "ancestor"(Z, Y)) }
}
```

Rules are beliefs too: plans and perceptions can add or remove them like facts.

## Guard a plan

`onlyWhen { satisfies { query } }` makes the plan applicable only if `query` can be proven on the beliefs, and
binds its variables for the body:

```kotlin
prologPlan {
    adding.goal {
        matchingGoal { "introduce"(P) }
    } onlyWhen {
        satisfies { "ancestor"(A, P) }
    } triggers {
        agent.print(P, " descends from ", A)
    }
}
```

`satisfies` comes from `it.unibo.jakta.dsl.plan`.

## Choose between plans

Several plans can be relevant for the same goal: the first applicable one in the library runs. Put the specific
cases first and a fallback last:

```kotlin
prologPlan {
    adding.goal { matchingGoal { "greet"(P) } } onlyWhen { satisfies { "friend"(P) } } triggers {
        agent.print("Hi ", P, "!")
    }
}
prologPlan {
    adding.goal { matchingGoal { "greet"(P) } } triggers {
        agent.print("Good morning, ", P)
    }
}
```

## Negate a condition

`not(...)` succeeds when its argument cannot be proven, in rules and guards alike:

```kotlin
+inferenceRule { "clear"(X) impliedBy not("on"(`_`, X)) }
```

## Compute in a guard

Arithmetic, comparisons and the standard library work as in Prolog:

```kotlin
satisfies { "age"(P, A) and (A greaterThanOrEqualsTo 18) and (N `is` (A + 1)) }
```

See [the DSL in a nutshell](../../explanation/incarnations/prolog/2p-kt.md#the-dsl-in-a-nutshell) for the operators.

## Query in a plan body

`testQuery { }` proves a query mid-body and binds its variables; the plan fails if the query does not hold:

```kotlin
triggers {
    testQuery { "parent"(Q, P) }
    agent.print(P, " is the child of ", Q)
}
```

## Use every solution

Guards and `testQuery` only use the first solution. For all of them, solve the query on the belief base:

```kotlin
triggers {
    val children = agent.beliefs.allSolutionsOf(beliefQuery { "parent"(P, C) })
        .filterIsInstance<Solution.Yes>()
        .map { it.substitution.getByName("C").toString() }
    agent.print(P, " has children ", children.joinToString())
}
```

`allSolutionsOf` comes from `it.unibo.jakta.logic`, `Solution` from `it.unibo.tuprolog.solve`.
A `findall/3` query in a guard is an alternative: `satisfies { findall(C, "parent"(P, C), L) }`.

## Read values in Kotlin

`X.value<T>()` converts the term bound to a variable:

```kotlin
val name: String = P.value()
val children: List<String> = L.value()
```

See [reading values](../../explanation/incarnations/prolog/beliefs-and-goals.md#reading-values-in-kotlin) for the
supported conversions.
