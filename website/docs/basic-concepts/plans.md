---
sidebar_position: 4
---

# Plans in JaKtA

Plans in JaKtA define **how** an agent achieves its goals by executing a sequence of actions. Plans react to events, such as new goals to achieve or changes in beliefs.

## Understanding Plans

Inheriting the successful model of Jason, in JaKtA, plans are composed of a **triggering event** deciding whether the plan is relevant, an optional **context** restricting its
applicability, and a **body** listing the operations to be performed whenever the plan is
executed.

The triggering event can be a goal/belief invocation/addition (**+**) or failure/deletion
(**-**), in the form: 
```
[+|-] <triggering event> onlyIf {<context>} then {<body>}.
```
We inherit from Jason the usage of a prefix unary + to indicate invocation or additions,
and a prefix unary - to indicate failures or deletions.

If a logical expression is present in the context block (prefixed by **onlyIf**), it is
then used to vet the relevant plan. 
The condition therein expressed should be a logic formula to be tested against the belief base via logic resolution. 
Finally, if the plan is selected for execution, the sequence of operations and actions
contained in its body (prefixed by then) is performed by the agent. There, actions
may consist of edits (additions or deletions) to the belief base, as well as additions of
further achievement or test goals, or invocations of external or internal actions (see
next paragraphs).

## Defining Plans in JaKtA DSL

In JaKtA, plans are declared within an agent’s configuration, specifying what to do when a goal is pursued.

```kt showLineNumbers
agent("Robot") {            
    plans {
        +/-achieve("goal"(X)) onlyIf { "guard"(X) } then { 
            achieve("goal"(X))
            test("goal"(Y))
            spawn("goal"(Z))
            +/-"belief"(A)
            update("belief"(C))
            execute("action"(X, Y, Z)) 
        }
        +/-test("goal"(Y)) onlyIf { "guard"(Y) } 
            then { /*...*/ }
        +/-"belief"(Z) onlyIf { "guard"(Z) } 
            then { /*...*/ }
    }
}
```

