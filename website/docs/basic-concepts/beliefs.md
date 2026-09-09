---
sidebar_position: 2
---

# Beliefs in JaKtA

Beliefs are a fundamental concept in the **Belief-Desire-Intention (BDI)** model and play a key role in defining an agent’s knowledge about the world. 
In JaKtA, beliefs represent **facts** and **rules** that an agent holds, and they can dynamically change as the agent interacts with its environment.

## Understanding Beliefs

JaKtA directly leverages (and exposes as an API) the logic programming toolkit for Kotlin [2P-Kt](https://github.com/tuProlog/2p-kt) and its internal [DSL for Prolog](https://pika-lab.gitlab.io/tuprolog/2p-in-kotlin/wiki/Developers%20Guide/kotlin-dsl/)

Beliefs influence an agent’s **decision-making** by triggering goals or activating plans when conditions change.

In JaKtA, Beliefs expose the **source** of their information, which can be:
- *self* source: If the information comes from the agent's internal knowledge
- *percept* source: If the information is obtained after perceiving changes that happen in the environment
- *other* source: Occurs in the case agents are exchanging message information, the source contains the sender of the message. 

## Defining Beliefs in JaKtA DSL

In JaKtA, beliefs can be defined within an agent’s configuration.

For instance, in the following, we define a belief base containing information about
paths in a graph, by means of logic facts, as well as logic rules for computing whether
some location X is reachable from another location Y:
```kt showLineNumbers
mas {
    agent("moon walker") {
    beliefs {
        fact { "path"("location1", "location2") }
        fact { "path"("location2", "location3") }                
        rule { "reachable"(X, Y) impliedBy "path"(X, Y) }
        rule { "reachable"(X, Z) impliedBy "path"(X, Y) and "reachable"(Y, Z) }
    }
}
```

Under the assumption that the graph represents some sort of map from the real
world, the above belief base can be exploited by the agent to reason about reachability
among any two locations in the map; in fact, through 2P-Kt, JaKtA fully supports
Prolog’s unification and resolution mechanisms.

If you need other info about the library, visit its [documentation](https://pika-lab.gitlab.io/tuprolog/2p-in-kotlin/wiki).

---

## Updating and Using Beliefs

Beliefs are not static; they can be updated during execution. 
Agents modify beliefs based on changes in the environment or after executing specific actions.

### Modifying Beliefs

```kt showLineNumbers
mas {
    agent("Drinker") {
        beliefs {
            fact { "holding"("beer") }
        }

        goals { achieve("eatSomething") }

        plans {
            +achieve("eatSomething") onlyIf { "holding"("beer").fromSelf } then {
                update("holding"("chips").fromSelf)
                achieve("eatSomething")
            }
            +achieve("eatSomething") onlyIf { "holding"("chips").fromSelf } then {
                execute("print"("I can finally eat!"))
                execute("stop")
            }
        }
    }
}.start()

```

### Adding Beliefs

```kt showLineNumbers
mas {
    agent("Drinker") {
        goals { achieve("drink") }

        plans {
            +achieve("drink") then {
                + "holding"("beer")
            }
        }
    }
}.start()

```

### Removing Beliefs

```kt showLineNumbers
mas {
    agent("Drinker") {
        beliefs {
            fact { "holding"("beer") }
        }
        goals { achieve("dropBeer") }
        plans {
            +achieve("dropBeer") then {
                - "holding"("beer").fromSelf
            }
        }
    }
}.start()

```

## Reacting to Belief Changes

Agents can define **event-driven behaviors** that trigger actions when a belief changes.
The events are triggered by the **addition** or the **removal** of a belief.

### Example: Reacting to a Belief Addition from percept source

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

### Example: Reacting to a Belief Addition from self source

```kt showLineNumbers
mas {
    agent("Drinker") {
        goals { achieve("drink") }

        plans {
            +achieve("drink") then {
                + "holding"("beer")
            }
            + "holding"("beer").fromSelf then {
                execute("print"("Finally I can have a beer!"))
            }
        }
    }
}.start()
```

### Example: Reacting to a Belief Removal from self source

```kt showLineNumbers
mas {
    agent("Drinker") {
        beliefs {
            fact { "holding"("beer") }
        }
        goals { achieve("dropBeer") }
        plans {
            +achieve("dropBeer") then {
                - "holding"("beer").fromSelf
            }
            - "holding"("beer").fromSelf then {
                execute("print"("Ops! I dropped my beer!"))
            }
        }
    }
}.start()

```
