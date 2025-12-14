package com.cisc682.realtimetranslator.lib

import android.util.Log
import com.google.mlkit.nl.translate.TranslateLanguage
import com.google.mlkit.nl.translate.Translation
import com.google.mlkit.nl.translate.TranslatorOptions
import kotlinx.coroutines.tasks.await
import java.util.Locale

/**
 * Wrapper around Locale and the current translator implementation
 */
class TranslationLib {
    companion object {
        /**
         * Translate text from one language to another
         *
         * @param sourceLangTag The source language
         * @param targetLangTag The target language
         * @param text The text to translate
         * @return The translated text
         */
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
                return translator.use {
                    // Make sure we have the model
                    it.downloadModelIfNeeded().await()
                    it.translate(text).await()
                }
            } catch (e: Exception) {
                Log.e("TranslationLib", "Failed to translate! ${e.localizedMessage ?: e.message}")
                return "Failed to translate!"
            }
        }

        /**
         * Get the Locale object for a given language
         */
        fun getLocale(languageTag: String): Locale {
            return Locale.forLanguageTag(languageTag)
        }

        /**
         * Get the readable display name for a language
         */
        fun getDisplayName(languageTag: String): String {
            return getLocale(languageTag).displayLanguage
        }

        /**
         * Get the list of languages supported by the translator
         */
        fun getAllLanguages(): List<String> {
            return TranslateLanguage.getAllLanguages()
        }
    }
}