package it.unibo.jakta.agents.distributed.dsl

import it.unibo.jakta.agents.bdi.dsl.JaktaDSL
import it.unibo.jakta.agents.distributed.dmas.DMas

@JaktaDSL
fun dmas(f: DMasScope.() -> Unit): DMas = DMasScope().also(f).build()
