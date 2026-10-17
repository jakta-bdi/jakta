---
sidebar_label: Tic-tac-toe
sidebar_position: 2
---

# Tic-tac-toe

import ExampleApp from '@site/src/components/ExampleApp/ExampleApp';

The [tictactoe example](https://github.com/jakta-bdi/jakta/tree/main/examples/tictactoe) is a BDI player that
reasons on the board with Prolog rules. The rules and plans are written by Kotlin code for the board size you
choose, so the same agent definition plays 3×3, 4×4 and 5×5. On the larger boards a line one cell shorter than the
board wins: three in a row on 4×4, four on 5×5.

Play against the agent by clicking a cell on your turn: your move goes to the game, which shows the new board
to the agent. The difficulty sets how often the agent plays a random cell instead of its best move: often when
*clumsy*, now and then when *distracted*, never when *focused*.

It runs below, right in your browser: the *agent trace* on the right shows what the agents print while they reason.

<ExampleApp name="tictactoe" title="Tic-tac-toe" />

```bash
./gradlew :examples:tictactoe:run                     # desktop
./gradlew :examples:tictactoe:jsBrowserDevelopmentRun # browser
```

## Why it matters: configuring the agent

Typically you would need to encode all rules manually when programming an agent. To play on a 4×4 board you would
write them all again, or encode the board size in rules that loop over it at run time. In JaKtA the agent is
configured by ordinary Kotlin code, which runs **once, when the agent is built**: loops, conditions and functions
decide which rules and plans the agent gets. The rules then do what they are good at, matching patterns on the
board while the agent plays.

This is *paradigm blending*: the logic stays declarative, and it is configured with plain Kotlin,
with types, functions and tests. The [2P-Kt DSL](../explanation/incarnations/prolog/2p-kt.md) makes the step
direct: a Kotlin expression like `cell(X, Y, empty)` *is* a Prolog term.

### Patterns as Kotlin functions

A line is a Prolog list of `cell(X, Y, Mark)` terms, as long as needed to win. Kotlin functions build the patterns
the agent looks for, for any winning length:

```kotlin
private fun cell(x: Term, y: Term, mark: Term): Struct = Struct.of("cell", x, y, mark)

/**
 * A line of [length] cells holding [owner]'s mark except for the empty cell `cell(X, Y, e)` at [gap].
 */
private fun JaktaLogicProgrammingScope.lineWithGap(length: Int, gap: Int, owner: Term): Struct = "aligned"(
    logicListOf((0 until length).map { if (it == gap) cell(X, Y, empty) else cell(`_`, `_`, owner) }),
)
```

On a 3×3 board, `lineWithGap(3, 2, me)` for the X player is the Prolog goal

```prolog
aligned([cell(_, _, x), cell(_, _, x), cell(X, Y, e)])
```

a line where X has two marks and the third cell is free: solving it binds `X` and `Y` to the winning cell.
On 5×5, `lineWithGap(4, 3, me)` gives a four-cell pattern instead, with no change to the agent.

### Rules generated in loops

What `aligned` means comes from a loop over the lines of the board. The game model already lists every run of
cells along a row, column or diagonal that is long enough to win (`BoardState.lines`, which the game also uses to
find the winner), and each line becomes one rule.
Threats and forks are then built on top of `aligned`, with more loops. Here `board` is the game's `BoardState` and
`length` its winning length:

```kotlin
believes {
    // aligned([cell(X1, Y1, S1), ...]): one rule for each line of the board, as long as needed to win
    for (line in board.lines) {
        val cells = line.mapIndexed { i, (x, y) -> cell(Integer.of(x), Integer.of(y), Var.of("S$i")) }
        +inferenceRule { "aligned"(logicListOf(cells)) impliedBy cells.reduce<Struct, Struct> { a, b -> a and b } }
    }
    // threat(X, Y, M, A, B): if M plays (X, Y), it threatens to complete a line at (A, B)
    for (move in 0 until length) {
        for (rest in (0 until length) - move) {
            +inferenceRule { "threat"(X, Y, M, A, B) impliedBy lineWithTwoGaps(length, move, rest) }
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

On 3×3, the rule for the first row is the Prolog clause

```prolog
aligned([cell(0, 0, S0), cell(1, 0, S1), cell(2, 0, S2)]) :- cell(0, 0, S0), cell(1, 0, S1), cell(2, 0, S2).
```

The coordinates are fixed when the agent is built, so solving `aligned` only looks up beliefs, with no arithmetic
or recursion at run time. This keeps the search for forks fast even on 5×5, where the agent must spot the
opponent's crossing lines before they become two threats at once.

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
for (gap in 0 until length) move("I can complete a line and win") { lineWithGap(length, gap, me) }
for (gap in 0 until length) {
    move("The opponent could complete a line, blocking") {
        lineWithGap(length, gap, opponent)
    }
}
move("Creating a fork") { "fork"(X, Y, me) }
// ... blocking the opponent's forks
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
| 3×3 | 15 | 21 | 8 lines of 3, centre |
| 4×4 | 31 | 20 | 24 lines of 3, no centre |
| 5×5 | 41 | 23 | 28 lines of 4, centre |

## How the agent plays

- **Perceptions become beliefs.** After every move the game publishes the board, and the agent turns it into
  `cell(X, Y, Mark)` beliefs (`e` for an empty cell), `turn(Mark)` for whose turn it is, and `distracted(Mark)` when
  the difficulty makes it play at random. Only what changed is added or removed (`handleBoardPerception` in
  [`TicTacToeSkills.kt`](https://github.com/jakta-bdi/jakta/blob/main/examples/tictactoe/src/commonMain/kotlin/TicTacToeSkills.kt)),
  so each move triggers just the new `turn` belief.
- **Its turn is an event.** A plan reacts to `turn(me)`: it thinks for a while, then pursues the goal `move`.
- **Plan order is the strategy.** Among the plans for `move` whose guard holds, the
  [first one in the library](../explanation/execution-model.md#plan-selection) is chosen. The plans are added in the
  priority order of the classic strategy by Newell and Simon: win, block, fork, block a fork, centre, opposite corner,
  corner, anything. Adding a rule to the strategy is adding a plan in the right place.
- **The game is the referee.** It applies moves, publishes the new board to every player and stops the node
  when somebody wins or the board is full. You play through the same game: a click puts your mark, and the
  agent perceives it like any other move.

:::note
The strategy never loses on 3×3, and this is tested:
[`TicTacToeOptimalityTest`](https://github.com/jakta-bdi/jakta/blob/main/examples/tictactoe/src/desktopTest/kotlin/TicTacToeOptimalityTest.kt)
plays the agent, as X and as O, against every possible sequence of opponent moves. On the larger boards the same
plans make a strong opponent, though not an unbeatable one. With perfect play the first player wins on 4×4
(two marks side by side in an empty row already threaten both ends), and 5×5 is a draw.
:::

## Learn more

- [Prolog incarnation](../explanation/incarnations/prolog/index.md), and [Prolog plans](../explanation/incarnations/prolog/prolog-plans.md)
- [Plans](../explanation/basic-concepts/plans.md) and [plan selection](../explanation/execution-model.md#plan-selection)
- [Caveats](../explanation/incarnations/prolog/caveats.md): guards use the first solution, and performance
