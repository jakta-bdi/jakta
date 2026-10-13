---
sidebar_position: 5
---

# Skills

In JaKtA 1.x there are no dedicated "action" constructs: plan bodies are plain `suspend` Kotlin code.
What an agent can *do* beyond changing its own state — send messages, move a robot, update a world model —
is modelled as a **skill**: an ordinary Kotlin object made available to plan bodies through
[context parameters](https://kotlinlang.org/docs/context-parameters.html).

This keeps capabilities explicit and type-checked: a plan that calls `agent.sendTo(...)` only compiles
where a `MessagingSkill` is in scope.

## Built-in skills

`jakta-core` provides, in `it.unibo.jakta.skills`:

| Skill | Provides |
|---|---|
| `MessagingSkill(node)` | `agent.sendTo(receiver, payload)` and `agent.broadcast(payload)` |
| `AgentTerminationSkill` | `agent.terminate()` — stop this agent |
| `NodeTerminationSkill(node)` | `terminateNode()` — stop the whole node |

The built-in skills take a `Node<Any>`, so they work with nodes built with `NodeBuilders.baseNode()`, whose body type is `Any`.
For agents with a custom body, write a skill for your node type (see [Give agents a body](../../how-to/custom-body.md)).

Plan bodies can also always reach the node directly (`node.terminateNode()`, `node.publishEvent(...)`).

## Skills are node-level abilities

A skill acts on the world through a node: `MessagingSkill` delivers messages through the node, `NodeTerminationSkill`
stops it, and a custom skill such as the Blocks World one below changes the world the node hosts and reports the
outcome as a perception on the node. That is why skills are built from a node (the `Skill` base class holds one).

JaKtA therefore encourages you to see a skill as a **requirement** of an agent, which the **node** executing the agent
satisfies. The agent states what it needs to be able to do; the node decides how it is done. The same agent can then
run on nodes that provide the skill differently, for instance a simulated world in tests and the real one in
production, without changing its definition.

### Giving skills at the node level

Wrap the agents of a node in a `context(...)` block. Every agent defined or added inside it can use the skill:

```kotlin
node {
    context(MessagingSkill(node)) {
        withAgents(ponger())
        agent<ToldBelief, String>(alice) {
            // plans here can call agent.sendTo(...)
        }
    }
}
```

A reusable agent states the skills it requires as context parameters of the function that defines it:

```kotlin
context(_: MessagingSkill)
fun ponger() = agent<ToldBelief, String, Any>(bob) {
    // plans here can call agent.sendTo(...)
}
```

The compiler checks the requirement: `withAgents(ponger())` only compiles where a `MessagingSkill` is in scope,
so an agent cannot be added to a node that does not provide what it needs.
The [ping-pong tutorial](../../tutorials/ping-pong.md) builds a complete example this way.

### Agent-specific skills

An agent can also give a skill to itself, since `node` is available inside `agent { }`, or to only some of its plans:

```kotlin
agent<ToldBelief, String, Any>(bob) {
    context(MessagingSkill(node)) {
        hasPlanLibrary {
            // only these plans can call agent.sendTo(...)
        }
    }
}
```

This works, but the agent then decides on its own how the skill is implemented, and it can no longer be reused on a
node that provides the skill differently. Prefer stating the requirement and letting the node satisfy it.

## Writing your own skill

A skill is any class. The [`blocksworld`](https://github.com/jakta-bdi/jakta/tree/main/examples/blocksworld)
example defines one to act on a world model and notify agents of the new state:

```kotlin
interface BlocksWorldSkills {
    suspend fun move(block: String, destination: String)
    suspend fun join()
    suspend fun displayWorld()
}

class BlocksWorldSkillsImpl(private val world: BlocksWorld, private val node: Node<*>) : BlocksWorldSkills {
    override suspend fun move(block: String, destination: String) {
        val destinationBlock = if (destination == "table") null else Block(destination)
        val state = world.move(Block(block), destinationBlock)
        node.publishEvent(BlocksWorldPerception(state))
    }
    // ...
}
```

To make it read naturally inside plan bodies, expose it as an extension on `PlanScope` that requires the skill
as a context parameter:

```kotlin
context(skills: BlocksWorldSkills)
val PlanScope<*, *, *>.blocksWorld
    get() = skills
```

Then install it around the agent and use it in plans:

```kotlin
context(BlocksWorldSkillsImpl(world, node)) {
    agent<PrologBelief, PrologGoal>(BaseAgentID("BlocksWorldAgent")) {
        // ...
        hasPlanLibrary {
            prologPlan {
                adding.goal {
                    matchingGoal { on(X, Y) }
                } triggers {
                    blocksWorld.move(X.value(), Y.value())
                }
            }
        }
    }
}
```

Skills that change the world should report the outcome back as a **perception**
(`node.publishEvent(...)`), which each agent turns into beliefs with `handlesPerceptionEvents` —
see [Nodes](../nodes.md).

For a skill that depends on the agent's body, see
[`TestSpatialRobot`](https://github.com/jakta-bdi/jakta/blob/main/jakta-core/src/commonTest/kotlin/it/unibo/jakta/dsl/examples/TestSpatialRobot.kt),
where a robot moves on a grid and recharges its battery.
