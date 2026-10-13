---
sidebar_position: 4
---

# String Incarnation Reference

`jakta-string-incarnation` uses `String` for both beliefs and goals. It provides three functions,
all in the package `it.unibo.jakta.belief`.

| Function | Where | Returns | Meaning |
|---|---|---|---|
| `String.ifGoalMatches(goal)` | triggers | `Unit?` | `Unit` if the goal (or belief) is exactly `goal`, `null` otherwise. |
| `String.matchesRegex(regex)` | triggers | `Boolean?` | `true` if the goal (or belief) contains a match of `regex`, `null` otherwise. |
| `GuardScope<String, Ctx>.containsBeliefMatching(belief)` | guards | `Ctx?` | The context if the belief base contains exactly `belief`, `null` otherwise. |

Despite their names, `ifGoalMatches` and `matchesRegex` are extensions of `String`,
so they work in belief triggers as well.

```kotlin
agent {
    embodiedAs { Any() }
    believes { +"sunny" }
    hasInitialGoals { !"goOut" }
    hasPlanLibrary {
        adding.goal {
            ifGoalMatches("goOut")
        } onlyWhen {
            containsBeliefMatching("sunny")
        } triggers {
            agent.believe("temperature(25)")
        }
        adding.belief {
            matchesRegex("^temperature\\(\\d+\\)$")
        } triggers {
            agent.print("New temperature reading")
        }
    }
}
```

Since the context of a `matchesRegex` trigger is `true`, use `adding.belief { takeIf { ... } }` when the body needs the
belief itself.
