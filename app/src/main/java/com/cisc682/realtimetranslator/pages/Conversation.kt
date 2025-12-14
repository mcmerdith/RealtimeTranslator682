package com.cisc682.realtimetranslator.pages

import android.Manifest.permission_group.PHONE
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.cisc682.realtimetranslator.lib.TranslationLib
import com.cisc682.realtimetranslator.ui.components.LanguageDropdown
import com.cisc682.realtimetranslator.ui.components.SpeechBubble
import com.cisc682.realtimetranslator.ui.components.createSpeechRecognizer
import com.cisc682.realtimetranslator.ui.components.createTextToSpeech
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import kotlinx.coroutines.launch

// Represents a single message in the conversation
data class Message(val primaryText: String, val secondaryText: String, val isPrimary: Boolean)

@OptIn(ExperimentalPermissionsApi::class)
@Preview(device = PHONE)
@Composable
fun ConversationPage() {
    val coroutineScope = rememberCoroutineScope()

    // Languages
    var primaryLangTag by rememberSaveable { mutableStateOf("en") }
    var secondaryLangTag by rememberSaveable { mutableStateOf("es") }

    // Information about conversation state
    var isSwapped by rememberSaveable { mutableStateOf(false) }
    var conversationHistory by rememberSaveable { mutableStateOf(listOf<Message>()) }

    val swapLanguages = { isSwapped = !isSwapped }

    val translateAndAddMessage = { isPrimary: Boolean, text: String ->
        // Translate and store state
        coroutineScope.launch {
            val sourceLang = if (isPrimary) primaryLangTag else secondaryLangTag
            val targetLang = if (isPrimary) secondaryLangTag else primaryLangTag
            val translatedText = TranslationLib.translate(sourceLang, targetLang, text)
            conversationHistory = conversationHistory + if (isPrimary) Message(
                text, translatedText, true
            ) else Message(translatedText, text, false)
        }
    }

    Column(
        modifier = Modifier.fillMaxSize().let {
            // Rotate the entire page if swapped
            if (isSwapped) {
                it.graphicsLayer { rotationZ = 180f }
            } else {
                it
            }
        }) {
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .graphicsLayer { rotationZ = 180f }) {
            ConversationHalf(
                languageTag = secondaryLangTag,
                isPrimary = false,
                isSwapped = isSwapped,
                messages = conversationHistory,
                onSwapPressed = swapLanguages,
                onLanguageChange = { newLanguage: String -> secondaryLangTag = newLanguage },
                onMessageCreated = {
                    translateAndAddMessage(false, it)
                }
            )
        }
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            ConversationHalf(
                languageTag = primaryLangTag,
                isPrimary = true,
                isSwapped = isSwapped,
                messages = conversationHistory,
                onSwapPressed = swapLanguages,
                onLanguageChange = { newLanguage: String -> primaryLangTag = newLanguage },
                onMessageCreated = {
                    translateAndAddMessage(true, it)
                }
            )
        }
    }
}

/**
 * The primary user interface for the conversation view
 *
 * @param languageTag The current language of this half
 * @param isPrimary Whether this half is the primary speaker
 * @param isSwapped Whether the speakers positions are swapped
 * @param messages The conversation history
 * @param onSwapPressed The function to call when the swap button is pressed
 * @param onLanguageChange The function to call when the language dropdown is changed
 * @param onMessageCreated The function to call when a message is created
 */
@Composable
fun ConversationHalf(
    languageTag: String,
    isPrimary: Boolean,
    isSwapped: Boolean,
    messages: List<Message>,
    onSwapPressed: () -> Unit,
    onLanguageChange: (String) -> Unit,
    onMessageCreated: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    // Text to Speech
    val speak = createTextToSpeech(languageTag)
    // Speech to Text
    val showSpeechRecognition = createSpeechRecognizer(
        languageTag = languageTag,
        flipped = if (isSwapped) isPrimary else !isPrimary,
        onSpeechResult = { onMessageCreated(it) })

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Message display box
        Card(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                if (messages.isEmpty()) {
                    Text(
                        modifier = Modifier.align(Alignment.Center),
                        text = "Tap the mic to start!"
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(), contentPadding = PaddingValues(
                            start = 8.dp, end = 8.dp, top = 8.dp, bottom = 72.dp
                        ), verticalArrangement = Arrangement.spacedBy(8.dp), reverseLayout = true
                    ) {
                        // Messages are stored as "latest last" but we want latest first
                        items(messages.reversed()) { message ->
                            val isMyMessage = message.isPrimary == isPrimary
                            val bubbleColor =
                                if (isMyMessage) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.secondaryContainer
                            val textColor =
                                if (isMyMessage) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSecondaryContainer

                            val nativeLangText =
                                if (isPrimary) message.primaryText else message.secondaryText
                            val otherLangText =
                                if (isPrimary) message.secondaryText else message.primaryText
                            val speechBubbleText = arrayOf(nativeLangText, otherLangText)

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = if (isMyMessage) Arrangement.End else Arrangement.Start
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(0.75f),
                                    horizontalArrangement = if (isMyMessage) Arrangement.End else Arrangement.Start
                                ) {
                                    SpeechBubble(
                                        text = speechBubbleText,
                                        alignment = if (isMyMessage) Alignment.End else Alignment.Start,
                                        color = bubbleColor,
                                        textColor = textColor,
                                        speak = speak,
                                    )
                                }
                            }
                        }
                    }
                }
                IconButton(
                    onClick = showSpeechRecognition,
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(16.dp)
                ) {
                    Icon(Icons.Default.Mic, contentDescription = "Speak message")
                }
            }
        }

        // Controls row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            LanguageDropdown(languageTag, onLanguageChange)
            Spacer(modifier = Modifier.weight(1f))
            Button(onClick = onSwapPressed) {
                Icon(Icons.Default.SwapHoriz, contentDescription = "Swap Languages")
                Spacer(modifier = Modifier.width(4.dp))
                Text(text = "Swap")
            }
        }
    }
}

@Preview
@Composable
private fun ConversationHalfPreview() {
    val messages = listOf(
        Message("Hello!", "Hola!", true),
        Message("How are you?", "Cómo estás?", false),
        Message("I'm fine, thank you.", "Bien, gracias.", true),
        Message(
            "do you know where there is a library nearby",
            "¿Sabes dónde hay una biblioteca cerca?",
            false
        )
    )
    Column(modifier = Modifier.fillMaxSize()) {
        ConversationHalf(
            "es",
            isPrimary = false,
            isSwapped = false,
            messages = messages,
            onSwapPressed = {},
            onLanguageChange = {},
            onMessageCreated = {},
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        )
        ConversationHalf(
            "en",
            isPrimary = true,
            isSwapped = false,
            messages = messages,
            onSwapPressed = {},
            onLanguageChange = {},
            onMessageCreated = {},
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        )

    }
}