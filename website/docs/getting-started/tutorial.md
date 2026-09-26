---
sidebar_position: 5
---

# Intermediate tutorial

This tutorial builds a two-agent *ping-pong* system. Along the way it shows how to:
- use your own Kotlin types as beliefs and goals, without any incarnation;
- turn incoming messages into beliefs;
- react to belief additions with plans;
- give agents a [skill](../basic-concepts/skills.md) — here, the ability to send messages.

It only needs `jakta-core`.

## Beliefs and goals are just types

The engine is generic over the belief and goal types.
Here goals are `String`s and a belief is a received message: its text and its sender.

```kotlin
typealias Message = Pair<String, AgentID>

val alice = BaseAgentID("Alice")
val bob = BaseAgentID("Bob")
```

## From messages to beliefs

A message sent to an agent is an *external event*. The agent decides how to turn it into updates of its
own state with `handlesMessageEvents`: return an `AgentUpdate` to accept it, or `null` to ignore it.
Both agents behave in the same way, so let's write it once as an extension of the agent builder:

```kotlin
fun AgentBuilder<Message, String, Any>.receivesTextMessages() {
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

## The agents

```kotlin
fun main(): Unit = runBlocking {
    mas(NodeBuilders.baseNode()) {
        node {
            context(MessagingSkill(node)) {
                agent<Message, String>(bob) {
                    receivesTextMessages()
                    hasPlanLibrary {
                        adding.belief {
                            takeIf { it.first == "Ping!" }
                        } triggers {
                            val (text, sender) = context
                            agent.print("Received \"$text\" from ${sender.displayName}")
                            agent.sendTo(sender, "Pong!")
                        }
                    }
                }
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

A few things to notice:

- **Triggers are functions.** `adding.belief { ... }` receives the new belief and returns either `null`
  (the plan is not relevant) or a *context* value. Here `takeIf` returns the belief itself.
- **The context flows into the body.** Inside `triggers { }`, `context` is the value returned by the trigger,
  so Bob can destructure it to find out who sent the ping.
  Incarnations provide richer matching: in the [Prolog incarnation](../explanation/incarnations/prolog/index.md)
  the context is the substitution produced by unification.
- **Skills are context parameters.** `context(MessagingSkill(node)) { ... }` makes the messaging skill available
  to every agent defined inside the block, and `agent.sendTo(...)` only compiles there.
  See [Skills](../basic-concepts/skills.md) to write your own.

## Imports

```kotlin
import it.unibo.jakta.agent.AgentID
import it.unibo.jakta.agent.BaseAgentID
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

## Going further

- The Prolog incarnation ships [KQML messaging](../explanation/communication.md#kqml-messaging-prolog-incarnation):
  `tell`, `achieve`, `askOne`, ... with `[source(X)]` annotations on received beliefs.
- The [`blocksworld`](https://github.com/jakta-bdi/jakta/tree/main/examples/blocksworld) example shows perceptions
  coming from a world model and a custom skill to act on it.

Next: [Tutorial: planning in the Blocks World](./blocks-world.md).
