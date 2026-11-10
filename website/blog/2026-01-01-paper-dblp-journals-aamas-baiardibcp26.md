---
slug: paper-dblp-journals-aamas-baiardibcp26
title: "New Publication: Testing BDI-based multi-agent systems using discrete event simulation"
date: 2026-01-01
authors: [anitvam, samubura, gciatto, danysk]
tags: [paper]
---

A new paper related to JaKtA has been published at Auton. Agents Multi Agent Syst. (2026).

<!-- truncate -->

<!-- jakta-publication-bib-key: DBLP:journals/aamas/BaiardiBCP26 -->

**Authors**: Martina Baiardi, Samuele Burattini, Giovanni Ciatto, Danilo Pianini
**Venue**: Auton. Agents Multi Agent Syst. (2026)
**Link**: [https://doi.org/10.1007/S10458-026-09744-W](https://doi.org/10.1007/S10458-026-09744-W)

### Abstract

Multi-agent systems are designed to deal with open, distributed systems with unpredictable dynamics, which makes them inherently hard to test. The value of using simulation for this purpose is recognized in the literature, although achieving sufficient fidelity (i.e., the degree of similarity between the simulation and the real-world system) remains a challenging task. This is exacerbated when dealing with cognitive agent models, such as the Belief Desire Intention (BDI) model, where the agent codebase is not suitable to run unchanged in simulation environments, thus increasing the reality gap between the deployed and simulated systems. We argue that BDI developers should be able to test in simulation the same specification that will be later deployed, with no surrogate representations. Thus, in this paper, we discuss how the control flow of BDI agents can be mapped onto a Discrete Event Simulation (DES), showing that such integration is possible at different degrees of granularity. We substantiate our claims by producing an open-source prototype integration between two pre-existing tools (JaKtA and Alchemist), showing that it is possible to produce a simulation-based testing environment for distributed BDI agents, and that different granularities in mapping BDI agents over DESs may lead to different degrees of fidelity.
