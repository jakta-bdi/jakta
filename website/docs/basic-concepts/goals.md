---
sidebar_position: 3
---

# Goals in JaKtA

Goals represent the objectives that an agent aims to achieve. 
In JaKtA, goals drive the agent’s behavior by activating plans that execute actions.

## Understanding Goals

Goals can indicate either something that the agent wants to **achieve** by finding
an appropriate plan, or something that it wants to **test** (discover), prioritising the
consultation of the knowledge base over the execution of plans.
Similarly to Jason, JaKtA supports the definition of agents’ initial goals by means of the goals block.
Further goals may arise during the execution of the MAS, 
thus creating a hierarchy of plans to execute in order to fulfill the initial goal. 

## Defining initial Goals in JaKtA DSL

In JaKtA, goals are defined as part of an agent’s configuration:

```kotlin
import io.github.jakta.bdi.dsl.agentSystem
agent("Robot") {
    goals { 
        test("batteryLevel")
        achieve("chargeBattery") 
    }
}
```

In this example, the `Robot` agent tests the current `batteryLevel`, and tries to achieve the goal of charging its battery.

## Adding Goals Dynamically

An agent can **adopt new goals** at runtime based on its current beliefs and environment.

#### Example: Adding a Goal Dynamically

```kotlin

mas {
    agent("Explorer") {
        beliefs {
            fact{ "batteryLevel"(80) }
        }
        goals { achieve("exploreTerrain") }

        plans {
            + achieve("exploreTerrain") onlyIf {
                "batteryLevel"(X).fromSelf and (X greaterThanOrEqualsTo 30) and (Y `is` (X - 10))
            } then {
                execute("print"("I'm exploring... Battery level is now", Y))
                update("batteryLevel"(Y).fromSelf) // Simulating battery drain
                achieve("exploreTerrain")
            }
            + achieve("exploreTerrain") onlyIf { "batteryLevel"(X).fromSelf and (X lowerThan  30) } then {
                execute("print"("Charging battery..."))
                update("batteryLevel"(100).fromSelf)
                execute("stop")
            }
        }
    }
}.start()
```

#### Expected Output:
```
[Explorer] I'm exploring... Battery level is now 70
[Explorer] I'm exploring... Battery level is now 60
[Explorer] I'm exploring... Battery level is now 50
[Explorer] I'm exploring... Battery level is now 40
[Explorer] I'm exploring... Battery level is now 30
[Explorer] I'm exploring... Battery level is now 20
[Explorer] Charging battery...
```
