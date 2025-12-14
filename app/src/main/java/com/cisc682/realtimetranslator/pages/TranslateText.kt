package com.cisc682.realtimetranslator.pages

import android.Manifest.permission_group.PHONE
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material.icons.outlined.Campaign
import androidx.compose.material.icons.outlined.ContentCopy
import androidx.compose.material.icons.outlined.ContentPaste
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cisc682.realtimetranslator.lib.TranslationLib
import com.cisc682.realtimetranslator.ui.components.LanguageDropdown
import com.cisc682.realtimetranslator.ui.components.createSpeechRecognizer
import com.cisc682.realtimetranslator.ui.components.createTextToSpeech
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Preview(device = PHONE)
@Composable
fun TranslateTextPage() {
    val context = LocalContext.current

    var sourceText by rememberSaveable { mutableStateOf("") }
    var translatedText by rememberSaveable { mutableStateOf("") }

    // Default source language to English, target to Spanish
    var sourceLangTag by rememberSaveable { mutableStateOf("en") }
    var targetLangTag by rememberSaveable { mutableStateOf("es") }

    // Speech-to-Text (Voice Recognition)
    val showSpeechRecognition = createSpeechRecognizer(
        languageTag = sourceLangTag,
        flipped = false,
        onSpeechResult = { sourceText = it })

    // Translate when text changes
    LaunchedEffect(sourceText, sourceLangTag, targetLangTag) {
        if (sourceText.isNotBlank()) {
            delay(500) // Debounce input
            translatedText = TranslationLib.translate(sourceLangTag, targetLangTag, sourceText)
        } else {
            translatedText = ""
        }
    }

    // Main layout
    Column(
        verticalArrangement = Arrangement.spacedBy(10.dp, Alignment.CenterVertically),
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        LanguageSelectionRow(
            sourceLanguage = sourceLangTag,
            targetLanguage = targetLangTag,
            onSourceLanguageSelected = { sourceLangTag = it },
            onTargetLanguageSelected = { targetLangTag = it },
            onSwapLanguages = {
                val tempLang = sourceLangTag
                sourceLangTag = targetLangTag
                targetLangTag = tempLang

                if (translatedText.isNotEmpty()) {
                    val tempText = sourceText
                    sourceText = translatedText
                    translatedText = tempText
                }
            })

        TranslationTextBox(
            currentText = sourceText,
            languageTag = sourceLangTag,
            onValueChange = { sourceText = it },
            modifier = Modifier.weight(1f)
        )

        HorizontalDivider()

        TranslationTextBox(
            currentText = translatedText,
            languageTag = targetLangTag,
            modifier = Modifier.weight(1f)
        )

        // Control row
        Box(modifier = Modifier.fillMaxWidth()) {
            IconButton(
                onClick = {
                    Toast.makeText(context, "Saved to library!", Toast.LENGTH_LONG).show()
                },
                enabled = translatedText.isNotEmpty(),
                modifier = Modifier.align(Alignment.BottomStart)
            ) {
                Icon(
                    Icons.Outlined.BookmarkBorder,
                    contentDescription = "Save",
                    tint = if (translatedText.isEmpty()) MaterialTheme.colorScheme.onSurfaceVariant.copy(
                        alpha = 0.5f
                    )
                    else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            FilledIconButton(
                onClick = {
                    showSpeechRecognition()
                },
                shape = CircleShape,
                modifier = Modifier
                    .size(48.dp)
                    .align(Alignment.BottomCenter)
            ) {
                Icon(
                    Icons.Filled.Mic,
                    contentDescription = "Voice input",
                    modifier = Modifier.fillMaxSize(0.6f)
                )
            }
        }
    }
}

/**
 * Language selection row with swap button.
 *
 * @param sourceLanguage Source language
 * @param targetLanguage Target language
 * @param onSourceLanguageSelected Callback for when source language is selected
 * @param onTargetLanguageSelected Callback for when target language is selected
 * @param onSwapLanguages Callback for when swap button is clicked
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LanguageSelectionRow(
    sourceLanguage: String,
    targetLanguage: String,
    onSourceLanguageSelected: (String) -> Unit,
    onTargetLanguageSelected: (String) -> Unit,
    onSwapLanguages: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceAround
    ) {
        LanguageDropdown(sourceLanguage, onSourceLanguageSelected)
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
        LanguageDropdown(targetLanguage, onTargetLanguageSelected)
    }
}

/**
 * The main UI element for translating text
 *
 * Operates in 2 different modes: view and edit
 *
 * If no callback is provided for `onValueChange`, a simple text component will be displayed
 * with some standard "copy" and "speak buttons"
 *
 * If a callback is provided for onValueChange, a text input will be displayed with an
 * additional "paste" button
 *
 * @param currentText The current value of the text
 * @param languageTag The language tag of the text
 * @param modifier Modifier for the text box container
 * @param onValueChange Callback for when the text changes
 */
@Composable
fun TranslationTextBox(
    currentText: String,
    languageTag: String,
    modifier: Modifier = Modifier,
    onValueChange: ((String) -> Unit)? = null
) {
    // Context vars
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current

    // Get the name from the tag
    val languageName = TranslationLib.getDisplayName(languageTag)

    // Clipboard
    var changeCopyIcon by rememberSaveable { mutableStateOf(false) }
    val clipboardManager = LocalClipboardManager.current

    // Reset copied states when text changes
    LaunchedEffect(currentText) {
        changeCopyIcon = false
    }

    // Text-to-Speech
    val speak = createTextToSpeech(languageTag)

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(MaterialTheme.colorScheme.surface)
            .padding(10.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp)
        ) {
            // Text input area
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                if (onValueChange == null) {
                    // Show the current text (or a placeholder) if it is not editable
                    if (currentText.isEmpty()) {
                        Text(
                            text = "Translation in $languageName will appear here...",
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                            fontSize = 20.sp
                        )
                    } else {
                        Text(
                            text = currentText,
                            fontSize = 20.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                } else {
                    // Display an input field if it is editable
                    BasicTextField(
                        value = currentText,
                        onValueChange = { v -> onValueChange(v) },
                        modifier = Modifier.fillMaxSize(),
                        textStyle = TextStyle.Default.copy(
                            fontSize = 20.sp, color = MaterialTheme.colorScheme.onSurface
                        ),
                        cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                        decorationBox = { innerTextField ->
                            if (currentText.isEmpty()) {
                                // Overlay a placeholder if there is no text
                                Text(
                                    text = "Enter text in $languageName (or tap microphone)",
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                                    fontSize = 20.sp
                                )
                            }
                            innerTextField()
                        })
                }
            }

            // Per box control row
            Row {
                IconButton(
                    onClick = {
                        // Copy translated text to clipboard
                        clipboardManager.setText(AnnotatedString(currentText))

                        // Tell the user
                        Toast.makeText(context, "Copied to clipboard!", Toast.LENGTH_LONG).show()

                        // Change the icon for 2 seconds
                        changeCopyIcon = true
                        coroutineScope.launch {
                            delay(2000)
                            changeCopyIcon = false
                        }
                    }, enabled = currentText.isNotEmpty()
                ) {
                    Icon(
                        if (changeCopyIcon) Icons.Filled.Check else Icons.Outlined.ContentCopy,
                        contentDescription = if (changeCopyIcon) "Text copied" else "Copy text",
                        tint = if (currentText.isEmpty()) MaterialTheme.colorScheme.onSurfaceVariant.copy(
                            alpha = 0.5f
                        )
                        else if (changeCopyIcon) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                IconButton(
                    onClick = {
                        speak(currentText)
                    }, enabled = currentText.isNotEmpty()
                ) {
                    Icon(
                        Icons.Outlined.Campaign,
                        contentDescription = "Speak text",
                        tint = if (currentText.isEmpty()) MaterialTheme.colorScheme.onSurfaceVariant.copy(
                            alpha = 0.5f
                        )
                        else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                if (onValueChange != null) {
                    // Show an additional "paste" button if the field is editable, right aligned
                    Spacer(modifier = Modifier.weight(1f))
                    IconButton(
                        onClick = {
                            // Get text from clipboard and paste into source text
                            val clipboardText = clipboardManager.getText()?.text
                            if (clipboardText != null && clipboardText.isNotBlank()) {
                                onValueChange(clipboardText)
                            }
                        },
                        enabled = currentText.isNotEmpty()
                    ) {
                        Icon(
                            Icons.Outlined.ContentPaste,
                            contentDescription = "Paste text to translate",
                            tint = if (currentText.isEmpty()) MaterialTheme.colorScheme.onSurfaceVariant.copy(
                                alpha = 0.5f
                            )
                            else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

