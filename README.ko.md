# diff2test-android

[![English](https://img.shields.io/badge/Language-English-1f6feb?style=for-the-badge)](./README.md)
[![한국어](https://img.shields.io/badge/언어-한국어-0f9d58?style=for-the-badge)](./README.ko.md)

`diff2test-android`는 코드 diff를 기반으로 Android ViewModel 테스트를 생성하기 위한 Kotlin 기반 CLI 프로젝트입니다.

현재 레포는 아래 세 계층으로 구성되어 있습니다.

- 핵심 엔진 모듈: 분석, 계획, 생성, 실행, 복구 정책을 담당합니다.
- CLI 앱: 로컬에서 직접 실행하는 진입점입니다.
- MCP 앱: 같은 엔진 계약을 MCP 도구 구조로 정리해 둔 카탈로그 스캐폴딩입니다.

## 현재 배포 상태

현재는 `npm` 패키지나 설치형 바이너리로 배포된 상태가 아닙니다.

- 지금 바로 사용할 수 있는 형태는 `소스 체크아웃 + Gradle + ./d2t 래퍼 스크립트`입니다.
- 정식 MCP 서버 transport는 아직 구현되지 않았습니다.
- 따라서 현재 기준의 실제 사용 방식은 `CLI preview`입니다.

정리하면, 지금은 "설치해서 바로 쓰는 제품"보다는 "레포를 내려받아 실행하는 개발자용 preview"에 가깝습니다.

## 누가, 어떻게 사용할 수 있나요?

현재 다른 사용자가 이 프로젝트를 쓰려면 아래 방식으로 사용하시면 됩니다.

1. 이 레포를 클론해주세요.
2. Java 17과 Git이 설치된 환경을 준비해주세요.
3. 루트에서 `./gradlew test`로 기본 빌드 상태를 확인해주세요.
4. `./d2t init`으로 사용자 설정 파일을 생성해주세요.
5. 본인이 사용하는 AI provider의 API key를 환경변수로 설정해주세요.
6. Android 프로젝트에서 ViewModel 변경을 만든 뒤 `./d2t auto --ai`를 실행해주세요.

예시는 아래와 같습니다.

```bash
git clone <repo-url>
cd diff2test-android
./gradlew test
./d2t init
source ~/.zshrc
./d2t doctor
./d2t auto --ai
```

즉, 현재는 누군가가 이 레포를 받아서 로컬 CLI 도구처럼 실행하는 방식으로 사용하시면 됩니다.

## 현재 지원 범위

현재 스캐폴딩은 `src/test` 기준의 ViewModel 중심 local unit test를 우선 대상으로 합니다.

현재 가능한 흐름은 아래와 같습니다.

- git diff 기준으로 변경된 ViewModel 후보를 찾습니다.
- 변경 메소드와 상태 업데이트 패턴을 heuristic 기반으로 분석합니다.
- 시나리오 중심의 `TestPlan`을 만듭니다.
- AI 또는 heuristic generator로 테스트 코드를 생성합니다.
- 생성 결과를 Gradle로 검증할 수 있습니다.

## 현재 미지원 또는 제한 사항

현재는 아래 항목이 아직 완성되지 않았습니다.

- Kotlin PSI 또는 symbol resolution 기반의 정밀 분석
- 정식 MCP transport 서버
- native Anthropic `messages` transport
- 자동 repair 루프의 실구현
- 설치형 패키지 배포
  예: Homebrew, SDKMAN, standalone zip, native binary

즉, README나 구조상 준비된 항목이 있어도 실제 구현은 preview 단계인 부분이 있습니다. 사용 전 이 점을 감안해주세요.

## 빠른 시작

```bash
./gradlew test
./d2t init
./d2t doctor
./d2t auto --ai
```

## 설치 없이 실행하는 방식

루트에 있는 [d2t](/Users/shingayeong/Desktop/projects/gayoung/diff2test-android/d2t) 스크립트는 내부적으로 Gradle의 CLI 앱 실행을 감싸는 래퍼입니다.

즉 아래 두 명령은 사실상 같은 역할을 합니다.

```bash
./d2t auto --ai
./gradlew :apps:cli:run --args="auto --ai"
```

현재는 이 래퍼 스크립트를 통해 사용하는 방식이 가장 간단합니다.

## AI 설정

CLI는 사용자 단위 설정 파일 `~/.config/d2t/config.toml`을 지원합니다.

초기 템플릿을 만들려면 아래 명령을 실행해주세요.

```bash
./d2t init
```

현재 설정 상태를 확인하려면 아래 명령을 실행해주세요.

```bash
./d2t doctor
```

설정 파일에는 실제 비밀키를 넣지 말고, 비밀키가 들어 있는 환경변수 이름만 넣어주세요.

OpenAI Responses API 예시는 아래와 같습니다.

```toml
[ai]
enabled = true
provider = "openai"
protocol = "responses-compatible"
api_key_env = "OPENAI_API_KEY"
model = "gpt-5"
base_url = "https://api.openai.com/v1"
connect_timeout_seconds = 30
request_timeout_seconds = 180
```

로컬 또는 self-hosted Responses-compatible 게이트웨이 예시는 아래와 같습니다.

```toml
[ai]
enabled = true
provider = "custom"
protocol = "responses-compatible"
api_key_env = "LLM_API_KEY"
model = "qwen3-coder-next-mlx"
base_url = "http://127.0.0.1:12345"
reasoning_effort = "high"
connect_timeout_seconds = 30
request_timeout_seconds = 300
```

환경변수를 로드한 뒤에는 아래처럼 실행해주세요.

```bash
source ~/.zshrc
./d2t auto --ai
```

## 명령어

```bash
./d2t init [--force]
./d2t doctor
./d2t scan
./d2t plan path/to/SomeViewModel.kt
./d2t generate path/to/SomeViewModel.kt --write [--ai|--no-ai]
./d2t auto [--ai|--no-ai] [--model model-name]
./d2t verify :module:testTask
```

## 현재 AI 경로의 제약

현재는 `responses-compatible` 엔드포인트만 지원합니다.

- OpenAI Responses API는 바로 사용할 수 있습니다.
- Anthropic의 native `messages` transport는 아직 지원 예정입니다.
- Anthropic 계열이더라도 Responses-compatible gateway를 제공하면 `provider = "custom"`으로 사용할 수 있습니다.

또한 AI 호출이 실패하면 현재 기본 동작은 heuristic generator로 fallback 합니다. 이 부분은 이후 옵션화가 더 필요합니다.

## 레거시 환경변수 fallback

config 파일이 없으면 CLI는 여전히 환경변수 기반 설정으로 fallback 합니다.

- Auth: `D2T_AI_AUTH_TOKEN`, `LLM_API_KEY`, `ANTHROPIC_AUTH_TOKEN`, `OPENAI_API_KEY`
- Model: `D2T_AI_MODEL`, `STRIX_LLM`, `ANTHROPIC_MODEL`, `OPENAI_MODEL`
- Base URL: `D2T_AI_BASE_URL`, `LLM_API_BASE`, `ANTHROPIC_BASE_URL`, `OPENAI_BASE_URL`
- Reasoning: `D2T_REASONING_EFFORT`, `STRIX_RESONING_EFFORT`, `OPENAI_REASONING_EFFORT`
- Connect timeout: `D2T_CONNECT_TIMEOUT_SECONDS`, `LLM_CONNECT_TIMEOUT_SECONDS`, `OPENAI_CONNECT_TIMEOUT_SECONDS`
- Request timeout: `D2T_REQUEST_TIMEOUT_SECONDS`, `LLM_REQUEST_TIMEOUT_SECONDS`, `OPENAI_REQUEST_TIMEOUT_SECONDS`

## 레포 구조

- `apps/cli`: 로컬 실행용 CLI 앱입니다.
- `apps/mcp-server`: MCP 카탈로그 스캐폴딩입니다.
- `modules/*`: 핵심 엔진 모듈입니다.
- `prompts/*`: 프롬프트 및 정책 템플릿입니다.
- `docs/*`: 아키텍처 및 계약 문서입니다.
- `fixtures/*`: 샘플 앱과 골든 데이터입니다.

## 앞으로 필요한 배포 형태

다른 사용자가 더 쉽게 쓰게 하려면 아래 단계가 필요합니다.

1. CLI를 zip 또는 installable distribution으로 패키징해야 합니다.
2. Homebrew 또는 SDKMAN 같은 설치 경로를 제공해야 합니다.
3. MCP는 실제 transport 서버로 구현해야 합니다.
4. `auto -> verify -> repair`를 하나의 안정적인 워크플로로 묶어야 합니다.

즉 "다른 사람이 지금 어떻게 쓰느냐"의 답은 현재는 간단합니다.

- 이 레포를 클론해서
- `./d2t`로 실행하는 방식으로 사용합니다.

설치형 배포와 정식 MCP 서버는 아직 지원 예정입니다.

## macOS 배포 권장 방식

macOS 기준으로는 `Homebrew`가 가장 자연스러운 배포 경로입니다.

다만 현재는 아직 Homebrew tap이 열려 있지 않아서, 지금 당장은 아래 두 방식 중 하나를 권장합니다.

1. 가장 간단한 방법: 레포를 클론해서 `./d2t`로 실행해주세요.
2. 배포를 준비하는 방법: Gradle `distZip`으로 CLI 배포 ZIP을 만든 뒤 Homebrew tap에 연결해주세요.

Homebrew를 사용한다고 해서 별도 가입이 필요한 것은 아닙니다.

- 사용자는 Homebrew 계정이 필요하지 않습니다.
- 배포자도 Homebrew 전용 계정이 필요한 것은 아닙니다.
- 실제로 필요한 것은 GitHub release와 tap 저장소입니다.

`distZip`은 Gradle이 CLI 실행 파일과 의존성을 묶어서 ZIP으로 만드는 작업입니다.

아래 명령으로 생성할 수 있습니다.

```bash
./gradlew :apps:cli:distZip
```

출력물은 아래 경로에 생성됩니다.

```bash
apps/cli/build/distributions/
```

자세한 절차는 [docs/homebrew-release.md](/Users/shingayeong/Desktop/projects/gayoung/diff2test-android/docs/homebrew-release.md)를 확인해주세요.
