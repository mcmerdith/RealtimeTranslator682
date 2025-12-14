package com.cisc682.realtimetranslator.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.sp
import com.cisc682.realtimetranslator.lib.TranslationLib

/**
 * Display a dropdown menu of all available languages
 * @param selectedLanguageTag The currently selected language
 * @param onLanguageSelected Callback when a new language is selected
 */
@Composable
fun LanguageDropdown(
    selectedLanguageTag: String,
    onLanguageSelected: (String) -> Unit
) {
    val languageTags = TranslationLib.Companion.getAllLanguages()
    var expanded by rememberSaveable { mutableStateOf(false) }

    Box {
        TextButton(onClick = { expanded = true }) {
            Text(TranslationLib.getDisplayName(selectedLanguageTag), fontSize = 16.sp)
            Icon(Icons.Filled.ArrowDropDown, contentDescription = "Select language")
        }
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            languageTags.forEach { languageTag ->
                DropdownMenuItem(
                    text = { Text(TranslationLib.getDisplayName(languageTag)) },
                    onClick = {
                        onLanguageSelected(languageTag)
                        expanded = false
                    })
            }
        }
    }
}

@Preview
@Composable
private fun LanguageDropdownPreview() {
    LanguageDropdown("en") { }
}