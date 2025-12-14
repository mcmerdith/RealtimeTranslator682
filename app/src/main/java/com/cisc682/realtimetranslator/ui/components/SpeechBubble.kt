package com.cisc682.realtimetranslator.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Campaign
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun SpeakButton(text: String, flipped: Boolean, speak: (String) -> Unit, color: Color) {
    val contentColor = if (text.isEmpty()) color.copy(alpha = 0.5f)
    else color
    IconButton(
        onClick = {
            speak(text)
        },
        enabled = text.isNotEmpty(),
        modifier = Modifier.requiredSize(24.dp),
    ) {
        Icon(
            Icons.Outlined.Campaign,
            contentDescription = "Speak text",
            tint = contentColor,
            modifier = Modifier
                .size(24.dp)
                .graphicsLayer(scaleX = if (flipped) -1f else 1f)
        )
    }
}

/**
 * Create a speech bubble
 */
@Composable
fun SpeechBubble(
    text: Array<String>,
    alignment: Alignment.Horizontal,
    color: Color,
    textColor: Color,
    modifier: Modifier = Modifier,
    speak: ((String) -> Unit)? = null
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp, alignment),
        modifier = modifier
            .width(IntrinsicSize.Min)
            .height(IntrinsicSize.Min)
            .background(color, RoundedCornerShape(10.dp))
            .padding(horizontal = 10.dp)
    ) {
        if (alignment == Alignment.End && speak != null) {
            SpeakButton(
                text = text.getOrElse(0) { "" },
                flipped = true,
                speak = speak,
                color = textColor
            )
        }

        Column(
            horizontalAlignment = alignment,
            modifier = Modifier
                .width(IntrinsicSize.Min)
                .weight(1f)
        ) {
            text.forEachIndexed { index, t ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(5.dp, alignment),
                    modifier = Modifier
                        .padding(
                            top = if (index == 0) 10.dp else 2.5.dp,
                            bottom = if (index == text.size - 1) 10.dp else 2.5.dp
                        )
                ) {

                    Text(
                        text = t,
                        color = textColor,
                        fontSize = if (index == 0) 16.sp else 12.sp,
                        modifier = Modifier
                            .width(IntrinsicSize.Max)
                    )
                }
            }
        }

        if (alignment == Alignment.Start && speak != null) {
            SpeakButton(
                text = text.getOrElse(0) { "" },
                flipped = false,
                speak = speak,
                color = textColor
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
        textColor = Color(205, 205, 205),
        speak = { }
    )
}
