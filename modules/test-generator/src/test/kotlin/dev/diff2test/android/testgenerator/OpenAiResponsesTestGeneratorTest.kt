package dev.diff2test.android.testgenerator

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class OpenAiResponsesTestGeneratorTest {
    @Test
    fun `reads strix and llm environment variables without remapping`() {
        val config = responsesApiConfigFromEnvironment(
            environment = mapOf(
                "LLM_API_KEY" to "sk-local",
                "LLM_API_BASE" to "http://127.0.0.1:12345",
                "STRIX_LLM" to "glm-4.7-flash-claude-opus-4.5-high-reasoning-distill",
                "STRIX_RESONING_EFFORT" to "high",
                "OPENAI_API_KEY" to "sk-ignored",
                "OPENAI_MODEL" to "gpt-ignored",
            ),
        )

        assertEquals("sk-local", config?.apiKey)
        assertEquals("http://127.0.0.1:12345", config?.baseUrl)
        assertEquals("glm-4.7-flash-claude-opus-4.5-high-reasoning-distill", config?.model)
        assertEquals("high", config?.reasoningEffort)
    }

    @Test
    fun `includes reasoning effort in responses request when configured`() {
        val requestBody = buildResponsesRequest(
            config = ResponsesApiConfig(
                apiKey = "sk-local",
                model = "qwen3-coder-next-mlx",
                baseUrl = "http://127.0.0.1:12345",
                reasoningEffort = "high",
            ),
            instructions = "Generate tests",
            input = "input",
        )

        assertTrue("\"reasoning\"" in requestBody)
        assertTrue("\"effort\":\"high\"" in requestBody)
    }

    @Test
    fun `extracts structured payload from responses api body`() {
        val responseBody = """
            {
              "output": [
                {
                  "type": "message",
                  "content": [
                    {
                      "type": "output_text",
                      "text": "{\"content\":\"```kotlin\\npackage com.example.auth\\n\\nclass SignUpViewModelGeneratedTest\\n```\",\"warnings\":[\"generated from ai\"]}"
                    }
                  ]
                }
              ]
            }
        """.trimIndent()

        val payload = extractStructuredPayload(responseBody)

        assertEquals(
            "package com.example.auth\n\nclass SignUpViewModelGeneratedTest",
            payload.content,
        )
        assertEquals(listOf("generated from ai"), payload.warnings)
    }
}
