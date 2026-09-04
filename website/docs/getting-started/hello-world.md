---
sidebar_position: 3
---

# Writing a simple agent

## Define an Agent with Beliefs, Goals, and Plans

In JaKtA, an agent consists of:

* **Beliefs**: Information about the world.
* **Goals**: States or objectives the agents wants to achieve.
* **Plans**: Sequences of actions that allow the agent to fulfill goals.

## Hello World written in JaKtA

This is a `Hello World` agent written with JaKtA syntax, printing the string "Hello, World!" to the console. 

Create a file in the source folder of your gradle project (i.e. `src/main/kotlin`) named `HelloAgent.kt`, containing:

```kt showLineNumbers
fun main() {
    mas {
        agent("myAgent") {
            goals {
                achieve("sayHello")
            }
            plans {
                +achieve("sayHello") then {
                    execute("print"("Hello, World!"))
                }
            }
        }
    }.start()
}
```

If the agent's elements are unfamiliar for you, continue the read with `Basic Concepts`.

## Run the system

Now, run your program using:
```bash
./gradlew run
```

You should see the following output:
```
[myAgent] Hello, World!
```

For more examples, check out [JaKtA Examples](https://github.com/jakta-bdi/jakta-examples).
