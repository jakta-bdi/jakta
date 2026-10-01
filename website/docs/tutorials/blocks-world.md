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

It is a [Compose Multiplatform](https://www.jetbrains.com/compose-multiplatform/) application that runs both on the
desktop and in the browser:

```bash
./gradlew :examples:blocksworld:run                      # desktop
./gradlew :examples:blocksworld:jsBrowserDevelopmentRun  # browser
```

Drag the blocks to arrange the goal (shown first) and the world, or shuffle them, then press **Play**.
The agent works until the world matches the goal, and an *agent trace* panel shows what it prints while it reasons.

## 1. The world model

[`model/BlocksWorld.kt`](https://github.com/jakta-bdi/jakta/blob/main/examples/blocksworld/src/commonMain/kotlin/model/BlocksWorld.kt)
knows nothing about agents. It keeps the stacks (bottom block first) in a `StateFlow`, which the UI observes:

```kotlin
data class Block(val id: String)

typealias Stacks = List<List<Block>>

class BlocksWorld(initial: Stacks) {

    private val mutableState = MutableStateFlow(initial)

    /**
     * The current state of the Blocks World.
     */
    val state: StateFlow<Stacks> = mutableState.asStateFlow()

    /**
     * How long a single move of the agent takes, to make its work visible.
     */
    var moveDelay: Duration = 1.seconds

    /**
     * Moves a block on top of [destination], or on the table if [destination] is null, taking [moveDelay].
     * Returns the new state of the world.
     */
    suspend fun move(block: Block, destination: Block?): Stacks {
        delay(moveDelay)
        return mutableState.updateAndGet { it.moved(block, destination) }
    }

    /**
     * Instantly moves a block, e.g. when the user rearranges the world.
     */
    fun rearrange(block: Block, destination: Block?) {
        mutableState.update { it.moved(block, destination) }
    }
}
```

`move` waits `moveDelay` (one second by default, adjustable from the UI) so that each move is visible, then moves the
block (`null` means the table) and returns the new state. The moved block and the destination must both be clear.
`rearrange` is used by the UI when you drag blocks yourself.

## 2. Perceptions and the skill

[`BlocksWorldSkills.kt`](https://github.com/jakta-bdi/jakta/blob/main/examples/blocksworld/src/commonMain/kotlin/BlocksWorldSkills.kt)
connects the world to the agent. First, a **perception** type carries a snapshot of the world:

```kotlin
data class BlocksWorldPerception(val state: List<List<Block>>) : Perception
```

Then a **skill** defines what the agent can do. Both operations publish a perception on the agent's node:
`move` after changing the world, `join` with its current state.

```kotlin
interface BlocksWorldSkills {
    /**
     * Moves a block to a specified destination in the Blocks World.
     *
     * @param block The identifier of the block to be moved.
     * @param destination The identifier of the destination block or "table" for moving to the table.
     */
    suspend fun move(block: String, destination: String)

    /**
     * Joins the Blocks World and sends the current state as a perception event.
     */
    suspend fun join()
}

class BlocksWorldSkillsImpl(private val world: BlocksWorld, private val node: Node<*>) : BlocksWorldSkills {

    override suspend fun move(block: String, destination: String) {
        val destinationBlock = if (destination == "table") null else Block(destination)
        val state = world.move(Block(block), destinationBlock)
        node.publishEvent(BlocksWorldPerception(state))
    }

    override suspend fun join() {
        node.publishEvent(BlocksWorldPerception(world.state.value))
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
    previousBeliefs.filter { it.matchBelief(filterQuery) != null }.toSet(),
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

[`Agent.kt`](https://github.com/jakta-bdi/jakta/blob/main/examples/blocksworld/src/commonMain/kotlin/Agent.kt)
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
            fun state(list: List): Struct = Struct.of("state", list)
            fun state(list: Var): Struct = Struct.of("state", list)
            fun tower(list: List): Struct = Struct.of("tower", list)
            fun tower(list: Var): Struct = Struct.of("tower", list)
            fun clear(block: Atom): Struct = Struct.of("clear", block)
            fun clear(block: Var): Struct = Struct.of("clear", block)
            fun on(block: Term, support: Term): Struct = Struct.of("on", block, support)
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
`state([['A', 'B'], ['C', 'D', 'E'], ['F']])`: `A` on `B`, `C` on `D` on `E`, and `F` alone on the table
(blocks are named `A`, `B`, ..., so their atoms are quoted).
The UI builds it from the arrangement you drag in the goal area, with `goalOf` in `BlocksWorldAppState.kt`.

## 4. The plans

Each plan below is a `prologPlan { }` in the agent's `hasPlanLibrary { }`.

**Start.** Join the world — which produces the first perception — then pursue the desired state.
If that fails, a failure plan for the same goal gives up and stops the node:

```kotlin
adding.goal {
    matchingGoal { start }
} triggers {
    blocksWorld.join()
    agent.achieve(desiredWorldState)
}

failing.goal {
    matchingGoal { start }
} triggers {
    agent.print("I could not reach the goal, giving up.")
    node.terminateNode()
}
```

`join()` publishes the perception *before* `achieve` posts the new goal. Both go into the agent's single event queue,
so by the time the first `state` plan is selected, the agent already believes the current `on/2` facts
(see [Execution model](../explanation/execution-model.md)).

**Build the towers, one at a time.** `state/1` is decomposed recursively: build the first tower, then the rest.
Once no tower is left, the agent stops its node.

```kotlin
adding.goal {
    matchingGoal { state(emptyLogicList) }
} triggers {
    agent.print("Finished! Final state reached.")
    node.terminateNode()
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
    agent.print("Check if block ", X, " is clear")
    agent.achieve(goal { clear(X) })
    agent.print("Check if block ", Y, " is clear")
    agent.achieve(goal { clear(Y) })
    agent.print("Moving block ", X, " on ", Y)
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
    agent.print("Block ", X, " is clear.")
}

adding.goal {
    matchingGoal { clear(X) }
} onlyWhen {
    satisfies { tower(logicList(H, tail = T)) and member(X, T) }
} triggers {
    agent.print("Block ", X, " is not clear.")
    agent.print("Check if I can move ", H, " to clear ", X)
    agent.achieve(goal { clear(H) }) // TODO the Jason solution does not include this
    agent.print("Moving block ", H, " on ", table)
    blocksWorld.move(H.value(), table.value)
    agent.print(X, " should now be clear.")
    agent.achieve(goal { clear(X) })
}
```

If `X` is not clear, the guard finds a tower `[H | T]` with `X` somewhere below `H`,
binding `H` in the plan context. The plan clears `H`, moves it to the table, and then **re-posts** `clear(X)`:
if more blocks were above `X`, the same plan applies again, until the first plan's guard succeeds.

The guard does not guarantee that `H` is the top of its stack (any tower suffix matches), so the plan first
achieves `clear(H)` itself before moving it.

## 5. Launching the MAS from the UI

[`ui/BlocksWorldAppState.kt`](https://github.com/jakta-bdi/jakta/blob/main/examples/blocksworld/src/commonMain/kotlin/ui/BlocksWorldAppState.kt)
starts the MAS in a coroutine when you press **Play**:

```kotlin
fun play(scope: CoroutineScope) {
    if (isRunning) return
    val desired = goalOf(goal)
    val currentWorld = world
    AgentTrace.clear()
    isRunning = true
    val job = scope.launch(agentDispatcher, start = CoroutineStart.LAZY) {
        try {
            mas(NodeBuilders.baseNode()) {
                blocksWorldNode(currentWorld, desired)
            }.run(CoroutineNodeRunner(SharedMemoryNetwork()))
        } finally {
            // a cancelled run must not flag a newer one as finished
            if (agentJob === coroutineContext.job) isRunning = false
        }
    }
    agentJob = job
    job.start()
}
```

The MAS is an ordinary suspending computation: it ends when the agent stops its node, and shuffling the world
while it runs simply cancels its coroutine.
The agent and the UI share the same `BlocksWorld` instance: the screen observes its `state` to draw the stacks,
while the agent changes it through its skill. `AgentTrace` (from the shared `examples:ui-common` module) collects
what the agent prints and shows it next to the world.
The platform entry points only mount the app: a window on the desktop
([`desktopMain/kotlin/Main.kt`](https://github.com/jakta-bdi/jakta/blob/main/examples/blocksworld/src/desktopMain/kotlin/Main.kt))
and the page body in the browser
([`jsMain/kotlin/Main.kt`](https://github.com/jakta-bdi/jakta/blob/main/examples/blocksworld/src/jsMain/kotlin/Main.kt)).

## Things to try

- Drag blocks to change the goal or the world, or press **Shuffle**, then **Play** again.
- Swap the order of the two `on(X, Y)` plans: the unguarded one is now always selected first,
  and the agent tries to move blocks that are already in place.
- Add a `removing.belief { matchingBelief { on(X, Y) } }` plan that prints every `on/2` fact the agent stops believing.
