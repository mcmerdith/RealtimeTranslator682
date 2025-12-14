package com.cisc682.realtimetranslator.ui.components

import android.Manifest
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.util.Log
import android.widget.Toast
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
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.Locale

/**
 * Create a speech recognizer function that can be used in a composable
 *
 * Handles permissions automatically
 *
 * @param languageTag The language to use for speech recognition
 * @param flipped Whether the speech recognition dialog should be flipped vertically
 * @param onSpeechResult The function to call when speech recognition is complete
 * @return A function that can be called to start speech recognition
 */
@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun createSpeechRecognizer(
    languageTag: String,
    flipped: Boolean,
    onSpeechResult: (String) -> Unit,
): () -> Unit {
    val context = LocalContext.current

    var active by rememberSaveable { mutableStateOf(false) }
    var showSpeechRecognition by rememberSaveable { mutableStateOf(false) }

    val permissionState =
        rememberPermissionState(permission = Manifest.permission.RECORD_AUDIO) { granted ->
            if (granted) {
                // Automatically show the dialog if permission is granted
                showSpeechRecognition = true
            } else {
                // Warn the user that speech recognition was denied
                Toast.makeText(context, "Microphone permission denied", Toast.LENGTH_LONG).show()
            }
        }

    LaunchedEffect(active) {
        // Whenever the active state changes, check for permission and show the dialog, or close it
        if (active) {
            if (permissionState.status.isGranted) {
                showSpeechRecognition = true
            } else {
                permissionState.launchPermissionRequest()
            }
        } else {
            showSpeechRecognition = false
        }
    }

    // If the speech recognition component should be shown, show it
    if (showSpeechRecognition) {
        SpeechRecognition(
            language = languageTag,
            flipped = flipped,
            onSpeechResult = onSpeechResult,
            onClose = {
                // Mark the component as ready to be closed
                active = false
            }
        )
    }

    // Return a function to initiate speech recognition
    return { ->
        active = true
    }
}

/**
 * Internal component to actually perform speech recognition
 *
 * Caller must handle ensuring the appropriate permissions are granted
 *
 * @param language The language to use for speech recognition
 * @param flipped Whether the speech recognition dialog should be flipped vertically
 * @param onSpeechResult The function to call when speech recognition is complete
 * @param onClose The function to call when the dialog is closed
 */
@Composable
private fun SpeechRecognition(
    language: String,
    flipped: Boolean,
    onSpeechResult: (String) -> Unit,
    onClose: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    val speechRecognizer = remember { SpeechRecognizer.createSpeechRecognizer(context) }
    val speechRecognizerIntent = remember { Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH) }

    // The text to display as a preview
    var partialText by remember { mutableStateOf("") }
    var isListening by remember { mutableStateOf(false) }
    // Current volume of mic input for the animation
    var rmsDb by remember { mutableFloatStateOf(0f) }

    // Set up the speech recognizer
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

    // Create the listener
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
            partialText = "Press the microphone to try again"
        }

        override fun onResults(results: Bundle?) {
            isListening = false
            val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
            // Complete recognition if results returned, or setup the retry state
            if (matches != null && matches.isNotEmpty() && matches[0].isNotBlank()) {
                partialText = matches[0]
                onSpeechResult(matches[0])
                coroutineScope.launch {
                    // Give the UI some time to render a preview, and for the caller to use the result
                    delay(500)
                    onClose()
                }
            } else {
                partialText = "Press the microphone to try again"
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
        // Start listening immediately
        speechRecognizer.startListening(speechRecognizerIntent)
    }

    DisposableEffect(Unit) {
        // Clean up the recognizer
        onDispose {
            speechRecognizer.destroy()
        }
    }

    // Animate the current mic volume
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
                    if (flipped) {
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
