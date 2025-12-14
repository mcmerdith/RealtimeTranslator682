package com.cisc682.realtimetranslator.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * A simple component to display a speech bubble with a line of primary text
 * and optional additional lines of smaller text
 *
 * @param text The lines of text. text[0] is the primary line, which will be used for text to speech (if applicable)
 * @param alignment Where the contents of the speech bubble should be aligned
 * @param color The background color of the speech bubble
 * @param modifier Modifiers to apply to the speech bubble container
 * @param speak An optional text to speech function, which will cause a "speak" button to be added
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
    // An outer row container (required for the button to work)
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp, alignment),
        modifier = modifier
            .width(IntrinsicSize.Min) // Be as small as possible
            .height(IntrinsicSize.Min)  // Be as small as possible
            .background(color, RoundedCornerShape(10.dp))
            .padding(horizontal = 10.dp)
    ) {
        // Add a speak button for right aligned bubbles
        if (alignment == Alignment.End && speak != null) {
            SpeakButton(
                text = text.getOrElse(0) { "" },
                flipped = true,
                speak = speak,
                color = textColor
            )
        }

        // The main text content
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
                        fontSize = if (index == 0) 16.sp else 12.sp, // Primary text is large, secondary text is small
                        modifier = Modifier
                            .width(IntrinsicSize.Max) // Be as large as necessary
                    )
                }
            }
        }

        // Add a speak button for left aligned bubbles
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
private fun SpeechBubblePreview() {
    Column {
        listOf<((String) -> Unit)?>({}, null).forEach { speak ->
            listOf(
                arrayOf("Original text", "Translated Text"),
                arrayOf("Primary text")
            ).forEach { text ->
                listOf(Alignment.Start, Alignment.End).forEach { alignment ->
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(0.dp, alignment),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        SpeechBubble(
                            text = text,
                            alignment = alignment,
                            color = Color(0, 110, 220),
                            textColor = Color(205, 205, 205),
                            speak = speak
                        )
                    }
                }
            }
        }
    }
}
