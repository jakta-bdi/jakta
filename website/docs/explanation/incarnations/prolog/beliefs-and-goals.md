---
title: Beliefs, Goals and Annotations
sidebar_position: 3
---

# Beliefs, Goals and Annotations

## Which function where?

Each function builds a term for one purpose. The main differences are whether it applies the plan's substitution,
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
matchingBelief { "ping"(1)[source(X)] }   // matches only told beliefs, binds X to the sender
matchingBelief { "ping"(1) }              // matches ping(1) whatever its source
matchingGoal { "job"(N)[source(S)] }      // needed to match a goal delegated by another agent
```

:::caution[Goals and beliefs match annotations differently]
A belief pattern **without** annotations matches beliefs with any source.
A goal pattern **without** annotations does *not* match a goal delegated by another agent
(`job(1)[source(alice)]`). Include `[source(S)]` in plans meant for delegated goals.
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
