package dev.diff2test.android.testgenerator

import kotlin.test.Test
import kotlin.test.assertEquals

class OpenAiResponsesTestGeneratorTest {
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
