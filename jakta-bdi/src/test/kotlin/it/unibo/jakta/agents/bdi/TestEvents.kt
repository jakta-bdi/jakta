package it.unibo.jakta.agents.bdi

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import it.unibo.jakta.agents.bdi.beliefs.Belief
import it.unibo.jakta.agents.bdi.events.Event
import it.unibo.jakta.agents.bdi.intentions.Intention
import it.unibo.tuprolog.core.Atom

class TestEvents : DescribeSpec({
    val belief = Belief.fromSelfSource(Atom.of("something"))
    val externalEvent = Event.ofBeliefBaseAddition(belief)
    val internalEvent = Event.ofBeliefBaseRemoval(belief, Intention.of())

    describe("An Event") {
        it("can be internal or external") {
            internalEvent.isInternal() shouldBe true
            internalEvent.isExternal() shouldBe false

            externalEvent.isExternal() shouldBe true
            externalEvent.isInternal() shouldBe false
        }
    }
})
