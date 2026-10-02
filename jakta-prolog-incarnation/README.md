# jakta-prolog-incarnation

Prolog-based concrete incarnation of [JaKtA](https://github.com/jakta-bdi/jakta), a Kotlin Multiplatform
framework for BDI (Belief-Desire-Intention) agent-oriented programming.

This module is what turns `jakta-core`'s generic engine into something you can actually write agents with:
it fixes beliefs and goals to Prolog terms (via [tuProlog/2p-kt](https://github.com/tuProlog/2p-kt)),
provides MGU-based belief/goal matching, a Prolog-flavored plan DSL (`prologPlan { }`, `matchingBelief { }`,
`satisfies { }`, ...), and a KQML-inspired agent messaging protocol (`tellTo`, `askOneTo`, `askAllTo`, ...).
It's the incarnation used throughout the [JaKtA documentation](https://jakta-bdi.github.io/) and examples.

See the [documentation](https://jakta-bdi.github.io/) and [main repository](https://github.com/jakta-bdi/jakta)
for setup and usage.

Licensed under Apache-2.0.
