package it.unibo.jakta.belief

/**
 * The belief revision function of an agent, as Jason's `brf`: it decides how each requested change to the beliefs
 * — believing, forgetting, or replacing those in a scope — changes the belief base, and which belief events it raises.
 * The default, [plain], treats beliefs as a set; other revisions can, for instance, merge the sources of equal beliefs,
 * keep a single value for some beliefs, or keep the beliefs consistent.
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
     * Revises [beliefs], the current content of the belief base, so that the beliefs in [scope] become [replacements].
     * By default, the beliefs in scope that are not among the replacements are removed, and the replacements that are
     * not held are added, comparing beliefs by equality.
     */
    fun replace(beliefs: Collection<Belief>, scope: (Belief) -> Boolean, replacements: List<Belief>): Revision<Belief> {
        val new = replacements.distinct()
        val gone = beliefs.filter { scope(it) && it !in new }
        val coming = new.filter { it !in beliefs }
        return Revision(stored = coming, dropped = gone, added = coming, removed = gone)
    }

    /**
     * The outcome of a revision. Nothing changes, and no event is raised, if all the lists are empty.
     * The belief base drops [dropped] first, then stores [stored] in order.
     *
     * @property stored the beliefs to put in the belief base, each replacing the equal belief already there, if any.
     * @property dropped the beliefs to take out of the belief base.
     * @property added the beliefs announced as added, each raising a belief addition event.
     * @property removed the beliefs announced as removed, each raising a belief removal event.
     */
    data class Revision<Belief : Any>(
        val stored: List<Belief> = emptyList(),
        val dropped: List<Belief> = emptyList(),
        val added: List<Belief> = emptyList(),
        val removed: List<Belief> = emptyList(),
    ) {
        /**
         * This revision followed by [next], provided that [next] does not drop what this revision stores
         * (the belief base applies every drop before every store).
         */
        operator fun plus(next: Revision<Belief>) = Revision(
            stored = stored + next.stored,
            dropped = dropped + next.dropped,
            added = added + next.added,
            removed = removed + next.removed,
        )
    }

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
        else -> BeliefRevision.Revision(stored = listOf(belief), added = listOf(belief))
    }

    override fun remove(beliefs: Collection<Belief>, belief: Belief) = when (belief) {
        in beliefs -> BeliefRevision.Revision(dropped = listOf(belief), removed = listOf(belief))
        else -> BeliefRevision.Revision()
    }
}
