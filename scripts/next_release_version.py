#!/usr/bin/env python3
import argparse
import pathlib
import re
import sys


SEMVER_RE = re.compile(r"^v?(\d+)\.(\d+)\.(\d+)$")
BUILD_VERSION_RE = re.compile(r'^\s*version\s*=\s*"([^"]+)"\s*$', re.MULTILINE)


def parse_semver(tag: str) -> tuple[int, int, int]:
    match = SEMVER_RE.fullmatch(tag.strip())
    if not match:
        raise ValueError(f"Unsupported tag format: {tag}")
    return tuple(int(part) for part in match.groups())


def normalize_semver(value: str) -> str | None:
    match = SEMVER_RE.fullmatch(value.strip())
    if not match:
        return None
    major, minor, patch = match.groups()
    return f"{int(major)}.{int(minor)}.{int(patch)}"


def parse_project_version(version_file: pathlib.Path) -> str | None:
    content = version_file.read_text(encoding="utf-8")
    match = BUILD_VERSION_RE.search(content)
    if not match:
        return None
    return normalize_semver(match.group(1))


def compare_versions(left: str, right: str) -> int:
    left_parts = parse_semver(left)
    right_parts = parse_semver(right)
    if left_parts < right_parts:
        return -1
    if left_parts > right_parts:
        return 1
    return 0


def write_output(path: pathlib.Path, values: dict[str, str]) -> None:
    with path.open("a", encoding="utf-8") as handle:
        for key, value in values.items():
            handle.write(f"{key}={value}\n")


def main() -> int:
    parser = argparse.ArgumentParser(
        description="Calculate the next release tag from the project version file."
    )
    parser.add_argument("--latest-tag", required=True, help="Latest release tag, for example v0.3.0")
    parser.add_argument("--version-file", required=True, help="Path to build.gradle.kts or another file containing the project version")
    parser.add_argument("--github-output", help="Optional GitHub Actions output file")
    args = parser.parse_args()

    latest_tag = args.latest_tag.strip()
    project_version = parse_project_version(pathlib.Path(args.version_file))

    values = {
        "latest_tag": latest_tag,
        "project_version": project_version or "",
    }

    if project_version is None:
        values["skip"] = "true"
        values["reason"] = "project version could not be parsed from the version file"
    else:
        comparison = compare_versions(f"v{project_version}", latest_tag)
        if comparison > 0:
            values["skip"] = "false"
            values["reason"] = "project version is ahead of the latest tag"
            values["source"] = "build.gradle.kts"
            values["next_tag"] = f"v{project_version}"
        elif comparison == 0:
            values["skip"] = "true"
            values["reason"] = "project version already matches the latest tag"
        else:
            values["skip"] = "true"
            values["reason"] = "project version is behind the latest tag"

    if args.github_output:
        write_output(pathlib.Path(args.github_output), values)
    else:
        for key, value in values.items():
            print(f"{key}={value}")

    return 0


if __name__ == "__main__":
    sys.exit(main())
