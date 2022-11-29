package io.github.anitvam.agents.bdi.goals.actions.impl

import io.github.anitvam.agents.bdi.goals.actions.InternalAction
import io.github.anitvam.agents.bdi.goals.actions.InternalRequest
import io.github.anitvam.agents.bdi.goals.actions.InternalResponse
import it.unibo.tuprolog.solve.Signature

internal data class InternalActionImpl(
    override val signature: Signature,
    val action: InternalRequest.() -> InternalResponse
) : InternalAction {
    override fun execute(request: InternalRequest): InternalResponse = request.action()
}
