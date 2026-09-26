---
title: String Incarnation
sidebar_position: 2
---

# String Incarnation

`jakta-string-incarnation` is the smallest possible incarnation: beliefs and goals are `String`s.

## Purpose

It exists to show the incarnation pattern with nothing else around it, and to write quick demos and tests.
It has no reasoning: a belief is either present or not.

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
            agent.print("Going out!")
        }
    }
}
```

## What it provides

All in `it.unibo.jakta.belief`:
- `ifGoalMatches(text)`: a trigger that matches a goal or belief *equal* to `text`;
- `matchesRegex(regex)`: a trigger that matches strings *containing* a match of `regex`;
- `containsBeliefMatching(text)`: a guard that holds when `text` is in the belief base.

## Caveats

- **No data flows into the body.** `ifGoalMatches` yields `Unit` and `matchesRegex` yields `true`, so the body cannot
  tell what matched. To use the matched text, write the trigger yourself: `adding.belief { takeIf { it.startsWith("temp:") } }`
  makes the belief itself the plan's `context`.
- **Exact matches only.** `containsBeliefMatching` checks for an equal string, not a pattern.
- **Regex is a substring search.** `matchesRegex("go")` also matches `"ago"`: anchor it (`"^go$"`) when needed.
- **No messaging protocol.** Messages must be turned into beliefs or goals by your own `handlesMessageEvents`.

The only incarnation that runs everywhere, native targets included. See the
[String Incarnation Reference](../../reference/string-incarnation.md).
