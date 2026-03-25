import pathlib
import subprocess
import tempfile
import unittest


SCRIPT = pathlib.Path(__file__).resolve().parents[1] / "next_release_version.py"


class NextReleaseVersionTest(unittest.TestCase):
    def run_script(self, latest_tag: str, version_value: str) -> str:
        with tempfile.TemporaryDirectory() as tmp_dir:
            version_file = pathlib.Path(tmp_dir) / "build.gradle.kts"
            version_file.write_text(
                f'group = "dev.diff2test.android"\nversion = "{version_value}"\n',
                encoding="utf-8",
            )
            completed = subprocess.run(
                [
                    "python3",
                    str(SCRIPT),
                    "--latest-tag",
                    latest_tag,
                    "--version-file",
                    str(version_file),
                ],
                check=True,
                text=True,
                capture_output=True,
            )
            return completed.stdout

    def test_creates_next_tag_when_project_version_is_ahead(self):
        output = self.run_script("v0.3.0", "0.3.1")

        self.assertIn("skip=false", output)
        self.assertIn("next_tag=v0.3.1", output)
        self.assertIn("source=build.gradle.kts", output)

    def test_skips_when_project_version_matches_latest_tag(self):
        output = self.run_script("v0.3.0", "0.3.0")

        self.assertIn("skip=true", output)
        self.assertIn("reason=project version already matches the latest tag", output)

    def test_skips_when_project_version_is_behind_latest_tag(self):
        output = self.run_script("v0.3.0", "0.2.0")

        self.assertIn("skip=true", output)
        self.assertIn("reason=project version is behind the latest tag", output)


if __name__ == "__main__":
    unittest.main()
