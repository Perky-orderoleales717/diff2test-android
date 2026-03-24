package dev.diff2test.android.changedetector

import dev.diff2test.android.core.ChangeSet
import dev.diff2test.android.core.ChangeSource
import dev.diff2test.android.core.ChangedFile
import dev.diff2test.android.core.ChangedSymbol
import dev.diff2test.android.core.SymbolKind
import java.nio.file.Files
import java.nio.file.Path

data class ScanRequest(
    val baseRef: String? = "HEAD",
    val headRef: String? = null,
    val source: ChangeSource = ChangeSource.GIT_DIFF,
    val workingDirectory: Path = Path.of(System.getProperty("user.dir")),
)

interface ChangeDetector {
    fun scan(request: ScanRequest = ScanRequest()): ChangeSet
}

class GitDiffChangeDetector : ChangeDetector {
    override fun scan(request: ScanRequest): ChangeSet {
        val command = buildGitDiffCommand(request)
        val process = ProcessBuilder(command)
            .directory(request.workingDirectory.toFile())
            .start()

        val stdout = process.inputStream.bufferedReader().use { it.readText() }
        val stderr = process.errorStream.bufferedReader().use { it.readText() }
        val exitCode = process.waitFor()

        if (exitCode != 0) {
            return ChangeSet(
                source = request.source,
                baseRef = request.baseRef,
                headRef = request.headRef,
                summary = "git diff failed: ${stderr.ifBlank { "unknown error" }}",
            )
        }

        val files = parseGitDiff(stdout)
        val symbolCount = files.sumOf { it.changedSymbols.size }
        val summary = if (files.isEmpty()) {
            "No git diff changes detected."
        } else {
            "Detected ${files.size} changed file(s) and $symbolCount Kotlin symbol candidate(s)."
        }

        return ChangeSet(
            source = request.source,
            baseRef = request.baseRef,
            headRef = request.headRef,
            files = files,
            summary = summary,
        )
    }
}

internal fun buildGitDiffCommand(request: ScanRequest): List<String> {
    return buildList {
        add("git")
        add("diff")
        add("--no-color")
        add("--unified=0")
        request.baseRef?.let(::add)
        request.headRef?.let(::add)
        add("--")
    }
}

internal fun parseGitDiff(diffText: String): List<ChangedFile> {
    if (diffText.isBlank()) {
        return emptyList()
    }

    val files = mutableListOf<ChangedFile>()
    var currentPath: Path? = null
    var currentHunks = mutableListOf<String>()
    var currentSymbols = linkedSetOf<ChangedSymbol>()
    var currentHunkLines = mutableListOf<String>()

    fun flushHunk() {
        if (currentHunkLines.isNotEmpty()) {
            currentHunks += currentHunkLines.joinToString("\n")
            currentHunkLines = mutableListOf()
        }
    }

    fun flushFile() {
        flushHunk()
        val path = currentPath ?: return
        files += ChangedFile(
            path = path,
            hunks = currentHunks.toList(),
            changedSymbols = currentSymbols.toList(),
        )
        currentPath = null
        currentHunks = mutableListOf()
        currentSymbols = linkedSetOf()
    }

    diffText.lineSequence().forEach { line ->
        when {
            line.startsWith("diff --git ") -> {
                flushFile()
                currentPath = parsePathFromDiffHeader(line)
            }

            line.startsWith("@@") -> {
                flushHunk()
                currentHunkLines += line
            }

            line.startsWith("+") && !line.startsWith("+++") -> {
                currentHunkLines += line
                detectChangedSymbolCandidate(line)?.let(currentSymbols::add)
            }

            line.startsWith("-") && !line.startsWith("---") -> {
                currentHunkLines += line
                detectChangedSymbolCandidate(line)?.let(currentSymbols::add)
            }
        }
    }

    flushFile()
    return files
}

private fun parsePathFromDiffHeader(header: String): Path? {
    val parts = header.split(" ")
    if (parts.size < 4) {
        return null
    }

    val candidate = parts[3]
        .removePrefix("b/")
        .removePrefix("\"")
        .removeSuffix("\"")

    return Path.of(candidate)
}

fun extractKotlinSymbols(sourceText: String): List<ChangedSymbol> {
    return sourceText.lineSequence()
        .mapNotNull(::detectChangedSymbolCandidate)
        .distinct()
        .toList()
}

fun extractKotlinSymbols(sourcePath: Path): List<ChangedSymbol> {
    if (!Files.exists(sourcePath)) {
        return emptyList()
    }
    return extractKotlinSymbols(Files.readString(sourcePath))
}

private fun detectChangedSymbolCandidate(line: String): ChangedSymbol? {
    val body = line.removePrefix("+").removePrefix("-").trim()

    val classMatch = CLASS_PATTERN.find(body)
    if (classMatch != null) {
        return ChangedSymbol(
            name = classMatch.groupValues[2],
            kind = SymbolKind.CLASS,
            signature = body,
        )
    }

    val methodMatch = METHOD_PATTERN.find(body)
    if (methodMatch != null) {
        return ChangedSymbol(
            name = methodMatch.groupValues[1],
            kind = SymbolKind.METHOD,
            signature = body,
        )
    }

    val propertyMatch = PROPERTY_PATTERN.find(body)
    if (propertyMatch != null) {
        val propertyName = propertyMatch.groupValues[1]
        val kind = if ("state" in propertyName.lowercase()) SymbolKind.STATE else SymbolKind.PROPERTY
        return ChangedSymbol(
            name = propertyName,
            kind = kind,
            signature = body,
        )
    }

    return null
}

private val CLASS_PATTERN =
    Regex("""^(?:public|private|internal|protected)?\s*(?:data\s+|sealed\s+|abstract\s+|open\s+)?(class|object|interface)\s+([A-Za-z_][A-Za-z0-9_]*)""")

private val METHOD_PATTERN =
    Regex("""^(?:public|private|internal|protected)?\s*(?:override\s+)?(?:suspend\s+)?fun\s+([A-Za-z_][A-Za-z0-9_]*)""")

private val PROPERTY_PATTERN =
    Regex("""^(?:public|private|internal|protected)?\s*(?:override\s+)?(?:lateinit\s+)?(?:val|var)\s+([A-Za-z_][A-Za-z0-9_]*)""")
