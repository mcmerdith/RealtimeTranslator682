package com.cisc682.realtimetranslator.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Create a speech bubble
 */
@Composable
fun SpeechBubble(
    text: Array<String>,
    alignment: Alignment.Horizontal,
    color: Color,
    textColor: Color,
    minWidth: Dp = 150.dp
) {
    Column(
        horizontalAlignment = alignment,
        verticalArrangement = Arrangement.spacedBy(20.dp, Alignment.Top),
        modifier = Modifier
            .background(color, RoundedCornerShape(10.dp))
            .padding(10.dp)
            .defaultMinSize(minWidth),
    ) {
        text.map { t ->
            Text(text = t, color = textColor)
        }
    }
}

@Preview
@Composable
fun SpeechBubblePreview() {
    SpeechBubble(
        text = arrayOf("Original Text", "Translated Text"),
        alignment = Alignment.Start,
        color = Color(0, 110, 220),
        textColor = Color(205, 205, 205)
    )
}
