package com.example.nammapustaka1.utils

import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.content
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object GeminiHelper {

    private const val API_KEY = "AIzaSyC-PNup-YdMf2rvmjhjXlIFhLC5zWgx0rk"
    private const val MODEL_NAME = "gemini-1.5-flash"

    private val model by lazy {
        GenerativeModel(
            modelName = MODEL_NAME,
            apiKey = API_KEY
        )
    }

    /**
     * Generate a Kannada summary for a book given its title and author.
     * Returns the summary string or null on error.
     */
    suspend fun generateKannadaSummary(title: String, author: String): String? {
        return withContext(Dispatchers.IO) {
            try {
                val prompt = """
                    ಕೆಳಗಿನ ಪುಸ್ತಕದ ಬಗ್ಗೆ ಕನ್ನಡದಲ್ಲಿ 2-3 ವಾಕ್ಯಗಳ ಸರಳ ಸಾರಾಂಶ ಬರೆಯಿರಿ.
                    ಗ್ರಾಮೀಣ ಶಾಲೆಯ ಮಕ್ಕಳಿಗೆ ಅರ್ಥವಾಗುವಂತೆ ಸರಳ ಭಾಷೆ ಬಳಸಿ.
                    
                    ಪುಸ್ತಕ: $title
                    ಲೇಖಕ: $author
                    
                    ಕೇವಲ ಸಾರಾಂಶ ಮಾತ್ರ ಬರೆಯಿರಿ, ಬೇರೆ ಏನೂ ಬರೆಯಬೇಡಿ.
                """.trimIndent()

                val response = model.generateContent(
                    content { text(prompt) }
                )
                response.text?.trim()
            } catch (e: Exception) {
                null
            }
        }
    }

    /**
     * Extract book info (title, author) from a camera-captured image description
     * or scanned text using Gemini vision.
     */
    suspend fun extractBookInfoFromText(rawText: String): Pair<String, String>? {
        return withContext(Dispatchers.IO) {
            try {
                val prompt = """
                    From the following text (scanned from a book cover or title page), 
                    extract ONLY the book title and author name.
                    
                    Text: $rawText
                    
                    Reply in this exact format:
                    TITLE: <book title>
                    AUTHOR: <author name>
                    
                    If you cannot find the information, reply:
                    TITLE: Unknown
                    AUTHOR: Unknown
                """.trimIndent()

                val response = model.generateContent(
                    content { text(prompt) }
                )
                val text = response.text?.trim() ?: return@withContext null
                val lines = text.lines()
                val title = lines.find { it.startsWith("TITLE:") }?.removePrefix("TITLE:")?.trim()
                val author = lines.find { it.startsWith("AUTHOR:") }?.removePrefix("AUTHOR:")?.trim()
                if (title != null && author != null) Pair(title, author) else null
            } catch (e: Exception) {
                null
            }
        }
    }
}
