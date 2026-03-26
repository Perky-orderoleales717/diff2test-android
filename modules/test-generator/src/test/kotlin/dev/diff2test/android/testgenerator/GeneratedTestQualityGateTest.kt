package dev.diff2test.android.testgenerator

import dev.diff2test.android.core.GeneratedFile
import dev.diff2test.android.core.GeneratedTestBundle
import dev.diff2test.android.core.RiskLevel
import dev.diff2test.android.core.TestPlan
import dev.diff2test.android.core.TestType
import java.nio.file.Path
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class GeneratedTestQualityGateTest {
    @Test
    fun `allows class-level dispatcher when runTest binds to it`() {
        val report = GeneratedTestQualityGate().evaluate(
            bundle(
                """
                    package com.example.auth

                    import kotlinx.coroutines.test.StandardTestDispatcher
                    import kotlinx.coroutines.test.runTest
                    import kotlin.test.Test
                    import kotlin.test.assertEquals

                    class LoginViewModelGeneratedTest {
                        private val testDispatcher = StandardTestDispatcher()

                        @Test
                        fun `login updates state on success`() = runTest(testDispatcher) {
                            assertEquals(1, 1)
                        }
                    }
                """.trimIndent(),
            ),
        )

        assertTrue(report.passed)
    }

    @Test
    fun `blocks detached class-level dispatcher when runTest does not bind it`() {
        val report = GeneratedTestQualityGate().evaluate(
            bundle(
                """
                    package com.example.auth

                    import kotlinx.coroutines.test.StandardTestDispatcher
                    import kotlinx.coroutines.test.runTest
                    import kotlin.test.Test
                    import kotlin.test.assertEquals

                    class LoginViewModelGeneratedTest {
                        private val testDispatcher = StandardTestDispatcher()

                        @Test
                        fun `login updates state on success`() = runTest {
                            assertEquals(1, 1)
                        }
                    }
                """.trimIndent(),
            ),
        )

        assertFalse(report.passed)
        assertTrue(report.issues.any { "StandardTestDispatcher()" in it })
    }

    private fun bundle(content: String) = GeneratedTestBundle(
        plan = TestPlan(
            targetClass = "LoginViewModel",
            targetMethods = listOf("login"),
            testType = TestType.LOCAL_UNIT,
            scenarios = emptyList(),
            requiredFakes = emptyList(),
            assertions = emptyList(),
            riskLevel = RiskLevel.LOW,
        ),
        files = listOf(
            GeneratedFile(
                relativePath = Path.of("src/test/kotlin/com/example/auth/LoginViewModelGeneratedTest.kt"),
                content = content,
            ),
        ),
    )
}
