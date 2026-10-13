---
sidebar_position: 5
---

# Intermediate: Ping-pong agents

This tutorial builds a two-agent *ping-pong* system. Along the way it shows how to:

- define agents, reusable or inline, and group them in nodes;
- run a MAS made of several nodes;
- use your own Kotlin types as beliefs and goals, without any incarnation;
- turn incoming messages into beliefs;
- react to belief additions with plans;
- give agents a [skill](../explanation/basic-concepts/skills.md) — here, the ability to send messages.

It runs in the project from [Getting Started](./index.mdx): put the code in `App.kt`, as in [Hello world](./hello-world.md),
and run it with `./gradlew run`. The complete program is also the
[`ping-pong`](https://github.com/jakta-bdi/jakta/tree/main/examples/ping-pong) example in the JaKtA repository.

## Beliefs and goals are just types

The engine is generic over the belief and goal types.
Here goals are `String`s, and a belief is a `ToldBelief`: the text another agent told, and who told it.
It is a type of your own, not the message itself: the agent decides which beliefs it gets from a message.

<details>
<summary>Imports</summary>

```kotlin
import co.touchlab.kermit.Logger
import co.touchlab.kermit.Severity
import it.unibo.jakta.agent.AgentID
import it.unibo.jakta.agent.BaseAgentID
import it.unibo.jakta.dsl.agent
import it.unibo.jakta.dsl.agent.AgentBuilder
import it.unibo.jakta.dsl.mas
import it.unibo.jakta.dsl.mas.runLocally
import it.unibo.jakta.dsl.node.NodeBuilders
import it.unibo.jakta.dsl.plan.triggers
import it.unibo.jakta.event.AgentUpdate
import it.unibo.jakta.skills.MessagingSkill
import it.unibo.jakta.skills.sendTo
import kotlinx.coroutines.runBlocking
```

</details>

```kotlin
private data class ToldBelief(val text: String, val sender: AgentID)

private val alice = BaseAgentID("Alice")
private val bob = BaseAgentID("Bob")
```

## From messages to beliefs

A message sent to an agent is an *external event*. The agent decides how to turn it into updates of its
own state with `handlesMessageEvents`: return an `AgentUpdate` to accept it, or `null` to ignore it.
Both agents behave in the same way, so let's write it once as an extension of the agent builder:

```kotlin
private fun AgentBuilder<ToldBelief, String, Any>.receivesTextMessages() {
    embodiedAs { Any() }
    handlesMessageEvents { message ->
        when (val payload = message.payload) {
            is String -> AgentUpdate.Belief(setOf(ToldBelief(payload, message.sender)), emptySet())
            else -> null
        }
    }
}
```

`AgentUpdate.Belief(additions, removals)` adds the belief `ToldBelief("Ping!", sender)`, which in turn generates
a belief-addition event that plans can react to. Perceptions work the same way through `handlesPerceptionEvents`.

## A reusable agent

Bob only answers pings, so he is defined once at top level with `agent(id) { }` and can be added to any node.
To answer he needs the messaging skill, which sends messages through the node hosting him. So Bob does not create it:
`ponger()` asks for it as a [context parameter](https://kotlinlang.org/docs/context-parameters.html),
and the node he is added to provides it:

```kotlin
context(_: MessagingSkill)
private fun ponger() = agent<ToldBelief, String, Any>(bob) {
    receivesTextMessages()
    hasPlanLibrary {
        adding.belief {
            takeIf { it.text == "Ping!" }
        } triggers {
            val (text, sender) = context
            agent.print("Received \"$text\" from ${sender.displayName}")
            agent.sendTo(sender, "Pong!")
            node.terminateNode()
        }
    }
}
```

## Two nodes

A node is a part of the MAS that lives on its own: it hosts its agents, delivers their perceptions and messages,
gives them their skills, and runs and stops independently of the other nodes. Nodes stand for logically different devices, such as a robot,
a server or a phone, each running several agents of the same MAS. Here every node runs in the same process
(`runLocally()` connects them through shared memory), but agents on different nodes still interact only by
messages, as they would across a network. See [Nodes](../explanation/nodes.md).

Alice and Bob live on separate nodes, so each of them can stop when done: `node.terminateNode()` stops only the
node of the agent calling it. On a shared node, Bob stopping after his answer would stop Alice too, before she
gets the pong.

Alice is defined inline, directly on her node, and Bob lives on another node, added with `withAgents`.
Each node gives its agents a `MessagingSkill` of its own, and messages cross nodes, so Alice reaches Bob on his node:

```kotlin
fun main(): Unit = runBlocking {
    Logger.setMinSeverity(Severity.Assert)
    mas(NodeBuilders.baseNode()) {
        node {
            context(MessagingSkill(node)) {
                withAgents(ponger())
            }
        }
        node {
            context(MessagingSkill(node)) {
                agent<ToldBelief, String>(alice) {
                    receivesTextMessages()
                    hasInitialGoals {
                        !"sendPing"
                    }
                    hasPlanLibrary {
                        adding.goal {
                            takeIf { it == "sendPing" }
                        } triggers {
                            agent.sendTo(bob, "Ping!")
                        }
                        adding.belief {
                            takeIf { it.text == "Pong!" }
                        } triggers {
                            agent.print("Got the pong, stopping.")
                            node.terminateNode()
                        }
                    }
                }
            }
        }
    }.runLocally()
}
```

It prints:

```text
Received "Ping!" from Alice
Got the pong, stopping.
```

A few things to notice:

- **Triggers are functions.** `adding.belief { ... }` receives the new belief and returns either `null`
  (the plan is not relevant) or a *context* value. Here `takeIf` returns the belief itself.
- **The context flows into the body.** Inside `triggers { }`, `context` is the value returned by the trigger,
  so Bob can destructure it to find out who sent the ping.
  Incarnations provide richer matching: in the [Prolog incarnation](../explanation/incarnations/prolog/index.md)
  the context is the substitution produced by unification.
- **Skills are given by the node.** `context(MessagingSkill(node)) { ... }` makes the messaging skill available
  to every agent defined or added inside the block, and `agent.sendTo(...)` only compiles there.
  A reusable agent like Bob declares the skills it needs with `context(...)`, and the node hosting it provides them.
  See [Skills](../explanation/basic-concepts/skills.md) to write your own.
- **The MAS ends with its last node.** `runLocally()` returns when every node has terminated.

## Going further

- The Prolog incarnation ships [KQML messaging](../explanation/incarnations/prolog/kqml.md):
  `tell`, `achieve`, `askOne`, ... with `[source(X)]` annotations on received beliefs.
- The [Blocks World showcase](../showcase/blocksworld.md) shows an agent planning recursively with Prolog rules.

Next: [Advanced: Thermostat](./thermostat.md), an agent interacting with an external environment.
