---
sidebar_position: 3
---

# Turn perceptions into Prolog facts

A perception is a plain Kotlin object (see [Connect agents to an external environment](../connect-environment.md)).
A Prolog agent turns it into facts in `handlesPerceptionEvents`. The snippets come from the
[`vacuum-world`](https://github.com/jakta-bdi/jakta/tree/main/examples/vacuum-world) example.

## Build facts with the 2P-Kt API

Outside plans there is no DSL scope: build terms with the 2P-Kt core types (`it.unibo.tuprolog.core`):

```kotlin
Fact.of(Struct.of("position", Integer.of(position.x), Integer.of(position.y)))
Fact.of(Struct.of("facing", facing.toAtom()))

fun Enum<*>.toAtom(): Atom = Atom.of(name.lowercase())
```

Use lowercase atoms: a string starting with an uppercase letter is a variable in the DSL, and `Atom.of("Bob")`
is the quoted atom `'Bob'`. Keeping these functions in one file, as the example does in `Beliefs.kt`, gives one place
where Kotlin values become terms, and where the code that reads the beliefs back finds the same names.

## Add what is new, remove what is stale

Diff the perceived facts with the current ones, so that unchanged facts generate no events and beliefs the agent
derived itself (like a memory of visited cells) are left alone. The facts a reading replaces are here the beliefs
with the same functors:

```kotlin
fun handleVacuumPerception(event: VacuumPerception, beliefs: Collection<PrologBelief>): AgentUpdate<*> {
    val perceived = event.toBeliefs()
    val previous = beliefs.filter { it.head.functor in perceived.map { new -> new.head.functor } }.toSet()
    // ... plus the remembered dust_at(X, Y) beliefs, see below
    return AgentUpdate.Belief(additions = perceived - previous, removals = previous - perceived)
}
```

To select the replaced beliefs more finely, match them against queries built with `newContextBeliefQuery { }` (from
`it.unibo.jakta.dsl.belief`): `belief.matchBelief(newContextBeliefQuery { "position"(X, Y) }) != null`.

The handler can also keep a memory derived from what it perceives. The vacuum robot adds `dust_at(X, Y)` for every
square it sees dusty and removes it for every square it sees empty, so the memory is always computed from the
position the reading was taken at:

```kotlin
val dustSeen = event.cellsWith(Content.DUST).map { cellBelief("dust_at", it) }.toSet() - beliefs.toSet()
val dustGone = event.cellsWith(Content.EMPTY).map { cellBelief("dust_at", it) }.toSet() intersect beliefs.toSet()
return AgentUpdate.Belief(
    additions = perceived - previous + dustSeen,
    removals = previous - perceived + dustGone,
)
```

## Hook it into the agent

```kotlin
handlesPerceptionEvents { if (it is VacuumPerception) handleVacuumPerception(it, beliefs) else null }
```

Returning `null` ignores a perception. Plans then react to the facts as to any belief, e.g.
`adding.belief { matchingBelief { "position"(X, Y) } }`.

The [`tictactoe`](https://github.com/jakta-bdi/jakta/tree/main/examples/tictactoe) example does the same with the
board (`handleBoardPerception` in `TicTacToeSkills.kt`).
