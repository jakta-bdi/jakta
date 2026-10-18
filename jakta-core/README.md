# jakta-core

Reference implementation and DSL entry points of [JaKtA](https://github.com/jakta-bdi/jakta), a Kotlin
Multiplatform framework for BDI (Belief-Desire-Intention) agent-oriented programming.

This module implements `jakta-api` and `jakta-dsl`: the BDI reasoning cycle, a coroutine-based scheduler
that runs agent intentions cooperatively, the `agent { }` / `mas { }` / `node { }` DSL entry points, and
reusable skills (messaging, termination). It's belief/goal-representation agnostic — pair it with a concrete
incarnation such as `jakta-prolog-incarnation` to write actual agents.

## Usage from JavaScript

The npm package exposes a JS-friendly facade, where beliefs and goals are plain JS values,
triggers and guards return a context object (or `null` when not matching), and plan bodies can be `async`:

```js
import { agent, runMas, setLogSeverity } from "@jakta/jakta-core";

setLogSeverity("Assert"); // only show agents' `print` output

const alice = agent("alice")
  .hasInitialGoals(["greet"])
  .onGoalAdded(g => (g === "square" ? 7 : null), async ({ context }) => context * context)
  .onGoalAdded(g => g === "greet" || null, async (self) => {
    const n = await self.achieve("square");
    self.print(`Hello from ${self.agentName}, 7^2 = ${n}`);
    self.terminateNode();
  });

await runMas([alice]);
```

As in Kotlin, a plan runs one step at a time: from one `await` of a promise returned by the plan scope
(`achieve`, `delay`, `external`) to the next, while other intentions run in between, and the code after an `await`
never runs if the node stops meanwhile. Wrap any other promise in `external` to keep this behaviour:

```js
const res = await self.external(fetch(url));
const data = await self.external(res.json());
```

Awaiting other promises directly works, but the code after them runs outside the agent's steps, even after the node
stops: prefer `await self.delay(ms)` over `setTimeout`.

See the [documentation](https://jakta-bdi.github.io/) and [main repository](https://github.com/jakta-bdi/jakta)
for setup and usage.

Licensed under Apache-2.0.
