package com.cisc682.realtimetranslator.pages

import android.content.Intent
import android.speech.RecognizerIntent
import android.speech.tts.TextToSpeech
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.VolumeUp
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material.icons.outlined.Campaign
import androidx.compose.material.icons.outlined.ContentCopy
import androidx.compose.material.icons.outlined.ContentPaste
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.util.Locale
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun TranslateTextPage() {
    var sourceText by rememberSaveable { mutableStateOf("") }
    var translatedText by rememberSaveable { mutableStateOf("") }
    var isStarred by rememberSaveable { mutableStateOf(false) }

    // Default source language to English, target to Spanish
    var sourceLanguage by rememberSaveable { mutableStateOf("English") }
    var targetLanguage by rememberSaveable { mutableStateOf("Spanish") }

    // State to track if source text was copied
    var isSourceCopied by rememberSaveable { mutableStateOf(false) }
    // State to track if translated text was copied
    var isTranslatedCopied by rememberSaveable { mutableStateOf(false) }

    val clipboardManager = LocalClipboardManager.current
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    // Text-to-Speech
    var tts by remember { mutableStateOf<TextToSpeech?>(null) }

    // Speech-to-Text (Voice Recognition)
    val speechRecognizerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val data = result.data
        val results = data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
        results?.let {
            if (it.isNotEmpty()) {
                sourceText = it[0] // Get the first recognized text
            }
        }
    }

    DisposableEffect(context) {
        val ttsInstance = TextToSpeech(context, null)
        tts = ttsInstance

        onDispose {
            ttsInstance.stop()
            ttsInstance.shutdown()
        }
    }

    LaunchedEffect(tts, targetLanguage) {
        tts?.language = getLocaleFromLanguage(targetLanguage)
    }

    val languages = listOf(
        "English", "Spanish", "French", "German", "Italian",
        "Chinese", "Japanese", "Korean", "Arabic", "Hindi"
    )

    LaunchedEffect(sourceText, sourceLanguage, targetLanguage) {
        if (sourceText.isNotBlank()) {
            delay(500) // Debounce input
            translatedText = getMockTranslation(sourceText, sourceLanguage, targetLanguage)
        } else {
            translatedText = ""
        }
    }

    // Reset copied states when text changes
    LaunchedEffect(sourceText) {
        if (sourceText.isNotEmpty()) {
            isSourceCopied = false
        }
    }

    LaunchedEffect(translatedText) {
        if (translatedText.isNotEmpty()) {
            isTranslatedCopied = false
        }
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    // Start voice recognition
                    val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                        putExtra(
                            RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                            RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
                        )
                        putExtra(
                            RecognizerIntent.EXTRA_LANGUAGE,
                            getLanguageCodeForSpeech(sourceLanguage)
                        )
                        putExtra(RecognizerIntent.EXTRA_PROMPT, "Speak now...")
                        // Optional: Set language preference
                        putExtra(
                            RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE,
                            getLanguageCodeForSpeech(sourceLanguage)
                        )
                    }

                    try {
                        speechRecognizerLauncher.launch(intent)
                    } catch (_: Exception) {
                        // Voice recognition not supported, fallback to mock
                        sourceText = "Hello, how are you?"
                    }
                },
                shape = CircleShape,
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = Color.White
            ) {
                Icon(Icons.Filled.Mic, contentDescription = "Voice input")
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            LanguageSelectionRow(
                sourceLanguage = sourceLanguage,
                targetLanguage = targetLanguage,
                languages = languages,
                onSourceLanguageSelected = { sourceLanguage = it },
                onTargetLanguageSelected = { targetLanguage = it },
                onSwapLanguages = {
                    val tempLang = sourceLanguage
                    sourceLanguage = targetLanguage
                    targetLanguage = tempLang

                    if (translatedText.isNotEmpty()) {
                        val tempText = sourceText
                        sourceText = translatedText
                        translatedText = tempText
                    }
                }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Source Text Area with paste button at top and copy button at bottom
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(1.dp)
            ) {
                Box(modifier = Modifier.fillMaxSize()) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(12.dp)
                    ) {
                        // Paste button at top right - ONLY SHOWS WHEN TEXT IS EMPTY
                        if (sourceText.isEmpty()) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 8.dp),
                                horizontalArrangement = Arrangement.End
                            ) {
                                IconButton(
                                    onClick = {
                                        // Get text from clipboard and paste into source text
                                        val clipboardText = clipboardManager.getText()?.text
                                        if (clipboardText != null && clipboardText.isNotBlank()) {
                                            sourceText = clipboardText
                                        }
                                    }
                                ) {
                                    Icon(
                                        Icons.Outlined.ContentPaste,
                                        contentDescription = "Paste text to translate",
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }

                        // Text input area
                        Box(modifier = Modifier.weight(1f)) {
                            BasicTextField(
                                value = sourceText,
                                onValueChange = { sourceText = it },
                                modifier = Modifier.fillMaxSize(),
                                textStyle = TextStyle.Default.copy(
                                    fontSize = 20.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                ),
                                cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                                decorationBox = { innerTextField ->
                                    if (sourceText.isEmpty()) {
                                        Text(
                                            text = "Enter text in $sourceLanguage... (or tap microphone)",
                                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                                            fontSize = 20.sp
                                        )
                                    }
                                    innerTextField()
                                }
                            )
                        }
                    }

                    // Copy button appears at bottom right INSIDE the text box, only when there's text
                    if (sourceText.isNotEmpty()) {
                        Box(
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .padding(bottom = 12.dp, end = 12.dp)
                        ) {
                            IconButton(
                                onClick = {
                                    // Copy source text to clipboard
                                    clipboardManager.setText(AnnotatedString(sourceText))
                                    isSourceCopied = true

                                    // Reset after 2 seconds
                                    scope.launch {
                                        delay(2000)
                                        isSourceCopied = false
                                    }
                                },
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.9f))
                                    .shadow(2.dp, CircleShape)
                            ) {
                                Icon(
                                    if (isSourceCopied) Icons.Filled.Check else Icons.Outlined.ContentCopy,
                                    contentDescription = if (isSourceCopied) "Text copied" else "Copy source text",
                                    tint = if (isSourceCopied) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

            // Translated Text Area
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(12.dp)
            ) {
                if (translatedText.isEmpty()) {
                    Text(
                        text = "Translation in $targetLanguage will appear here...",
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                        fontSize = 20.sp
                    )
                } else {
                    Text(
                        text = translatedText,
                        fontSize = 20.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            // Audio icon for translated text at the bottom LEFT
            if (translatedText.isNotEmpty()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // LEFT SIDE: Audio icon only
                    Row {
                        IconButton(
                            onClick = {
                                tts?.language = getLocaleFromLanguage(targetLanguage)
                                tts?.speak(translatedText, TextToSpeech.QUEUE_FLUSH, null, null)
                            },
                            enabled = translatedText.isNotEmpty()
                        ) {
                            Icon(
                                Icons.AutoMirrored.Outlined.VolumeUp,
                                contentDescription = "Speak translation",
                                tint = if (translatedText.isEmpty())
                                    MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                                else MaterialTheme.colorScheme.primary
                            )
                        }
                    }

                    // RIGHT SIDE: Other action buttons
                    Row {
                        IconButton(onClick = { /* TODO: Save implementation */ }) {
                            Icon(
                                Icons.Outlined.BookmarkBorder,
                                contentDescription = "Save translation",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        IconButton(onClick = { isStarred = !isStarred }) {
                            Icon(
                                if (isStarred) Icons.Filled.Star else Icons.Filled.StarBorder,
                                contentDescription = if (isStarred) "Remove from favorites" else "Add to favorites",
                                tint = if (isStarred) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        IconButton(
                            onClick = {
                                // Copy translated text to clipboard
                                clipboardManager.setText(AnnotatedString(translatedText))
                                isTranslatedCopied = true

                                // Reset after 2 seconds
                                scope.launch {
                                    delay(2000)
                                    isTranslatedCopied = false
                                }
                            },
                            enabled = translatedText.isNotEmpty()
                        ) {
                            Icon(
                                if (isTranslatedCopied) Icons.Filled.Check else Icons.Outlined.ContentCopy,
                                contentDescription = if (isTranslatedCopied) "Text copied" else "Copy translation",
                                tint = if (translatedText.isEmpty())
                                    MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                                else if (isTranslatedCopied) MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        IconButton(
                            onClick = {
                                tts?.language = getLocaleFromLanguage(targetLanguage)
                                tts?.speak(translatedText, TextToSpeech.QUEUE_FLUSH, null, null)
                            },
                            enabled = translatedText.isNotEmpty()
                        ) {
                            Icon(
                                Icons.Outlined.Campaign,
                                contentDescription = "Speak translation",
                                tint = if (translatedText.isEmpty())
                                    MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                                else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LanguageSelectionRow(
    sourceLanguage: String,
    targetLanguage: String,
    languages: List<String>,
    onSourceLanguageSelected: (String) -> Unit,
    onTargetLanguageSelected: (String) -> Unit,
    onSwapLanguages: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceAround
    ) {
        LanguageDropdown(sourceLanguage, languages, onSourceLanguageSelected)
        IconButton(
            onClick = onSwapLanguages,
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f))
        ) {
            Icon(
                Icons.Filled.SwapHoriz,
                contentDescription = "Swap languages",
                tint = MaterialTheme.colorScheme.primary
            )
        }
        LanguageDropdown(targetLanguage, languages, onTargetLanguageSelected)
    }
}

@Composable
fun LanguageDropdown(
    selectedLanguage: String,
    languages: List<String>,
    onLanguageSelected: (String) -> Unit
) {
    var expanded by rememberSaveable { mutableStateOf(false) }

    Box {
        TextButton(onClick = { expanded = true }) {
            Text(selectedLanguage, fontSize = 16.sp)
            Icon(Icons.Filled.ArrowDropDown, contentDescription = "Select language")
        }
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            languages.forEach { language ->
                DropdownMenuItem(text = { Text(language) }, onClick = {
                    onLanguageSelected(language)
                    expanded = false
                })
            }
        }
    }
}

private fun getLocaleFromLanguage(language: String): Locale {
    return when (language.lowercase(Locale.ROOT)) {
        "english" -> Locale.ENGLISH
        "spanish" -> Locale("es", "ES")
        "french" -> Locale.FRENCH
        "german" -> Locale.GERMAN
        "italian" -> Locale.ITALIAN
        "chinese" -> Locale.CHINESE
        "japanese" -> Locale.JAPANESE
        "korean" -> Locale.KOREAN
        "arabic" -> Locale("ar")
        "hindi" -> Locale("hi", "IN")
        else -> Locale.getDefault()
    }
}

// Helper function to get language code for speech recognition
private fun getLanguageCodeForSpeech(language: String): String {
    return when (language.lowercase(Locale.ROOT)) {
        "english" -> "en-US"
        "spanish" -> "es-ES"
        "french" -> "fr-FR"
        "german" -> "de-DE"
        "italian" -> "it-IT"
        "chinese" -> "zh-CN"
        "japanese" -> "ja-JP"
        "korean" -> "ko-KR"
        "arabic" -> "ar-SA"
        "hindi" -> "hi-IN"
        else -> Locale.getDefault().toString()
    }
}

// Helper function for mock translations
private fun getMockTranslation(text: String, sourceLang: String, targetLang: String): String {
    return when (text.lowercase(Locale.ROOT)) {
        "hello" -> when (targetLang.lowercase(Locale.ROOT)) {
            "spanish" -> "Hola"
            "french" -> "Bonjour"
            "german" -> "Hallo"
            "chinese" -> "你好"
            "japanese" -> "こんにちは"
            "korean" -> "안녕하세요"
            else -> "[Translated: $text]"
        }
        "thank you" -> when (targetLang.lowercase(Locale.ROOT)) {
            "spanish" -> "Gracias"
            "french" -> "Merci"
            "japanese" -> "ありがとう"
            "korean" -> "감사합니다"
            else -> "[Translated: $text]"
        }
        "good morning" -> when (targetLang.lowercase(Locale.ROOT)) {
            "spanish" -> "Buenos días"
            "french" -> "Bonjour"
            "german" -> "Guten Morgen"
            "japanese" -> "おはようございます"
            else -> "[Translated: $text]"
        }
        "how are you?" -> when (targetLang.lowercase(Locale.ROOT)) {
            "spanish" -> "¿Cómo estás?"
            "french" -> "Comment allez-vous?"
            "german" -> "Wie geht es dir?"
            "japanese" -> "お元気ですか？"
            else -> "[Translated: $text]"
        }
        else -> "[Translated from $sourceLang to $targetLang]: $text"
    }
}
