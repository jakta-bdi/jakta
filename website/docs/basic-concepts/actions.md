---
sidebar_position: 5
---

# Actions in JaKtA

Actions in JaKtA represent the **concrete operations** that an agent performs to achieve its goals and execute plans. Actions can be used to modify beliefs, interact with the environment, or communicate with other agents.

There are two types of actions in JaKtA, depending on the context they refer to:
- **Internal Actions**: offer an API dedicated to inspect and __modify__ the agent's state.
- **External Actions**: offer an API dedicated to inspect and __modify__ the environment's state.

JaKtA finely controls the modifications to the state of agents and environment using a strict API.
In this way, users cannot modify the state by mistake, and need to expressively declare the modifications they are performing.
These changes are referred to as **Side Effect**s.

## Internal Actions

### Definition

The definition of an internal action happens inside the agent's body:
```kt showLineNumbers
agent("name"){
  actions { 
    action("print", 2) {
        println("[${request.agent.name}] ${request.arguments.joinToString( " " )}")
    }
  }
  plans {
    +achieve("greet") then { "print"("Hello World!") }
  }
}
```

Since their definition can be reused among multiple agents 
(and also because their implementation can be really complex),
they can be declared as external objects and referred to, like the following:
```kt showLineNumbers
object Print : AbstractInternalAction("print", 2) {
  override fun action(request: InternalRequest) {
    println("[" + request.agent.name + "] " + request.arguments.joinToString(" "))
  }
}

agent("name"){
  actions { action(Print) }
  plans {
    +achieve("greet") then { "print"("Hello World!") }
  }
}
```

### Side Effects

The available side effects that can be used to alter the agent state are represented in the following example:
```kt showLineNumbers
agent("Agent name") {
  actions { // internal
    action("Action name", <Arity>) {
      addBelief(<Belief>)          
      removeBelief(<Belief>)
      addIntention(<Intention>)
      removeIntention(<Intention>)
      addEvent(<Event>)
      removeEvent(<Event>)
      addPlan(<Plan>)
      removePlan(<Plan>)
      stopAgent()
      sleepAgent(<Time in milliseconds>)
      pauseAgent()
    }
  }
}
```

### Standard library

| **Action**  | **# Args** |**Description** | **Example**  | **Effect/Output**  |
|-------------| --- |----------------|--------------|--------------------|
| `print` | 2 | Concatenates arguments into a string, separated by spaces, and prints it with the agent’s name | `print("Hello,", "world!")`, `print("Number is", X)` | `[AgentName] Hello, world!`, `[AgentName] Number is 7` |
| `fail`     | 0 | Causes the current plan to fail, thus inducing an alternative plan selection       | `fail()`                     | The agent's current plan is terminated.                 |
| `stop`     | 0 | Immediately stops the agent's execution, preventing further actions.        | `stop()`                     | The agent halts and stops processing.                   |
| `pause`    | 1 | Pauses execution for the specified time (in milliseconds).                  | `pause(1000)`                | The agent pauses execution for 1 second.                |


> **Note**:
> An extension of the standard library is currently work in progress

## External Actions

### Definition
```kt showLineNumbers
object Send : AbstractExternalAction("send", 3) {
  override fun action(request: ExternalRequest) {
    val receiver = request.argument(0)
    val ilf = IllocutionaryForce.parse(request.argument(1))
    val content = request.argument(2)
    val sender = request.sender
    sendMessage(receiver, Message(sender, ilf, content))
  }
}

agent("Agent Name"){
    actions { action(Send) }
        plans {
            +achieve("greet_twice") then { 
                execute("send"("receiver", "tell", "hello!"))
            }
        }
    }
}
```

### Side Effects

The side effects available to alter the environment's state are in the following example:
```kt showLineNumbers
environment {
  actions {
    action("Action Name", <Arity>) {
      addAgent(<Agent>)
      removeAgent(<AgentId>)
      addData(<Key>, <Value>)
      removeData(<Key>)
      updateData(<Key>, <Value>)
      sendMessage(<AgentId>, <Payload>) 
      broadcastMessage(<Payload>)
    }
  }
}
```

### Standard Library

There is not a standard library currently available for external actions.