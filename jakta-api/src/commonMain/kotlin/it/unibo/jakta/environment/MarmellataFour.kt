package it.unibo.jakta.environment

import it.unibo.jakta.environment.XSkill.Companion.xSkill
import it.unibo.jakta.environment.YSkill.Companion.ySkill


fun <X> writePlan(block: context(X) () -> Unit): context(X) () -> Unit {
    return block
}

fun <X, Y> writePlan(block: context(X, Y) () -> Unit): context(X, Y) () -> Unit {
    return block
}


class XSkill {
    fun x() {}

    companion object {
        context(x: XSkill)
        val xSkill get() = x
    }
}

class YSkill {
    fun y() {}

    companion object {
        context(y: YSkill)
        val ySkill get() = y
    }
}

fun main() {

    // define the plan depending on some skills
    val plan = writePlan<XSkill,YSkill> {
        xSkill.x()
        ySkill.y()
    }

    context(XSkill(), YSkill()) {
        plan() // actually run the plan
    }

}
