# jakta-llm-incarnation

Natural-language incarnation of [JaKtA](https://github.com/jakta-bdi/jakta), a Kotlin Multiplatform framework for
BDI (Belief-Desire-Intention) agent-oriented programming.

Beliefs and goals are plain `String`s. Plans are ordinary JaKtA plans, whose triggers and guards are written in
natural language with `{parameters}`. They match literally, or by meaning according to a language model called
through [Koog](https://github.com/JetBrains/koog), which also extracts the parameters' values.
Plan bodies can also run test queries on the beliefs and revise contradicted beliefs.
Plans are selected synchronously, so the agent blocks while the model answers: hence it runs on the JVM (17+) only.

```kotlin
with(LlmReasoner(executor, OpenAIModels.Chat.GPT5_6Luna, maxQuestions = 100)) {
    agent {
        embodiedAs { Any() }
        believes { +"Bob is a friend" }
        hasInitialGoals { !"say hi to Bob" }
        hasPlanLibrary {
            adding.goal { meaning("greet {name}") } onlyWhen { holds("{name} is a friend") } triggers {
                agent.print("Hello, ${context["name"]}!")
            }
            failing.goal { meaning("{task}") } triggers { agent.print("I cannot ${context["task"]}") }
        }
    }
}
```

Licensed under Apache-2.0.
