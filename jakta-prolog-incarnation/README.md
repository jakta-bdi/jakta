# jakta-prolog-incarnation

Prolog-based concrete incarnation of [JaKtA](https://github.com/jakta-bdi/jakta), a Kotlin Multiplatform
framework for BDI (Belief-Desire-Intention) agent-oriented programming.

This module is what turns `jakta-core`'s generic engine into something you can actually write agents with:
it fixes beliefs and goals to Prolog terms (via [tuProlog/2p-kt](https://github.com/tuProlog/2p-kt)),
provides MGU-based belief/goal matching, a Prolog-flavored plan DSL (`prologPlan { }`, `matchingBelief { }`,
`satisfies { }`, ...), and a KQML-inspired agent messaging protocol (`tellTo`, `askOneTo`, `askAllTo`, ...).
It's the incarnation used throughout the [JaKtA documentation](https://jakta-bdi.github.io/) and examples.

## Usage from JavaScript

The npm package exposes a JS-friendly facade, where beliefs, goals, triggers and guards are Prolog text, and the
variables they bind are available to the (possibly `async`) plan bodies as `context`:

```js
import { prologAgent, runMas, setLogSeverity } from "@jakta/jakta-prolog-incarnation";

setLogSeverity("Assert"); // only show agents' `print` output

const counter = prologAgent("counter")
  .believes(["limit(3)", "big(X) :- limit(L), X >= L"])
  .hasInitialGoals(["start(0, 3)"])
  .onGoalAdded("start(N, N)", (self) => {
    self.print(`done at ${self.context.N}`);
    self.terminateNode();
  })
  // The guard is a query on the beliefs, which can bind new variables (here `S`).
  .onGoalAdded("start(N, X)", async (self) => {
    self.print(`counting ${self.context.N}`);
    await self.achieve("start(S, X)"); // variables bound so far are replaced by their values
  }, "N < X, S is N + 1");

await runMas([counter]);
```

`believe`, `forget`, `achieve` and `alsoAchieve` take Prolog text too, and `query` solves a query on the beliefs,
binding its variables for the rest of the plan. Plans run with the same semantics as in `jakta-core`'s JS facade
(see its README), including `delay` and `external`. The package also exports `jakta-core`'s facade (`agent`), so
Prolog agents and agents with plain JS beliefs can run in the same `runMas`.

See the [documentation](https://jakta-bdi.github.io/) and [main repository](https://github.com/jakta-bdi/jakta)
for setup and usage.

Licensed under Apache-2.0.
