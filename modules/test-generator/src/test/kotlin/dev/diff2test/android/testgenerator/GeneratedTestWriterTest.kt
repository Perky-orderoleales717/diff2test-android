package dev.diff2test.android.testgenerator

import dev.diff2test.android.core.GeneratedFile
import dev.diff2test.android.core.GeneratedTestBundle
import dev.diff2test.android.core.RiskLevel
import dev.diff2test.android.core.TestPlan
import dev.diff2test.android.core.TestType
import java.nio.file.Files
import java.nio.file.Path
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class GeneratedTestWriterTest {
    @Test
    fun `writes generated files under requested output root`() {
        val root = Files.createTempDirectory("d2t-generated-tests")
        val writer = FileSystemGeneratedTestWriter()
        val bundle = GeneratedTestBundle(
            plan = TestPlan(
                targetClass = "SignUpViewModel",
                targetMethods = listOf("submitRegistration"),
                testType = TestType.LOCAL_UNIT,
                scenarios = emptyList(),
                requiredFakes = emptyList(),
                assertions = emptyList(),
                riskLevel = RiskLevel.LOW,
            ),
            files = listOf(
                GeneratedFile(
                    relativePath = Path.of("src/test/kotlin/com/example/auth/SignUpViewModelTest.kt"),
                    content = "package com.example.auth",
                ),
            ),
        )

        val writtenFiles = writer.write(bundle, root)

        assertEquals(1, writtenFiles.size)
        assertTrue(Files.exists(writtenFiles.first()))
        assertEquals("package com.example.auth", Files.readString(writtenFiles.first()))
    }

    @Test
    fun `infers module root from source path`() {
        val source = Path.of("fixtures/sample-app/app/src/main/java/com/example/auth/SignUpViewModel.kt")

        val moduleRoot = inferModuleRootFromTarget(source)

        assertEquals(Path.of("fixtures/sample-app/app"), moduleRoot)
    }
}

