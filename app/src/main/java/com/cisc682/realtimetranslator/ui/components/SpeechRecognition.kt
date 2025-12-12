package com.cisc682.realtimetranslator.ui.components

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.util.Log
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.Locale

@Composable
fun SpeechRecognition(
    context: Context,
    language: String,
    isPrimarySpeaker: Boolean,
    onSpeechResult: (String) -> Unit,
    onClose: () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    val speechRecognizer = remember { SpeechRecognizer.createSpeechRecognizer(context) }
    val speechRecognizerIntent = remember { Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH) }
    var partialText by remember { mutableStateOf("") }
    var isListening by remember { mutableStateOf(false) }
    var rmsDb by remember { mutableFloatStateOf(0f) }

    speechRecognizerIntent.putExtra(
        RecognizerIntent.EXTRA_LANGUAGE_MODEL,
        RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
    )
    speechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, language)
    speechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
    speechRecognizerIntent.putExtra(
        RecognizerIntent.EXTRA_SPEECH_INPUT_POSSIBLY_COMPLETE_SILENCE_LENGTH_MILLIS,
        2000L
    )

    val recognitionListener = object : RecognitionListener {
        override fun onReadyForSpeech(params: Bundle?) {
            isListening = true
            partialText = "Listening for ${Locale.forLanguageTag(language).displayLanguage}..."
        }

        override fun onBeginningOfSpeech() {}
        override fun onRmsChanged(rmsdB: Float) {
            rmsDb = rmsdB
        }

        override fun onBufferReceived(buffer: ByteArray?) {}
        override fun onEndOfSpeech() {
            isListening = false
        }

        override fun onError(error: Int) {
            Log.e("SpeechRecognition", "Error: $error")
            isListening = false
            partialText = "Try again"
        }

        override fun onResults(results: Bundle?) {
            isListening = false
            val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
            if (matches != null && matches.isNotEmpty() && matches[0].isNotBlank()) {
                partialText = matches[0]
                onSpeechResult(matches[0])
                coroutineScope.launch {
                    delay(500)
                    onClose()
                }
            } else {
                partialText = "Try again"
            }
        }

        override fun onPartialResults(partialResults: Bundle?) {
            val matches = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
            if (matches != null && matches.isNotEmpty()) {
                partialText = matches[0]
            }
        }

        override fun onEvent(eventType: Int, params: Bundle?) {}
    }

    speechRecognizer.setRecognitionListener(recognitionListener)

    LaunchedEffect(Unit) {
        speechRecognizer.startListening(speechRecognizerIntent)
    }

    DisposableEffect(Unit) {
        onDispose {
            speechRecognizer.destroy()
        }
    }

    val animatedScale by animateFloatAsState(
        targetValue = if (isListening) (1f + rmsDb / 8f).coerceIn(1f, 1.5f) else 1f,
        label = ""
    )

    Dialog(onDismissRequest = onClose) {
        Surface(
            shape = MaterialTheme.shapes.large,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .wrapContentHeight()
                .let {
                    if (!isPrimarySpeaker) {
                        it.graphicsLayer { rotationZ = 180f }
                    } else {
                        it
                    }
                }
        ) {
            Column(
                modifier = Modifier
                    .padding(vertical = 24.dp, horizontal = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    text = partialText,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp)
                )
                IconButton(
                    onClick = {
                        if (!isListening) {
                            speechRecognizer.startListening(speechRecognizerIntent)
                        } else {
                            speechRecognizer.stopListening()
                        }
                    },
                    colors = if (isListening) {
                        IconButtonDefaults.iconButtonColors(contentColor = MaterialTheme.colorScheme.primary)
                    } else {
                        IconButtonDefaults.iconButtonColors()
                    }
                ) {
                    Icon(
                        imageVector = Icons.Default.Mic,
                        contentDescription = "Microphone",
                        modifier = Modifier.graphicsLayer {
                            scaleX = animatedScale
                            scaleY = animatedScale
                        }
                    )
                }
            }
        }
    }
}
