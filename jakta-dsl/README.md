# jakta-dsl

DSL builder interfaces of [JaKtA](https://github.com/jakta-bdi/jakta), a Kotlin Multiplatform framework for
BDI (Belief-Desire-Intention) agent-oriented programming.

This module defines the shape of JaKtA's declarative syntax — `AgentBuilder`, `PlanBuilder`,
`PlanLibraryBuilder`, `MasBuilder`, and the rest of the builder interfaces behind the `@JaktaDSL` marker —
built only on `jakta-api`'s abstractions, with no implementation of its own. `jakta-core` provides the
default implementation and the actual `agent { }` / `mas { }` / `node { }` entry points.

See the [documentation](https://jakta-bdi.github.io/) and [main repository](https://github.com/jakta-bdi/jakta)
for setup and usage.

Licensed under Apache-2.0.
