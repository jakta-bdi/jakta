# jakta-situated

Generic skills for situated agents of [JaKtA](https://github.com/jakta-bdi/jakta), a Kotlin
Multiplatform framework for BDI (Belief-Desire-Intention) agent-oriented programming.

Agents with these skills in their context can know and change their position (`SpatialSkill`),
find the agents nearby (`NeighborhoodSkill`), and read and write properties observable by their
environment (`PropertySkill`), without depending on how the environment is implemented.
`InMemorySpace` implements them in memory; `alchemist-jakta-incarnation` implements them on top of the
[Alchemist](https://alchemistsimulator.github.io/) simulator.

See the [documentation](https://jakta-bdi.github.io/) and [main repository](https://github.com/jakta-bdi/jakta)
for setup and usage.

Licensed under Apache-2.0.
