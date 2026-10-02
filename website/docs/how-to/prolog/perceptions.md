---
sidebar_position: 3
---

# Turn perceptions into Prolog facts

A perception is a plain Kotlin object (see [Connect agents to an environment](../connect-environment.md)).
A Prolog agent turns it into facts in `handlesPerceptionEvents`. The snippets come from the
[`vacuum-world`](https://github.com/jakta-bdi/jakta/tree/main/examples/vacuum-world) example.

## Build facts with the 2P-Kt API

Outside plans there is no DSL scope: build terms with the 2P-Kt core types (`it.unibo.tuprolog.core`):

```kotlin
Fact.of(Struct.of("location", Integer.of(location.x), Integer.of(location.y)))
Fact.of(Struct.of("direction", Atom.of(facing.name.lowercase())))
```

Use lowercase atoms: a string starting with an uppercase letter is a variable in the DSL, and `Atom.of("Bob")`
is the quoted atom `'Bob'`.

## Describe what the perception replaces

Queries built with `newContextBeliefQuery { }` (from `it.unibo.jakta.dsl.belief`) select the beliefs a perception
is about:

```kotlin
private val perceivedQueries = listOf(
    newContextBeliefQuery { "location"(X, Y) },
    newContextBeliefQuery { "direction"(X) },
    newContextBeliefQuery { "square"(X, Y) },
)
```

## Add what is new, remove what is stale

Diff the perceived facts with the current ones, so that unchanged facts generate no events and beliefs the agent
derived itself (like a memory of visited cells) are left alone:

```kotlin
fun handleVacuumPerception(event: VacuumPerception, beliefs: Collection<PrologBelief>): AgentUpdate<*> {
    val old = beliefs.filter { belief -> perceivedQueries.any { belief.matchBelief(it) != null } }.toSet()
    val new = event.toBeliefs()
    // ... plus the remembered dust_at(X, Y) beliefs, see below
    return AgentUpdate.Belief(new - old, old - new)
}
```

The handler can also keep a memory derived from what it perceives. The vacuum robot adds `dust_at(X, Y)` for every
square it sees dusty and removes it for every square it sees clean, so the memory is always computed from the
position the reading was taken at:

```kotlin
val robot = RobotState(event.location, event.facing)
val (dusty, clean) = event.squares.entries.partition { it.value == Content.DUST }
val seenDust = dusty.map { dustAt(robot.cellOf(it.key)) }.toSet()
val goneDust = clean.map { dustAt(robot.cellOf(it.key)) }.filter { it in beliefs }.toSet()
return AgentUpdate.Belief(new - old + (seenDust - beliefs.toSet()), old - new + goneDust)
```

## Hook it into the agent

```kotlin
handlesPerceptionEvents { if (it is VacuumPerception) handleVacuumPerception(it, beliefs) else null }
```

Returning `null` ignores a perception. Plans then react to the facts as to any belief, e.g.
`adding.belief { matchingBelief { "location"(X, Y) } }`.

The [`tictactoe`](https://github.com/jakta-bdi/jakta/tree/main/examples/tictactoe) example does the same with the
board (`handleBoardPerception` in `TicTacToeSkills.kt`).
