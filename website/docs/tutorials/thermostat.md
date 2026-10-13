---
sidebar_position: 6
---

# Advanced: Thermostat

import ExampleApp from '@site/src/components/ExampleApp/ExampleApp';

This tutorial builds a thermostat agent that keeps a room at a target temperature while the room keeps changing:
it loses heat towards the outside, and you can open the window (drag the room temperature), change the target, or
make the day colder or warmer at any time. Along the way it shows how to:

- model an **external environment** in plain Kotlin, with its own dynamics;
- let agents **perceive** it, turning perceptions into beliefs;
- let agents **act** on it through a skill;
- choose between plans with **guards** over the agent's beliefs.

Like the previous tutorials it only needs `jakta-core`: beliefs are plain Kotlin types, no Prolog involved.
The complete program is the [`thermostat`](https://github.com/jakta-bdi/jakta/tree/main/examples/thermostat) example,
and it runs right here in your browser:

<ExampleApp name="thermostat" title="Thermostat" />

```bash
./gradlew :examples:thermostat:run                     # desktop
./gradlew :examples:thermostat:jsBrowserDevelopmentRun # browser
```

```mermaid
flowchart LR
    room[(Room)] -- "TemperatureReading, TargetSet, ModeChanged" --> handler[handlesPerceptionEvents]
    handler -- "AgentUpdate.Belief" --> beliefs[(Beliefs)]
    beliefs -- "Temperature added" --> plans[Plans]
    plans -- "hvac.heat() / cool() / off()" --> room
    you((You)) -- sliders --> room
```

## 1. Model the external environment

JaKtA has no environment class to extend: the external environment is your own Kotlin model. The room has a temperature,
the target set on the thermostat, the temperature outside, and a heating and cooling system:

<details>
<summary>Imports of <code>Room.kt</code></summary>

```kotlin
import it.unibo.jakta.event.AgentEvent.External.Perception
import it.unibo.jakta.node.Node
import kotlin.time.Duration
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
```

</details>

```kotlin
private const val INSULATION_LOSS = 0.02
private const val POWER = 0.4

enum class Mode(val power: Double) {
    OFF(0.0),
    HEATING(POWER),
    COOLING(-POWER),
}

data class RoomState(val temperature: Double, val target: Double, val outside: Double, val mode: Mode = Mode.OFF)

class Room(initial: RoomState) {
    private val mutableState = MutableStateFlow(initial)
    val state: StateFlow<RoomState> = mutableState.asStateFlow()

    fun setTemperature(degrees: Double) = mutableState.update { it.copy(temperature = degrees) }
    fun setTarget(degrees: Double) = mutableState.update { it.copy(target = degrees) }
    fun setOutside(degrees: Double) = mutableState.update { it.copy(outside = degrees) }
    fun switch(mode: Mode) = mutableState.update { it.copy(mode = mode) }

    fun step() = mutableState.update {
        it.copy(temperature = it.temperature + (it.outside - it.temperature) * INSULATION_LOSS + it.mode.power)
    }
}
```

At each `step` the room drifts towards the outside temperature, and the system adds or removes some heat.
The state is a `StateFlow`, so the UI can observe it, and the sliders call the setters: the room changes whether
the agent likes it or not.

## 2. Perceive it

A **perception** is any class implementing `AgentEvent.External.Perception`. The room has three devices, and each
one streams what it senses as a perception of its own, as real sensors would: the thermometer reads the temperature,
the dial reports the target it is set to, and the heating and cooling system reports when it switches.

```kotlin
data class TemperatureReading(val degrees: Double) : Perception

data class TargetSet(val degrees: Double) : Perception

data class ModeChanged(val mode: Mode) : Perception

suspend fun Room.simulate(node: Node<*>, stepTime: Duration) {
    var last: RoomState? = null
    while (true) {
        delay(stepTime)
        step()
        val now = state.value
        if (now.target != last?.target) node.publishEvent(TargetSet(now.target))
        if (now.mode != last?.mode) node.publishEvent(ModeChanged(now.mode))
        node.publishEvent(TemperatureReading(now.temperature))
        last = now
    }
}
```

`simulate` lets time pass and publishes the perceptions on the node: every agent of the node receives them.
The thermometer reads the temperature at every step, while the dial and the system speak only when they change,
and once at the start. The agent never polls the room; the room tells it what changed.

## 3. Act on it

What an agent can do on the external environment is a **skill**: here, switching the heating and cooling system.

```kotlin
class Hvac(private val room: Room) {
    fun heat() = room.switch(Mode.HEATING)
    fun cool() = room.switch(Mode.COOLING)
    fun off() = room.switch(Mode.OFF)
}
```

A skill is an ordinary object. The [ping-pong tutorial](./ping-pong.md) gave agents `MessagingSkill` through a
`context(...)` block; a skill used by a single agent can also simply be passed to it, as below.
See [Skills](../explanation/basic-concepts/skills.md).

:::tip[From an emulated room to real devices]
The room here is an emulated external system, so the tutorial runs anywhere, even in the browser. The same structure connects agents
to any external system, such as real sensors and actuators: a loop reading a real thermometer publishes the
`TemperatureReading`s instead of `simulate`, and `Hvac` switches a real heating and cooling system instead of changing `Room`.
The agent does not change: it still perceives readings and acts through the skill.
:::

## 4. Turn perceptions into beliefs

The agent decides what a perception means to it. Whatever the device, it updates a single belief about the room,
an object of your own type:

<details>
<summary>Imports of <code>Thermostat.kt</code></summary>

```kotlin
import it.unibo.jakta.agent.BaseAgentID
import it.unibo.jakta.dsl.agent
import it.unibo.jakta.dsl.mas
import it.unibo.jakta.dsl.node
import it.unibo.jakta.dsl.node.NodeBuilders
import it.unibo.jakta.dsl.plan.triggers
import it.unibo.jakta.event.AgentUpdate
import it.unibo.jakta.node.CoroutineNodeRunner
import it.unibo.jakta.node.SharedMemoryNetwork
import kotlin.math.roundToInt
import kotlin.time.Duration
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
```

</details>

```kotlin
private const val TOLERANCE = 0.5

data class RoomBelief(val temperature: Double, val target: Double, val mode: Mode)

private fun Double.roundedToTenths() = (this * 10).roundToInt() / 10.0
```

The agent starts believing it knows nothing about the temperature yet. `handlesPerceptionEvents` then copies the
current belief with the field each perception tells about, and returns an `AgentUpdate.Belief(additions, removals)`
that replaces the old belief with the new one:

```kotlin
fun thermostat(hvac: Hvac) = agent<RoomBelief, String, Any>(BaseAgentID("thermostat")) {
    embodiedAs { Any() }
    believes {
        // no temperature read yet: NaN fails every comparison, so no plan applies until the first reading
        +RoomBelief(temperature = Double.NaN, target = 20.0, mode = Mode.OFF)
    }
    handlesPerceptionEvents { perception ->
        val room = beliefs.single()
        val updated = when (perception) {
            is TemperatureReading -> room.copy(temperature = perception.degrees.roundedToTenths())
            is TargetSet -> room.copy(target = perception.degrees)
            is ModeChanged -> room.copy(mode = perception.mode)
            else -> room
        }
        if (updated == room) null else AgentUpdate.Belief(setOf(updated), setOf(room))
    }
    // the plans, below
}
```

The agent holds one belief about the room at a time, and each change generates one *belief addition* event, for the
new `RoomBelief`. A perception that changes nothing is ignored: rounding to tenths of a degree keeps the agent from
reacting to differences no thermostat would display. Until the first reading the temperature is `NaN`, which fails
every comparison in the plans below, so the agent does nothing before it knows the temperature.

## 5. Choose what to do with guards

The agent has no goals: it is purely reactive. Every new `RoomBelief` triggers three plans, and their
**guards** — `onlyWhen { }`, which sees the trigger's `context` and the agent's `beliefs` — decide which one applies:

```kotlin
hasPlanLibrary {
    adding.belief {
        this
    } onlyWhen {
        context.takeIf { it.temperature < it.target - TOLERANCE && it.mode != Mode.HEATING }
    } triggers {
        agent.print("It's ${context.temperature}°C, heating up to ${context.target}°C")
        hvac.heat()
    }
    adding.belief {
        this
    } onlyWhen {
        context.takeIf { it.temperature > it.target + TOLERANCE && it.mode != Mode.COOLING }
    } triggers {
        agent.print("It's ${context.temperature}°C, cooling down to ${context.target}°C")
        hvac.cool()
    }
    adding.belief {
        this
    } onlyWhen {
        val reached = when (context.mode) {
            Mode.HEATING -> context.temperature >= context.target
            Mode.COOLING -> context.temperature <= context.target
            Mode.OFF -> false
        }
        context.takeIf { reached }
    } triggers {
        agent.print("It's ${context.temperature}°C, switching off")
        hvac.off()
    }
}
```

- The **trigger** `this` makes a plan relevant to every new belief about the room, and passes it on as the plan's
  `context`: changing the target alone is enough to make the thermostat react.
- The **guard** returns the context when the plan is applicable in the current situation, or `null` otherwise.
  When no guard holds, nothing happens: the temperature is fine, or the system is already doing the right thing.
- Heating starts below `target - TOLERANCE` but stops only at the target, so the thermostat does not switch on and
  off at every tenth of a degree.

## 6. Run it

The room and the agent run side by side: `simulate` drives the room, the MAS runs the agent.

```kotlin
suspend fun runThermostat(room: Room, stepTime: Duration): Unit = coroutineScope {
    val home = node(NodeBuilders.baseNode()) {
        withAgents(thermostat(Hvac(room)))
    }
    launch { room.simulate(home, stepTime) }
    mas(NodeBuilders.baseNode()) {
        withNodes(home)
    }.run(CoroutineNodeRunner(SharedMemoryNetwork()))
}
```

The node is built on its own with `node(...)`, so that `simulate` can publish perceptions on it, and then added to
the MAS with `withNodes`. Neither the room nor the agent ever stops: cancelling the coroutine stops both.

The UI ([`ui/ThermostatApp.kt`](https://github.com/jakta-bdi/jakta/blob/main/examples/thermostat/src/commonMain/kotlin/ui/ThermostatApp.kt))
starts it in a `LaunchedEffect` when it is shown, draws the room from `room.state`, and adds an *agent trace* of
what the thermostat prints.

## Things to try

- Drag the room temperature below the target: the agent starts heating; above it, cooling.
- Set the outside to -10 °C: the heating cannot keep up, and the agent keeps heating at full power.
- Add a `Mode.ECO` with less power, and a plan that uses it when the temperature is only slightly low.
- Add a second room with its own thermostat on the same node: both receive every perception, so give the
  perceptions a room name and let each agent ignore the other room's.

## Going further

- [Connect agents to an external environment](../how-to/connect-environment.md) and [Give agents a body](../how-to/custom-body.md)
  collect the patterns of this tutorial.
- The [showcase](../showcase/vacuum-world.md) runs larger agents written with the
  [Prolog incarnation](../explanation/incarnations/prolog/index.md), where beliefs are logic terms and plans
  are selected by unification.
