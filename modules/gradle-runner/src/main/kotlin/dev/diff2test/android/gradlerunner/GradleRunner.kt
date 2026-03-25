package dev.diff2test.android.gradlerunner

import dev.diff2test.android.core.ExecutionResult
import dev.diff2test.android.core.ExecutionStatus
import dev.diff2test.android.core.GradleRunRequest
import java.nio.file.Files
import java.nio.file.Path

interface GradleRunner {
    fun run(request: GradleRunRequest): ExecutionResult
}

class JvmGradleRunner : GradleRunner {
    override fun run(request: GradleRunRequest): ExecutionResult {
        val projectRoot = findGradleProjectRoot(request.workingDirectory)
        val invocation = resolveGradleInvocation(projectRoot)
        val command = mutableListOf(invocation.executable)
        command.addAll(invocation.arguments)
        command.add(request.task)

        if (!request.testFilter.isNullOrBlank()) {
            command.addAll(listOf("--tests", request.testFilter.orEmpty()))
        }

        val process = ProcessBuilder(command)
            .directory(projectRoot.toFile())
            .redirectErrorStream(true)
            .start()

        val output = process.inputStream.bufferedReader().use { it.readText() }
        val exitCode = process.waitFor()

        return ExecutionResult(
            status = if (exitCode == 0) ExecutionStatus.PASSED else ExecutionStatus.FAILED,
            command = command,
            exitCode = exitCode,
            stdout = output,
        )
    }
}

internal data class GradleInvocation(
    val executable: String,
    val arguments: List<String>,
)

internal fun findGradleProjectRoot(start: Path): Path {
    var current: Path? = start.toAbsolutePath().normalize()

    while (current != null) {
        if (Files.exists(current.resolve("gradlew")) || Files.exists(current.resolve("settings.gradle.kts"))) {
            return current
        }
        current = current.parent
    }

    return start.toAbsolutePath().normalize()
}

internal fun resolveGradleInvocation(projectRoot: Path): GradleInvocation {
    val normalizedProjectRoot = projectRoot.toAbsolutePath().normalize()
    val localWrapper = normalizedProjectRoot.resolve("gradlew")
    if (Files.exists(localWrapper)) {
        return GradleInvocation(
            executable = localWrapper.toString(),
            arguments = emptyList(),
        )
    }

    val wrapperRoot = findWrapperRoot(normalizedProjectRoot)
    if (wrapperRoot != null) {
        return GradleInvocation(
            executable = wrapperRoot.resolve("gradlew").toString(),
            arguments = listOf("-p", normalizedProjectRoot.toString()),
        )
    }

    return GradleInvocation(
        executable = "gradle",
        arguments = listOf("-p", normalizedProjectRoot.toString()),
    )
}

private fun findWrapperRoot(start: Path): Path? {
    var current: Path? = start.toAbsolutePath().normalize()

    while (current != null) {
        if (Files.exists(current.resolve("gradlew"))) {
            return current
        }
        current = current.parent
    }

    return null
}
