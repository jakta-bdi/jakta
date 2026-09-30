# Exploration: an LLM incarnation of JaKtA with Koog

Branch `feat/llm-incarnation`. New modules:

- `jakta-llm-incarnation`: the incarnation (JVM + JS);
- `examples:llm-smart-home`: a runnable example and a live-LLM test, skipped unless a provider is configured.

Beliefs and goals are `String`s. Plans have natural-language triggers and conditions with `{parameters}`.
One structured-output LLM call per event picks the plan and fills in its parameters.

```kotlin
with(LlmReasoner(executor, model, maxCalls = 20)) {
    agent<String, String, Any> {
        embodiedAs { Any() }
        believes { +"the living room window is open"; +"the heating is off" }
        hasInitialGoals { !"Brr, it's freezing in here!" }
        hasPlanLibrary {
            llmPlans {
                goal("make the room warmer", onlyWhen = "a window is open") {
                    agent.forget("the living room window is open")
                    agent.believe("the living room window is closed")
                    agent.achieve("set the heating to 21 degrees")
                }
                goal("set the heating to {degrees} degrees") {
                    revise("the heating is set to ${context["degrees"]} degrees") // LLM belief revision
                }
                belief("the temperature is {degrees} degrees") { /* ... */ }
                failure("{request}") { agent.print("Sorry, I cannot ${context["request"]}") }
            }
            // test("the door is {state}") in any body: an LLM test goal, returns the bindings or null
        }
    }
}
```

## 1. Koog findings

| | |
|---|---|
| Version | **1.3.0** (GitHub release and Maven Central, 2026-09-24; 1.2.0 was 2026-08-28). |
| Built with | Kotlin 2.3.10. It works from this repo's Kotlin 2.4.20, which the root build forces on every `org.jetbrains.kotlin` dependency. |
| JVM | Bytecode is **Java 17** (class version 61). The module uses `configureKotlinMultiplatform(targetJvm = JVM_17)`, while the other modules target 1.8. Otherwise inlining `executeStructured` fails. |
| Published targets | `jvm`, `js` (IR), `wasmJs`, `android`, `iosArm64`, `iosX64`, `iosSimulatorArm64`. |
| Missing targets | **No `linuxX64`, `linuxArm64`, `mingwX64`, `macosArm64`**, which `jakta-api/core/string-incarnation` build for. The module therefore uses `includeNative = false` (JVM + JS, like the Prolog and RDF incarnations). Koog does support wasmJs and iOS, but the JaKtA build doesn't enable those targets, so I skipped them. |

Artifacts (group `ai.koog`):

- `prompt-executor-model`: the only dependency of the incarnation (`api`). It provides `PromptExecutor`, the `prompt {}` DSL, `executeStructured<T>`, `LLModel`, `LLMCapability` and `LLMParams`, plus `@LLMDescription` through `agents-tools`. On the JVM it also brings the OpenAI, Anthropic and Ollama clients and Ktor CIO.
- `agents-test`: test only. `getMockExecutor { mockLLMAnswer(json) onRequestContains "..." / onCondition { } / asDefaultResponse }`. It matches on the text of the **last** message of the prompt.
- `http-client-ktor`: runtime only, in the example. The JVM clients built without a factory (e.g. `OpenAILLMClient(key)`) find their HTTP client through `ServiceLoader`.
- `prompt-executor-llms-all` (`simpleOpenAIExecutor`, `simpleAnthropicExecutor`, `simpleOllamaAIExecutor`) is **published only as `1.3.0-beta`**. I avoided it: `MultiLLMPromptExecutor(OpenAILLMClient(key))` gives the same result. On JS, the `simple*` and client constructors need an explicit `KoogHttpClient.Factory`.

API notes that differ from older docs and from memory:

- `PromptExecutor.execute(...)` returns a single `Message.Assistant`, not a list.
- `executeStructured<T>(prompt, model)` returns a `Result<StructuredResponse<T>>`. It uses native JSON-schema output if the `LLModel` declares `Schema.JSON.Standard` or `Basic`. Otherwise it uses "manual" mode, which **appends a user message** with format instructions. That's why the offline tests use `OpenAIModels.Chat.GPT5_6Luna` (native): the mock then sees the prompt unchanged.
- Model capabilities are data on the `LLModel`, so a model declared without `Temperature` should not get a temperature. I haven't checked whether every client filters it out, so the incarnation sends `temperature = 0` only when the model declares the capability.

Available but not used (each is a drop-in when needed):

- Tool calling (`ToolDescriptor`, `agents-tools`). One structured answer per event fits plan selection better than a tool loop.
- Embeddings: `embeddings-base` `Embedder.embed(text): Vector` / `diff`, and `embeddings-llm` `LLMEmbedder(client, model)`. This is the natural cheap pre-filter.
- Caching: `prompt-executor-cached` `CachedPromptExecutor(cache, nested)`. It wraps any executor with no change to the incarnation.
- Robustness: `StructureFixingParser` (a second LLM call repairs malformed JSON) and `MultiLLMPromptExecutor` fallback settings.

## 2. How JaKtA incarnations work

- `jakta-api` and `jakta-core` are generic over `Belief`, `Goal` and `Body`. A plan is a synchronous `trigger: (Entity) -> Context?`, a synchronous `guard: GuardScope<Belief, Context>.() -> Context?`, and a `suspend` body receiving the `Context`.
- `BaseAgentLifecycle.selectPlan` runs *synchronously in the agent loop*. It filters the plans of the event's kind by `isRelevant` (trigger non-null and result type compatible), then by `isApplicable` (guard non-null), and takes the first. When nothing applies, a goal addition becomes a `GoalFailedEvent`, a failure completes the waiting `achieve` exceptionally, and a belief event is logged.
- The **string incarnation** matches by equality or regex, and no data reaches the body.
- The **Prolog incarnation** unifies terms and carries a `MutableSubstitutionPlanContext` from trigger to guard to body. Its guards call a Prolog solver, and KQML handles messaging.

## 3. Design

### The central constraint

Triggers and guards are **synchronous and evaluated one plan at a time**, while an LLM call is `suspend`, slow and costly. I considered three options:

1. **Make triggers and guards `suspend` in core.** This is the principled fix, but it's an API change across `jakta-api`, `jakta-core` and every incarnation, and it would stall the agent loop on the network anyway. Out of scope.
2. **`runBlocking` inside guards.** This doesn't exist on JS, blocks the agent loop, and costs one call per candidate plan. Rejected.
3. **Chosen: one dispatcher plan per event kind.** `llmPlans { }` registers one ordinary JaKtA plan for each of *goal added*, *belief added* and *goal failed*, but only for the kinds that have LLM plans. Each dispatcher is relevant for every event of its kind. Its body is `suspend` and runs inside the intention, so the agent keeps processing other events while the LLM answers. The body makes **one** structured-output call over the beliefs, the event and all the LLM plans of that kind (batching for free), then runs the chosen plan's body with an `LlmContext(event, bindings)`.

Consequences:

- Semantics match JaKtA's first-applicable rule, but "first" is judged by the LLM over the numbered list.
- Ordinary JaKtA plans declared *before* `llmPlans` take precedence, and those after it are shadowed for that kind. They can be mixed: the example handles failures with a plain `failing.goal { this }`, so failures cost no LLM call.
- LLM plans return `Unit`, because a dispatcher has one result type. `achieveWithResult<T>` isn't supported.

### Bindings: the "unification"

Parameters are written inside the text, as in `"set the heating to {degrees} degrees"`. The prompt lists each option's parameters, and the LLM returns `bindings: [{parameter, value}]`. A condition can introduce parameters that are bound from the beliefs, as a Prolog guard can.

I used a list of objects rather than a map because it works with strict JSON schemas (OpenAI strict mode rejects open maps). Values are strings, and the body converts them.

### Queries and belief revision

- The belief base is the core's `MutableSet<String>`: `believe` and `forget` are exact, with no LLM involved.
- `test("the door is {state}")` is an LLM test goal. It returns the bindings (`{state=closed}`) or `null`.
- `revise(belief)` asks the LLM which current beliefs the new one contradicts or makes obsolete, forgets them, then believes the new one. So `revise("the heating is set to 23 degrees")` retracts both "the heating is off" and "...21 degrees".
- Revision is **explicit, not automatic**. The core creates the belief base internally, with no hook to customise it, and a revision call on every `believe` would double the cost of every belief change.

### Failure handling

| Situation | Result |
|---|---|
| No LLM plan applies (the LLM answers `0`) | A goal fails and becomes a `GoalFailedEvent`, handled by LLM `failure(...)` plans or plain `failing.goal`. An added belief is ignored, logged at debug level. |
| Executor error (network, auth, OOM-killed local model) | The exception propagates out of the plan body and the goal fails, as above. Seen live: an OOM-killed Ollama led to every request going to the failure plan, and the agent carried on. |
| Malformed output (bad JSON, option out of range, a parameter left unbound, a retraction index out of range) | `require` fails and the goal fails. The chosen body never runs. |
| The LLM keeps sending a goal back to the plan that posted it | `LlmReasoner(..., maxCalls)`: once the budget is spent every call throws, so goals fail and the agent terminates. |
| A failure that no failure plan handles | The core completes the parent's `achieve` exceptionally, so the parent fails too. In the example this used to leave `terminateNode` unreached (a hang), which is why the example uses a catch-all failure plan. |

### Determinism and testability

- `LlmReasoner` takes any Koog `PromptExecutor`, so there is no extra abstraction: tests pass Koog's `getMockExecutor`.
- The 7 offline tests pass on **JVM, JS/node and JS/browser**. They cover:
  - selecting the first applicable plan, including the exact prompt text;
  - no applicable plan leading to an LLM failure plan;
  - malformed JSON leading to goal failure;
  - an unbound parameter leading to goal failure;
  - belief-triggered plans;
  - `test` (true with bindings, and false) and `revise`;
  - the call budget stopping a loop.
- Live runs use `temperature = 0` where the model supports it. They are still not deterministic.
- `examples:llm-smart-home:test` is the gated integration test. It uses `assumeTrue`, so it's *skipped*, not passed, when neither `JAKTA_LLM_PROVIDER`, `ANTHROPIC_API_KEY` nor `OPENAI_API_KEY` is set. It has a 10-minute timeout and a 20-call budget.

### Cost and latency

- A goal event costs 1 call. A failure handled by LLM failure plans costs 1 more.
- A belief event costs 1 call **only if** the agent has LLM belief plans; otherwise no dispatcher is registered. With belief plans, *every* belief change calls the LLM, including those made by bodies and by `revise`.
- `test` and `revise` cost 1 call each.
- The prompt grows linearly with beliefs × plans. There is no retrieval or pre-filter yet.
- Not implemented (upgrade paths):
  - an exact-match or embedding pre-filter before the call: `Embedder` exists, and the risk is breaking first-applicable order;
  - caching: `CachedPromptExecutor`, keyed on the whole prompt, which includes the beliefs, so it's safe;
  - a cheaper model for revision than for selection.

## 4. What was run

- `./gradlew :jakta-llm-incarnation:check`: green (compile JVM and JS, 7 tests × 3 platforms, ktlint, detekt). Also passed by the pre-commit hook on each commit.
- `./gradlew :examples:llm-smart-home:check`: green, with the live test skipped because there are no API keys on this machine.
- **No run against OpenAI or Anthropic**: no key was available. The default models (`GPT5_6Luna`, `Haiku_4_5`) are therefore unverified live.
- **Live runs against a local Ollama** (CPU only, about 5 to 20 s per call):
  - `qwen2.5-coder:7b` (the example's `JAKTA_LLM_MODEL` override):
    - Correct: "Brr, it's freezing" picked *make the room warmer while a window is open*. The window closed, the heating went to 21, and `revise` retracted "the heating is off". The pizza request got 0 plans and went to the failure plan.
    - Wrong: for "Make it 23 degrees" and for the subgoal "set the heating to 21 degrees", the model repeatedly chose plan 1. It even wrote that plan 1's condition was false. Plan 1 posts that same subgoal, so it looped until `maxCalls` stopped it; before the budget existed, it ran until the timeout.
    - Also wrong, in earlier runs: `bindings: []`, which validation caught, and a wrong plan number. Listing parameters per option and matching triggers "by meaning" (not "as an instance") fixed the missing bindings and the pizza case.
  - `qwen2.5-coder:14b`: Ollama was OOM-killed on this machine, so there's no quality signal. It did exercise the executor-error path.

## 5. How to run the example with a real model

```bash
# Anthropic (default when ANTHROPIC_API_KEY is set): claude-haiku-4-5
ANTHROPIC_API_KEY=... ./gradlew :examples:llm-smart-home:run
# OpenAI (default when OPENAI_API_KEY is set): gpt-5.6-luna
OPENAI_API_KEY=... ./gradlew :examples:llm-smart-home:run
# Local Ollama (default otherwise): llama3.2, or any pulled model
ollama serve & ollama pull llama3.2
JAKTA_LLM_PROVIDER=ollama ./gradlew :examples:llm-smart-home:run
JAKTA_LLM_PROVIDER=ollama JAKTA_LLM_MODEL=qwen2.5-coder:7b ./gradlew :examples:llm-smart-home:run
# The same scenario as an asserted integration test
ANTHROPIC_API_KEY=... ./gradlew :examples:llm-smart-home:test
```

- `JAKTA_LLM_PROVIDER` (`anthropic`, `openai`, `ollama`) forces the provider.
- `JAKTA_LLM_MODEL` replaces the default model's id and keeps its declared capabilities.
- Keys are only ever read from the environment.
- To see why the LLM chose each plan, set `Logger.setMinSeverity(Severity.Debug)` (the core is verbose at that level). The `reason` field of every answer is logged.

## 6. Limitations

- **Plan choice is only as good as the model.** Validation catches malformed answers, but not well-formed wrong ones, such as choosing a plan whose condition is false. Small local models do that often. See the first open question.
- JVM (17+) and JS only, with no native targets, because Koog doesn't publish them. No wasmJs or iOS, because JaKtA doesn't build those.
- No `removing.goal` / `removing.belief` LLM plans, and no plan results other than `Unit`.
- No messaging protocol. Map a `String` message to a belief such as `"bob says: ..."` with `handlesMessageEvents`, and LLM belief plans take it from there.
- The whole belief base goes into every prompt, with no retrieval, so this won't scale to large belief bases as is.
- `maxCalls` is a lifetime budget per `LlmReasoner`, not a rate: a long-running agent needs a large value, or a new reasoner.

## 7. Side findings in the existing code (not changed)

- `BeliefBaseImpl.snapshot()` is `this.copy()` of a data class, which shares the same `MutableSet`. So `agent.beliefs` is a **live view**, not a snapshot, and its `toString()` prints the implementation. The incarnation copies it (`toList()`) before numbering beliefs.
- On JS, Kotlin compiles `Regex` with the `u` flag, where an unescaped `}` is a syntax error (`\{(\w+)}` works on the JVM). The JS tests caught this here, and the pattern is worth keeping in mind for other incarnations.
- For every belief change without a matching plan, the core logs a *warning* ("No plan found for BeliefAddEvent"), which is noisy for agents that don't react to beliefs.

## 8. Open questions for you

1. **How much to trust the model's selection.** Should the answer list relevance and applicability *per plan* (`[{relevant, conditionHolds}]`), so that code picks the first applicable plan and can reject inconsistent answers? That's more output tokens, but it would have caught the 7B model's "condition is false, choose it anyway". *Default: not done; the model's `option` is trusted.*
2. **Suspending guards in core.** Is it worth making `trigger` and `guard` `suspend`? LLM plans could then be real JaKtA plans, freely mixed and ordered, at the cost of an API change and slow plan selection. *Default: not done; the dispatcher design works without touching core.*
3. **Automatic belief revision.** Should `believe` revise automatically? That needs a core hook for a custom `BeliefBase` and doubles the cost of every belief change. *Default: explicit `revise`.*
4. **Default models and budget.** The defaults are `claude-haiku-4-5` for Anthropic, `gpt-5.6-luna` for OpenAI and `llama3.2` for Ollama, with `maxCalls = 20` in the example and unlimited in the library. Should the library have a finite default budget? *Default: unlimited, documented.*
5. **Publishing.** Should `jakta-llm-incarnation` be published (Maven Central / npm) like the others? It inherits the publishing setup, and I didn't add it to the root Kover aggregation or to the website docs. *Default: left as-is for an exploration branch.*
6. **Pre-filter and caching.** Add an embedding pre-filter or `CachedPromptExecutor` now, or wait for a real workload to measure? *Default: wait.*
