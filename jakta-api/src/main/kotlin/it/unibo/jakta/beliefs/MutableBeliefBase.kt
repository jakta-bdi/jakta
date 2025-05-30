package it.unibo.jakta.beliefs

interface MutableBeliefBase<Belief : Any> :
    BeliefBase<Belief>,
    MutableCollection<Belief> {

    /**
     * @return the immutable [BeliefBase] version of this instance.
     */
    fun snapshot(): BeliefBase<Belief>

    companion object {
        /** @return an empty [MutableBeliefBase] */
        fun <Belief : Any> empty(): MutableBeliefBase<Belief> = BeliefBaseImpl()

        /**
         * Generates a [ASBeliefBase] from a collection of [Belief]
         * @param beliefs: the [Iterable] of [Belief] the [ASBeliefBase] will be composed of
         * @return the new [ASBeliefBase]
         */
        fun <Belief : Any> of(beliefs: Iterable<Belief>): MutableBeliefBase<Belief> =
            BeliefBaseImpl(beliefs.toMutableSet())

        fun <Belief : Any> of(vararg beliefs: Belief): MutableBeliefBase<Belief> = of(beliefs.asList())
    }
}
