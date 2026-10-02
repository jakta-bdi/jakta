---
sidebar_position: 4
---

# Communication

Agents communicate by exchanging **messages**. Sending requires the `MessagingSkill`, and every agent decides how
received messages affect its state.

## Sending messages

`MessagingSkill(node)` (in `jakta-core`) provides:

- `agent.sendTo(receiver, payload)` to send a message to an agent, and
- `agent.broadcast(payload)` to send it to every agent.

The payload can be any object.

```kotlin
node {
    context(MessagingSkill(node)) {
        agent(alice) {
            // ...
            hasPlanLibrary {
                adding.goal {
                    takeIf { it == "sendPing" }
                } triggers {
                    agent.sendTo(bob, "Ping!")
                }
            }
        }
    }
}
```

## Receiving messages

Received messages are external events. `handlesMessageEvents` converts them into an `AgentUpdate` — typically new
beliefs, which then trigger `adding.belief { }` plans — or returns `null` to ignore the message:

```kotlin
handlesMessageEvents { message ->
    when (val payload = message.payload) {
        is String -> AgentUpdate.Belief(setOf(payload to message.sender), emptySet())
        else -> null
    }
}
```

`AgentUpdate.Goal(additions, removals)` can be returned instead, to accept a request as a new goal.

The [intermediate tutorial](../tutorials/ping-pong.md) builds a full ping-pong example with these primitives.

## KQML messaging

The [Prolog incarnation](./incarnations/prolog/index.md) adds Jason-style KQML performatives on top of these
primitives (`tell`, `achieve`, `askOne`, ...), with `[source(sender)]` annotations on what agents receive:
see [KQML messaging](./incarnations/prolog/kqml.md).

## Indirect communication

Agents can also coordinate *indirectly* (stigmergy) by acting on a shared world model through a
[skill](./basic-concepts/skills.md) and perceiving its changes — see [Nodes and environment](./nodes.md#perceptions).
