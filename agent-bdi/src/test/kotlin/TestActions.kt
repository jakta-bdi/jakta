import io.github.anitvam.agents.bdi.goals.ActInternally
import io.github.anitvam.agents.bdi.goals.actions.ActionLibrary
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import it.unibo.tuprolog.core.Atom
import it.unibo.tuprolog.core.Struct

class TestActions : DescribeSpec({
    val library = ActionLibrary.default()
    val goal = ActInternally(Struct.of("print", Atom.of("Hello World!")))

    describe("An Action") {
        it("should be selected from ActionLibrary properly") {
            val action = library.lookup(goal)
            action.name shouldBe "print"
        }
        it("should be executed with the exact number of parameters") {
            val substitution = library.invoke(goal)
            substitution.isSuccess shouldBe true
            substitution.size shouldBe 0
        }
    }
})
