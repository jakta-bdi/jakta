# Communication in JaKtA

Agents in JaKtA communicate in different ways to coordinate and share information. This document covers two primary communication mechanisms:

1. **Direct Communication** – Using messages and beliefs.
2. **Stigmergy** – Indirect coordination, which happens through modifications in the environment state.


## 1. Direct Communication

Direct communication involves agents exchanging messages explicitly or sharing beliefs in a common knowledge base.

Agents can send and receive structured messages using **performatives**.
The available performatives in JaKtA base implementation are `Tell` and `Achieve`:
  - `Tell`: shares the message as an information. This means that the receiver will see it as a belief.
  - `Achieve`: shares the message as an instruction. The receiver will see it as a goal to pursue.

### Example: `Tell` performative

```kt showLineNumbers
fun main() {
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

        agent("receiver") {
            plans {
                + "handleMessage"("source"(S), X) then {
                    execute("print"("Received message: ", X))
                    execute("stop")
                }
            }
        }
    }.start()
}
```

The expected output is:
```
[receiver] Received message:  Hello!
[sender] message sent!
```

> **Important**: 
> 
> Using messages, the agent knows who is the **source** of the information, meaning that I can inspect the name of the agent communicating with me.
> This does not happen if I communicate using the `Achieve` performative and stigmergy.


### Example: `Achieve` performative
```kt showLineNumbers
fun main() {
     mas {
        environment{
            actions {
                action("send", 2) {
                    val receiver: String = argument<Atom>(0).value
                    val payload: Struct = argument(1)
                    sendMessage(receiver, Message(this.sender, Achieve, payload))
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

        agent("receiver") {
            plans {
                + achieve("handleMessage"(X)) then {
                    execute("print"("Received message: ", X))
                    execute("stop")
                }
            }
        }
    }.start()
}
```

The expected output is:
```
[receiver] Received message:  Hello!
[sender] message sent!
```

## 2. Indirect Communication

Stigmergy allows agents to leave traces in the environment, influencing other agents' behaviors without direct interaction.

### Example: Using an Environmental Marker

In this example, an agent leaves a "marker" in the environment, which another agent detects and reacts to.

```kt showLineNumbers
fun main() {
   mas {
        environment{
            from(
                // Environment that manages the markers
                StigmergyEnvironment()
            )
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
}
```

The expected outcome from the execution of this example is:
```
[Follower] Perceived marker: 'Worker was here!'
[Worker] placed marker in environment.
```

More information about custom environments [here](environment.md).

## Summary

| Communication Type | Description | Example |
|--------------------|-------------|---------|
| **Direct (Shared Goals)** | Agents exchange explicit goals to achieve. The sender of the message is unknown with this communication option. | `Achieve` performative example |
| **Direct (Shared Beliefs)** | Agents communicate using beliefs, triggering plans. With this communication option the agent knows the source of the message. | `Tell` performative example |
| **Stigmergy** | Agents modify the environment to influence others. | Environmental Marker example |

By leveraging these mechanisms, you can design complex, coordinated multi-agent systems in JaKtA.

For more examples, refer to [JaKtA Examples](https://github.com/jakta-bdi/jakta-examples).

