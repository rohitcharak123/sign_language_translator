package com.example.model

/**
 * Supported sign languages with grammar models and regional datasets.
 */
enum class SignLanguage(
    val code: String,
    val displayName: String,
    val flagEmoji: String,
    val datasetRef: String,
    val grammarStructure: String,
    val isTwoHandedAlphabet: Boolean = false
) {
    ASL(
        code = "ASL",
        displayName = "American Sign Language",
        flagEmoji = "🇺🇸",
        datasetRef = "WLASL / MS-ASL",
        grammarStructure = "Topic-Comment (Time + Topic + Comment)",
        isTwoHandedAlphabet = false
    ),
    ISL(
        code = "ISL",
        displayName = "Indian Sign Language",
        flagEmoji = "🇮🇳",
        datasetRef = "INCLUDE Dataset",
        grammarStructure = "Subject-Object-Verb (SOV)",
        isTwoHandedAlphabet = false
    ),
    BSL(
        code = "BSL",
        displayName = "British Sign Language",
        flagEmoji = "🇬🇧",
        datasetRef = "BSL Corpus",
        grammarStructure = "Topic-Comment / Spatial placement",
        isTwoHandedAlphabet = true
    ),
    JSL(
        code = "JSL",
        displayName = "Japanese Sign Language (Shuwa)",
        flagEmoji = "🇯🇵",
        datasetRef = "JSL Lexicon",
        grammarStructure = "SOV with Mouthing (Shu-wa)",
        isTwoHandedAlphabet = false
    ),
    KSL(
        code = "KSL",
        displayName = "Korean Sign Language",
        flagEmoji = "🇰🇷",
        datasetRef = "KSL Corpus",
        grammarStructure = "SOV with Non-Manual Signals",
        isTwoHandedAlphabet = false
    ),
    AUSLAN(
        code = "AUSLAN",
        displayName = "Australian Sign Language",
        flagEmoji = "🇦🇺",
        datasetRef = "Auslan Archive",
        grammarStructure = "Topic-Comment (BANZSL family)",
        isTwoHandedAlphabet = true
    );

    companion object {
        fun fromCode(code: String): SignLanguage {
            return entries.firstOrNull { it.code.equals(code, ignoreCase = true) } ?: ASL
        }
    }
}
