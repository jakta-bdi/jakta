---
sidebar_label: Vacuum World
sidebar_position: 3
---

# Vacuum World

import ExampleApp from '@site/src/components/ExampleApp/ExampleApp';

The [vacuum-world example](https://github.com/jakta-bdi/jakta/tree/main/examples/vacuum-world) is a robot that
keeps a map clean forever, in the style of the EIS Vacuum World. It senses its location, its direction and the
squares around it, and acts by moving forward, turning and cleaning.

Press *Start* and watch it work. Dust appears at a configurable rate, and you can click a square to drop or remove
some, also while the robot works.

It runs below, right in your browser: the *agent trace* on the right shows what the agents print while they reason.

<ExampleApp name="vacuum-world" title="Vacuum World" />

```bash
./gradlew :examples:vacuum-world:run                     # desktop
./gradlew :examples:vacuum-world:jsBrowserDevelopmentRun # browser
```

## Why it matters

Most examples run until their goal is achieved. This agent has no final state: it lives in a **dynamic
environment** that changes while it acts, and it only knows the world through the **body** it is embodied in.

### An agent in a dynamic environment

Dust appears by itself after each action, and the user adds or removes it at any time. The robot does not plan a
path in advance: its main goal, `vacuum`, is achieved by one step at a time, chosen from what it perceives *now*,
and pursued again at the end of every step.

```kotlin
fun step(guard: JaktaLogicProgrammingScope.() -> Struct, why: String?, action: suspend Agent.() -> Unit) =
    prologPlan {
        adding.goal { matchingGoal { vacuum } } onlyWhen { satisfies(guard) } triggers {
            why?.let { agent.print(it) }
            agent.action()
            agent.alsoAchieve(goal { vacuum })
        }
    }

step({ "square"(here, dust) }, "Dust here: cleaning") { clean() }
step({ "square"(forward, dust) }, "I see dust ahead") { forward() }
step({ "square"(left, dust) }, "I see dust on my left: turning") { turnLeft() }
step({ "square"(right, dust) }, "I see dust on my right: turning") { turnRight() }
// no dust in sight, but some remembered: go back to the nearest (see below)
step({ "best"(forward) }, null) { forward() }
step({ "best"(left) }, null) { turnLeft() }
step({ "best"(right) }, null) { turnRight() }
step({ Atom.of("true") }, "Dead end: turning around") { turnRight() }
```

The plans are tried in order, so their order is the robot's priority: clean, go towards dust it can see, go back to
dust it remembers, and otherwise explore. Because every step is selected again against fresh beliefs, dust dropped next to the robot is
picked up at the next step, without any special handling.

When no dust is in sight, the robot explores the free square it **visited least recently**. It remembers its visits
with `visited(X, Y, Time)` beliefs, updated by two plans whenever its `location` changes:

```kotlin
prologPlan {
    adding.belief { matchingBelief { "location"(X, Y) } } onlyWhen {
        satisfies { "time"(T) and "visited"(X, Y, O) }
    } triggers {
        agent.forget(belief { "visited"(X, Y, O) })
        agent.believe(belief { "visited"(X, Y, T) })
    }
}
```

Inference rules turn that memory into a decision. `target(S, X, Y)` maps a square relative to the robot (forward,
left, right) to a cell, and `best(S)` holds for the free square no other free square beats:

```kotlin
// last_visit(S, T): when the robot was last on square S, or -1 if never
+inferenceRule { "last_visit"(S, T) impliedBy ("target"(S, X, Y) and "visited"(X, Y, T)) }
+inferenceRule {
    "last_visit"(S, Integer.of(-1)) impliedBy ("target"(S, X, Y) and not("visited"(X, Y, `_`)))
}
// best(S): a free square that no other free square was visited less recently than
+inferenceRule {
    "best"(S) impliedBy (
        "free"(S) and "last_visit"(S, T) and
            not("free"(R) and "last_visit"(R, W) and Struct.of("<", W, T))
        )
}
```

The robot also **remembers the dust it sees** but cannot clean right away, for instance dust on its left while it
goes after the dust ahead. Its perception handler keeps a `dust_at(X, Y)` belief for every cell it saw dusty, and
forgets it when it sees the cell clean. When no dust is in sight but some is remembered, a plan goes back to the
nearest one before exploring again:

```kotlin
prologPlan {
    adding.goal { matchingGoal { vacuum } } onlyWhen {
        context.takeIf { beliefs.routeToKnownDust() != null }
    } triggers {
        val (step, dustCell) = checkNotNull(agent.beliefs.routeToKnownDust())
        agent.print("Going to the dust I saw at (${dustCell.x}, ${dustCell.y})")
        val facing = agent.beliefs.facing()
        when (step) {
            facing -> agent.forward()
            facing.left -> agent.turnLeft()
            else -> agent.turnRight()
        }
        agent.alsoAchieve(goal { vacuum })
    }
}
```

Here the paradigms meet again: the guard is plain Kotlin over the Prolog beliefs. `routeToKnownDust` searches the
shortest path to the nearest remembered dust through the cells the robot knows are free, those it visited and those
where it saw dust, and returns its first step; a breadth-first search is a few lines of Kotlin, and would be
much slower as a Prolog query on every step.

The robot never sees the whole map, yet these memories are enough to sweep it, and the
[tests](https://github.com/jakta-bdi/jakta/blob/main/examples/vacuum-world/src/commonTest/kotlin/VacuumRobotTest.kt)
check that it finds and cleans all the dust, and that it goes back to the dust it remembers before exploring.

### Embodiment

The environment and the robot are kept apart. `VacuumWorld` only holds what is out there: the map, the dust and the
time. The robot's position, its facing and how much it cleaned belong to its **body**:

```kotlin
data class RobotState(val position: Pos, val facing: Direction, val cleaned: Int = 0)

class VacuumBody(val world: VacuumWorld, start: Pos = ROBOT_START) {
    private val mutableState = MutableStateFlow(RobotState(start, Direction.EAST))
    val state: StateFlow<RobotState> = mutableState.asStateFlow()

    fun forward() = mutableState.update { robot ->
        val ahead = robot.cellOf(Square.FORWARD)
        if (world.state.value.contentAt(ahead) == Content.OBSTACLE) robot else robot.copy(position = ahead)
    }

    fun sense(): VacuumPerception {
        val robot = state.value
        val world = world.state.value
        val squares = Square.entries.associateWith { world.contentAt(robot.cellOf(it)) }
        return VacuumPerception(robot.position, robot.facing, world.time, squares)
    }
    // turnLeft(), turnRight(), clean()
}
```

The agent is embodied with `embodiedAs { body }`, and the UI draws the robot from the body's state.

**Actions go through the body.** `VacuumSkill` finds the body of the acting agent in the node, and each action
takes some time, lets time pass in the world, may let dust appear, and ends with the robot sensing again:

```kotlin
private val Agent.body get() = node.agents.getValue(id)

fun Agent.look() {
    val body = body
    node.publishEvent(body.sense()) { it === body }
}

private suspend fun Agent.act(action: VacuumBody.() -> Unit) {
    delay(stepTime())
    body.action()
    body.world.tick()
    body.world.maybeSpawnDust(dustChance())
    look()
}
```

**Perceptions are local.** The robot never receives the world: `sense()` reads its location, its direction, the time,
and what the four squares around it hold, and `publishEvent` delivers the reading only to the body that sensed it.
The agent turns each reading into `location/2`, `direction/1`, `time/1` and `square/2` beliefs, keeping only what
changed, updates its `dust_at/2` memory, and leaves its memory of `visited/3` cells alone.

## Learn more

- [Give agents a body](../how-to/custom-body.md), step by step
- [Bodies](../explanation/nodes.md#bodies) and [perceptions](../explanation/nodes.md#perceptions) in nodes
- [`achieve` vs `alsoAchieve`](../explanation/execution-model.md#achieve-vs-alsoachieve), used to loop on the
  `vacuum` goal
- [Prolog plans](../explanation/incarnations/prolog/prolog-plans.md) and inference rules in the
  [Prolog incarnation](../explanation/incarnations/prolog/index.md)
