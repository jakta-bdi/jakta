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

It only needs `jakta-core`. The complete program is the [`ping-pong`](https://github.com/jakta-bdi/jakta/tree/main/examples/ping-pong) example; run it with `./gradlew :examples:ping-pong:run`.

## Beliefs and goals are just types

The engine is generic over the belief and goal types.
Here goals are `String`s and a belief is a received message: its text and its sender.

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
private typealias Message = Pair<String, AgentID>

private val alice = BaseAgentID("Alice")
private val bob = BaseAgentID("Bob")
```

## From messages to beliefs

A message sent to an agent is an *external event*. The agent decides how to turn it into updates of its
own state with `handlesMessageEvents`: return an `AgentUpdate` to accept it, or `null` to ignore it.
Both agents behave in the same way, so let's write it once as an extension of the agent builder:

```kotlin
private fun AgentBuilder<Message, String, Any>.receivesTextMessages() {
    embodiedAs { Any() }
    handlesMessageEvents { message ->
        when (val payload = message.payload) {
            is String -> AgentUpdate.Belief(setOf(payload to message.sender), emptySet())
            else -> null
        }
    }
}
```

`AgentUpdate.Belief(additions, removals)` adds the belief `("Ping!", sender)`, which in turn generates
a belief-addition event that plans can react to. Perceptions work the same way through `handlesPerceptionEvents`.

## A reusable agent

Bob only answers pings, so he is defined once at top level with `agent(id) { }` and can be added to any node.
The block runs for the node the agent joins, so `node` is available to give him the messaging skill:

```kotlin
private val ponger = agent<Message, String, Any>(bob) {
    receivesTextMessages()
    context(MessagingSkill(node)) {
        hasPlanLibrary {
            adding.belief {
                takeIf { it.first == "Ping!" }
            } triggers {
                val (text, sender) = context
                agent.print("Received \"$text\" from ${sender.displayName}")
                agent.sendTo(sender, "Pong!")
                node.terminateNode()
            }
        }
    }
}
```

## Two nodes

Alice is defined inline, directly on her node, and Bob lives on another node, added with `withAgents`.
Messages cross nodes, so the program does not change if both agents share a node instead:

```kotlin
fun main(): Unit = runBlocking {
    Logger.setMinSeverity(Severity.Assert)
    mas(NodeBuilders.baseNode()) {
        node {
            withAgents(ponger)
        }
        node {
            context(MessagingSkill(node)) {
                agent<Message, String>(alice) {
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
                            takeIf { it.first == "Pong!" }
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
- **Skills are context parameters.** `context(MessagingSkill(node)) { ... }` makes the messaging skill available
  to every agent, or plan, defined inside the block, and `agent.sendTo(...)` only compiles there.
  See [Skills](../explanation/basic-concepts/skills.md) to write your own.
- **Each node stops on its own.** Bob and Alice each call `node.terminateNode()`, and `runLocally()` returns
  when every node has terminated.

## Going further

- The Prolog incarnation ships [KQML messaging](../explanation/incarnations/prolog/kqml.md):
  `tell`, `achieve`, `askOne`, ... with `[source(X)]` annotations on received beliefs.
- The [Blocks World showcase](../showcase/blocksworld.md) shows an agent planning recursively with Prolog rules.

Next: [Advanced: Thermostat](./thermostat.md), an agent interacting with an external environment.
