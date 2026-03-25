# Homebrew Release Guide

This project ships a macOS-friendly CLI distribution through a Homebrew tap backed by the Gradle application distribution.

## What `distZip` Produces

`distZip` is the Gradle Application plugin task that packages the CLI into a ready-to-install ZIP.

Run:

```bash
./gradlew :apps:cli:distZip
```

Output:

```bash
apps/cli/build/distributions/d2t.zip
```

The ZIP contains:

- the `d2t` launcher script
- the compiled CLI JARs
- runtime dependencies

For a local non-Homebrew install, you can also run:

```bash
./gradlew :apps:cli:installDist
```

That produces a runnable layout under:

```bash
apps/cli/build/install/d2t/
```

## Current Release Model

There are now two supported maintainer paths:

1. Manual release
2. Automated release after a tag or workflow dispatch
3. Automated tagging after a merge to `main`, followed by automated release publication when the project version is bumped

The automation is defined in:

- [`.github/workflows/tag-release.yml`](/Users/shingayeong/Desktop/projects/gayoung/diff2test-android/.github/workflows/tag-release.yml)
- [`.github/workflows/release.yml`](/Users/shingayeong/Desktop/projects/gayoung/diff2test-android/.github/workflows/release.yml)

The workflow:

- validates the repository with Gradle
- builds `d2t.zip`
- computes the SHA256 checksum
- publishes or updates the GitHub Release for the tag
- optionally updates the Homebrew tap formula if the tap token is configured

## What Is Automated Today

There are now two automation layers:

- `tag-release.yml` watches pushes to `main` and decides whether a new release tag should be created
- `release.yml` builds and publishes the release after a tag exists

When `tag-release.yml` creates a tag automatically, it also dispatches `release.yml` directly. This avoids the GitHub Actions limitation where tags created by `GITHUB_TOKEN` do not automatically trigger another workflow from the resulting push event.

The release workflow triggers on:

- tag pushes like `v0.2.0`
- manual `workflow_dispatch`

What it does automatically:

- `./gradlew test`
- `./gradlew -p fixtures/sample-app :app:test`
- `./gradlew :apps:cli:distZip`
- upload `d2t.zip` to the GitHub Release
- calculate SHA256 and show it in the workflow summary

What it can also do automatically:

- update the tap repo formula

That last part requires one GitHub Actions secret:

- `HOMEBREW_TAP_TOKEN`

The token needs write access to:

- `gay00ung/homebrew-diff2test-android`

Without that secret, the workflow still publishes the release asset and prints the formula values you need to update manually.

The tag workflow now resolves the next version from one source only:

1. Read [`build.gradle.kts`](/Users/shingayeong/Desktop/projects/gayoung/diff2test-android/build.gradle.kts)
2. Parse `version = "x.y.z"`
3. Compare it to the latest release tag
4. Create a new tag only when the project version is higher than the latest tag

That means commit messages alone no longer produce releases. If you do not bump `build.gradle.kts`, no new tag or release will be created automatically.

## What Is Still Manual

The automation does **not** update [`build.gradle.kts`](/Users/shingayeong/Desktop/projects/gayoung/diff2test-android/build.gradle.kts) for you. If you want the next release to be `0.3.1`, set `version = "0.3.1"` in the PR before merge.

That means you still choose one of two operational styles:

- bump the project version in the PR and let `tag-release.yml` create the matching tag after merge
- or push a tag manually when you want full control

## Recommended Maintainer Flow

For now, the cleanest flow is:

1. Merge the release branch into `main`
2. Update the project version in the PR to the intended release version
3. Let `tag-release.yml` create the next tag automatically
4. Let `tag-release.yml` dispatch `release.yml`
5. Let `release.yml` publish `d2t.zip`
6. If `HOMEBREW_TAP_TOKEN` is configured, let the workflow update the tap repo automatically
7. If the token is not configured, update the tap repo manually

## Manual Release Steps

If you need to do this release manually, use this sequence:

### 1. Merge to `main`

Merge your PR first.

### 2. Set the version

Update:

- [`build.gradle.kts`](/Users/shingayeong/Desktop/projects/gayoung/diff2test-android/build.gradle.kts)

Example:

```kotlin
version = "0.2.0"
```

### 3. Build the release ZIP

```bash
./gradlew test
./gradlew -p fixtures/sample-app :app:test
./gradlew :apps:cli:distZip
```

### 4. Compute the SHA256

```bash
shasum -a 256 apps/cli/build/distributions/d2t.zip
```

### 5. Create the tag

```bash
git checkout main
git pull
git tag v0.2.0
git push origin v0.2.0
```

### 6. Create or update the GitHub Release

Upload:

- `apps/cli/build/distributions/d2t.zip`

Release URL pattern:

```text
https://github.com/gay00ung/diff2test-android/releases/download/v0.2.0/d2t.zip
```

### 7. Update the tap repo formula

Tap repo:

- `gay00ung/homebrew-diff2test-android`

Formula path:

- `Formula/d2t.rb`

Update:

- `url`
- `sha256`
- `version`

You can do it manually or with the helper script:

```bash
python3 scripts/update_homebrew_formula.py \
  --formula /path/to/homebrew-diff2test-android/Formula/d2t.rb \
  --homepage https://github.com/gay00ung/diff2test-android \
  --url https://github.com/gay00ung/diff2test-android/releases/download/v0.2.0/d2t.zip \
  --sha256 <SHA256> \
  --version 0.2.0
```

Then commit and push the tap repo.

## Tap Repo Layout

Recommended repositories:

- source repo: `diff2test-android`
- tap repo: `homebrew-diff2test-android`

The tap repo should contain:

```text
Formula/
  d2t.rb
```

## User Install Flow

Users can install directly without an explicit tap step:

```bash
brew install gay00ung/diff2test-android/d2t
```

Or:

```bash
brew tap gay00ung/diff2test-android
brew install d2t
```

Then:

```bash
d2t init
d2t doctor
d2t auto --ai
```
