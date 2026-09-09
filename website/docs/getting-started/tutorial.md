---
sidebar_position: 5
---

# Intermediate tutorial

This section covers slightly more advanced concepts in JaKtA, 
helping you build more sophisticated BDI agents. 

You will learn how to:
- Implement agent communication.
- Use event-driven behaviors.

## Prerequisites

Before proceeding, ensure you are familiar with:

- Basic JaKtA agent creation (covered [here](hello-world.md)).
- Kotlin programming fundamentals.


## 1. Agent Communication

Agents in JaKtA can communicate **directly** using messages. 
The message exchange implementation is not provided by default from JaKtA by choice:
users can inject their preferred message exchange frameworks in the Multi-Agent Systems.


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

### Expected behavior

The expected output is something similar to:
```
[receiver] Received message:  Hello!
[sender] message sent!
```

### Deeper explanation

More information about communication in JaKtA in [explanation](../explanation/communication.md).

## 2. Event-Driven Behaviors

JaKtA allows agents to react to dynamic environmental changes using plan triggers.

```kt showLineNumbers
fun main() {
     mas {
        environment(
            // Custom implementation of the environment which models temperature
            TemperatureEnvironment()
        )
        agent("reactiveAgent") {
            plans {
                +"temperature"(X).fromPercept onlyIf { X greaterThan 30 } then {
                    execute("print"("Warning: High temperature detected!"))
                }

                +"temperature"(X).fromPercept then {
                    execute("print"("Temperature under the threshold"))
                }
            }
        }
    }.start()
}
```

### Expected Behavior

If the `temperature` belief changes to a value above 30, the console should print:
```
[reactiveAgent] Warning: High temperature detected!
```

### Deeper explanation

More information about environments in JaKtA in [explanation](../explanation/environment.md)

## Troubleshoot

The MAS you defined is not behaving as expected?
You can try using Kotlin debugger to inspect the runtime value of variables to identify the mistake.

However, Kotlin debugger may be helpful only inside actions body.
This happens because the DSL is executable Kotlin code,
thus if inspected, the monitor would show you the DSL classes instead of the agency ones.

We acknowledge this behavior mey be surprising for non-expert Kotlin users,
and in future we plan to release an **inspector** that may help you in the development process.

In the meanwhile,
you can gather information about what's happening inside agents' lifecycle by declaring `debugEnabled` variable:
```kt
mas{ ... }.start(debugEnabled = true) 
```

Next Steps

Experiment with different message types and performatives.

Implement complex decision-making strategies.

Integrate external APIs for more dynamic agent behaviors.

For more examples, refer to JaKtA Examples.

