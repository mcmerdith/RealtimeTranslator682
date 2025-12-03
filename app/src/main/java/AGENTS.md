# App Design

The app is written in Jetpack Compose targeting Android 8.

The main layout is in `MainActivity.kt`. Do not modify this file.

The layout of the app is a 3 tab design, where each page of the layout is a separate file
within the `pages` package.

All UI components should be in an individual file within the `ui.components` package.

# UI Interactions

## Text Translation

Two text boxes in a column layout, where each has a selector for which language to use.

There are buttons for copy and pasting, speaking the translated audio, or starring
translated phrases.

There is a microphone icon at the bottom of the screen for voice input.

## Conversational Translation

A "mirrored" design where each half is a copy of the user interface.

The interface is a box which contains speech bubbles. The bubbles for the user facing the
interface are on the right side, and the other participant are on the right.

The interface has a selector for which language to use, a button to speak the translated
text and a button to swap the languages for each participant.

There is a microphone icon at the bottom of the main interface box.

## Review Conversations / Translations

The main interface is a list of previous conversations that have been translated.

The list has filters at the top to sort by date, language, and tags.

Each list item has a button to star it, a context menu to perform additional actions like
adding tags, and opens a detailed view when clicked.

### Detailed View

The detailed view will have the same layout as the conversational view with speech bubbles
for the sentences spoken during the conversation.

Tapping a speech bubble should open it in an even more detailed view where each word can be
tapped individually to show more information about it in that language.

# Components

## Speech Bubbles

The speech bubble has both the original and translated text.

On large devices they are in a row layout, and on smaller devices they are in a column layout