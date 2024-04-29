package it.unibo.alchemist.jakta.utils

import it.unibo.tuprolog.core.Term
import it.unibo.tuprolog.solve.libs.oop.ObjectRef

inline fun <reified T> Term.fix(): T = castTo<ObjectRef>().`object` as T
