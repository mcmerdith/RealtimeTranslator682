package com.cisc682.realtimetranslator.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Create a speech bubble
 */
@Composable
fun SpeechBubble(
    text: Array<String>,
    alignment: Alignment.Horizontal,
    color: Color,
    textColor: Color,
    modifier: Modifier = Modifier
) {
    Column(
        horizontalAlignment = alignment,
        modifier = modifier
            .defaultMinSize(150.dp)
            .width(IntrinsicSize.Min)
            .background(color, RoundedCornerShape(10.dp))
    ) {
        text.forEachIndexed { index, t ->
            if (index > 0) {
                HorizontalDivider(
                    color = textColor.copy(alpha = 0.25f)
                )
            }
            Text(
                text = t,
                color = textColor,
                fontSize = if (index == 0) 16.sp else 14.sp,
                modifier = Modifier
                    .width(IntrinsicSize.Max)
                    .padding(
                        horizontal = 10.dp,
                        vertical = if (index == 0 || index == text.size - 1) 10.dp else 5.dp
                    )
            )
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
