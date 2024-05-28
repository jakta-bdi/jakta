package it.unibo.jakta.alchemist

import it.unibo.alchemist.model.Dependency

enum class JaktaLifecyclePhase : Dependency {
    SENSE,
    DELIBERATE,
    ACT,
    ;

    fun next(): JaktaLifecyclePhase = entries.let { it[(ordinal + 1) % it.size] }
}
