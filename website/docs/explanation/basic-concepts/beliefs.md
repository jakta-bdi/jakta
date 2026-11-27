---
sidebar_position: 2
---

import Tabs from '@theme/Tabs';
import TabItem from '@theme/TabItem';

# Beliefs

Beliefs are a fundamental concept in the [BDI](./bdi.mdx) model: they are the agent's knowledge about the world,
itself, and other agents. Beliefs change as the agent [perceives](../interaction/perception.md), receives
[messages](../interaction/communication.md), or acts.

A belief can be any Kotlin type: a string, a data class, an enum. Beliefs are compared with `equals`, so the belief
base never holds the same belief twice. An [incarnation](../incarnations/index.md) can fix a richer type: with the
Prolog incarnation, a belief is a Prolog fact or inference rule.

:::tip
The examples come in two flavours: plain **Kotlin** types, and the **Prolog** incarnation. Pick one tab, and every
page keeps showing that one.
:::

## Initial beliefs

Initial beliefs are declared with `believes { }`, adding each belief with `+`.
They can also be added one at a time with `addBelief(belief)` in the agent builder.

<Tabs groupId="incarnation" queryString>
  <TabItem value="kotlin" label="Kotlin" default>

A barista who starts with some coffee beans, from the [sub-goals](../../how-to/sub-goals.md) example:

```kotlin
agent<String, Task> {
    embodiedAs { Any() }
    believes {
        +"beans"
    }
}
```

  </TabItem>
  <TabItem value="prolog" label="Prolog">

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

`initialBelief { }` creates a fact, and `inferenceRule { head impliedBy body }` a rule: `sibling(X, Y)` holds
whenever its body can be proven. Inside these blocks you are in the
[2P-Kt Prolog DSL](https://tuprolog.github.io/2p-kt/reference/prolog-dsl/): strings invoked like functions
(`"parent"(...)`) build compound terms, and `X`, `Y`, `Z`, ... are logic variables.

  </TabItem>
</Tabs>

## Updating beliefs from plans

Plan bodies change beliefs with `agent.believe(...)` and `agent.forget(...)`:

<Tabs groupId="incarnation" queryString>
  <TabItem value="kotlin" label="Kotlin" default>

```kotlin
adding.goal { this as? Grind } triggers {
    agent.print("Grinding the beans")
    agent.forget("beans")
}
```

  </TabItem>
  <TabItem value="prolog" label="Prolog">

Build the belief with `belief { }`, so that the variables bound by the plan's trigger or guard are substituted in:

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

  </TabItem>
</Tabs>

## Querying beliefs

Plan [guards](./plans.md#guards) read the current belief base as `beliefs`, and plan bodies as `agent.beliefs`.

<Tabs groupId="incarnation" queryString>
  <TabItem value="kotlin" label="Kotlin" default>

The belief base is a collection, queried like any other: grinding is applicable only while there are beans.

```kotlin
adding.goal { this as? Grind } onlyWhen { context.takeIf { "beans" in beliefs } } triggers {
    // ...
}
```

  </TabItem>
  <TabItem value="prolog" label="Prolog">

Beliefs are queried by proving a goal against them, rules included, which binds the query's variables:

- in guards, `satisfies { query }` succeeds if the query can be proven;
- in bodies, `testQuery { query }` does the same, and makes the plan [fail](./goals.md#failure) if it cannot;
- `agent.beliefs.unifiesWith(query)` checks a single belief.

```kotlin
onlyWhen {
    satisfies { "sibling"("bob", S) }
}
```

Beliefs told by other agents are also annotated with their **source**, as in Jason, and plans can match on it: see
[Annotations and sources](../incarnations/prolog/2p-kt.md#annotations-and-sources).

  </TabItem>
</Tabs>

## Perceptions and messages

Agents do not share a global belief base: each agent turns what it perceives from its node into its own beliefs, with
`handlesPerceptionEvents` (see [Perception](../interaction/perception.md)), and does the same with the messages it
receives, with `handlesMessageEvents` (see [Receiving messages](../interaction/communication.md#the-receiver-decides-what-a-message-means)).

## Belief events

Every change to the belief base generates an event that can trigger plans:
`adding.belief { }` reacts to additions and `removing.belief { }` to removals. See [Triggers](./plans.md#triggers).
Adding a belief already present, or removing one that is not, changes nothing and triggers no plan.
