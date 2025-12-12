package com.cisc682.realtimetranslator.lib

import com.google.mlkit.nl.translate.TranslateLanguage
import com.google.mlkit.nl.translate.Translation
import com.google.mlkit.nl.translate.TranslatorOptions
import kotlinx.coroutines.tasks.await

class TranslationLib {
    companion object {
        suspend fun translate(sourceLanguage: String, targetLanguage: String, text: String): String {
            val options = TranslatorOptions.Builder()
                .setSourceLanguage(TranslateLanguage.fromLanguageTag(sourceLanguage)!!)
                .setTargetLanguage(TranslateLanguage.fromLanguageTag(targetLanguage)!!)
                .build()
            val translator = Translation.getClient(options)
            try {
                translator.downloadModelIfNeeded().await()
                return translator.translate(text).await()
            } finally {
                translator.close()
            }
        }
    }
}