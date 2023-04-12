package io.github.anitvam.agents.bdi.dsl.examples.tris

import io.github.anitvam.agents.bdi.actions.ExternalRequest
import io.github.anitvam.agents.bdi.actions.impl.AbstractExternalAction
import it.unibo.tuprolog.solve.Signature

object Put : AbstractExternalAction(Signature("put", 3)) {
    override fun action(request: ExternalRequest) {
        val x = request.arguments[0].castToInteger().value.toInt()
        val y = request.arguments[1].castToInteger().value.toInt()
        val z = request.arguments[2].castToAtom().value[0]
        updateData("cell" to Triple(x, y, z))
    }
}