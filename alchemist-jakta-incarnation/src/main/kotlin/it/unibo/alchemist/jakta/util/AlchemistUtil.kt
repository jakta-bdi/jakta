package it.unibo.alchemist.jakta.util

/**
 * Object that represents an Alchemist empty molecule.
 */
object EmptyMolecule

fun Any?.valueOrEmptyMolecule(): Any = this ?: EmptyMolecule
