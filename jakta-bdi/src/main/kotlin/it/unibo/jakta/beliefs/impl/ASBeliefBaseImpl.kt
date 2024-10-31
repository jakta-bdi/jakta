package it.unibo.jakta.beliefs.impl

import it.unibo.jakta.Jakta
import it.unibo.jakta.beliefs.ASBelief
import it.unibo.jakta.beliefs.ASBeliefBase
import it.unibo.jakta.beliefs.ASMutableBeliefBase
import it.unibo.jakta.beliefs.BeliefBase
import it.unibo.tuprolog.collections.ClauseMultiSet
import it.unibo.tuprolog.core.Struct
import it.unibo.tuprolog.solve.Solver
import it.unibo.tuprolog.solve.flags.TrackVariables
import it.unibo.tuprolog.solve.flags.Unknown
import it.unibo.tuprolog.theory.Theory
import it.unibo.tuprolog.unify.Unificator

internal data class ASBeliefBaseImpl(
    private var beliefs: ClauseMultiSet,
    override var delta: List<BeliefBase.Update<ASBelief>> = emptyList(),
) : ASMutableBeliefBase, ASBeliefBase {

    constructor() : this(ClauseMultiSet.empty(Unificator.default))

    override fun snapshot(): ASBeliefBase = this.copy()

    override fun select(query: Struct): List<ASBelief> {
        val solution = Solver.prolog.newBuilder()
            .flag(Unknown, Unknown.FAIL)
            .staticKb(operatorExtension + Theory.of(beliefs))
            .flag(TrackVariables) { ON }
            .build()
            .solveOnce(query)
        return if (solution.isYes && solution.solvedQuery != null) {
            listOf(ASBelief.wrap(solution.solvedQuery!!))
        } else {
            emptyList()
        }
    }

    override fun select(query: ASBelief) = select(query.content.head)

    override fun update(belief: ASBelief): Boolean {
        val element = beliefs.find { it.head?.functor == belief.content.head.functor }
        return if (element != null) {
            beliefs = ClauseMultiSet.of(Unificator.default, beliefs.filter { it != belief }).add(belief.content)
            true
        } else {
            false
        }
    }

    override fun remove(belief: ASBelief): Boolean = when (beliefs.count(belief.content)) {
        0L -> false
        else -> true.also {
            val match = beliefs.filterIsInstance<ASBelief>().first { it == belief }
            delta += BeliefBase.Update.Removal(match)
            beliefs = ClauseMultiSet.of(Unificator.default, beliefs.filter { it != belief })
        }
    }

    override fun add(belief: ASBelief): Boolean = when (beliefs.count(belief.content)) {
        // There's no Belief that unify the param inside the MultiSet, so it's inserted
        0L -> true.also {
            beliefs.add(belief.content)
            delta += BeliefBase.Update.Addition(belief)
        }
        // There are Beliefs that unify the param, so the belief it's not inserted
        else -> false
    }

    override val size = beliefs.size

    override fun isEmpty() = beliefs.isEmpty()

    override fun iterator() = beliefs.filterIsInstance<ASBelief>().iterator()

    override fun containsAll(elements: Collection<ASBelief>) = beliefs.containsAll(elements.map { it.content })

    override fun contains(element: ASBelief) = beliefs.contains(element.content)

    override fun toString(): String =
        beliefs.joinToString { ASBelief.from(it.castToRule()).toString() }

    companion object {
        private val operatorExtension = Theory.of(
            Jakta.parseClause("&(A, B) :- A, B"),
            Jakta.parseClause("|(A, _) :- A"),
            Jakta.parseClause("|(_, B) :- B"),
            Jakta.parseClause("~(X) :- not(X)"),
        )
    }
}
