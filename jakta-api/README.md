# jakta-api

Core interfaces of [JaKtA](https://github.com/jakta-bdi/jakta), a Kotlin Multiplatform framework for BDI
(Belief-Desire-Intention) agent-oriented programming.

This module defines JaKtA's abstractions — `Agent`, `Plan`, `Intention`, `Node`, events — generic over the
representation of beliefs and goals, with no implementation and no fixed belief/goal type. It's the contract
that `jakta-core` implements and that concrete incarnations (e.g. `jakta-prolog-incarnation`) build on.

See the [documentation](https://jakta-bdi.github.io/) and [main repository](https://github.com/jakta-bdi/jakta)
for setup and usage.

Licensed under Apache-2.0.
