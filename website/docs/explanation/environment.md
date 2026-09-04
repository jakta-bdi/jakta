# Environment in JaKtA

The environment in JaKtA provides a shared space where agents can interact, observe changes, and influence each other's behavior. 
This feature is currently under development and will offer more advanced functionalities in the future.

The environment is not directly modifiable by agents at runtime.
This choice is taken to finely control the environment state, thus guaranteeing that its state is always consistent among agents that shares it.

## 1. Environment Features

Currently, JaKtA environment supports:
- Shared properties that agents can read (as perceptions) and modify (through external actions).
- Event-based triggers for detecting changes in the environment.
- Custom extensions to define specialized behavior (e.g., stigmergy).

Some future enhancements include:
- Physical features (e.g., positioning)
- Holarchies
- ...

---

## 2. Using the Environment in JaKtA DSL

### 2.1 Default Environment Implementation

Default environment implementation doesn't contain any action implementation.
Initially, this decision was made by choice for sharing a general instance for the environment that users could customize with their specific needs.
However, in the future we plan to provide a wider selection to ease the MAS definition and ease the customization process for developers. 

#### Example: Default Environment
Omitting the environment in the DSL definitions means that you are using the default implementation.
This implies that NO external actions can be used in this MAS.

```kt showLineNumbers

fun main() {
    mas {
        agent("myAgent") {
            goals { achieve("sayHello") }
            plans {
                +achieve("sayHello") then {
                    execute("print"("Hello, World!"))
                }
            }
        }
    }.start()
}
```

#### Example: Extension of the default environment

The default environment implementation allows to define custom external actions using JaKtA DSL.

```kt showLineNumbers
mas {
    environment{
        actions {
            action("send", 2) {
                val receiver: String = argument<Atom>(0).value
                val payload: Struct = argument(1)
                sendMessage(receiver, Message(this.sender, Tell, payload))
            }
        }
    }

    agent("sender") {
        goals { achieve("sendMessage") }
        plans {
            + achieve("sendMessage") then {
                execute("send"("receiver", "handleMessage"("Hello!")))
                execute("print"("message sent!"))
            }
        }
    }

    agent("receiver") { ... } 
}.start()
```

---

### 2.2 Custom Environment Implementation

Developers can extend the environment to define custom behavior beyond simple property management.

#### Example: Custom Environment

```kt showLineNumbers
class TemperatureEnvironment: EnvironmentImpl(externalActions = emptyMap(), perception = Perception.empty()) {
    override fun percept(): BeliefBase =
        BeliefBase.of(Belief.fromPerceptSource(Struct.of("temperature", Numeric.of(15))))

    override fun copy(
        agentIDs: Map<String, AgentID>,
        externalActions: Map<String, ExternalAction>,
        messageBoxes: Map<AgentID, MessageQueue>,
        perception: Perception,
        data: Map<String, Any>
    ): TemperatureEnvironment = TemperatureEnvironment()
}

mas {
    environment(TemperatureEnvironment())
    agent("reactiveAgent") {
        plans {
            +"temperature"(X).fromPercept onlyIf { X greaterThan 30 } then {
                execute("print"("Warning: High temperature detected!"))
            }
        }
    }
}.start()
```

#### Example: Custom Environment + extension

```kt showLineNumbers
class StigmergyEnvironment(
    agentIDs: Map<String, AgentID> = emptyMap(),
    externalActions: Map<String, ExternalAction> = emptyMap(),
    messageBoxes: Map<AgentID, MessageQueue> = emptyMap(),
    perception: Perception = Perception.empty(),
    data: Map<String, Any> = emptyMap(),
): EnvironmentImpl(externalActions, agentIDs, messageBoxes, perception, data) {
    override fun percept(): BeliefBase {
        return BeliefBase.of(
            data.map { Belief.fromPerceptSource(Struct.of(it.key, Atom.of(it.value.toString()))) }
        )
    }

    override fun copy(
        agentIDs: Map<String, AgentID>,
        externalActions: Map<String, ExternalAction>,
        messageBoxes: Map<AgentID, MessageQueue>,
        perception: Perception,
        data: Map<String, Any>
    ): StigmergyEnvironment = StigmergyEnvironment(agentIDs,externalActions, messageBoxes, perception, data)
}
```

```kt showLineNumbers 
mas {
    environment{
        from(StigmergyEnvironment())
        actions {
            action("leaveMarker", 1) {
                updateData("marker" to argument<Atom>(0))
            }
        }
    }

    agent("Worker") {
        goals { achieve("leaveMarker") }

        plans {
            + achieve("leaveMarker") then {
                execute("leaveMarker"("$name was here!"))
                execute("print"("placed marker in environment."))
            }
        }
    }

    agent("Follower") {
        plans {
            + "marker"(X).fromPercept then {
                execute("print"("Perceived marker:", X))
                execute("stop")
            }
        }
    }
}.start()

```

## 3. Summary

| Implementation Type | Description | 
|---------------------|-------------|
| **Basic Environment** | Agents cannot perfom any mutation of the environment state |
| **Basic Environment + Actions** | Agents can read and modify shared properties in the environment, by using external actions. | 
| **Custom Environment** | Specialized logic that extends the environment. | 
| **Custom Environment + Actions** | Agents actions can be defined for mutating the custom environment's state. | 

As JaKtA's environment module evolves, these capabilities will be expanded to support richer agent interactions and more complex simulations.

For additional examples, visit [JaKtA Examples](https://github.com/jakta-bdi/jakta-examples).

