---
sidebar_position: 9
---

# Simulate a MAS with Alchemist

The `alchemist-jakta-incarnation` module runs JaKtA nodes as devices of an
[Alchemist](https://alchemistsimulator.github.io/) simulation: each Alchemist node hosts a JaKtA node,
agents run in simulated time, and messages travel over Alchemist's network model.
It requires Java 17+.

## Dependencies

```kotlin
dependencies {
    implementation("it.unibo.jakta:alchemist-jakta-incarnation:<VERSION>")
    implementation("it.unibo.jakta:jakta-core:<VERSION>")
}
```

## Define the device

An entry point is an extension function on `JaktaForAlchemistRuntime` that builds a device with
`device(NodeBuilders.alchemistNode()) { node { ... } }`. Inside, you define agents as usual:

```kotlin
@file:JvmName("PingPong")

package my.simulation

fun <P : Position<P>> JaktaForAlchemistRuntime<P>.entrypoint() = device(NodeBuilders.alchemistNode()) {
    node {
        context(MessagingSkill(node)) {
            agent(BaseAgentID("Alice")) {
                embodiedAs { Any() }
                hasInitialGoals { !"start" }
                hasPlanLibrary {
                    adding.goal {
                        takeIf { it == "start" }
                    } triggers {
                        agent.print("Time: ${alchemistEnvironment.simulation.time}")
                        delay(5000.milliseconds) // simulated time
                        agent.print("Time after delay: ${alchemistEnvironment.simulation.time}")
                    }
                }
            }
        }
    }
}
```

`alchemistEnvironment` gives access to the Alchemist environment (and simulation) from the runtime,
and `delay` advances in simulated time.

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
val simulation = LoadAlchemist.from(yamlUrl).getDefault<Any, Nothing>()
simulation.play()
simulation.run()
```

Complete, runnable scenarios — two agents on the same device, and two agents on two devices communicating over the
network — are in the module tests:
[`alchemist-jakta-incarnation/src/test`](https://github.com/jakta-bdi/jakta/tree/main/alchemist-jakta-incarnation/src/test).
