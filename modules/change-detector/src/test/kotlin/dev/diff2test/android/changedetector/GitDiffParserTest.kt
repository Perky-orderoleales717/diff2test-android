package dev.diff2test.android.changedetector

import dev.diff2test.android.core.SymbolKind
import java.nio.file.Path
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class GitDiffParserTest {
    @Test
    fun `parses changed files hunks and Kotlin symbol candidates`() {
        val diff = """
            diff --git a/app/src/main/java/com/example/LoginViewModel.kt b/app/src/main/java/com/example/LoginViewModel.kt
            index 1111111..2222222 100644
            --- a/app/src/main/java/com/example/LoginViewModel.kt
            +++ b/app/src/main/java/com/example/LoginViewModel.kt
            @@ -10,0 +11,4 @@ class LoginViewModel
            +    suspend fun loadData() {
            +    }
            +    val uiState = MutableStateFlow(LoginState())
            @@ -20 +25 @@ class LoginViewModel
            -    fun oldCall() = Unit
            +    fun refresh() = Unit
        """.trimIndent()

        val files = parseGitDiff(diff)

        assertEquals(1, files.size)
        assertEquals(Path.of("app/src/main/java/com/example/LoginViewModel.kt"), files.first().path)
        assertEquals(2, files.first().hunks.size)
        assertTrue(files.first().changedSymbols.any { it.name == "loadData" && it.kind == SymbolKind.METHOD })
        assertTrue(files.first().changedSymbols.any { it.name == "refresh" && it.kind == SymbolKind.METHOD })
        assertTrue(files.first().changedSymbols.any { it.name == "uiState" && it.kind == SymbolKind.STATE })
    }
}
