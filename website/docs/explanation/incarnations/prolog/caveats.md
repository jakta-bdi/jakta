---
title: Prolog Incarnation Caveats
sidebar_label: Caveats
sidebar_position: 4
---

# Prolog Incarnation Caveats

Things that commonly trip people up, and how to deal with them.

### Capitalized strings are variables

`"likes"("Bob", "alice")` is `likes(Bob, alice)` with `Bob` a **variable**. Use lowercase atoms, or
`Atom.of("Bob")`.

### Beliefs and goals must be ground

`initialBelief`, `initialGoal`, `belief { }` and `goal { }` throw `IllegalArgumentException`
(`Belief must be ground, ...`) if the term contains unbound variables. In a body, make sure every variable was bound
by the trigger or the guard.

### Unknown predicates fail silently

The solver is configured to make unknown predicates **fail** rather than raise an error. A typo in a guard, like
`satisfies { "ancestr"(A, P) }`, just makes the plan never applicable. If a plan never fires, check predicate
names and arities first.

### Guards use the first solution

`satisfies { }` and `testQuery { }` only look at the first solution. To use all of them, collect them explicitly,
e.g. with `agent.beliefs.allSolutionsOf(query)` or a `findall/3` query.

### Delegated goals need `[source(S)]`

Goal patterns without annotations do not match goals received with `delegateAchieveTo`. See
[annotations](./beliefs-and-goals.md#annotations-and-sources).

### Perceptions do not replace beliefs by themselves

A perception handler returns *additions* and *removals*. If it only adds `on(a, b)`, the stale `on(a, table)` stays.
Remove the old facts explicitly, as the Blocks World example does with a
`newContextBeliefQuery { "on"(X, Y) }` filter.

### No test goals

AgentSpeak's `?g` test goals are not available. Use `satisfies { }` in guards, or `testQuery { }` in bodies.

### KQML questions need an explicit answer

`askOneTo` puts a `replyOne(Q, M)[source(S)]` goal in the receiver, and someone must write the plan that answers it
(see [Communication](../../communication.md#kqml-messaging-prolog-incarnation)). `askOneTo` returns `null` only on
timeout; check `isSuccess` on the returned substitution before reading the variables.
`askAllTo` currently sends an `askOne` message, so it behaves like `askOneTo`.

### Performance

Each `satisfies` call builds a solver whose theory is the current belief base. That is cheap for small belief bases,
but it grows with their size: keep bulky data in Kotlin objects (for example in a [skill](../../../basic-concepts/skills.md)),
and keep in beliefs what the agent reasons about.

### Platforms

2P-Kt targets the JVM and JavaScript, so the Prolog incarnation is not available for native targets.
