# jakta-llm-incarnation

Natural-language incarnation of [JaKtA](https://github.com/jakta-bdi/jakta), a Kotlin Multiplatform framework for
BDI (Belief-Desire-Intention) agent-oriented programming.

Beliefs and goals are plain `String`s. Plans have natural-language triggers and conditions with `{parameters}`.
A language model, called through [Koog](https://github.com/JetBrains/koog), picks the plan for each event and
extracts its parameters, answers test queries on the beliefs, and revises contradicted beliefs.
It runs on the JVM (17+) and JS.

```kotlin
with(LlmReasoner(simpleOpenAIExecutor(apiKey), OpenAIModels.Chat.GPT5_6Luna)) {
    agent {
        embodiedAs { Any() }
        believes { +"Bob is a friend" }
        hasInitialGoals { !"say hi to Bob" }
        hasPlanLibrary {
            llmPlans {
                goal("greet {name}", onlyWhen = "{name} is a friend") {
                    agent.print("Hello, ${context["name"]}!")
                }
                failure("{task}") { agent.print("I cannot ${context["task"]}") }
            }
        }
    }
}
```

Licensed under Apache-2.0.
