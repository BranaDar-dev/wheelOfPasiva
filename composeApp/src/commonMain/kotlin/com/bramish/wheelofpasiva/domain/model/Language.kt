package com.bramish.wheelofpasiva.domain.model

/**
 * Supported languages for the game.
 */
enum class Language {
    ENGLISH,
    HEBREW;

    companion object {
        /**
         * Converts a string to Language enum.
         */
        fun fromString(value: String?): Language {
            return when (value?.uppercase()) {
                "HEBREW" -> HEBREW
                else -> ENGLISH // Default to English
            }
        }
    }

    /**
     * Returns the display name of the language.
     */
    fun displayName(): String {
        return when (this) {
            ENGLISH -> "English"
            HEBREW -> "עברית"
        }
    }

    /**
     * Returns the alphabet for this language.
     */
    fun getAlphabet(): List<Char> {
        return when (this) {
            ENGLISH -> ('A'..'Z').toList()
            HEBREW -> listOf(
                'א', 'ב', 'ג', 'ד', 'ה', 'ו', 'ז', 'ח', 'ט',
                'י', 'כ', 'ל', 'מ', 'נ', 'ס', 'ע', 'פ', 'צ',
                'ק', 'ר', 'ש', 'ת'
            )
        }
    }
}
