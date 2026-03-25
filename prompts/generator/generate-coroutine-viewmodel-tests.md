# generate-coroutine-viewmodel-tests

Generate Kotlin tests from a `TestPlan`.

Rules:

- honor project naming and assertion style
- default to `runTest`
- keep one observable outcome per scenario
- avoid verify-only tests without assertions
- reuse project patterns before inventing new abstractions
- prefer manual fake/stub classes over mocking frameworks
- do not use MockK, Mockito, `mockkStatic`, reflection, or private field mutation
- do not emit `TODO`, caveat comments, or "adjust manually" placeholders inside code
- for async completion prefer `advanceUntilIdle()`; do not use `cancel()` to finish work
- only call suspend collaborators from a coroutine test body
- avoid asserting transient loading state immediately after `launch` unless scheduler control is explicit and necessary
- prefer stable final-state assertions after `advanceUntilIdle()` over fragile intermediate-state assertions
- when using `StandardTestDispatcher` inside `runTest`, bind it to `testScheduler`
- do not create a class-level `StandardTestDispatcher()` that is independent from the current `runTest` scheduler
