package com.cisc682.realtimetranslator.pages

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.toMutableStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.PreviewScreenSizes
import androidx.compose.ui.unit.dp
import com.cisc682.realtimetranslator.ui.components.DateFilterState
import com.cisc682.realtimetranslator.ui.components.ReviewCard
import com.cisc682.realtimetranslator.ui.components.ReviewControlBar
import com.cisc682.realtimetranslator.ui.components.SpeechBubble
import com.cisc682.realtimetranslator.ui.components.createTextToSpeech

// ... (other imports)

// Data Models
data class ConversationSummary(
    val id: String,
    val location: String,
    val timestamp: String,
    val sourceLang: String,
    val targetLang: String,
    val sourceLangTag: String, // Language tag for TTS (e.g., "en")
    val targetLangTag: String, // Language tag for TTS (e.g., "fr")
    val previewText: String,
    var isStarred: Boolean,
    val rawTimestamp: Long
)

data class ReviewMessage(
    val id: String,
    val text: String,
    val isMe: Boolean
)

@PreviewScreenSizes
@Composable
fun ReviewPage() {
    // 1. State Management
    var selectedConversationId by rememberSaveable { mutableStateOf<String?>(null) }

    // Mock Data (Assuming timestamp is parsable or we mock it better)
    // For this example, I'll assume "2 hours ago" etc. are just display strings.
    // In a real app, ConversationSummary needs a Long timestamp. 
    // I will mock the data to be compatible with filtering for demonstration if possible, 
    // or just implement string matching for non-date filters for now.
    // Wait, Date logic needs Long timestamps. I'll stick to string matching for now 
    // or add a rawTimestamp field to ConversationSummary for filtering.
    // Let's add rawTimestamp to the mock data for filtering to work properly.

    val conversations = remember {
        listOf(
            ConversationSummary(
                "1",
                "Paris",
                "2 hours ago",
                "🇺🇸 EN",
                "🇫🇷 FR",
                "en",
                "fr",
                "Where is the best croissant?",
                true,
                System.currentTimeMillis() - 7200000
            ),
            ConversationSummary(
                "2",
                "Tokyo",
                "Yesterday",
                "🇺🇸 EN",
                "🇯🇵 JP",
                "en",
                "ja",
                "Can you help me find the station?",
                false,
                System.currentTimeMillis() - 86400000
            ),
            ConversationSummary(
                "3",
                "Madrid",
                "Last Week",
                "🇺🇸 EN",
                "🇪🇸 ES",
                "en",
                "es",
                "I would like to order paella.",
                false,
                System.currentTimeMillis() - 604800000
            )
        ).toMutableStateList()
    }

    val mockMessages = remember { /* ... same as before ... */
        mapOf(
            "1" to listOf(
                ReviewMessage("1", "Hello, where can I find a good croissant?", true),
                ReviewMessage("2", "Bonjour, il y a une excellente boulangerie au coin.", false),
                ReviewMessage("3", "Thank you!", true)
            ),
            "2" to listOf(
                ReviewMessage("1", "Excuse me, where is the station?", true)
            )
        )
    }

    // 2. Hardware Back Button Handler
    BackHandler(enabled = selectedConversationId != null) {
        selectedConversationId = null
    }

    // 3. View Switcher
    if (selectedConversationId == null) {
        ReviewListView(
            conversations = conversations,
            onConversationClick = { id -> selectedConversationId = id },
            onToggleStar = { id, isStarred ->
                val index = conversations.indexOfFirst { it.id == id }
                if (index != -1) {
                    conversations[index] = conversations[index].copy(isStarred = isStarred)
                }
            },
            onDelete = { id ->
                conversations.removeIf { it.id == id }
            },
            onDeleteMultiple = { ids ->
                conversations.removeIf { ids.contains(it.id) }
            }
        )
    } else {
        val messages = mockMessages[selectedConversationId] ?: emptyList()
        val selectedConversation = conversations.find { it.id == selectedConversationId }
        ReviewDetailView(
            conversationId = selectedConversationId!!,
            messages = messages,
            sourceLangTag = selectedConversation?.sourceLangTag ?: "en",
            targetLangTag = selectedConversation?.targetLangTag ?: "en",
            onBackClick = { selectedConversationId = null }
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ReviewListView(
    conversations: List<ConversationSummary>,
    onConversationClick: (String) -> Unit,
    onToggleStar: (String, Boolean) -> Unit,
    onDelete: (String) -> Unit,
    onDeleteMultiple: (List<String>) -> Unit
) {
    // Filter State
    var searchQuery by remember { mutableStateOf("") }
    var dateFilter by remember { mutableStateOf<DateFilterState>(DateFilterState.AllTime) }
    var selectedLanguages by remember { mutableStateOf(emptySet<String>()) }
    var selectedLocations by remember { mutableStateOf(emptySet<String>()) }

    var isSelectionMode by remember { mutableStateOf(false) }
    val selectedIds = remember { mutableStateListOf<String>() }

    // Derived Data for Filters
    val availableLanguages = remember(conversations) {
        conversations.flatMap { listOf(it.sourceLang, it.targetLang) }.distinct().sorted()
    }
    val availableLocations = remember(conversations) {
        conversations.map { it.location }.distinct().sorted()
    }

    // Filtering Logic
    val filteredConversations = conversations.filter { item ->
        val matchesSearch = item.previewText.contains(searchQuery, ignoreCase = true) ||
                item.location.contains(searchQuery, ignoreCase = true)

        val matchesDate = when (dateFilter) {
            is DateFilterState.AllTime -> true
            is DateFilterState.Today -> {
                // Simplified check for "Today" (within last 24h for demo, or match calendar day)
                // Using simplistic check: rawTimestamp > start of today
                // For prototype, let's just use within 24h
                val now = System.currentTimeMillis()
                val diff = now - item.rawTimestamp
                diff < 86400000 // 24 hours
            }

            is DateFilterState.CustomRange -> {
                val range = dateFilter as DateFilterState.CustomRange
                item.rawTimestamp in range.startDate..range.endDate
            }
        }

        val matchesLanguage = if (selectedLanguages.isEmpty()) true else {
            selectedLanguages.contains(item.sourceLang) || selectedLanguages.contains(item.targetLang)
        }

        val matchesLocation = if (selectedLocations.isEmpty()) true else {
            selectedLocations.contains(item.location)
        }

        matchesSearch && matchesDate && matchesLanguage && matchesLocation
    }

    Column(modifier = Modifier.fillMaxSize()) { ->
        if (isSelectionMode) {
            @OptIn(ExperimentalMaterial3Api::class)
            TopAppBar(
                title = { Text("${selectedIds.size} Selected") },
                navigationIcon = {
                    IconButton(onClick = {
                        isSelectionMode = false
                        selectedIds.clear()
                    }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Close")
                    }
                },
                actions = {
                    IconButton(onClick = {
                        onDeleteMultiple(selectedIds.toList())
                        isSelectionMode = false
                        selectedIds.clear()
                    }) {
                        Icon(Icons.Default.Delete, "Delete")
                    }
                }
            )
        } else {
            ReviewControlBar(
                searchQuery = searchQuery,
                onSearchQueryChange = { searchQuery = it },
                dateFilter = dateFilter,
                onDateFilterChange = { dateFilter = it },
                languages = availableLanguages,
                selectedLanguages = selectedLanguages,
                onLanguageFilterChange = { selectedLanguages = it },
                locations = availableLocations,
                selectedLocations = selectedLocations,
                onLocationFilterChange = { selectedLocations = it }
            )
        }
        if (filteredConversations.isEmpty() && conversations.isNotEmpty() && (searchQuery.isNotEmpty() || dateFilter !is DateFilterState.AllTime || selectedLanguages.isNotEmpty() || selectedLocations.isNotEmpty())) {
            // Empty state for "No results found"
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(
                    text = "No results found",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else if (conversations.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(
                    text = "No conversation / translation history",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            LazyColumn {
                // Sticky Header
                if (filteredConversations.isNotEmpty()) {
                    stickyHeader {
                        Box(
                            modifier = Modifier
                                .fillParentMaxWidth()
                                .padding(8.dp)
                                .background(Color.Transparent)
                        ) {
                            Text(
                                if (dateFilter is DateFilterState.AllTime) "Recent" else "Results",
                                modifier = Modifier.padding(start = 16.dp)
                            )
                        }
                    }
                }

                items(filteredConversations, key = { it.id }) { item ->
                    ReviewCard(
                        location = item.location,
                        timestamp = item.timestamp,
                        languagePair = "${item.sourceLang} ${item.targetLang}",
                        previewText = item.previewText,
                        isStarred = item.isStarred,
                        isSelectionMode = isSelectionMode,
                        isSelected = selectedIds.contains(item.id),
                        onToggleStar = { onToggleStar(item.id, it) },
                        onClick = {
                            if (isSelectionMode) {
                                if (selectedIds.contains(item.id)) selectedIds.remove(item.id) else selectedIds.add(
                                    item.id
                                )
                                if (selectedIds.isEmpty()) isSelectionMode = false
                            } else {
                                onConversationClick(item.id)
                            }
                        },
                        onLongClick = {
                            if (!isSelectionMode) {
                                isSelectionMode = true
                                selectedIds.add(item.id)
                            }
                        },
                        onSelectionChange = { selected ->
                            if (selected) selectedIds.add(item.id) else selectedIds.remove(item.id)
                            if (selectedIds.isEmpty()) isSelectionMode = false
                        },
                        onDelete = { onDelete(item.id) },
                        onExport = { /* Export logic */ }
                    )
                }
            }
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReviewDetailView(
    conversationId: String,
    messages: List<ReviewMessage>,
    sourceLangTag: String,
    targetLangTag: String,
    onBackClick: () -> Unit
) {
    // Create TTS instances for both languages
    val speakSource = createTextToSpeech(sourceLangTag)
    val speakTarget = createTextToSpeech(targetLangTag)

    Column(modifier = Modifier.fillMaxSize()) {
        TopAppBar(
            title = { Text("Conversation Details") },
            navigationIcon = {
                IconButton(onClick = onBackClick) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                }
            },
            actions = {
                IconButton(onClick = {}) { Icon(Icons.Default.Share, "Share") }
                IconButton(onClick = {}) { Icon(Icons.Default.Delete, "Delete") }
            }
        )
        LazyColumn(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(messages) { message ->
                // isMe = true means source language (e.g., English), false means target language (e.g., French)
                val speak = if (message.isMe) speakSource else speakTarget
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = if (message.isMe) Arrangement.End else Arrangement.Start
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(0.75f),
                        horizontalArrangement = if (message.isMe) Arrangement.End else Arrangement.Start
                    ) {
                        SpeechBubble(
                            text = arrayOf(message.text),
                            alignment = if (message.isMe) Alignment.End else Alignment.Start,
                            color = if (message.isMe) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.secondaryContainer,
                            textColor = if (message.isMe) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSecondaryContainer,
                            speak = speak
                        )
                    }
                }
            }
        }
    }
}
