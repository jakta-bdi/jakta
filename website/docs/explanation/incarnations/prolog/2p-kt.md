---
title: 2P-Kt Integration and Syntax
sidebar_label: 2P-Kt integration
sidebar_position: 1
---

# 2P-Kt Integration and Syntax

[2P-Kt](https://tuprolog.github.io/2p-kt/) (tuProlog in Kotlin) is a multiplatform Prolog implementation.
The Prolog incarnation relies on it for three things:

| JaKtA concept | 2P-Kt counterpart |
|---|---|
| `PrologBelief` | `Rule` (a fact is a rule whose body is `true`) |
| `PrologGoal` | `Struct` |
| Writing terms in Kotlin | 2P-Kt's Prolog DSL (`"f"(X)`, `impliedBy`, `and`, ...) |
| Triggers | Unification, with a JaKtA-specific unifier that also matches annotations |
| Guards (`satisfies`) and `testQuery` | A classic 2P-Kt solver, run with the belief base as its theory |

The incarnation exposes the 2P-Kt modules it uses (`core`, `solve-classic`, `parser`, `dsl-*`, `serialize`)
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

A few more rules and a guard, all verified against JaKtA:

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

## Using 2P-Kt directly

Outside the DSL, for example when turning a perception into facts, build terms with the 2P-Kt core API.
The [Blocks World](../../../getting-started/blocks-world.md) example does this:

```kotlin
Fact.of(Struct.of("on", Atom.of(block.id), support))
```

`Atom`, `Struct`, `Var`, `Fact`, `Rule` and `Substitution` are all in `it.unibo.tuprolog.core`.

## Learn more about 2P-Kt

- [2P-Kt documentation](https://tuprolog.github.io/2p-kt/)
- [Prolog DSL reference](https://tuprolog.github.io/2p-kt/reference/prolog-dsl/)
- [Getting started with 2P-Kt in Kotlin](https://tuprolog.github.io/2p-kt/tutorials/getting-started-kotlin/)
- [API reference](https://tuprolog.github.io/2p-kt/api/)
- [Web IDE](https://tuprolog.github.io/2p-kt/web-ide/), to try Prolog queries in the browser
- [The DSL design paper](http://ceur-ws.org/Vol-2706/paper14.pdf)
