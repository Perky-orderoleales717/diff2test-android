# Homebrew Release Guide

This project can be distributed on macOS as a Homebrew formula backed by the Gradle application distribution.

## Do You Need a Homebrew Account?

No.

- End users do not need to sign up for Homebrew.
- Maintainers do not need a special Homebrew account either.
- To publish your own tap, you only need a GitHub repository that Homebrew can read.

## What `distZip` Means

`distZip` is a Gradle Application plugin task that builds an installable ZIP distribution for the CLI app.

In this project it packages:

- the `d2t` launcher script
- the compiled CLI jars
- runtime dependencies

Run it like this:

```bash
./gradlew :apps:cli:distZip
```

The output will be written under:

```bash
apps/cli/build/distributions/
```

For example:

```bash
apps/cli/build/distributions/d2t-0.1.0-SNAPSHOT.zip
```

You can also install the distribution locally without Homebrew:

```bash
./gradlew :apps:cli:installDist
```

This writes a runnable layout under:

```bash
apps/cli/build/install/d2t/
```

## Recommended Release Flow

The recommended order is:

1. Build a ZIP distribution with `distZip`.
2. Upload the ZIP to a GitHub Release.
3. Create a separate Homebrew tap repository.
4. Add a formula that downloads the release ZIP.
5. Tell users to install with `brew install`.

## Suggested Repository Layout

Use:

- source repo: `diff2test-android`
- tap repo: `homebrew-diff2test-android`

The tap repo should contain:

```text
Formula/
  d2t.rb
```

## Creating the Release Artifact

Build the ZIP:

```bash
./gradlew :apps:cli:distZip
```

Generate the SHA256:

```bash
shasum -a 256 apps/cli/build/distributions/d2t-0.1.0-SNAPSHOT.zip
```

Then upload the ZIP to a GitHub Release in the main repository.

## Formula Template

See [`packaging/homebrew/d2t.rb`](../packaging/homebrew/d2t.rb) for a starter formula.

Replace:

- `__URL__`
- `__SHA256__`
- `__VERSION__`
- `__HOMEPAGE__`

with your actual release values.

## User Install Flow

After the tap is published, users should be able to run:

```bash
brew tap <github-user-or-org>/diff2test-android
brew install d2t
```

Or:

```bash
brew install <github-user-or-org>/diff2test-android/d2t
```

Then:

```bash
d2t init
d2t doctor
d2t auto --ai
```

## Current Caveat

This repository is not yet publishing GitHub Releases or a Homebrew tap automatically.

Today, the practical options are:

- clone the repository and run `./d2t`
- build `distZip` yourself and distribute that ZIP
- create and maintain a separate Homebrew tap
