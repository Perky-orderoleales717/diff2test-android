#!/usr/bin/env bash
set -euo pipefail

repo_root="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
tmp_dir="$(mktemp -d "${TMPDIR:-/tmp}/d2t-fixture-XXXXXX")"

cleanup() {
  rm -rf "$tmp_dir"
}
trap cleanup EXIT

cp -R "$repo_root/fixtures/sample-app/." "$tmp_dir"

targets=(
  "$repo_root/fixtures/sample-app/app/src/main/java/com/example/auth/LoginViewModel.kt"
  "$repo_root/fixtures/sample-app/app/src/main/java/com/example/auth/SignUpViewModel.kt"
  "$repo_root/fixtures/sample-app/app/src/main/java/com/example/onboarding/OnboardingViewModel.kt"
  "$repo_root/fixtures/sample-app/app/src/main/java/com/example/search/SearchViewModel.kt"
)

for target in "${targets[@]}"; do
  echo "[fixture-smoke] generating $(basename "$target")"
  "$repo_root/gradlew" :apps:cli:run --args="generate $target --write --no-ai --output-root $tmp_dir/app"
done

echo "[fixture-smoke] verifying generated tests in copied fixture app"
"$repo_root/gradlew" -p "$tmp_dir" :app:test --tests '*GeneratedTest'
