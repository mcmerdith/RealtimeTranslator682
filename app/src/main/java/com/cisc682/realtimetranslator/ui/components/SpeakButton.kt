package com.cisc682.realtimetranslator.ui.components

import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Campaign
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

/**
 * A simple component to speak text
 *
 * @param text The text to be spoken when clicked
 * @param flipped Whether the icon should be flipped horizontally
 * @param speak The function to call when the button is clicked
 * @param color The color of the icon
 */
@Composable
fun SpeakButton(text: String, flipped: Boolean, speak: (String) -> Unit, color: Color) {
    val contentColor = if (text.isEmpty()) color.copy(alpha = 0.5f)
    else color
    IconButton(
        onClick = {
            speak(text)
        },
        enabled = text.isNotEmpty(),
        modifier = Modifier.Companion.requiredSize(24.dp),
    ) {
        Icon(
            Icons.Outlined.Campaign,
            contentDescription = "Speak text",
            tint = contentColor,
            modifier = Modifier.Companion
                .size(24.dp)
                .graphicsLayer(scaleX = if (flipped) -1f else 1f)
        )
    }
}

@Preview
@Composable
private fun SpeakButtonPreview() {
    SpeakButton("Hello World", false, {}, Color.Black)
}