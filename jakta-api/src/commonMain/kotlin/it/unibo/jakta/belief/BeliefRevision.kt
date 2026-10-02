package it.unibo.jakta.belief

/**
 * The belief revision function of an agent, as Jason's `brf`: it decides how each requested addition or removal of
 * a belief changes the belief base, and which belief events it raises.
 *
 * @param Belief The type representing the agent's *beliefs*.
 */
interface BeliefRevision<Belief : Any> {

    /**
     * Revises [beliefs], the current content of the belief base, to add [belief].
     */
    fun add(beliefs: Collection<Belief>, belief: Belief): Revision<Belief>

    /**
     * Revises [beliefs], the current content of the belief base, to remove [belief].
     */
    fun remove(beliefs: Collection<Belief>, belief: Belief): Revision<Belief>

    /**
     * The outcome of a revision. Nothing changes, and no event is raised, if all the sets are empty.
     *
     * @property stored the beliefs to put in the belief base, each replacing the equal belief already there, if any.
     * @property dropped the beliefs to take out of the belief base.
     * @property added the beliefs announced as added, each raising a belief addition event.
     * @property removed the beliefs announced as removed, each raising a belief removal event.
     */
    data class Revision<Belief : Any>(
        val stored: Set<Belief> = emptySet(),
        val dropped: Set<Belief> = emptySet(),
        val added: Set<Belief> = emptySet(),
        val removed: Set<Belief> = emptySet(),
    )

    /**
     * Factory methods for [BeliefRevision].
     */
    companion object {
        /**
         * The default revision, treating the beliefs as a set: a belief is added if no equal belief is held,
         * and removed if an equal belief is held, raising the corresponding event; otherwise nothing changes.
         */
        fun <Belief : Any> plain(): BeliefRevision<Belief> = PlainBeliefRevision()
    }
}

private class PlainBeliefRevision<Belief : Any> : BeliefRevision<Belief> {
    override fun add(beliefs: Collection<Belief>, belief: Belief) = when (belief) {
        in beliefs -> BeliefRevision.Revision()
        else -> BeliefRevision.Revision(stored = setOf(belief), added = setOf(belief))
    }

    override fun remove(beliefs: Collection<Belief>, belief: Belief) = when (belief) {
        in beliefs -> BeliefRevision.Revision(dropped = setOf(belief), removed = setOf(belief))
        else -> BeliefRevision.Revision()
    }
}
