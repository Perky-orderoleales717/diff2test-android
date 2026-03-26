<div align="center">
  <h1>diff2test-android</h1>
  <p><strong>Diff-driven Android ViewModel test generation CLI</strong></p>
  <p>Detect changed ViewModels, plan tests, generate candidate local unit tests, and verify them with Gradle.</p>
  <p>
    <a href="https://github.com/gay00ung/diff2test-android/stargazers">
      <img alt="GitHub stars" src="https://img.shields.io/github/stars/gay00ung/diff2test-android?style=flat-square">
    </a>
    <a href="https://github.com/gay00ung/diff2test-android/releases">
      <img alt="Release" src="https://img.shields.io/github/v/release/gay00ung/diff2test-android?style=flat-square">
    </a>
    <a href="https://github.com/gay00ung/diff2test-android/releases">
      <img alt="Homebrew" src="https://img.shields.io/badge/install-Homebrew-fbbf24?style=flat-square&logo=homebrew">
    </a>
    <img alt="CLI 1.0" src="https://img.shields.io/badge/cli-1.0.0-2563eb?style=flat-square">
    <img alt="MCP experimental" src="https://img.shields.io/badge/mcp-experimental-f97316?style=flat-square">
    <img alt="Kotlin 1.9.25" src="https://img.shields.io/badge/kotlin-1.9.25-7f52ff?style=flat-square">
    <img alt="Java 17" src="https://img.shields.io/badge/java-17-437291?style=flat-square">
  </p>
  <p>
    <a href="./README.md">English</a>
    ·
    <a href="./README.ko.md">한국어</a>
  </p>
</div>

<p align="center">
  <img src="./docs/assets/readme-hero.png" alt="d2t workflow banner" width="960">
</p>

> `d2t` 1.0 focuses on one thing: turning changed Android ViewModels into verifiable local unit tests. The CLI is the primary product. The MCP app remains experimental.

## Why d2t

Most Android test generation tools fail in one of two ways:

- they ignore the actual code diff and generate too much
- they generate code, but stop before verification

`d2t` is built around a narrower loop:

1. detect changed `*ViewModel.kt` files from `git diff`
2. analyze the changed ViewModel surface and collaborators
3. build a scenario-first `TestPlan`
4. generate candidate local unit tests
5. verify those generated tests with Gradle

That makes it useful as a developer workflow tool, not just a code dump generator.

## What 1.0 Includes

`d2t` 1.0 is intentionally narrow.

- Diff-driven Android ViewModel local unit test generation
- Gradle-backed verification of generated tests
- Bring-your-own API key support
- OpenAI Responses API support
- Anthropic Messages API support
- Gemini GenerateContent API support
- Custom `responses-compatible` endpoints
- Custom `chat-completions` endpoints
- Release ZIP and Homebrew distribution

## What 1.0 Does Not Promise

- A transport-bound MCP server
- Instrumented `androidTest` generation
- Compose UI test generation
- Full end-to-end autonomous repair loops
- Perfect external classpath symbol resolution in every Android build graph

## Install

### Homebrew

```bash
brew install gay00ung/diff2test-android/d2t
```

Optional tap flow:

```bash
brew tap gay00ung/diff2test-android
brew install d2t
```

### Release ZIP

Download `d2t.zip` from the latest release:

```bash
unzip d2t.zip
cd d2t
./bin/d2t help
```

### Run from Source

```bash
git clone https://github.com/gay00ung/diff2test-android.git
cd diff2test-android
./gradlew test
./d2t help
```

## Quick Start

### 1. Initialize config

```bash
d2t init
```

### 2. Point d2t at your AI provider

```toml
[ai]
enabled = true
provider = "custom"
protocol = "chat-completions"
api_key_env = "LLM_API_KEY"
model = "qwen3-coder-next-mlx"
base_url = "http://127.0.0.1:12345/v1"
connect_timeout_seconds = 30
request_timeout_seconds = 300
```

### 3. Confirm config

```bash
d2t doctor
```

### 4. Generate and verify tests for current changes

```bash
d2t auto --ai
```

If you are running from source, use `./d2t` instead of `d2t`.

## Supported AI Protocols

`d2t` stores only the environment variable name for the API key in `~/.config/d2t/config.toml`. Keep the secret itself in your shell environment.

Supported provider/protocol combinations:

- `provider = "openai"` with `protocol = "responses-compatible"`
- `provider = "anthropic"` with `protocol = "anthropic-messages"`
- `provider = "gemini"` with `protocol = "gemini-generate-content"`
- `provider = "custom"` with `protocol = "responses-compatible"`
- `provider = "custom"` with `protocol = "chat-completions"`

Example:

```bash
source ~/.zshrc
d2t doctor
d2t auto --ai
```

## How It Works

At a high level:

```text
git diff
  -> changed ViewModel detection
  -> ViewModel analysis
  -> TestPlan generation
  -> AI or deterministic code generation
  -> quality gate
  -> Gradle verification
```

Important implementation details:

- `d2t` does not ask the model to guess from the entire repo blindly
- the diff and the analyzed ViewModel surface narrow the generation scope first
- generated tests must pass a built-in quality gate before verification
- `auto` generates and verifies in one command
- `--repair` enables one bounded repair pass for common import and coroutine-test utility issues

## Commands

```bash
d2t init [--force]
d2t doctor
d2t scan
d2t plan path/to/SomeViewModel.kt
d2t generate path/to/SomeViewModel.kt --write [--ai|--no-ai] [--strict-ai]
d2t auto [--ai|--no-ai] [--strict-ai] [--model model-name] [--no-verify] [--repair]
d2t verify :module:testTask
```

## Troubleshooting

### `No changed ViewModel files were detected`

- make sure your current working tree actually contains a modified `*ViewModel.kt`
- or pass an explicit file path to `plan` or `generate`

### AI requests time out

- increase `request_timeout_seconds`
- try a smaller or faster model
- prefer `protocol = "chat-completions"` when your gateway handles that path more reliably

### Generated tests fail the quality gate

- the generator produced code that `d2t` considers too fragile or incomplete
- this is usually a generation-quality problem, not a Gradle problem
- retry with a stronger model or add `--repair` when verification is enabled

### Verification fails after generation

- inspect the generated test under `src/test/kotlin/...GeneratedTest.kt`
- run the printed Gradle verification command directly
- if the failure is import or coroutine utility related, retry with `d2t auto --ai --repair`

## Repository Layout

- `apps/cli`: main CLI application
- `apps/mcp-server`: experimental MCP-facing catalog scaffold
- `modules/*`: engine modules
- `prompts/*`: prompt templates and policies
- `fixtures/*`: sample app and verification fixtures
- `docs/*`: architecture and release docs

## Release and Distribution

This repository includes:

- Homebrew packaging via [`packaging/homebrew/d2t.rb`](./packaging/homebrew/d2t.rb)
- release automation via [`.github/workflows/release.yml`](./.github/workflows/release.yml)
- tag automation via [`.github/workflows/tag-release.yml`](./.github/workflows/tag-release.yml)
- distribution ZIP output via `./gradlew :apps:cli:distZip`

The generated archive is written to:

```bash
apps/cli/build/distributions/d2t.zip
```

## Product Boundaries

The CLI is the stable surface.

The MCP app is still experimental:

- it is useful as a catalog scaffold
- it is not yet positioned as a production transport-bound MCP server

For the current release gate and roadmap:

- [`docs/release-gate-1.0.md`](./docs/release-gate-1.0.md)
- [`docs/roadmap-1.0.md`](./docs/roadmap-1.0.md)
