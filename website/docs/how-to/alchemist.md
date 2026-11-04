---
sidebar_position: 9
---

# Simulate a MAS with Alchemist

The `alchemist-jakta-incarnation` module runs JaKtA nodes as devices of an
[Alchemist](https://alchemistsimulator.github.io/) simulation: each Alchemist node hosts a JaKtA node,
agents run in simulated time, and messages travel over Alchemist's network model.
It requires Java 17+.
The complete program is the [`alchemist-ping-pong`](https://github.com/jakta-bdi/jakta/tree/main/examples/alchemist-ping-pong)
example, where Alice and Bob play ping-pong from two devices; run it with `./gradlew :examples:alchemist-ping-pong:run`
to watch it in Alchemist's viewer.

## Dependencies

```kotlin
dependencies {
    implementation("it.unibo.jakta:alchemist-jakta-incarnation:<VERSION>")
    implementation("it.unibo.jakta:jakta-core:<VERSION>")
}
```

## Define the device

An entry point is an extension function on `JaktaForAlchemistRuntime` that builds a device with
`device(NodeBuilders.alchemistNode()) { node { ... } }`; agents are defined inside as usual.
`device` and `alchemistNode` come from `it.unibo.jakta.dsl`, `JaktaForAlchemistRuntime` from
`it.unibo.alchemist.jakta.properties`.

```kotlin
@file:JvmName("PingPong")

package my.simulation

fun <P : Position<P>> JaktaForAlchemistRuntime<P>.entrypoint() = device(NodeBuilders.alchemistNode()) {
    node {
        context(MessagingSkill(node)) {
            agent(BaseAgentID("Alice")) {
                // as in any other node
            }
        }
    }
}
```

## Use simulated time

`delay` advances in simulated time, and `alchemistEnvironment` gives access to the Alchemist environment
and simulation:

```kotlin
} triggers {
    agent.print("Time: ${alchemistEnvironment.simulation.time}")
    delay(5000.milliseconds)
    agent.print("Time after delay: ${alchemistEnvironment.simulation.time}")
}
```

## Configure the simulation

Select the `jakta` incarnation in the Alchemist YAML file, and point each program to the entry point
(the JVM class name set by `@file:JvmName`, followed by the function name):

```yaml
incarnation: jakta

network-model:
  type: ConnectWithinDistance
  parameters: [ 5 ]

deployments:
  - type: Point
    parameters: [ 2, 2 ]
    programs:
      - program: my.simulation.PingPong.entrypoint
        time-distribution: 1
```

Then run it as any Alchemist simulation, e.g. programmatically:

```kotlin
val simulation = LoadAlchemist.from(yamlUrl).getDefault<Any, Euclidean2DPosition>()
simulation.play()
simulation.run()
```

To watch it, add Alchemist's viewer (`it.unibo.alchemist:alchemist-swingui`, at Alchemist's version) and attach it to
the simulation before running it:

```kotlin
val viewer = SwingGUI<Any, Euclidean2DPosition>(simulation.environment)
viewer.isRealTime = true
simulation.addOutputMonitor(viewer)
```
