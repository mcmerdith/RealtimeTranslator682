# Review Module

## Overview
The **Review Module** allows users to browse, search, filter, and manage their past translation history. It provides a list of conversation summaries and a detailed view for specific sessions.

## Features
*   **Conversation List**: Scrollable list of past conversations with sticky headers for time periods (e.g., "Recent").
*   **Search**: Real-time filtering by content preview or location.
*   **Advanced Filters**:
    *   **Date**: Filter by "Today" or a custom date range using a native Date Picker.
    *   **Language**: Multi-select support for source and target languages.
    *   **Location**: Multi-select support for conversation locations.
*   **Selection Mode**: Long-press to select multiple conversations for batch actions (e.g., Delete).
*   **Favorites**: Star conversations to mark them as important.

## Architecture & Files
The module is self-contained within the `com.cisc682.realtimetranslator` package:

*   **`pages/Review.kt`**: Main entry point. Manages state (navigation, filters, selection) and orchestrates the ListView and DetailView.
*   **`ui/components/ReviewCard.kt`**: granular UI component displaying a single conversation summary. Supports efficient selection state toggling.
*   **`ui/components/ReviewFilters.kt`**: Contains the **ReviewControlBar** (Search + Chips) and the Dialog implementations (`MultiSelectDialog`, `DateFilterDialog`).

## Dependencies & Design Note
> **IMPORTANT**: The implementation of the **Detailed Review Card** (the detailed conversation view) relies heavily on the design decisions of other modules, specifically the main Translation/Conversation interface.
>
> The current `ReviewDetailView` adapts existing components (like `SpeechBubble`) but final visual consistency and shared data models must be aligned with the core Translation module.
