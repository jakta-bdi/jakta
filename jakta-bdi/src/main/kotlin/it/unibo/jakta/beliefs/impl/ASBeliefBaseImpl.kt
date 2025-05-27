package it.unibo.jakta.beliefs.impl

import it.unibo.jakta.Jakta
import it.unibo.jakta.beliefs.ASBelief
import it.unibo.jakta.beliefs.ASBeliefBase
import it.unibo.jakta.beliefs.ASMutableBeliefBase
import it.unibo.jakta.beliefs.Belief
import it.unibo.jakta.events.BeliefBaseAddition
import it.unibo.jakta.events.BeliefBaseRemoval
import it.unibo.jakta.events.BeliefBaseUpdate
import it.unibo.jakta.events.Event
import it.unibo.tuprolog.core.Struct
import it.unibo.tuprolog.solve.Solution
import it.unibo.tuprolog.solve.Solver
import it.unibo.tuprolog.solve.flags.TrackVariables
import it.unibo.tuprolog.solve.flags.Unknown
import it.unibo.tuprolog.theory.Theory
import java.util.ArrayDeque
import java.util.Queue

internal data class ASBeliefBaseImpl(
    val beliefs: MutableList<ASBelief> = mutableListOf(),
    override var events: Queue<Event.BeliefEvent> = ArrayDeque(),
) : ASMutableBeliefBase, ASBeliefBase, Collection<Belief> by beliefs {
    // private var beliefs: ClauseMultiSet = ClauseMultiSet.empty(Unificator.default)

    override fun snapshot(): ASBeliefBase = this.copy()

    override fun select(query: Struct): List<ASBelief> {
        val solution = getSolutionOf(query)
        return if (solution.isYes && solution.solvedQuery != null) {
            listOf(ASBelief.wrap(solution.solvedQuery!!))
        } else {
            emptyList()
        }
    }

    override fun select(query: ASBelief) = select(query.content.head)

    override fun getSolutionOf(query: Struct): Solution = Solver.prolog.newBuilder()
        .flag(Unknown, Unknown.FAIL)
        .staticKb(operatorExtension + Theory.of(beliefs.map { it.content }))
        .flag(TrackVariables) { ON }
        .build()
        .solveOnce(query)

    override fun getSolutionOf(belief: ASBelief): Solution = getSolutionOf(belief.content.head)

    override fun update(belief: ASBelief): Boolean {
        val element = beliefs.find { it.content.head.functor == belief.content.head.functor }
        return if (element != null) {
            beliefs.remove(element)
            beliefs.add(belief)
            events.add(BeliefBaseUpdate(belief, element))
            true
        } else {
            false
        }
    }

    override fun update(beliefBase: ASBeliefBase): Boolean {
        when (beliefs == beliefBase) {
            false -> {
                // 1. each literal l in p not currently in b is added to b
                beliefs.addAll(beliefBase.filterIsInstance<ASBelief>())

                // 2. each literal l in b no longer in p is deleted from b
                beliefs.forEach { belief: ASBelief ->
                    if (!beliefBase.contains(belief) && belief.content.head.args.first() == ASBelief.SOURCE_PERCEPT) {
                        remove(belief)
                    }
                }
                return true
            } // TODO("Can be done better. Different lists for percepts and self sourced beliefs; update events are not curr. generated)
            else -> return false
        }
    }

    override fun remove(belief: ASBelief): Boolean = when (getSolutionOf(belief).isNo) {
        false -> true.also {
            val match = beliefs.first { it == belief }
            events.add(BeliefBaseRemoval(match))
            beliefs.remove(match)
        }
        else -> false
    }

    override fun add(belief: ASBelief): Boolean = when (getSolutionOf(belief).isNo) {
        // There's no Belief that unify the param inside the MultiSet, so it's inserted
        true -> {
            beliefs.add(belief)
            events.add(BeliefBaseAddition(belief))
            true
        }
        // There are Beliefs that unify the param, so the belief it's not inserted
        else -> false
    }

    // override val size = beliefs.size

    // override fun count(): Int = beliefs.size

    // override fun isEmpty() = beliefs.isEmpty()

    // override fun iterator() = beliefs.filterIsInstance<Belief>().iterator()

    // override fun containsAll(elements: Collection<Belief>): Boolean = beliefs.containsAll(
    //    elements.filterIsInstance<ASBelief>()
    // )

    // override fun contains(element: Belief) = when (element) {
    //    is ASBelief -> beliefs.contains(element)
    //    else -> throw IllegalArgumentException("Expected an instance of [ASBelief], but got ")
    // }

    override fun toString(): String =
        beliefs.joinToString { ASBelief.from(it.content.castToRule()).toString() }

    companion object {
        private val operatorExtension = Theory.of(
            Jakta.parseClause("&(A, B) :- A, B"),
            Jakta.parseClause("|(A, _) :- A"),
            Jakta.parseClause("|(_, B) :- B"),
            Jakta.parseClause("~(X) :- not(X)"),
        )
    }
}
