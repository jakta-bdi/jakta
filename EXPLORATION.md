# Exploration: an LLM incarnation of JaKtA with Koog

Branch `feat/llm-incarnation`.

- `jakta-llm-incarnation`: the incarnation (JVM only, see [section 3](#3-v3-design-current)).
- `examples:llm-smart-home`: a runnable example, and a live-LLM test that is skipped unless a provider is configured.
- No change to `jakta-api`, `jakta-dsl` or `jakta-core`.

Beliefs and goals are `String`s. Plans are **ordinary JaKtA plans**. Their triggers and guards are written in
natural language with `{parameters}`, and match literally or, through a language model, by meaning. The engine's
own plan selection tries them in order and runs the first applicable one, blocking the agent while the LLM answers.

```kotlin
with(LlmReasoner(executor, model, maxQuestions = 100)) {
    agent<String, String, Any> {
        embodiedAs { Any() }
        believes { +"Bob is a friend" }
        hasInitialGoals { !"say hi to Bob" }
        hasPlanLibrary {
            adding.goal { meaning("greet {name}") } onlyWhen { holds("{name} is an enemy") } triggers { /* ... */ }
            adding.goal { meaning("greet {name}") } onlyWhen { holds("{name} is a friend") } triggers {
                agent.print("Hello, ${context["name"]}!")     // "say hi to Bob" means "greet {name}", name = Bob
                test("{name} is at home")                       // LLM test goal on the beliefs: bindings or null
                revise("Bob was greeted")                       // forgets the beliefs it contradicts, then believes it
            }
            failing.goal { meaning("{task}") } triggers { agent.print("I cannot ${context["task"]}") }
        }
    }
}
```

## 1. Koog findings

| | |
|---|---|
| Version | **1.3.0** (GitHub release and Maven Central, 2026-09-24; 1.2.0 was 2026-08-28). |
| Built with | Kotlin 2.3.10. It works from this repo's Kotlin 2.4.20, which the root build forces on every `org.jetbrains.kotlin` dependency. |
| JVM | Bytecode is **Java 17** (class version 61). The module targets JVM 17, like `alchemist-jakta-incarnation`, while the other modules target 1.8. Otherwise inlining `executeStructured` fails. |
| Published targets | `jvm`, `js` (IR), `wasmJs`, `android`, `iosArm64`, `iosX64`, `iosSimulatorArm64`. |
| Missing targets | **No `linuxX64`, `linuxArm64`, `mingwX64`, `macosArm64`**, which `jakta-api/core/string-incarnation` build for. v2 was JVM + JS; v3 blocks on the LLM calls, which JS cannot do, so it is a JVM-only module. |

Artifacts (group `ai.koog`):

- `prompt-executor-model`: the incarnation's only dependency (`api`). It provides `PromptExecutor`, the `prompt {}` DSL, `executeStructured<T>`, `LLModel`, `LLMCapability` and `LLMParams`, plus `@LLMDescription` through `agents-tools`. On the JVM it also brings the OpenAI, Anthropic and Ollama clients and Ktor CIO.
- `agents-test`: test only. `getMockExecutor { mockLLMAnswer(json) onCondition { ... } / onRequestContains "..." / asDefaultResponse }` matches on the text of the **last** prompt message.
- `http-client-ktor`: runtime only, in the example. The JVM clients built without a factory (e.g. `OpenAILLMClient(key)`) find their HTTP client through `ServiceLoader`.
- `prompt-executor-llms-all` (`simpleOpenAIExecutor`, ...) is published only as `1.3.0-beta`. I avoided it: `MultiLLMPromptExecutor(OpenAILLMClient(key))` is equivalent. On JS, clients need an explicit `KoogHttpClient.Factory`.

API notes:

- `PromptExecutor.execute(...)` returns a single `Message.Assistant`, not a list.
- `executeStructured<T>(prompt, model)` returns a `Result<StructuredResponse<T>>`. It uses native JSON-schema output when the `LLModel` declares `Schema.JSON.Standard` or `Basic`. Otherwise it uses "manual" mode, which **appends a user message** with format instructions. The offline tests therefore use a native model (`OpenAIModels.Chat.GPT5_6Luna`), so the mock sees the prompt unchanged.
- Capabilities are data on the `LLModel`. The incarnation sends `temperature = 0` only when the model declares `Temperature` (GPT-5 models don't).
- Available but unused, each a drop-in:
  - `CachedPromptExecutor(InMemoryPromptCache(n), executor)` from `prompt-executor-cached`. Its keys exclude timestamps, but are 32-bit hashes.
  - Embeddings: `Embedder` / `LLMEmbedder`.
  - Tool calling.
  - `StructureFixingParser`, which repairs malformed JSON with a second call.

## 2. How JaKtA incarnations work

- `jakta-api` and `jakta-core` are generic over `Belief`, `Goal` and `Body`. A plan has three parts:
  - a `trigger: (Entity) -> Context?`;
  - a `guard: GuardScope<Belief, Context>.() -> Context?`;
  - a `suspend` body receiving the `Context`.
- Incarnations provide trigger and guard helpers:
  - string: `ifGoalMatches`, `containsBeliefMatching`;
  - Prolog: unification, `satisfies { }` through a solver, plus KQML messaging;
  - RDF: SPARQL patterns.
- On `main`, triggers and guards are **synchronous**. The lifecycle selects a plan synchronously in the agent loop:
  1. filter the plans by kind and `isRelevant`;
  2. filter those by `isApplicable`;
  3. take the first.
- Then `run(agent, entity)` evaluates the trigger and the guard **again** to compute the context. In total, the selected plan's trigger runs 3 times and its guard 2 times.

## 3. v3 design (current)

v1 worked around synchronous triggers with wrapper plans ([section 4](#4-superseded-designs)). v2 made triggers and guards `suspend` in core. On review, you asked to keep the API and to block in the selection instead: v3 does that, with **no core change**.

### Blocking selection

- `meaning` and `holds` are ordinary (non-suspend) trigger and guard helpers. When the text does not match literally, they call the LLM inside `runBlocking`, so the agent's thread waits for the answer while the engine selects the plan.
- **JVM only**: JS has a single thread and no `runBlocking`, so a synchronous trigger cannot wait for an HTTP response there. The module is now a plain Kotlin/JVM module (`src/main`, `src/test`), like the Alchemist incarnation.
- **Errors do not hold**: on `main`, an exception thrown by a trigger or guard propagates out of the agent loop. So a malformed answer, an executor error or an exhausted budget makes the trigger or guard return null (logged as an error). If no plan is left, the goal fails and failure plans run, as before. `test` and `revise` run in plan bodies, so they still throw and fail the plan.
- **Remembered answers**: the engine evaluates the selected plan's trigger 3 times and its guard twice (`isRelevant`, `isApplicable`, then `run`). `LlmReasoner` remembers its latest 100 answers, keyed by the full question (for guards, it includes the beliefs). So the LLM is asked once per distinct question, and the repeated evaluations get the same answer, instead of failing with "Execution not possible without a plan context" when the model changes its mind.
- **Budget**: `maxQuestions` (was `maxCalls`) counts every question, including those answered from memory. Otherwise a plan that the LLM matches to its own subgoal would loop forever for free, since every iteration repeats a remembered question. The budget is consumed about 3 times faster than the LLM calls it caps.

### The incarnation

- **`String.meaning(template)`** is a trigger helper. It holds when the goal or belief matches the template, e.g. `"greet {name}"`.
  1. **Literally first**: `literalMatch` turns the template into an anchored regex with one `(.+?)` per parameter, so `"set the heating to 21 degrees"` binds `degrees = 21`. This needs no LLM call.
  2. Otherwise **by meaning**: one structured call asks whether the event matches the trigger and requests a binding for each parameter.
  It returns an `LlmContext(event, bindings)`, or null.
- **`GuardScope.holds(condition)`** is a guard helper. It replaces the bound parameters (`"{name} is a friend"` becomes `"Bob is a friend"`), then checks:
  1. whether that is literally one of the beliefs, possibly binding the remaining parameters, like a Prolog fact lookup;
  2. otherwise, by asking whether the beliefs state or clearly imply it.
  It returns the context extended with any new bindings, or null.
- **`PlanScope.test(condition)`** is an LLM test goal in plan bodies, with the same literal-first logic. It returns the bindings, or null.
- **`PlanScope.revise(belief)`** asks which current beliefs the new one contradicts or makes obsolete, forgets them, and believes the new one. It's explicit: `agent.believe` still adds a belief without revision.
- **Answer format**: every question gets a structured `Answer(reason, holds, bindings: [{parameter, value}])`. A list rather than a map, so OpenAI strict schemas accept it.
  - Validation: if `holds` is true, every parameter of the template must be bound, or the answer is invalid (the trigger or guard does not hold, `test` throws). Extra bindings are dropped.
  - Revision answers carry belief numbers, which must be in range.
- **Budget**: `LlmReasoner(executor, model, maxQuestions = Int.MAX_VALUE)`, see above. Past the budget, triggers and guards do not hold and `test`/`revise` throw, so goals fail instead of looping.

**Per-plan flags (v1's open question 1)**: this is now inherent. Each plan's trigger and guard are separate yes-or-no questions with their own validated answer, and the engine picks the first applicable plan in declaration order. That restores Jason ordering, so a well-formed answer that is wrong for one plan no longer changes which later plan runs.

### Cost

For one event, the engine on `main` evaluates the trigger of **every** plan of the event's kind, then the guard of every relevant plan, before taking the first applicable one (it does not stop at the first, unlike v2). Thanks to the remembered answers, the LLM is asked at most one question per distinct trigger and one per distinct guard. The calls are sequential, and literal matches are free.

Plans sharing a trigger share the question: the first offline test shows it, with the trigger asked once for two plans. Ordering still matters for cost: in the example, the specific `set the heating to {degrees} degrees` plan comes first, so the subgoals posted by other plans match it literally.

v1 cost exactly one call per event, but with "first" judged by the LLM. v2 and v3 trade calls for engine-controlled ordering and per-plan validation. The upgrade paths are left as open questions in the limitations.

### Determinism and tests

There's no extra abstraction: `LlmReasoner` takes any Koog `PromptExecutor`, and tests pass `getMockExecutor`.

`jakta-llm-incarnation` has 8 offline tests, on the JVM:

1. plans are tried in order, with triggers and guards matched by meaning, including the exact prompt texts (each asked once);
2. a literal match makes no LLM call;
3. no matching plan leads to a failure plan;
4. a malformed answer fails the goal, not the agent;
5. a match with an unbound parameter fails the goal;
6. added beliefs trigger plans by meaning;
7. `test` (literal, true, false) and `revise`;
8. the question budget stops a self-feeding plan.

Live runs use `temperature = 0` where the model supports it, but are not deterministic. `examples:llm-smart-home:test` is skipped (`assumeTrue`) unless `JAKTA_LLM_PROVIDER`, `ANTHROPIC_API_KEY` or `OPENAI_API_KEY` is set. It has a 10-minute timeout and a 100-question budget.

## 4. Superseded designs

### v2: suspending triggers and guards (`feat(core)!`)

`Plan.trigger` and `Plan.guard` became `suspend`, `isRelevant`/`isApplicable`/`run(agent, entity)` were replaced by `contextFor` and `run(agent, context)` (evaluating each trigger and guard once, stopping at the first applicable plan), throwing matchers failed the event, and `tryStep` launched the selection undispatched. It worked on JS too, but it was a breaking API change. It is kept, unmerged, on the local branch `backup/llm-suspending-plans`.

### v1: one wrapper plan per event kind

`llmPlans { goal(...); belief(...); failure(...) }` registered one catch-all JaKtA plan per event kind. Its body made one structured call listing every LLM plan and ran the chosen one. That avoided touching core, at the cost of "fake" plans that shadowed later plans, `Unit`-only results, no removal plans, and plan order judged by the LLM. v2 and v3 remove all of these: LLM plans are real plans, any result type and every event kind work, and ordering is the engine's.

Live runs of v1 against a local Ollama (CPU only), before you asked for no live runs:

- `qwen2.5-coder:7b`:
  - Correct: "Brr, it's freezing" led to closing the window and setting the heating to 21. `revise` retracted "the heating is off", and the pizza request went to the failure plan.
  - Wrong: it repeatedly picked a plan whose condition it said was false. That plan posts the same subgoal, so it looped until the call budget, which was added because of this.
- `qwen2.5-coder:14b`: OOM-killed, which only exercised the executor-error path.

v2 and v3 ask different questions (one yes/no per plan), and the example orders plans so canonical subgoals match literally, which removes that loop. **Neither has been run against a real model.**

## 5. How to run the example with a real model

```bash
ANTHROPIC_API_KEY=... ./gradlew :examples:llm-smart-home:run        # claude-haiku-4-5
OPENAI_API_KEY=...    ./gradlew :examples:llm-smart-home:run        # gpt-5.6-luna
JAKTA_LLM_PROVIDER=ollama [JAKTA_LLM_MODEL=qwen2.5-coder:7b] ./gradlew :examples:llm-smart-home:run  # llama3.2 by default
ANTHROPIC_API_KEY=... ./gradlew :examples:llm-smart-home:test       # the same scenario, asserted
```

- `JAKTA_LLM_PROVIDER` (`anthropic`, `openai`, `ollama`) forces the provider. Without it, the first provider with a key is used, else a local Ollama.
- `JAKTA_LLM_MODEL` replaces the default model id and keeps its declared capabilities.
- Keys are read only from the environment.
- `Logger.setMinSeverity(Severity.Debug)` logs every question, answer and reason.

## 6. Current limitations

- **Never run live since v1**: no API key was available, and you asked for no Ollama runs. Only the mock-based tests ran.
- **Model quality**: validation catches malformed answers and unbound parameters, but not a well-formed wrong yes or no. Small local models gave many of those in v1.
- **Blocking**: the agent's thread waits for every LLM call. On `Dispatchers.Default`, many agents waiting at once can exhaust its threads; in simulation (Alchemist), LLM latency is invisible to simulated time.
- **JVM only**, because of the blocking. No messaging protocol: map `String` messages to beliefs with `handlesMessageEvents`.
- **Errors in triggers and guards are swallowed** (logged, the plan does not apply), since throwing would stop the agent on `main`.
- **Sequential calls, all plans evaluated**: the engine evaluates every plan's trigger, even after an applicable one, so an event costs one question per distinct trigger of its kind. Remembered answers remove only the repetitions. Only a core change (stopping at the first applicable plan, as v2 did) would cut this.
- **Remembered answers are global to the reasoner**: an answer about the meaning of an event is reused even much later (only the latest 100 are kept). Guard answers include the beliefs in their key, so they are asked again once beliefs change.
- **Every belief change** evaluates the belief-plans' triggers, costing LLM calls unless they match literally.
- **Revision is explicit**: on `main` there's no core hook for it. #977, on `develop`, adds one (`BeliefRevision`, see question 3).
- **`maxQuestions`**: unlimited by default, a lifetime budget per reasoner (not a rate), and it counts remembered answers.
- **Prompt size**: the whole belief base goes into every guard and revision prompt, with no retrieval.

Open questions for you, each with a proposed answer (added 2026-10-02, to make the decisions quicker):

1. **Engine evaluations**: would you accept a small, non-breaking core change so selection evaluates each trigger and guard once and stops at the first applicable plan? It would make the remembered answers unnecessary for consistency, and cut the cost.

   *Proposed:* yes, as its own small core PR on `develop`. Evaluating each trigger and guard once is what any incarnation with expensive matching needs (Prolog solving costs too, and #976 showed how sensitive the solver is to small changes). Keep `isRelevant`/`isApplicable` for compatibility, and add one selection path that computes the context once and stops at the first applicable plan, as v2 did. Then drop the remembered answers here, or keep them only as a cache.
2. **Errors in triggers and guards**: should core catch them and fail the event (as v2 did), instead of each incarnation swallowing them?

   *Proposed:* yes, in the same core PR. A trigger or guard that throws should make that plan inapplicable and log the error, as here, rather than stop the agent. Doing it once in core removes the `blocking` wrapper's catch from this incarnation and protects every other incarnation too. It's a behaviour change, so it belongs on `develop`.
3. **Revision hook**: add a core hook (a custom `BeliefBase`) so `believe` can revise automatically, at the cost of one call per belief change?

   *Proposed:* this hook now exists, as `BeliefRevision` in #977 (on `develop`), which every belief change goes through. `revise` can become an `LlmBeliefRevision` installed with `revisesBeliefsWith(...)`, so `believe` and perception updates revise automatically, while the explicit `revise` stays for plans that want it only sometimes. It would block like triggers and guards do, which is consistent with this module being JVM-only. That makes #972 depend on #977, so it should target `develop` too.
4. **Default budget**: should the library default to a finite budget (the example and the README use 100)?

   *Proposed:* make `maxQuestions` a required parameter, with no default. Any finite default is wrong for someone (a long-running agent exhausts 100 quickly, since remembered answers count), while unlimited hides the self-feeding loops of section 4. Requiring it makes the cost a conscious choice, and the README can recommend 100 for experiments.
5. **Publishing**: should `jakta-llm-incarnation` be published and documented on the website like the other incarnations?

   *Proposed:* publish it, marked experimental in its README and KDoc, after at least one live run with a real model (none since v1). Document it with the 2.0 docs, next to the other incarnations, since it should land on `develop`.

## 7. Side findings in the existing code

- `BeliefBaseImpl.snapshot()` was a live view, and `executeInTestScope` (and many other common tests) discarded the `TestResult` of `runTest`, so JS tests couldn't fail. Both were fixed in #968, now on `main`; the incarnation still copies the beliefs (`toList()`) before numbering them, which is harmless.
- On JS, `Regex` uses the `u` flag, where an unescaped `}` is a syntax error (`\{(\w+)}` works on the JVM). The v2 JS tests caught it; the escaped regex is kept.
- For every belief change without a matching plan, the core logs a *warning*. That's noisy for agents that don't react to beliefs.
