---
sidebar_position: 4
---

# Port a Jason agent

The Prolog incarnation keeps Jason's knowledge representation, so most of an AgentSpeak program translates line by
line. The [Blocks World showcase](../../showcase/blocksworld.md) is a complete port of the Jason agent of the same name.

## Beliefs and rules

```text
parent(abraham, isaac).
ancestor(X, Y) :- parent(X, Y).
```

```kotlin
believes {
    +initialBelief { "parent"("abraham", "isaac") }
    +inferenceRule { "ancestor"(X, Y) impliedBy "parent"(X, Y) }
}
```

## Initial goals

`!start.` becomes:

```kotlin
hasInitialGoals { !initialGoal { "start".toAtom() } }
```

## Plans

`+!introduce(P) : ancestor(A, P) <- .print(P, " descends from ", A).` becomes a plan with a trigger, a guard and a
Kotlin body:

```kotlin
prologPlan {
    adding.goal { matchingGoal { "introduce"(P) } } onlyWhen { satisfies { "ancestor"(A, P) } } triggers {
        agent.print(P, " descends from ", A)
    }
}
```

| AgentSpeak | JaKtA |
|---|---|
| `+!g` | `adding.goal { matchingGoal { g } }` |
| `-!g` (failure handling) | `failing.goal { matchingGoal { g } }` |
| `+b` / `-b` | `adding.belief { matchingBelief { b } }` / `removing.belief { ... }` |
| `: context` | `onlyWhen { satisfies { context } }` |
| `[source(S)]` in a trigger | the same annotation: `matchingBelief { b[source(S)] }` |

## Plan bodies

| AgentSpeak | JaKtA |
|---|---|
| `!g` | `agent.achieve(goal { g })` |
| `!!g` | `agent.alsoAchieve(goal { g })` |
| `?q` | `testQuery { q }` (no test goals: see [guards and queries](./guards-and-rules.md#query-in-a-plan-body)) |
| `+b` / `-b` | `agent.believe(belief { b })` / `agent.forget(belief { b })` |
| `.print(...)` | `agent.print(...)` |
| environment actions | a [skill](../../explanation/basic-concepts/skills.md) |

## Messages

`.send` becomes a KQML function, with a `MessagingSkill` in scope:

| AgentSpeak | JaKtA |
|---|---|
| `.send(bob, tell, b)` / `.send(bob, untell, b)` | `agent.tellTo(bob, belief { b })` / `agent.untellTo(bob, beliefQuery { b })` |
| `.send(bob, achieve, g)` / `.send(bob, unachieve, g)` | `agent.delegateAchieveTo(bob, goal { g })` / `agent.sendUnachieveTo(bob, goal { g })` |
| `.send(bob, askOne, q, A)` | `agent.askOneTo(bob, beliefQuery { q })` |
| `.send(bob, askAll, q, L)` | `agent.askAllTo(bob, beliefQuery { q })` |
| `.broadcast(tell, b)` | `agent.broadcastTell(belief { b })` |

Unlike Jason, questions need an answering plan: see [Ask and answer with KQML](./kqml.md#answer-a-question).

## What changes

- **Bodies are Kotlin**: loops, conditionals and calls replace AgentSpeak body formulas.
- **Agents live in nodes** and act through skills, not a Jason environment.
- **A belief has a single source**: a belief told by two agents is kept once (see
  [caveats](../../explanation/incarnations/prolog/caveats.md#a-belief-has-a-single-source)).

See [Jason-like Kotlin Agents](../../history/jakta-and-jason.md) for how JaKtA relates to Jason.
