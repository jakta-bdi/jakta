---
sidebar_position: 6
---

# Tutorial: planning in the Blocks World

The [`blocksworld`](https://github.com/jakta-bdi/jakta/tree/main/examples/blocksworld) example is a port of the
classic Jason blocks-world agent: an agent rearranges stacks of blocks until they match a desired configuration.
It combines most JaKtA features in a small program:

- a **world model** written in plain Kotlin;
- a **skill** the agent uses to act on it, and **perceptions** reporting its new state;
- Prolog **inference rules** that let the agent reason about towers;
- **guards** to choose between alternative plans, and **recursive sub-goals** to decompose the problem.

Run it with:

```bash
./gradlew :examples:blocksworld:run
```

A Compose Desktop window opens: set the seed, the number of blocks and the goal, then press **Play**.

## 1. The world model

[`model/BlocksWorld.kt`](https://github.com/jakta-bdi/jakta/blob/main/examples/blocksworld/src/main/kotlin/model/BlocksWorld.kt)
knows nothing about agents. It keeps a list of stacks (bottom block first), shuffled from a random seed,
and exposes coroutine-safe operations guarded by a `Mutex`:

```kotlin
data class Block(val id: String)

class BlocksWorld(seed: Long = 42, blockCount: Int = 6) {
    suspend fun move(block: Block, destination: Block?): List<List<Block>> = mutex.withLock { /* ... */ }
    suspend fun getState(): List<List<Block>> = mutex.withLock { getStateUnsafe() }
    suspend fun printState() = mutex.withLock { /* ... */ }
}
```

`move` checks that both the moved block and the destination are clear, moves the block (`null` means the table),
and returns the new state. Stacking onto another block takes one second, so the UI can show it.

## 2. Perceptions and the skill

[`BlocksWorldSkills.kt`](https://github.com/jakta-bdi/jakta/blob/main/examples/blocksworld/src/main/kotlin/BlocksWorldSkills.kt)
connects the world to the agent. First, a **perception** type carries a snapshot of the world:

```kotlin
data class BlocksWorldPerception(val state: List<List<Block>>) : Perception
```

Then a **skill** defines what the agent can do. Every operation that changes (or reveals) the world publishes
a perception on the agent's node:

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

    override suspend fun join() {
        val state = world.getState()
        node.publishEvent(BlocksWorldPerception(state))
    }

    override suspend fun displayWorld() {
        world.printState()
    }
}
```

The agent turns each perception into `on(Block, Support)` facts, **replacing** all the `on/2` facts it had before:

```kotlin
val filterQuery = newContextBeliefQuery { "on"(X, Y) }

fun handleBlocksWorldPerceptions(
    event: BlocksWorldPerception,
    previousBeliefs: Collection<PrologBelief>,
): AgentUpdate<*> = AgentUpdate.Belief(
    event.state.toPrologFacts(),
    previousBeliefs.filter {
        it matches filterQuery
    }.toSet(),
)
```

`toPrologFacts()` maps every stack to facts such as `on(a, table)` and `on(b, a)`.
Since the engine applies additions minus removals and removals minus additions, facts that did not change
generate no events.

Finally, the skill is exposed to plan bodies as a property that is only available where the skill is in scope:

```kotlin
context(skills: BlocksWorldSkills)
val PlanScope<*, *, *>.blocksWorld
    get() = skills
```

## 3. The agent

[`Agent.kt`](https://github.com/jakta-bdi/jakta/blob/main/examples/blocksworld/src/main/kotlin/Agent.kt)
defines a node with one agent, wrapped in the skill's context:

```kotlin
fun MasBuilder<BaseNode<Any>, BaseNodeBuilder<Any, BaseNode<Any>>>.blocksWorldNode(
    world: BlocksWorld,
    desiredWorldState: PrologGoal,
) = node {
    context(BlocksWorldSkillsImpl(world, node)) {
        agent<PrologBelief, PrologGoal>(BaseAgentID("BlocksWorldAgent")) {
            val start = "start".toAtom()
            val table = "table".toAtom()
            fun tower(list: List): Struct = Struct.of("tower", list)
            fun on(block: Term, support: Term): Struct = Struct.of("on", block, support)
            // ... similar helpers for state/1 and clear/1
```

The small helper functions build Prolog terms with readable names: `tower(X)` instead of `Struct.of("tower", X)`.

### Beliefs: reasoning about towers

```kotlin
embodiedAs { Any() }
believes {
    +initialBelief { clear(table) }
    +inferenceRule { clear(X) impliedBy not(on(`_`, X)) }
    +inferenceRule {
        tower(logicListOf(X)) impliedBy (
            on(X, table)
            )
    }
    +inferenceRule {
        tower(logicList(X, Y, tail = T)) impliedBy (
            on(X, Y) and
                tower(logicList(Y, tail = T))
            )
    }
}
```

The agent only ever *perceives* `on/2` facts; everything else is derived:

- the table is always clear, and a block is clear if nothing is on it (`not` is negation as failure);
- `tower([X])` holds if `X` is on the table, and `tower([X, Y | T])` holds if `X` is on `Y` and `[Y | T]`
  is itself a tower. A tower is listed **top first**: `tower([a, b])` means `a` is on `b`, which is on the table.

### Handling perceptions

```kotlin
hasInitialGoals {
    !initialGoal { start }
}
handlesPerceptionEvents {
    when (it) {
        is BlocksWorldPerception -> handleBlocksWorldPerceptions(it, beliefs)
        else -> null
    }
}
```

### The goal

The desired configuration is a `state/1` goal holding a list of towers, for instance
`state([[a, b], [c, d, e], [f]])`: `a` on `b`, `c` on `d` on `e`, and `f` alone on the table.
The UI parses it from text like `[A, B]; [C, D, E]; [F]`.

## 4. The plans

Each plan below is a `prologPlan { }` in the agent's `hasPlanLibrary { }`.

**Start.** Join the world — which produces the first perception — then pursue the desired state:

```kotlin
adding.goal {
    matchingGoal { start }
} triggers {
    blocksWorld.join()
    blocksWorld.displayWorld()
    agent.achieve(desiredWorldState)
}
```

`join()` publishes the perception *before* `achieve` posts the new goal. Both go into the agent's single event queue,
so by the time the first `state` plan is selected, the agent already believes the current `on/2` facts
(see [Execution model](../explanation/execution-model.md)).

**Build the towers, one at a time.** `state/1` is decomposed recursively: build the first tower, then the rest.

```kotlin
adding.goal {
    matchingGoal { state(emptyLogicList) }
} triggers {
    agent.print("Finished! Final state reached.")
    blocksWorld.displayWorld()
}

adding.goal {
    matchingGoal { state(logicList(H, tail = T)) }
} triggers {
    agent.print("Building the tower ", H)
    agent.achieve(goal { tower(H) })
    agent.achieve(goal { state(T) })
}
```

`agent.achieve` suspends until the sub-goal is achieved, so towers are built in order,
and `goal { }` substitutes the variables bound by the trigger (`H`, `T`).

**Build a tower.** Three plans handle `tower/1`, and here their order matters:

```kotlin
adding.goal {
    matchingGoal { tower(T) }
} onlyWhen {
    satisfies { tower(T) }
} triggers {
    agent.print("Tower ", T, " is already built.")
}

adding.goal {
    matchingGoal { tower(logicListOf(X)) }
} triggers {
    agent.achieve(goal { on(X, table) })
}

adding.goal {
    matchingGoal { tower(logicList(X, Y, tail = T)) }
} triggers {
    agent.achieve(goal { tower(logicList(Y, tail = T)) })
    agent.achieve(goal { on(X, Y) })
}
```

All three can be **relevant** for the same goal, but JaKtA picks the **first applicable** plan in the library.
The first one has a guard: if the inference rules can already prove `tower(T)`, there is nothing to do.
Only when that guard fails does one of the other plans run, building the tower bottom-up:
first the tower below, then the top block on it.

**Put a block on another.** The same pattern — "already done" first, then the actual work:

```kotlin
adding.goal {
    matchingGoal { on(X, Y) }
} onlyWhen {
    satisfies { on(X, Y) }
} triggers {
    agent.print("Block ", X, " is on ", Y)
}

adding.goal {
    matchingGoal { on(X, Y) }
} triggers {
    agent.achieve(goal { clear(X) })
    agent.achieve(goal { clear(Y) })
    blocksWorld.move(X.value(), Y.value())
}
```

`X.value()` converts the Prolog atom bound to `X` into a Kotlin `String` for the skill.

**Clear a block.**

```kotlin
adding.goal {
    matchingGoal { clear(X) }
} onlyWhen {
    satisfies { clear(X) }
} triggers {
    agent.print("Block", X, "is clear.")
}

adding.goal {
    matchingGoal { clear(X) }
} onlyWhen {
    satisfies { tower(logicList(H, tail = T)) and member(X, T) }
} triggers {
    agent.achieve(goal { clear(H) })
    blocksWorld.move(H.value(), table.value)
    agent.achieve(goal { clear(X) })
}
```

If `X` is not clear, the guard finds a tower `[H | T]` with `X` somewhere below `H`,
binding `H` in the plan context. The plan clears `H`, moves it to the table, and then **re-posts** `clear(X)`:
if more blocks were above `X`, the same plan applies again, until the first plan's guard succeeds.

The guard does not guarantee that `H` is the top of its stack (any tower suffix matches), so the plan first
achieves `clear(H)` itself before moving it.

## 5. Launching the MAS from the UI

[`ui/BlocksWorldViewModel.kt`](https://github.com/jakta-bdi/jakta/blob/main/examples/blocksworld/src/main/kotlin/ui/BlocksWorldViewModel.kt)
starts the MAS in a coroutine when you press **Play**:

```kotlin
fun play(scope: CoroutineScope) {
    if (agentJob?.isActive == true) {
        return
    }

    val currentGoal = goal

    agentJob = scope.launch {
        mas(NodeBuilders.baseNode()) {
            blocksWorldNode(world, currentGoal)
        }.run(CoroutineNodeRunner(SharedMemoryNetwork()))
    }
}

fun stop() {
    agentJob?.cancel()
    agentJob = null
}
```

The MAS is an ordinary suspending computation: **Stop** simply cancels its coroutine.
The agent and the UI share the same `BlocksWorld` instance. The screen polls it (every 33 ms) to draw the stacks,
while the agent changes it through its skill.
[`Main.kt`](https://github.com/jakta-bdi/jakta/blob/main/examples/blocksworld/src/main/kotlin/Main.kt)
only opens the window and sets the log level to `Error`, so the console shows what the agent prints.

## Things to try

- Change the goal text or the seed, and press **Reset** then **Play**.
- Swap the order of the two `on(X, Y)` plans: the unguarded one is now always selected first,
  and the agent tries to move blocks that are already in place.
- Add a `removing.belief { matchingBelief { on(X, Y) } }` plan that prints every `on/2` fact the agent stops believing.
