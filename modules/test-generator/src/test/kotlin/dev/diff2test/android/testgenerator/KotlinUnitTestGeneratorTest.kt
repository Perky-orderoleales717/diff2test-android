package dev.diff2test.android.testgenerator

import dev.diff2test.android.core.CollaboratorDependency
import dev.diff2test.android.core.RiskLevel
import dev.diff2test.android.core.StyleGuide
import dev.diff2test.android.core.TargetMethod
import dev.diff2test.android.core.TestContext
import dev.diff2test.android.core.TestPlan
import dev.diff2test.android.core.TestScenario
import dev.diff2test.android.core.TestType
import dev.diff2test.android.core.ViewModelAnalysis
import java.nio.file.Files
import java.nio.file.Path
import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertFalse

class KotlinUnitTestGeneratorTest {
    @Test
    fun `imports and instantiates saved state handle for generated search tests`() {
        val repoRoot = findRepoRoot()
        val analysis = ViewModelAnalysis(
            className = "SearchViewModel",
            packageName = "com.example.search",
            filePath = repoRoot.resolve("fixtures/sample-app/app/src/main/java/com/example/search/SearchViewModel.kt"),
            constructorDependencies = listOf(
                CollaboratorDependency(name = "savedStateHandle", type = "SavedStateHandle"),
                CollaboratorDependency(name = "repository", type = "SearchRepository"),
                CollaboratorDependency(name = "ioDispatcher", type = "CoroutineDispatcher"),
            ),
            publicMethods = listOf(
                TargetMethod(name = "onQueryChanged", signature = "fun onQueryChanged(value: String)", mutatesState = true),
            ),
            stateHolders = listOf("uiState: StateFlow<SearchUiState>"),
            primaryStateHolderName = "uiState",
            primaryStateType = "SearchUiState",
        )
        val plan = TestPlan(
            targetClass = "SearchViewModel",
            targetMethods = listOf("onQueryChanged"),
            testType = TestType.LOCAL_UNIT,
            scenarios = listOf(
                TestScenario(
                    name = "onQueryChanged updates state on success",
                    goal = "happy path",
                    expectedOutcome = "state updated",
                ),
            ),
            requiredFakes = emptyList(),
            assertions = emptyList(),
            riskLevel = RiskLevel.LOW,
        )

        val bundle = KotlinUnitTestGenerator().generate(
            plan = plan,
            context = TestContext(moduleName = "app", styleGuide = StyleGuide()),
            analysis = analysis,
        )

        val content = bundle.files.single().content
        assertContains(content, "import androidx.lifecycle.SavedStateHandle")
        assertContains(content, "savedStateHandle = SavedStateHandle()")
    }

    @Test
    fun `generates shared flow event assertion for onboarding fixture`() {
        val repoRoot = findRepoRoot()
        val analysis = ViewModelAnalysis(
            className = "OnboardingViewModel",
            packageName = "com.example.onboarding",
            filePath = repoRoot.resolve("fixtures/sample-app/app/src/main/java/com/example/onboarding/OnboardingViewModel.kt"),
            publicMethods = listOf(
                TargetMethod(
                    name = "completeOnboarding",
                    signature = "fun completeOnboarding()",
                    body = "_events.tryEmit(OnboardingEvent.NavigateHome)",
                    mutatesState = true,
                ),
            ),
            stateHolders = listOf("events: SharedFlow<OnboardingEvent>"),
        )
        val plan = TestPlan(
            targetClass = "OnboardingViewModel",
            targetMethods = listOf("completeOnboarding"),
            testType = TestType.LOCAL_UNIT,
            scenarios = listOf(
                TestScenario(
                    name = "completeOnboarding emits navigate home",
                    goal = "event emission",
                    expectedOutcome = "NavigateHome is emitted",
                ),
            ),
            requiredFakes = emptyList(),
            assertions = emptyList(),
            riskLevel = RiskLevel.LOW,
        )

        val bundle = KotlinUnitTestGenerator().generate(
            plan = plan,
            context = TestContext(moduleName = "app", styleGuide = StyleGuide()),
            analysis = analysis,
        )

        val content = bundle.files.single().content
        assertContains(content, "viewModel.completeOnboarding()")
        assertContains(content, "assertEquals(OnboardingEvent.NavigateHome, viewModel.events.replayCache.last())")
    }

    @Test
    fun `does not generate replay cache event assertion for non replaying shared flow`() {
        val repoRoot = findRepoRoot()
        val analysis = ViewModelAnalysis(
            className = "SignUpViewModel",
            packageName = "com.example.auth",
            filePath = repoRoot.resolve("fixtures/sample-app/app/src/main/java/com/example/auth/SignUpViewModel.kt"),
            publicMethods = listOf(
                TargetMethod(
                    name = "submitRegistration",
                    signature = "fun submitRegistration()",
                    body = "_events.emit(SignUpEvent.RegistrationCompleted)",
                    mutatesState = true,
                ),
            ),
            stateHolders = listOf(
                "uiState: StateFlow<SignUpUiState>",
                "events: SharedFlow<SignUpEvent>",
            ),
        )
        val plan = TestPlan(
            targetClass = "SignUpViewModel",
            targetMethods = listOf("submitRegistration"),
            testType = TestType.LOCAL_UNIT,
            scenarios = listOf(
                TestScenario(
                    name = "submitRegistration emits event",
                    goal = "event emission",
                    expectedOutcome = "RegistrationCompleted is emitted",
                ),
            ),
            requiredFakes = emptyList(),
            assertions = emptyList(),
            riskLevel = RiskLevel.LOW,
        )

        val bundle = KotlinUnitTestGenerator().generate(
            plan = plan,
            context = TestContext(moduleName = "app", styleGuide = StyleGuide()),
            analysis = analysis,
        )

        val content = bundle.files.single().content
        assertFalse("replayCache.last()" in content)
    }

    private fun findRepoRoot(): Path {
        var current: Path? = Path.of(System.getProperty("user.dir")).toAbsolutePath().normalize()

        while (current != null) {
            if (Files.exists(current.resolve("fixtures/sample-app"))) {
                return current
            }
            current = current.parent
        }

        error("Could not locate repository root from ${System.getProperty("user.dir")}")
    }
}
