# jakta-string-incarnation

Minimal, string-based concrete incarnation of [JaKtA](https://github.com/jakta-bdi/jakta), a Kotlin
Multiplatform framework for BDI (Belief-Desire-Intention) agent-oriented programming.

This module fixes `jakta-core`'s generic `Belief`/`Goal` types to plain `String`, with a couple of matcher
helpers (`ifGoalMatches`, `matchesRegex`, `containsBeliefMatching`) and no other machinery. It exists mainly
to demonstrate the [concrete incarnation pattern](https://jakta-bdi.github.io/docs/explanation/incarnations)
with the bare minimum — for actually writing agents, `jakta-prolog-incarnation` is the richer, documented
option.

See the [documentation](https://jakta-bdi.github.io/) and [main repository](https://github.com/jakta-bdi/jakta)
for setup and usage.

Licensed under Apache-2.0.
