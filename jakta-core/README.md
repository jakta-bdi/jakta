# jakta-core

Reference implementation and DSL entry points of [JaKtA](https://github.com/jakta-bdi/jakta), a Kotlin
Multiplatform framework for BDI (Belief-Desire-Intention) agent-oriented programming.

This module implements `jakta-api` and `jakta-dsl`: the BDI reasoning cycle, a coroutine-based scheduler
that runs agent intentions cooperatively, the `agent { }` / `mas { }` / `node { }` DSL entry points, and
reusable skills (messaging, termination). It's belief/goal-representation agnostic — pair it with a concrete
incarnation such as `jakta-prolog-incarnation` to write actual agents.

See the [documentation](https://jakta-bdi.github.io/) and [main repository](https://github.com/jakta-bdi/jakta)
for setup and usage.

Licensed under Apache-2.0.
