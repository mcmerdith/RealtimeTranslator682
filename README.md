# Realtime Translator (CISC682)

Realtime Translator is an Android application designed to provide seamless real-time translation with a comprehensive history review system. Built with modern Android technologies, it leverages on-device Machine Learning to ensure fast and private translations.

## Features

### 1. Face-to-Face Conversation Mode (`Conversation.kt`)
- **Split-Screen UI**: Designed for two people to talk face-to-face with a 180-degree rotated view for the secondary user.
- **Hands-Free**: Tappable microphones for each user to speak in their selected language.
- **Auto-Speak**: Automatically speaks the translated text for fluid conversation.

### 2. Text Translation Mode (`TranslateText.kt`)
- **Classic Translation**: Type or paste text to translate.
- **Voice Input/Output**: Speak to translate or listen to the translation.
- **Utilities**: Copy-to-clipboard and save-to-history functionality.

### 3. Review History (`Review.kt`)
- **History Browsing**: Scrollable list of past translations.
- **Text-to-Speech Playback**: Listen to any message in the conversation.
- **Advanced Filters**:
  - **Search**: Filter by content or location.
  - **Date & Language**: Filter by date ranges and language pairs.
  - **Location**: Location-based filtering.
- **Starring**: Bookmark important translations for quick access.
- **Multi-Select**: Long-press to select and delete multiple conversations.


## Tech Stack

- **Language**: [Kotlin](https://kotlinlang.org/)
- **UI Framework**: [Jetpack Compose](https://developer.android.com/jetpack/compose) (Material 3)
- **ML Engine**: [Google ML Kit](https://developers.google.com/ml-kit/language/translation) (On-device Translation)
- **Permissions**: [Accompanist Permissions](https://google.github.io/accompanist/permissions/)
- **Asynchronous**: Kotlin Coroutines

## Prerequisites

- **Android Studio**: Koala (2024.1.1) or newer recommended.
- **JDK**: Version 11 or higher.
- **Android SDK**: Min SDK 26 (Android 8.0), Target SDK 36.

## How to Run

1. **Clone the repository**:
   ```bash
   git clone <repository_url>
   cd RealtimeTranslator682
   ```

2. **Open in Android Studio**:
   - Launch Android Studio.
   - Select "Open" and navigate to the `RealtimeTranslator682` directory.

3. **Sync Gradle**:
   - Allow Android Studio to sync the project and download dependencies.

4. **Run the App**:
   - Connect an Android device (Android 8.0+) via USB or create/launch an Emulator.
   - Click the **Run** button (green play icon) in the toolbar (or press `Shift + F10`).
   - Grant necessary permissions (Microphone) when prompted.

## Online References & Resources

- [Jetpack Compose Documentation](https://developer.android.com/jetpack/compose)
- [Material Design 3 Components](https://m3.material.io/components)
- [Google ML Kit: On-device Translation](https://developers.google.com/ml-kit/language/translation)
- [Accompanist Libraries](https://google.github.io/accompanist/)
- [Android Developers Guide](https://developer.android.com/)
