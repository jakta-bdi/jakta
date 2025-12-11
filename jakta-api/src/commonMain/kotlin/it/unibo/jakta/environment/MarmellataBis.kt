package it.unibo.jakta.environment

context(t: T)
fun <T> test(block:  T.()-> Unit){
    t.block()
}

context(t: T, p: P)
fun <T, P> test(block:  (T, P)-> Unit){
        block(t,p)
}

context(x : X)
fun <X> testLambdaContext (block: context(X) () -> Unit) {
    block(x)
}

// TODO Questo sarebbe fattibile, ma poi esplode quando prova a risolvere gli overload
//context(x : X, y: Y)
//fun <X, Y> testLambdaContext (block: context(X, Y) () -> Unit) {
//    block(x, y)
//}

class Pippo {
    fun pippo() {}
}

class Pluto {
    fun pluto() {}
}

fun main() {

    //Finchè hai un solo contesto tutto ok
    context(Pluto()) {
        test {
            pluto()
        }
    }

    //Quando ne hai di più però...
    context(Pippo(), Pluto()) {
        test<Pippo, Pluto> {t, p ->
            t.pippo()
            p.pluto()
        }
    }

    context(Pippo()) {
        testLambdaContext {
            // non ho reference a Pippo
            // ZIO BRICCO
        }
    }
}


