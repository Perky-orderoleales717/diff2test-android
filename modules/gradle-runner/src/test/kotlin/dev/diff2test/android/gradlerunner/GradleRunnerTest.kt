package dev.diff2test.android.gradlerunner

import java.nio.file.Files
import kotlin.test.Test
import kotlin.test.assertEquals

class GradleRunnerTest {
    @Test
    fun `finds wrapper in parent directories`() {
        val root = Files.createTempDirectory("d2t-gradle-root")
        Files.createFile(root.resolve("gradlew"))
        val nested = Files.createDirectories(root.resolve("apps/cli/build/tmp"))

        val resolved = findGradleProjectRoot(nested)

        assertEquals(root.toAbsolutePath().normalize(), resolved)
    }

    @Test
    fun `uses local wrapper when project root contains gradlew`() {
        val root = Files.createTempDirectory("d2t-gradle-project")
        Files.createFile(root.resolve("gradlew"))

        val invocation = resolveGradleInvocation(root)

        assertEquals(root.resolve("gradlew").toString(), invocation.executable)
        assertEquals(emptyList(), invocation.arguments)
    }

    @Test
    fun `uses ancestor wrapper with project flag when project root has no wrapper`() {
        val workspaceRoot = Files.createTempDirectory("d2t-gradle-workspace")
        Files.createFile(workspaceRoot.resolve("gradlew"))
        val projectRoot = Files.createDirectories(workspaceRoot.resolve("fixtures/sample-app"))
        Files.writeString(projectRoot.resolve("settings.gradle.kts"), "rootProject.name = \"sample-app\"\n")

        val invocation = resolveGradleInvocation(projectRoot)

        assertEquals(workspaceRoot.resolve("gradlew").toString(), invocation.executable)
        assertEquals(listOf("-p", projectRoot.toAbsolutePath().normalize().toString()), invocation.arguments)
    }
}
