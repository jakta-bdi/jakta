package it.unibo.jakta.agents.bdi.dsl

import io.kotest.core.spec.style.DescribeSpec
import it.unibo.jakta.agents.bdi.messages.Achieve
import it.unibo.jakta.agents.bdi.messages.Message
import it.unibo.jakta.agents.bdi.messages.Tell
import it.unibo.tuprolog.core.Atom
import it.unibo.tuprolog.core.Struct

class TestMessagePassing : DescribeSpec({
    describe("Agent") {
        it("should communicate using messages") {
            mas {
                environment {
                    actions {
                        action("send", 3) {
                            val receiver: Atom = argument(0)
                            val type: Atom = argument(1)
                            val message: Struct = argument(2)
                            when (type.value) {
                                "tell" -> sendMessage(receiver.value, Message(this.sender, Tell, message))
                                "achieve" -> sendMessage(
                                    receiver.value,
                                    Message(this.sender, Achieve, message),
                                )
                            }
                        }
                    }
                }
                agent("pinger") {
                    beliefs {
                        fact { "turn"("me") }
                        fact { "other"("ponger") }
                    }
                    goals {
                        achieve("send_ping")
                    }
                    plans {
                        +achieve("send_ping") onlyIf {
                            "turn"("source"("self"), "me") and "other"("source"("self"), R)
                        } then {
                            update("turn"("source"("self"), "other"))
                            achieve("sendMessageTo"("ball", R))
                        }

                        +"ball"("source"(R)) onlyIf {
                            "turn"("source"("self"), "other") and "other"("source"("self"), R)
                        } then {
                            update("turn"("source"("self"), "me"))
                            iact("print"("Received ball from ", R))
                            -"ball"("source"(R))
                            iact("print"("Pinger hasDone"))
                        }

                        +achieve("sendMessageTo"(M, R)) then {
                            iact("print"("Sending message ", M))
                            execute("send"(R, "tell", M))
                        }
                    }
                }
                agent("ponger") {
                    beliefs {
                        fact { "turn"("other") }
                        fact { "other"("pinger") }
                    }
                    plans {
                        +"ball"("source"(S)) onlyIf {
                            "turn"("source"("self"), "other") and "other"("source"("self"), S)
                        } then {
                            update("turn"("source"("self"), "me"))
                            -"ball"("source"(S))
                            achieve("sendMessageTo"("ball", S))
                            achieve("handle_ping")
                        }

                        +achieve("handle_ping") then {
                            update("turn"("source"("self"), "other"))
                            iact("print"("Ponger has Done"))
                        }

                        +achieve("sendMessageTo"(M, R)) then {
                            iact("print"("Sending message ", M))
                            execute("send"(R, "tell", M))
                        }
                    }
                }
            }.start()
        }
    }
})
