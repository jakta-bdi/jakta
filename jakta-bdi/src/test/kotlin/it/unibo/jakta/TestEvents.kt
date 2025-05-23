package it.unibo.jakta

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import it.unibo.jakta.beliefs.ASBelief
import it.unibo.jakta.events.BeliefBaseAddition
import it.unibo.jakta.events.BeliefBaseRemoval
import it.unibo.jakta.intentions.ASIntention
import it.unibo.tuprolog.core.Atom

class TestEvents : DescribeSpec({
    val belief = ASBelief.fromSelfSource(Atom.of("something"))
    val externalEvent = BeliefBaseAddition(belief)
    val internalEvent = BeliefBaseRemoval(belief, ASIntention.of())

    describe("An Event") {
        it("can be internal or external") {
            internalEvent.isInternal() shouldBe true
            internalEvent.isExternal() shouldBe false

            externalEvent.isExternal() shouldBe true
            externalEvent.isInternal() shouldBe false
        }
    }
})
