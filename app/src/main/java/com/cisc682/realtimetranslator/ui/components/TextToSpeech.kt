package com.cisc682.realtimetranslator.ui.components

import android.speech.tts.TextToSpeech
import android.widget.Toast
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import com.cisc682.realtimetranslator.lib.TranslationLib

enum class TtsState {
    READY,
    FAILED,
    INIT
}

@Composable
fun createTextToSpeech(languageTag: String): (text: String) -> Unit {
    // Text-to-speech is not available in preview mode
    if (LocalInspectionMode.current) return { }
    var tts by remember { mutableStateOf<TextToSpeech?>(null) }
    var ttsState by rememberSaveable { mutableStateOf(TtsState.INIT) }
    val context = LocalContext.current

    DisposableEffect(context) {
        val ttsInstance = TextToSpeech(context) {
            ttsState = if (it == TextToSpeech.SUCCESS) TtsState.READY else TtsState.FAILED
        }
        tts = ttsInstance

        onDispose {
            ttsInstance.stop()
            ttsInstance.shutdown()
        }
    }

    LaunchedEffect(ttsState, languageTag) {
        when (ttsState) {
            TtsState.INIT -> {}
            TtsState.READY -> {
                tts?.let {
                    it.language = TranslationLib.getLocale(languageTag)
                } ?: run {
                    Toast.makeText(
                        context,
                        "Text to speech setup failed (not initialized)!",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
            TtsState.FAILED -> {
                Toast.makeText(context, "Text to speech setup failed!", Toast.LENGTH_LONG).show()
            }
        }
    }

    return { text ->
        when (ttsState) {
            TtsState.INIT -> {
                Toast.makeText(
                    context,
                    "Text to speech not ready, try again later",
                    Toast.LENGTH_LONG
                ).show()
            }
            TtsState.READY -> {
                tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, null) ?: run {
                    Toast.makeText(context, "Text to speech not initialized!", Toast.LENGTH_LONG).show()
                }
            }
            TtsState.FAILED -> {
                Toast.makeText(context, "Text to speech failed!", Toast.LENGTH_LONG).show()
            }
        }
    }
}