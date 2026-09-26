---
sidebar_position: 2
---

# Beliefs in JaKtA

Beliefs are a fundamental concept in the **Belief-Desire-Intention (BDI)** model: they are the agent's knowledge
about the world, itself, and other agents. Beliefs change as the agent perceives, receives messages, or acts.

The type of a belief is chosen by the [incarnation](../explanation/incarnations/index.md).
The examples below use the **Prolog incarnation**, where a belief is a Prolog clause (`PrologBelief`, a
[2P-Kt](https://github.com/tuProlog/2p-kt) `Rule`): either a **fact** or an **inference rule**.

## Initial beliefs

Initial beliefs are declared with `believes { }`, adding each belief with `+`:

```kotlin
agent<PrologBelief, PrologGoal, Any> {
    embodiedAs { Any() }
    believes {
        +initialBelief { "parent"("alice", "bob") }
        +initialBelief { "parent"("alice", "carol") }
        +inferenceRule { "sibling"(X, Y) impliedBy ("parent"(Z, X) and "parent"(Z, Y) and (X neq Y)) }
    }
}
```

- `initialBelief { }` creates a fact.
- `inferenceRule { head impliedBy body }` creates a rule: `sibling(X, Y)` holds whenever its body can be proven.

Inside these blocks you are in the 2P-Kt Prolog DSL: strings invoked like functions (`"parent"(...)`) build
compound terms, and `X`, `Y`, `Z`, ... are logic variables. Check out the
[2P-Kt documentation](https://github.com/tuProlog/2p-kt) for the full syntax.

Beliefs can also be added one at a time with `addBelief(belief)` in the agent builder.

## Updating beliefs from plans

Plan bodies change beliefs with `agent.believe(...)` and `agent.forget(...)`.
In a Prolog plan use `belief { }`: variables bound by the plan's trigger or guard are substituted in.

```kotlin
prologPlan {
    adding.goal {
        matchingGoal { "move"(X) }
    } triggers {
        agent.forget(belief { "position"("home") })
        agent.believe(belief { "position"(X) })
    }
}
```

## Querying beliefs

- In plan **guards**, `satisfies { query }` succeeds if the query can be proven from the current beliefs,
  binding its variables. See [Plans](./plans.md#guards).
- In plan **bodies**, `testQuery { query }` does the same and fails the plan if the query does not hold.
- `agent.beliefs` gives direct access to the belief base, for instance `agent.beliefs.unifiesWith(query)`.

## Belief sources

Beliefs coming from other agents are annotated with their **source**, like in Jason.
A belief received through [KQML messaging](../explanation/communication.md#kqml-messaging-prolog-incarnation)
can be matched with `[source(X)]`, binding `X` to the sender:

```kotlin
prologPlan {
    adding.belief {
        matchingBelief { "ping"(1)[source(X)] }
    } triggers {
        agent.print("Received a ping from ", X)
    }
}
```

`source` and the `[...]` annotation operator are imported from `it.unibo.jakta` (`import it.unibo.jakta.source`,
`import it.unibo.jakta.get`).

## Perceptions

Agents do not share a global belief base: what an agent perceives from its [node](../explanation/nodes.md)
is converted into belief updates by the agent itself, with `handlesPerceptionEvents`:

```kotlin
handlesPerceptionEvents {
    when (it) {
        is BlocksWorldPerception -> handleBlocksWorldPerceptions(it, beliefs)
        else -> null
    }
}
```

The handler returns an `AgentUpdate.Belief(additions, removals)` or `null` to ignore the perception.
It can read the current `beliefs`, so it can replace stale perceived facts
(see [`BlocksWorldSkills.kt`](https://github.com/jakta-bdi/jakta/blob/main/examples/blocksworld/src/main/kotlin/BlocksWorldSkills.kt)).

## Belief events

Every change to the belief base generates an event that can trigger plans:
`adding.belief { }` reacts to additions and `removing.belief { }` to removals. See [Plans](./plans.md).
