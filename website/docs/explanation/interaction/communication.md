---
sidebar_position: 4
---

# Communication

Agents are **social**: they interact with other agents to cooperate, coordinate or negotiate[^wooldridge-jennings].
Agent communication builds on speech act theory[^searle], where saying something is doing something: a message is an
action meant to change the mental state of its receiver, informing it of a fact or asking it to achieve a goal.
Agent communication languages such as KQML[^kqml] name these intentions with **performatives** (`tell`, `achieve`,
`askOne`, ...). Since agents are autonomous, though, no sender can change a receiver's mind directly: it is the
receiver that decides what a message means to it. Agents can also communicate **indirectly**, through the
environment, by [stigmergy](https://en.wikipedia.org/wiki/Stigmergy).

## Messages in JaKtA

JaKtA keeps the core of direct communication minimal: a message carries a sender, a receiver and a **payload**, which
can be any object. Sending is an action, provided by the `MessagingSkill` like any other [skill](./acting.md):
`agent.sendTo(receiver, payload)` sends a message to an agent, on whichever node it lives, and
`agent.broadcast(payload)` to every agent.

```kotlin
adding.goal {
    takeIf { it == "sendPing" }
} triggers {
    agent.sendTo(bob, "Ping!")
}
```

## The receiver decides what a message means

A received message is an external event, and the receiver's `handlesMessageEvents` decides what it means: new
beliefs, as an informative message, new goals, as a request, or nothing at all, by returning `null`:

```kotlin
handlesMessageEvents { message ->
    when (val payload = message.payload) {
        is String -> AgentUpdate.Belief(setOf(ToldBelief(payload, message.sender)), emptySet())
        else -> null
    }
}
```

Here `ToldBelief(text, sender)` is a data class of the agent's own: beliefs need not be messages.
`AgentUpdate.Goal(additions, removals)` would accept the message as a request instead. The resulting belief or goal
events trigger plans as any other, so the agent reacts to messages as it reacts to what it perceives.

The [ping-pong tutorial](../../tutorials/ping-pong.md) builds a complete example with these primitives.

:::info[KQML messaging]
The [Prolog incarnation](../incarnations/prolog/index.md) adds Jason-style[^jason] KQML performatives on top of these
primitives, with `[source(sender)]` annotations on what agents receive:
see [KQML messaging](../incarnations/prolog/kqml.md).
:::

## Indirect communication

Agents can also coordinate through the environment: they act on a shared world model through a [skill](./acting.md)
and [perceive](./perception.md) the changes the others made, without ever addressing each other.

[^wooldridge-jennings]: Michael Wooldridge, Nicholas R. Jennings. [Intelligent agents: theory and practice](https://doi.org/10.1017/S0269888900008122). The Knowledge Engineering Review 10(2), 1995.
[^searle]: John R. Searle. [*Speech Acts: An Essay in the Philosophy of Language*](https://doi.org/10.1017/CBO9781139173438). Cambridge University Press, 1969.
[^jason]: Rafael H. Bordini, Jomi F. Hübner, Michael Wooldridge. [*Programming Multi-Agent Systems in AgentSpeak using Jason*](https://doi.org/10.1002/9780470061848). Wiley, 2007.
[^kqml]: Tim Finin, Richard Fritzson, Don McKay, Robin McEntire. [KQML as an Agent Communication Language](https://doi.org/10.1145/191246.191322). CIKM 1994.
