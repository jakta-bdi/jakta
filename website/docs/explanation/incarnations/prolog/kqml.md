---
sidebar_label: KQML messaging
sidebar_position: 4
---

# KQML Messaging

The Prolog incarnation implements [KQML](https://en.wikipedia.org/wiki/Knowledge_Query_and_Manipulation_Language)
performatives on top of JaKtA's [messages](../../interaction/communication.md), in the style of Jason. A performative says what the
receiver should do with the content: believe it, pursue it, or answer it. Receiving agents hand KQML payloads to
`handleKQMLPayload`, which turns each one into an update of their beliefs or goals:

| Function | Performative | Effect on the receiver |
|---|---|---|
| `tellTo(receiver, belief)` / `broadcastTell(belief)` | tell | adds the belief, annotated with `[source(sender)]` |
| `untellTo(receiver, query)` / `broadcastUntell(query)` | untell | removes the matching beliefs received from that sender |
| `delegateAchieveTo(receiver, goal)` / `broadcastAchieve(goal)` | achieve | adds the goal, annotated with `[source(sender)]` |
| `sendUnachieveTo(receiver, query)` / `broadcastUnachieve(query)` | unachieve | removes the goal delegated by that sender |
| `askOneTo(receiver, query)` | askOne | adds a `replyOne(query, id)[source(sender)]` goal; the sender suspends until a reply arrives |
| `askAllTo(receiver, query)` | askAll | adds a `replyAllTo(query, id)[source(sender)]` goal; the sender suspends until a reply with all the answers arrives |

## Sources

Everything an agent receives carries the sender in a `source` annotation, so plans can tell their own beliefs and
goals from those of others, and answer the sender. Goals and beliefs follow the same
[annotation rules](./2p-kt.md#annotations-and-sources): a pattern without annotations matches whatever the
source, and `[source(S)]` binds `S` to the sender, or to `self`.

## Questions

Questions are goals. Unlike Jason, they are not answered automatically: the receiver decides how to answer with a
plan for `replyOne` (or `replyAllTo`) goals, which tells the answer back with the question id.

In Jason, an `askOne` is answered as a [test goal](../../basic-concepts/goals.md#why-there-are-no-test-goals) on
the receiver's beliefs: the interpreter queries them and replies on its own, and only falls back on a plan when no
belief matches. JaKtA has no test goals, so nothing can answer on the agent's behalf: the question becomes an
ordinary goal, and **every agent that is asked questions needs explicit reply plans**. Without them, the question
goal fails for lack of a plan, and the asker waits until its timeout. In exchange, answering is a plan like any
other: the receiver chooses what to reveal and to whom, can compute or fetch the answer, or ask someone else first.

```mermaid
sequenceDiagram
  participant A as Alice
  participant B as Bob
  A->>B: askOneTo(bob, beliefQuery { "b"(X) })
  Note over A: suspended until the reply
  B->>B: handleKQMLPayload → goal replyOne(Q, M)[source(alice)]
  B->>B: replyOne plan proves Q on its beliefs
  B->>A: tellTo(alice, M, belief)
  Note over A: resumes, X is bound
```

`askOneTo` binds the query's variables in the asking plan; `askAllTo` returns one substitution per answer instead.
Both return `null` if no reply arrives before the timeout.

See [Ask and answer with KQML](../../../how-to/prolog/kqml.md) for the code of each step, and the
[Contract Net](../../../showcase/contract-net.md) showcase for a complete protocol, where agents on different nodes
agree on who does what with `tell` messages.
