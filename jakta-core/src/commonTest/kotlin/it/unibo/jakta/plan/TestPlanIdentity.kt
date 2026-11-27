package it.unibo.jakta.plan

import it.unibo.jakta.agent.AgentID
import it.unibo.jakta.agent.AgentSpecification
import it.unibo.jakta.agent.AgentState
import it.unibo.jakta.agent.BaseAgent
import it.unibo.jakta.agent.BaseAgentID
import it.unibo.jakta.agent.BaseAgentState
import it.unibo.jakta.dsl.plan.PlanLibraryBuilderImpl
import it.unibo.jakta.dsl.plan.labelled
import it.unibo.jakta.dsl.plan.triggers
import it.unibo.jakta.dsl.plan.withMetadata
import kotlin.reflect.typeOf
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

/**
 * Plans have a stable identity, an optional label and metadata (#1071).
 */
class TestPlanIdentity {
    private val beliefPlans = mutableListOf<Plan.Belief<String, String, *, *>>()
    private val goalPlans = mutableListOf<Plan.Goal<String, String, *, *>>()
    private val library = PlanLibraryBuilderImpl<String, String>({ beliefPlans += it }, { goalPlans += it })

    @Test
    fun theIdOfAPlanIsStable() {
        val plan = library.adding.goal { takeIf { it == "g" } } triggers { }
        assertEquals(plan.id, plan.id, "Reading the id twice should give the same id")
        assertTrue(!plan.id.isLabel, "A plan without a label should have a generated id")
    }

    @Test
    fun labelsAndMetadataAreSetFromTheDsl() {
        val plan = library.adding.goal { takeIf { it == "clean" } }
            .withMetadata("priority" to 2, "atomic" to true)
            .labelled("clean-nearest")
            .onlyWhen { context } triggers { }

        assertEquals(PlanID.labelled("clean-nearest"), plan.id)
        assertEquals("clean-nearest", plan.id.toString())
        assertEquals(mapOf("priority" to 2, "atomic" to true), plan.metadata)
        assertEquals("GoalAdditionPlan(clean-nearest)", plan.toString())
        assertEquals(listOf<Plan<*, *, *, *, *>>(plan), goalPlans)
    }

    @Test
    fun labelledWorksAsAnInfixFunctionBeforeTheGuard() {
        val plan = library.adding.belief { takeIf { it == "b" } } labelled "on-b" onlyWhen { context } triggers { }
        assertEquals("on-b", plan.id.id)
        assertEquals(listOf<Plan<*, *, *, *, *>>(plan), beliefPlans)
    }

    @Test
    fun plansAreEqualWhenTheirIdsAre() {
        val first = GoalAdditionPlan<String, String, String, Unit>(
            trigger = { it },
            guard = { context },
            body = { },
            resultType = typeOf<Unit>(),
            id = PlanID.labelled("same"),
        )
        val sameLabel = first.copy(trigger = { null })
        val otherKind = GoalRemovalPlan<String, String, String, Unit>(
            trigger = { it },
            guard = { context },
            body = { },
            resultType = typeOf<Unit>(),
            id = PlanID.labelled("same"),
        )
        val unlabelled = first.copy(id = PlanID())

        assertEquals(first, sameLabel)
        assertEquals(first.hashCode(), sameLabel.hashCode())
        assertNotEquals<Any>(first, otherKind)
        assertNotEquals(first, unlabelled)
    }

    @Test
    fun blankLabelsAreRejected() {
        assertFailsWith<IllegalArgumentException> { PlanID.labelled(" ") }
    }

    @Test
    fun anAgentCannotHaveTwoPlansWithTheSameLabel() {
        library.adding.goal { takeIf { it == "a" } } labelled "twice" triggers { }
        library.adding.goal { takeIf { it == "b" } } labelled "twice" triggers { }

        val error = assertFailsWith<IllegalArgumentException> { BaseAgent(specification(goalPlans)) }
        assertTrue("twice" in error.message.orEmpty())
    }

    @Test
    fun aPlanAddedAtRuntimeCannotReuseALabel() {
        library.adding.goal { takeIf { it == "a" } } labelled "once" triggers { }
        val agent = BaseAgent(specification(goalPlans.toList()))
        val duplicate = library.adding.goal { takeIf { it == "b" } } labelled "once" triggers { }

        assertFailsWith<IllegalArgumentException> { agent.state.addPlan(duplicate) }
    }

    private fun specification(plans: List<Plan.Goal<String, String, *, *>>) =
        object : AgentSpecification<String, String, Unit> {
            override val body: Unit = Unit
            override val id: AgentID = BaseAgentID("test-agent")
            override val initialGoals: List<String> = emptyList()
            override val initialState: AgentState<String, String> = BaseAgentState(
                beliefs = emptyList(),
                intentions = emptySet(),
                beliefPlans = emptyList(),
                goalPlans = plans,
                perceptionHandler = { null },
                messageHandler = { null },
            )
        }
}
