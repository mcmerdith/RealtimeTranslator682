package com.cisc682.realtimetranslator.pages

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.toMutableStateList
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.PreviewScreenSizes
import androidx.compose.ui.unit.dp
import com.cisc682.realtimetranslator.ui.components.ReviewCard
import com.cisc682.realtimetranslator.ui.components.ReviewTopBar
import com.cisc682.realtimetranslator.ui.components.SpeechBubble

// Data Models
data class ConversationSummary(
    val id: String,
    val location: String,
    val timestamp: String,
    val sourceLang: String,
    val targetLang: String,
    val previewText: String,
    var isStarred: Boolean
)

data class Message(
    val id: String,
    val text: String,
    val isMe: Boolean
)

@PreviewScreenSizes
@Composable
fun ReviewPage() {
    // 1. State Management
    var selectedConversationId by rememberSaveable { mutableStateOf<String?>(null) }
    
    // Mock Data
    val conversations = remember {
        listOf(
            ConversationSummary("1", "Paris", "2 hours ago", "🇺🇸 EN", "🇫🇷 FR", "Where is the best croissant?", true),
            ConversationSummary("2", "Tokyo", "Yesterday", "🇺🇸 EN", "🇯🇵 JP", "Can you help me find the station?", false),
            ConversationSummary("3", "Madrid", "Last Week", "🇺🇸 EN", "🇪🇸 ES", "I would like to order paella.", false)
        ).toMutableStateList()
    }

    val mockMessages = remember {
        mapOf(
            "1" to listOf(
                Message("1", "Hello, where can I find a good croissant?", true),
                Message("2", "Bonjour, il y a une excellente boulangerie au coin.", false),
                Message("3", "Thank you!", true)
            ),
            "2" to listOf(
                Message("1", "Excuse me, where is the station?", true)
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
        ReviewDetailView(
            conversationId = selectedConversationId!!,
            messages = messages,
            onBackClick = { selectedConversationId = null }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReviewListView(
    conversations: List<ConversationSummary>,
    onConversationClick: (String) -> Unit,
    onToggleStar: (String, Boolean) -> Unit,
    onDelete: (String) -> Unit,
    onDeleteMultiple: (List<String>) -> Unit
) {
    // Filter State
    var filterState by remember { mutableStateOf(ReviewFilterState()) }
    var activeSheet by remember { mutableStateOf(FilterType.NONE) }
    
    // Selection State
    var isSelectionMode by remember { mutableStateOf(false) }
    val selectedIds = remember { mutableStateListOf<String>() }
    
    // Filtering Logic
    val filteredConversations = remember(conversations, filterState) {
        conversations.filter { item ->
            // Date Filter (Placeholder Logic)
            val dateMatch = if (filterState.dateRange == "Today") item.timestamp.contains("hours") || item.timestamp == "Just now"
                            else if (filterState.dateRange == "Last 7 Days") true // Simplified for mock
                            else true // "All Time" or default

            // Language Filter
            val langMatch = if (filterState.selectedLanguages.isEmpty()) true 
                            else filterState.selectedLanguages.any { it in item.sourceLang || it in item.targetLang }

            // Location Filter
            val locMatch = if (filterState.selectedLocations.isEmpty()) true
                           else filterState.selectedLocations.contains(item.location)
                           
            dateMatch && langMatch && locMatch
        }
    }

    // Modal Bottom Sheet
    val sheetState = rememberModalBottomSheetState()
    if (activeSheet != FilterType.NONE) {
        ModalBottomSheet(
            onDismissRequest = { activeSheet = FilterType.NONE },
            sheetState = sheetState
        ) {
            when (activeSheet) {
                FilterType.DATE -> DateFilterSheet(
                    onPresetSelected = { preset -> 
                        filterState = filterState.copy(dateRange = preset)
                        activeSheet = FilterType.NONE
                    },
                    onCustomRangeClick = { /* TODO: Open DatePickerDialog */ activeSheet = FilterType.NONE }
                )
                FilterType.LANGUAGE -> MultiSelectFilterSheet(
                    title = "Select Languages",
                    options = conversations.flatMap { listOf(it.sourceLang, it.targetLang) }.groupingBy { it }.eachCount(),
                    selectedOptions = filterState.selectedLanguages,
                    onApply = { selected ->
                        filterState = filterState.copy(selectedLanguages = selected)
                        activeSheet = FilterType.NONE
                    }
                )
                FilterType.LOCATION -> MultiSelectFilterSheet(
                    title = "Select Locations",
                    options = conversations.map { it.location }.groupingBy { it }.eachCount(),
                    selectedOptions = filterState.selectedLocations,
                    onApply = { selected ->
                        filterState = filterState.copy(selectedLocations = selected)
                        activeSheet = FilterType.NONE
                    }
                )
                else -> {}
            }
        }
    }

    Scaffold(
        topBar = {
            if (isSelectionMode) {
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
                ReviewTopBar(
                    onSearchQueryChange = {},
                    filterState = filterState,
                    onDateClick = { activeSheet = FilterType.DATE },
                    onLanguageClick = { activeSheet = FilterType.LANGUAGE },
                    onLocationClick = { activeSheet = FilterType.LOCATION }
                )
            }
        }
    ) { innerPadding ->
        if (filteredConversations.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(
                    text = "No conversation / translation history",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            LazyColumn(contentPadding = innerPadding) {
                // ... (Sticky Header content remains same, omitted for brevity but logic preserves it if not replaced)
                 stickyHeader {
                    Box(modifier = Modifier.fillParentMaxWidth().padding(8.dp).background(Color.Transparent)) {
                         Text("Recent", modifier = Modifier.padding(start = 16.dp))
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
                                if (selectedIds.contains(item.id)) selectedIds.remove(item.id) else selectedIds.add(item.id)
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
    messages: List<Message>,
    onBackClick: () -> Unit
) {
    Scaffold(
        topBar = {
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
        }
    ) { innerPadding ->
        LazyColumn(
            contentPadding = innerPadding,
            modifier = Modifier.padding(16.dp),
            verticalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(8.dp)
        ) {
            items(messages) { message ->
                // Adapting to existing SpeechBubble API
                SpeechBubble(
                    text = arrayOf(message.text),
                    alignment = if (message.isMe) Alignment.End else Alignment.Start,
                    color = if (message.isMe) Color(0xFFE3F2FD) else Color(0xFFF5F5F5), // Light Blue for me, Gray for other
                    textColor = Color.Black
                )
            }
        }
    }
}
