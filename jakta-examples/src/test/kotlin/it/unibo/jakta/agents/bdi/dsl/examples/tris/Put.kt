package it.unibo.jakta.agents.bdi.dsl.examples.tris

import it.unibo.jakta.agents.bdi.actions.ExternalRequest
import it.unibo.jakta.agents.bdi.actions.impl.AbstractExternalAction
import it.unibo.tuprolog.solve.Signature

object Put : AbstractExternalAction(Signature("put", 4)) {
    override fun action(request: ExternalRequest) {
        val x = request.arguments[0].castToInteger().value.toInt()
        val y = request.arguments[1].castToInteger().value.toInt()
        val z = request.arguments[2].castToAtom().value[0]
        val other = request.arguments[3].castToAtom().value[0]
        println("Putting $z at ($x, $y) and passing turn to $other")
        updateData(mapOf("cell" to Triple(x, y, z), "turn" to "$other-agent"))
    }
}
