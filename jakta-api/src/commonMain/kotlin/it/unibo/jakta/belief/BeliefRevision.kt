package it.unibo.jakta.belief

/**
 * The belief revision function of an agent, as Jason's `brf`: it decides how each requested change to the beliefs
 * — believing, forgetting, or replacing those in a scope — changes the belief base, and which belief events it raises.
 * By default, it treats beliefs as a set ([plain]): a revision overrides only the operations it changes, for instance
 * to merge the sources of equal beliefs, keep a single value for some beliefs, or keep the beliefs consistent.
 *
 * Beliefs may carry metadata, such as their sources or a confidence: the core never looks at it, and it is the belief
 * type and the revision chosen for it that give it meaning. For that:
 * - equal beliefs are the same belief, whatever their metadata: the belief type's equality ignores it;
 * - each operation carries the metadata it is about inside the beliefs it is given: [add] and [remove] add or remove
 *   the metadata of their belief, and the scope of [replace] says, for each belief held, the belief to remove from it;
 * - the revision decides how the metadata of equal beliefs combine, and what each change announces.
 *
 * Beliefs without metadata need none of this: an equal belief is simply the same belief.
 *
 * @param Belief The type representing the agent's *beliefs*.
 */
interface BeliefRevision<Belief : Any> {

    /**
     * How the belief base, holding [current], changes when asked to believe [toAdd].
     * By default, [toAdd] is stored and announced as added, unless an equal belief is already held.
     */
    fun add(current: Collection<Belief>, toAdd: Belief): Revision<Belief> = when (toAdd) {
        in current -> Revision()
        else -> Revision(add = listOf(toAdd), announceAdded = listOf(toAdd))
    }

    /**
     * How the belief base, holding [current], changes when asked to forget [toRemove].
     * By default, the equal belief held, if any, is dropped and announced as removed.
     */
    fun remove(current: Collection<Belief>, toRemove: Belief): Revision<Belief> = when (toRemove) {
        in current -> Revision(remove = listOf(toRemove), announceRemoved = listOf(toRemove))
        else -> Revision()
    }

    /**
     * How the belief base, holding [current], changes when asked that the beliefs in [scope] are now [replacements].
     * The [scope] gives, for each belief held, the belief to remove from it (all of it, or only some of its metadata),
     * or `null` if the belief is out of scope: what it gives is removed, unless it is among the replacements.
     * By default, comparing beliefs by equality, the beliefs in scope that are not among the replacements are dropped,
     * and the replacements that are not held are stored, each announced as removed or added.
     */
    fun replace(current: Collection<Belief>, replacements: List<Belief>, scope: (Belief) -> Belief?): Revision<Belief> {
        val incoming = replacements.distinct()
        val outdated = current.mapNotNull(scope).filter { it !in incoming }
        val new = incoming.filter { it !in current }
        return Revision(add = new, remove = outdated, announceAdded = new, announceRemoved = outdated)
    }

    /**
     * The outcome of a revision: how the belief base changes ([add], [remove]), and which belief events are raised
     * ([announceAdded], [announceRemoved]). The two are separate, so that, e.g., storing a belief with a new source can
     * announce only the addition of that source. Nothing changes, and no event is raised, if all the lists are empty.
     * The belief base drops [remove] first, then stores [add] in order.
     *
     * @property add the beliefs to put in the belief base, each replacing the equal belief already there, if any.
     * @property remove the beliefs to take out of the belief base.
     * @property announceAdded the beliefs announced as added, each raising a belief addition event.
     * @property announceRemoved the beliefs announced as removed, each raising a belief removal event.
     */
    data class Revision<Belief : Any>(
        val add: List<Belief> = emptyList(),
        val remove: List<Belief> = emptyList(),
        val announceAdded: List<Belief> = emptyList(),
        val announceRemoved: List<Belief> = emptyList(),
    ) {
        /**
         * This revision followed by [next], provided that [next] does not remove what this revision stores
         * (the belief base applies every remove before every add).
         */
        operator fun plus(next: Revision<Belief>) = Revision(
            add = add + next.add,
            remove = remove + next.remove,
            announceAdded = announceAdded + next.announceAdded,
            announceRemoved = announceRemoved + next.announceRemoved,
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
        fun <Belief : Any> plain(): BeliefRevision<Belief> = object : BeliefRevision<Belief> {}
    }
}
