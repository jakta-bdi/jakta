package it.unibo.jakta.plan

import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

/**
 * Identifier of a Plan, fixed when the plan is created.
 * @param[id] the identifier as a string: the label given by the user, or a generated one.
 * @param[isLabel] whether [id] is a label given by the user rather than a generated identifier.
 */
data class PlanID(val id: String = generateId(), val isLabel: Boolean = false) {
    override fun toString(): String = id

    /** Factory methods for [PlanID]. */
    companion object {
        /**
         * The id of a plan labelled [label] by the user.
         */
        fun labelled(label: String): PlanID {
            require(label.isNotBlank()) { "A plan label cannot be blank" }
            return PlanID(label, isLabel = true)
        }

        @OptIn(ExperimentalUuidApi::class)
        private fun generateId(): String = Uuid.random().toString()
    }
}
