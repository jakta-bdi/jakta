---
sidebar_label: 2P-Kt integration
sidebar_position: 1
---

# 2P-Kt Integration and Syntax

[2P-Kt](https://tuprolog.github.io/2p-kt/) (tuProlog in Kotlin) is a multiplatform Prolog implementation.
The Prolog incarnation relies on it for:

| JaKtA concept | 2P-Kt counterpart |
|---|---|
| `PrologBelief` | `Rule` (a fact is a rule whose body is `true`) |
| `PrologGoal` | `Struct` |
| Writing terms in Kotlin | 2P-Kt's Prolog DSL (`"f"(X)`, `impliedBy`, `and`, ...) |
| Triggers | Unification, with a JaKtA-specific unifier that also matches annotations |
| Guards (`satisfies`) and `testQuery` | A 2P-Kt `solve-prolog` solver, run with the belief base as its theory |

The incarnation exposes the 2P-Kt modules it uses (`core`, `solve-prolog`, `parser`, `dsl-*`, `serialize`)
as `api` dependencies, so the whole 2P-Kt API is available in your code without extra dependencies.

## The DSL in a nutshell

Inside `initialBelief { }`, `inferenceRule { }`, `initialGoal { }` and `prologPlan { }` you are in a 2P-Kt
*logic programming scope*, where Kotlin expressions build Prolog terms:

| Kotlin | Prolog |
|---|---|
| `"parent"("abraham", "isaac")` | `parent(abraham, isaac)` |
| `"age"("jacob", 20)`, `"pi"(3.14)`, `"ok"(true)` | `age(jacob, 20)`, `pi(3.14)`, `ok(true)` |
| `X`, `Y`, `Z`, ... <br/> `` `_` `` | variables `X`, `Y`, `Z` <br/> the anonymous variable `_` |
| `"sons"("abraham", listOf("isaac", "ishmael"))` | `sons(abraham, [isaac, ishmael])` |
| `logicListOf(1, 2, 3)` / `logicList(X, Y, tail = T)` / `emptyLogicList` | `[1, 2, 3]` / `[X, Y \| T]` / `[]` |
| `"ancestor"(X, Y) impliedBy ("parent"(X, Z) and "ancestor"(Z, Y))` | `ancestor(X, Y) :- parent(X, Z), ancestor(Z, Y).` |
| `a or b`, `not(a)`, `naf(a)` | `a ; b`, `not(a)`, `\+ a` |
| `X equalsTo Y`, `X neq Y` | `X = Y`, `X \= Y` |
| `X greaterThan 1`, `X lowerThanOrEqualsTo 3`, `X arithEq 3` | `X > 1`, `X =< 3`, `X =:= 3` |
| ``M `is` (N + 1)`` | `M is N + 1` |
| `member(X, L)`, `findall(...)`, `between(...)` | the Prolog standard library predicates |

For instance, a rule and a guard using lists and arithmetic:

```kotlin
believes {
    +initialBelief { "age"("jacob", 20) }
    +initialBelief { "sons"("abraham", listOf("isaac", "ishmael")) }
    +inferenceRule { "adult"(X) impliedBy ("age"(X, A) and (A greaterThanOrEqualsTo 18)) }
}
// ...
prologPlan {
    adding.goal {
        matchingGoal { "count"(N) }
    } onlyWhen {
        satisfies { "sons"("abraham", S) and member(X, S) and (N lowerThan 1) and (M `is` (N + 1)) }
    } triggers {
        agent.print("son ", X, " next ", M)   // son isaac next 1
    }
}
```

:::caution
Strings that start with an uppercase letter or `_` become **variables**: `"likes"("Bob", "alice")` is
`likes(Bob, alice)`, where `Bob` is a variable. Use lowercase atoms, or `Atom.of("Bob")` for the atom `'Bob'`.
:::

## Term builders

Each builder function makes a term for one purpose. The main differences are whether it applies the plan's substitution,
and whether it demands a **ground** term (one without variables):

| Where | Beliefs | Goals | Ground? |
|---|---|---|---|
| Agent definition | `initialBelief { }`, `inferenceRule { }` | `initialGoal { }` | yes, except rules |
| Trigger (pattern) | `matchingBelief { }` | `matchingGoal { }` | no, variables get bound |
| Guard (query) | `satisfies { }` | — | no, variables get bound |
| Plan body (new term) | `belief { }` | `goal { }` | yes, after substitution |
| Plan body (query) | `beliefQuery { }`, `testQuery { }` | — | no |
| Outside any plan | `newContextBeliefQuery { }` | — | no |

For example, in a body `agent.believe(belief { "introduced"(P) })` stores `introduced(jacob)` when `P = jacob`.
If `P` were unbound, `belief { }` would throw instead.

## Annotations and sources

As in Jason, terms can carry **annotations**, written with `[...]`. The main one is `source(...)`:

- beliefs and goals an agent creates itself count as `[source(self)]`;
- beliefs told by another agent, and goals it delegated, get `[source(sender)]`.

```kotlin
matchingBelief { "ping"(1) }                  // ping(1), whatever its source
matchingBelief { "ping"(1)[source(X)] }       // the same, binding X to the sender, or to self
matchingBelief { "ping"(1)[source(self)] }    // only the agent's own ping(1)
matchingGoal { "job"(N)[source(S)] }          // any job goal, binding S to the agent that delegated it, or to self
```

:::note[The same rules for beliefs and goals]
A pattern **without** annotations matches beliefs and goals from any source, and a belief or goal without
annotations counts as `[source(self)]`, as in Jason.
:::

`source` and `self` come from `it.unibo.jakta`, and the `[...]` syntax needs `import it.unibo.jakta.get`.

## Reading values in Kotlin

`X.value<T>()` converts the term bound to `X` into a Kotlin value:

| Prolog term | Kotlin types |
|---|---|
| atom `jacob` | `String` |
| integer `20` | `Int`, `Long`, `Short`, `Byte`, `Double`, `Float`, `String` |
| real `2.5` | `Double`, `Float`, `Int`, `Long`, `String` |
| list `[1, 2]` | `List<T>`, `Set<T>`, `Sequence<T>`, with elements converted recursively |
| tuple or 2-element list | `Pair<A, B>` |
| `true` / `false` | `Boolean` |

```kotlin
val name: String = P.value()   // "jacob"
val next: Int = M.value()      // 1
```

Numeric conversions are exact: reading `2.5` as an `Int` throws.
`value()` also throws if the variable is unbound, or if the term cannot be converted to the requested type.

## Using 2P-Kt directly

Outside the DSL there is no logic programming scope: terms are built with the 2P-Kt core API (`Atom`, `Struct`, `Var`,
`Fact`, `Rule` and `Substitution`, all in `it.unibo.tuprolog.core`), e.g. `Fact.of(Struct.of("on", Atom.of("a"), table))`.
See [Turn perceptions into Prolog facts](../../../how-to/prolog/perceptions.md).

## Learn more about 2P-Kt

- [2P-Kt documentation](https://tuprolog.github.io/2p-kt/)
- [Prolog DSL reference](https://tuprolog.github.io/2p-kt/reference/prolog-dsl/)
- [Getting started with 2P-Kt in Kotlin](https://tuprolog.github.io/2p-kt/tutorials/getting-started-kotlin/)
- [API reference](https://tuprolog.github.io/2p-kt/api/)
- [Web IDE](https://tuprolog.github.io/2p-kt/web-ide/), to try Prolog queries in the browser
- [The DSL design paper](http://ceur-ws.org/Vol-2706/paper14.pdf)
