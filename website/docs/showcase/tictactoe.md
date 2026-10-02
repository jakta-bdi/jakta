---
sidebar_label: Tic-tac-toe
sidebar_position: 2
---

# Tic-tac-toe

import ExampleApp from '@site/src/components/ExampleApp/ExampleApp';

The [tictactoe example](https://github.com/jakta-bdi/jakta/tree/main/examples/tictactoe) is a BDI player that
reasons on the board with Prolog rules. The rules and plans are written by Kotlin code for the board size you
choose, so the same agent definition plays 3×3, 4×4 and 5×5.

Play against the agent by clicking a cell on your turn: your move goes to the environment, which shows the new board
to the agent. Lower the difficulty to make the agent sometimes distracted, so that it plays a random cell.

It runs below, right in your browser: the *agent trace* on the right shows what the agents print while they reason.

<ExampleApp name="tictactoe" title="Tic-tac-toe" />

```bash
./gradlew :examples:tictactoe:run                     # desktop
./gradlew :examples:tictactoe:jsBrowserDevelopmentRun # browser
```

## Why it matters: Kotlin writes the Prolog

In Jason, an agent is a fixed `.asl` file. To play on a 4×4 board you would write another one, or encode the board
size in rules that loop over it at run time. In JaKtA the agent is defined by ordinary Kotlin code, which runs
**once, when the agent is built**: Kotlin loops, conditions and functions decide which Prolog rules and plans the
agent gets. Prolog then does what it is good at, matching patterns on the board while the agent plays.

This is *paradigm blending*: the logic stays declarative, and the program that writes it is plain Kotlin,
with types, functions and tests. The [2P-Kt DSL](../explanation/incarnations/prolog/2p-kt.md) makes the step
direct: a Kotlin expression like `cell(X, Y, empty)` *is* a Prolog term.

### Patterns as Kotlin functions

A line is a Prolog list of `cell(X, Y, Mark)` terms. Kotlin functions build the patterns the agent looks for,
for any board size:

```kotlin
private fun cell(x: Term, y: Term, mark: Term): Struct = Struct.of("cell", x, y, mark)

/**
 * A line of [size] cells holding [owner]'s mark except for the empty cell `cell(X, Y, e)` at [gap].
 */
private fun JaktaLogicProgrammingScope.lineWithGap(size: Int, gap: Int, owner: Term): Struct = "aligned"(
    logicListOf((0 until size).map { if (it == gap) cell(X, Y, empty) else cell(`_`, `_`, owner) }),
)
```

On a 3×3 board, `lineWithGap(3, 2, me)` for the X player is the Prolog goal

```prolog
aligned([cell(_, _, x), cell(_, _, x), cell(X, Y, e)])
```

a line where X has two marks and the third cell is free: solving it binds `X` and `Y` to the winning cell.
On 4×4, `lineWithGap(4, 3, me)` gives a four-cell pattern instead, with no change to the agent.

### Rules generated in loops

What `aligned` means comes from a loop over the four directions, each a step along columns and rows:

```kotlin
private val directions = mapOf(
    "horizontal" to (1 to 0),
    "vertical" to (0 to 1),
    "diagonal" to (1 to 1),
    "antidiagonal" to (1 to -1),
)

believes {
    for ((direction, step) in directions) {
        val (dx, dy) = step
        // a line of one cell, or a cell followed by the rest of the line, one step further
        +inferenceRule { direction(logicListOf(cell(X, Y, S))) impliedBy cell(X, Y, S) }
        +inferenceRule {
            direction(logicList(cell(A, B, C), cell(X, Y, S), tail = T)) impliedBy (
                cell(A, B, C) and
                    (X `is` (A + dx)) and
                    (Y `is` (B + dy)) and
                    cell(X, Y, S) and
                    direction(logicList(cell(X, Y, S), tail = T))
                )
        }
        +inferenceRule { "aligned"(L) impliedBy direction(L) }
    }
    // ...
}
```

For `"horizontal"`, the second rule is the Prolog clause

```prolog
horizontal([cell(A, B, C), cell(X, Y, S) | T]) :-
    cell(A, B, C), X is A + 1, Y is B + 0, cell(X, Y, S), horizontal([cell(X, Y, S) | T]).
```

The Kotlin `step` values end up as the constants `1` and `0`: the four directions are four copies of the same rule,
written once.

### Configuration that depends on the board

Kotlin conditions decide which parts of the strategy the agent has at all. Forks — a move that threatens two lines
at once — are what make the strategy unbeatable on 3×3, but on larger boards they are not enough to be optimal, and
they are slow to compute. So the threat and fork rules are only generated for 3×3:

```kotlin
// the fork strategy is optimal on 3×3; on larger boards it is not, and too slow to be worth it
if (size == OPTIMAL_SIZE) {
    // threat(X, Y, M, A, B): if M plays (X, Y), it threatens to complete a line at (A, B)
    for (move in 0 until size) {
        for (rest in (0 until size) - move) {
            +inferenceRule { "threat"(X, Y, M, A, B) impliedBy lineWithTwoGaps(size, move, rest) }
        }
    }
    // fork(X, Y, M): if M plays (X, Y), it threatens two lines at once, and cannot be stopped
    +inferenceRule {
        "fork"(X, Y, M) impliedBy (
            "threat"(X, Y, M, A, B) and "threat"(X, Y, M, C, D) and not(eq(A, C) and eq(B, D))
            )
    }
}
```

The plan library is generated the same way. One local function makes a plan for the goal `move` out of a Prolog
guard; loops and conditions call it:

```kotlin
/** A plan to move that plays the cell (X, Y) found by [guard], explaining why. */
fun move(why: String, guard: JaktaLogicProgrammingScope.() -> Struct) = prologPlan {
    adding.goal { matchingGoal { moveGoal } } onlyWhen {
        satisfies(guard)
    } triggers {
        agent.print(why, ": playing (", X, ", ", Y, ")")
        game.put(X.value(), Y.value(), mark)
    }
}

move("Oops, I got distracted") { "distracted"(me) and cell(X, Y, empty) }
for (gap in 0 until size) move("I can complete a line and win") { lineWithGap(size, gap, me) }
for (gap in 0 until size) {
    move("The opponent could complete a line, blocking") {
        lineWithGap(size, gap, opponent)
    }
}
if (size == OPTIMAL_SIZE) {
    move("Creating a fork") { "fork"(X, Y, me) }
    // ... blocking the opponent's forks
}
if (size % 2 == 1) {
    val centre = Integer.of(size / 2)
    move("Taking the centre") { eq(X, centre) and eq(Y, centre) and cell(X, Y, empty) }
}
// ... corners, then any empty cell
```

The guard's solution binds `X` and `Y`, and the plan body reads them back in Kotlin with `X.value()`
(see [Reading values in Kotlin](../explanation/incarnations/prolog/beliefs-and-goals.md#reading-values-in-kotlin)).
The result is a different agent for each board:

| Board | Inference rules | Plans for `move` | Differences |
|---|---|---|---|
| 3×3 | 19 | 21 | lines of 3, threat and fork rules, four fork plans, centre |
| 4×4 | 12 | 18 | lines of 4, no forks, no centre |
| 5×5 | 12 | 21 | lines of 5, no forks, centre |

## How the agent plays

- **Perceptions become beliefs.** After every move the environment publishes the board, and the agent turns it into
  `cell(X, Y, Mark)` beliefs (`e` for an empty cell), `turn(Mark)` for whose turn it is, and `distracted(Mark)` when
  the difficulty makes it play at random. Only what changed is added or removed (`handleBoardPerception` in
  [`TicTacToeSkills.kt`](https://github.com/jakta-bdi/jakta/blob/main/examples/tictactoe/src/commonMain/kotlin/TicTacToeSkills.kt)),
  so each move triggers just the new `turn` belief.
- **Its turn is an event.** A plan reacts to `turn(me)`: it thinks for a while, then pursues the goal `move`.
- **Plan order is the strategy.** Among the plans for `move` whose guard holds, the
  [first one in the library](../explanation/execution-model.md#plan-selection) is chosen. The plans are added in the
  priority order of the classic strategy by Newell and Simon: win, block, fork, block a fork, centre, opposite corner,
  corner, anything. Adding a rule to the strategy is adding a plan in the right place.
- **The environment is the referee.** It applies moves, publishes the new board to every player and stops the node
  when somebody wins or the board is full. You play through the same environment: a click puts your mark, and the
  agent perceives it like any other move.

:::note
The strategy never loses on 3×3, and this is tested:
[`TicTacToeOptimalityTest`](https://github.com/jakta-bdi/jakta/blob/main/examples/tictactoe/src/desktopTest/kotlin/TicTacToeOptimalityTest.kt)
plays the agent, as X and as O, against every possible sequence of opponent moves.
:::

## Learn more

- [Prolog incarnation](../explanation/incarnations/prolog/index.md), and [Prolog plans](../explanation/incarnations/prolog/prolog-plans.md)
- [Plans](../explanation/basic-concepts/plans.md) and [plan selection](../explanation/execution-model.md#plan-selection)
- [Caveats](../explanation/incarnations/prolog/caveats.md): guards use the first solution, and performance
