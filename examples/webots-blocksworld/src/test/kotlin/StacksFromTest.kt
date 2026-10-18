import kotlin.test.Test
import kotlin.test.assertEquals
import model.Block

class StacksFromTest {
    @Test
    fun boxesAreStackedBySlotAndHeight() {
        val positions = mapOf(
            "C" to doubleArrayOf(SLOT_DISTANCE, SLOTS[0], 0.085), // on A
            "A" to doubleArrayOf(SLOT_DISTANCE + 0.01, SLOTS[0] - 0.01, 0.035),
            "B" to doubleArrayOf(0.8, 0.5, 0.025), // fallen on the floor
        )
        assertEquals(
            listOf(listOf(Block("A"), Block("C")), listOf(), listOf(), listOf()),
            stacksFrom(positions),
        )
    }
}
