package com.cisc682.realtimetranslator.lib

import com.google.mlkit.nl.translate.TranslateLanguage
import com.google.mlkit.nl.translate.Translation
import com.google.mlkit.nl.translate.TranslatorOptions
import kotlinx.coroutines.tasks.await
import java.util.Locale

class TranslationLib {
    companion object {
        suspend fun translate(sourceLangTag: String, targetLangTag: String, text: String): String {
            val sourceLang = TranslateLanguage.fromLanguageTag(sourceLangTag)
            val targetLang = TranslateLanguage.fromLanguageTag(targetLangTag)
            if (sourceLang == null || targetLang == null) {
                return "Translation failed (not supported)!"
            }
            val options = TranslatorOptions.Builder()
                .setSourceLanguage(sourceLang)
                .setTargetLanguage(targetLang)
                .build()
            val translator = Translation.getClient(options)
            try {
                translator.downloadModelIfNeeded().await()
                return translator.translate(text).await()
            } finally {
                translator.close()
            }
        }

        fun getLocale(languageTag: String): Locale {
            return Locale.forLanguageTag(languageTag)
        }

        fun getDisplayName(languageTag: String): String {
            return getLocale(languageTag).displayLanguage
        }

        fun getAllLanguages(): List<String> {
            return TranslateLanguage.getAllLanguages()
        }
    }
}