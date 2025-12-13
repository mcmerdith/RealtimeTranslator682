package com.cisc682.realtimetranslator.pages

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
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.tooling.preview.PreviewScreenSizes
import androidx.compose.ui.unit.dp
import com.cisc682.realtimetranslator.lib.TranslationLib
import com.cisc682.realtimetranslator.ui.components.LanguageDropdown
import com.cisc682.realtimetranslator.ui.components.SpeechBubble
import com.cisc682.realtimetranslator.ui.components.createSpeechRecognizer
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import kotlinx.coroutines.launch

// Represents a single message in the conversation
data class Message(val primaryText: String, val secondaryText: String, val isPrimary: Boolean)

@OptIn(ExperimentalPermissionsApi::class)
@PreviewScreenSizes
@Composable
fun ConversationPage() {
    // Languages
    var primaryLangTag by remember { mutableStateOf("en") }
    var secondaryLangTag by remember { mutableStateOf("es") }

    // Information about conversation state
    var isSwapped by remember { mutableStateOf(false) }
    var conversationHistory by remember { mutableStateOf(listOf<Message>()) }


    val onSwapLanguages = {
        isSwapped = !isSwapped
    }

    val onPrimaryLanguageChange = { newLanguage: String ->
        primaryLangTag = newLanguage
    }

    val onSecondaryLanguageChange = { newLanguage: String ->
        secondaryLangTag = newLanguage
    }

    // Speech recognition state
    var isPrimarySpeaker by remember { mutableStateOf(true) }
    val coroutineScope = rememberCoroutineScope()

    // Speech to Text
    val showSpeechRecognition = createSpeechRecognizer(
        language = if (isPrimarySpeaker) primaryLangTag else secondaryLangTag,
        flipped = if (isSwapped) isPrimarySpeaker else !isPrimarySpeaker,
        onSpeechResult = {
            coroutineScope.launch {
                val sourceLang = if (isPrimarySpeaker) primaryLangTag else secondaryLangTag
                val targetLang = if (isPrimarySpeaker) secondaryLangTag else primaryLangTag
                val translatedText = TranslationLib.translate(sourceLang, targetLang, it)
                conversationHistory = conversationHistory + if (isPrimarySpeaker) Message(
                    it, translatedText, true
                ) else Message(translatedText, it, false)
            }
        })

    val onMicClick = { isPrimary: Boolean ->
        isPrimarySpeaker = isPrimary
        showSpeechRecognition()
    }

    Column(
        modifier = Modifier.fillMaxSize().let {
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
                language = secondaryLangTag,
                messages = conversationHistory,
                onSwap = onSwapLanguages,
                isPrimary = false,
                onMicClick = { onMicClick(false) },
                onLanguageChange = onSecondaryLanguageChange
            )
        }
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            ConversationHalf(
                language = primaryLangTag,
                messages = conversationHistory,
                onSwap = onSwapLanguages,
                isPrimary = true,
                onMicClick = { onMicClick(true) },
                onLanguageChange = onPrimaryLanguageChange
            )
        }
    }
}

@Composable
fun ConversationHalf(
    language: String,
    messages: List<Message>,
    onSwap: () -> Unit,
    isPrimary: Boolean,
    onMicClick: () -> Unit,
    onLanguageChange: (String) -> Unit
) {
    Column(
        modifier = Modifier
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
                    Text(modifier = Modifier.align(Alignment.Center), text = "Tap the mic to start!")
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(), contentPadding = PaddingValues(
                            start = 8.dp, end = 8.dp, top = 8.dp, bottom = 72.dp
                        ), verticalArrangement = Arrangement.spacedBy(8.dp), reverseLayout = true
                    ) {
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
                                    )
                                }
                            }
                        }
                    }
                }
                IconButton(
                    onClick = onMicClick,
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(16.dp)
                ) {
                    Icon(Icons.Default.Mic, contentDescription = "Record audio")
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
            LanguageDropdown(language, onLanguageChange)
            Spacer(modifier = Modifier.weight(1f))
            Button(onClick = onSwap) {
                Icon(Icons.Default.SwapHoriz, contentDescription = "Swap Languages")
                Spacer(modifier = Modifier.width(4.dp))
                Text(text = "Swap")
            }
        }
    }
}
