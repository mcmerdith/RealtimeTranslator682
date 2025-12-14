package com.cisc682.realtimetranslator.ui.components

import android.app.DatePickerDialog
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

// --- Data Models ---

sealed class DateFilterState {
    object AllTime : DateFilterState()
    object Today : DateFilterState()
    data class CustomRange(val startDate: Long, val endDate: Long) : DateFilterState()
}

// --- Main Control Bar ---

@Composable
fun ReviewControlBar(
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    dateFilter: DateFilterState,
    onDateFilterChange: (DateFilterState) -> Unit,
    languages: List<String>,
    selectedLanguages: Set<String>,
    onLanguageFilterChange: (Set<String>) -> Unit,
    locations: List<String>,
    selectedLocations: Set<String>,
    onLocationFilterChange: (Set<String>) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. Search Bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = onSearchQueryChange,
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("Search conversations...") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search") },
            shape = RoundedCornerShape(24.dp),
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                focusedBorderColor = MaterialTheme.colorScheme.primary
            )
        )

        // 2. Filter Bar
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            // Date Chip
            FilterChipComponent(
                label = when (dateFilter) {
                    is DateFilterState.AllTime -> "Date"
                    is DateFilterState.Today -> "Today"
                    is DateFilterState.CustomRange -> {
                        val simpleFormat = SimpleDateFormat("MMM d", Locale.getDefault())
                        "${simpleFormat.format(Date(dateFilter.startDate))} - ${
                            simpleFormat.format(
                                Date(dateFilter.endDate)
                            )
                        }"
                    }
                },
                isActive = dateFilter !is DateFilterState.AllTime,
                icon = Icons.Default.DateRange,
                onClick = { /* Open Date Dialog */ }
            ) { dismiss ->
                DateFilterDialog(
                    currentFilter = dateFilter,
                    onApply = onDateFilterChange,
                    onDismiss = dismiss
                )
            }

            // Language Chip
            FilterChipComponent(
                label = if (selectedLanguages.isEmpty()) "Language" else "Languages (${selectedLanguages.size})",
                isActive = selectedLanguages.isNotEmpty(),
                icon = Icons.Default.Translate,
                onClick = { /* Open Language Dialog */ }
            ) { dismiss ->
                MultiSelectDialog(
                    title = "Select Languages",
                    options = languages,
                    selectedOptions = selectedLanguages,
                    onApply = onLanguageFilterChange,
                    onDismiss = dismiss
                )
            }

            // Location Chip
            FilterChipComponent(
                label = if (selectedLocations.isEmpty()) "Location" else "Locations (${selectedLocations.size})",
                isActive = selectedLocations.isNotEmpty(),
                icon = Icons.Default.Place,
                onClick = { /* Open Location Dialog */ }
            ) { dismiss ->
                MultiSelectDialog(
                    title = "Select Locations",
                    options = locations,
                    selectedOptions = selectedLocations,
                    onApply = onLocationFilterChange,
                    onDismiss = dismiss
                )
            }
        }
    }
}

// --- Helper Components ---

@Composable
fun FilterChipComponent(
    label: String,
    isActive: Boolean,
    icon: ImageVector,
    onClick: () -> Unit,
    dialogContent: @Composable (dismiss: () -> Unit) -> Unit
) {
    var showDialog by remember { mutableStateOf(false) }

    val backgroundColor =
        if (isActive) MaterialTheme.colorScheme.secondaryContainer else Color.Transparent
    val contentColor =
        if (isActive) MaterialTheme.colorScheme.onSecondaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
    val borderColor = if (isActive) Color.Transparent else MaterialTheme.colorScheme.outline

    Box(
        modifier = Modifier
            .clip(CircleShape)
            .background(backgroundColor)
            .border(1.dp, borderColor, CircleShape)
            .clickable { showDialog = true }
            .padding(horizontal = 12.dp, vertical = 8.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = contentColor,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge,
                color = contentColor,
                fontWeight = if (isActive) FontWeight.Bold else FontWeight.Normal
            )
        }
    }

    if (showDialog) {
        dialogContent { showDialog = false }
    }
}

// --- Dialogs ---

@Composable
fun MultiSelectDialog(
    title: String,
    options: List<String>,
    selectedOptions: Set<String>,
    onApply: (Set<String>) -> Unit,
    onDismiss: () -> Unit
) {
    var currentSelection by remember { mutableStateOf(selectedOptions) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                LazyColumn(
                    modifier = Modifier.heightIn(max = 300.dp) // Limit height
                ) {
                    items(options) { option ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .clickable {
                                    currentSelection = if (currentSelection.contains(option)) {
                                        currentSelection - option
                                    } else {
                                        currentSelection + option
                                    }
                                },
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = currentSelection.contains(option),
                                onCheckedChange = { isChecked ->
                                    currentSelection = if (isChecked) {
                                        currentSelection + option
                                    } else {
                                        currentSelection - option
                                    }
                                }
                            )
                            Text(
                                text = option,
                                style = MaterialTheme.typography.bodyLarge,
                                modifier = Modifier.padding(start = 16.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(onClick = {
                        onApply(currentSelection)
                        onDismiss()
                    }) {
                        Text("Apply")
                    }
                }
            }
        }
    }
}

@Composable
fun DateFilterDialog(
    currentFilter: DateFilterState,
    onApply: (DateFilterState) -> Unit,
    onDismiss: () -> Unit
) {
    var selectedState by remember { mutableStateOf(currentFilter) }
    val context = LocalContext.current
    val calendar = Calendar.getInstance()

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Filter by Date",
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                // Radio Buttons
                DateFilterOption(
                    label = "All Time",
                    selected = selectedState is DateFilterState.AllTime,
                    onClick = { selectedState = DateFilterState.AllTime }
                )
                DateFilterOption(
                    label = "Today",
                    selected = selectedState is DateFilterState.Today,
                    onClick = { selectedState = DateFilterState.Today }
                )
                DateFilterOption(
                    label = "Custom Range",
                    selected = selectedState is DateFilterState.CustomRange,
                    onClick = {
                        val today = System.currentTimeMillis()
                        selectedState = DateFilterState.CustomRange(today, today)
                    }
                )

                // Custom Range Inputs
                if (selectedState is DateFilterState.CustomRange) {
                    val rangeState = selectedState as DateFilterState.CustomRange
                    val dateFormatter = SimpleDateFormat("MMM d, yyyy", Locale.getDefault())

                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Button(
                            onClick = {
                                DatePickerDialog(
                                    context,
                                    { _, year, month, day ->
                                        calendar.set(year, month, day)
                                        val newStart = calendar.timeInMillis
                                        selectedState = rangeState.copy(startDate = newStart)
                                    },
                                    calendar.get(Calendar.YEAR),
                                    calendar.get(Calendar.MONTH),
                                    calendar.get(Calendar.DAY_OF_MONTH)
                                ).show()
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.secondaryContainer,
                                contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .padding(end = 4.dp)
                        ) {
                            Text(dateFormatter.format(Date(rangeState.startDate)))
                        }

                        Button(
                            onClick = {
                                DatePickerDialog(
                                    context,
                                    { _, year, month, day ->
                                        calendar.set(year, month, day)
                                        val newEnd = calendar.timeInMillis
                                        selectedState = rangeState.copy(endDate = newEnd)
                                    },
                                    calendar.get(Calendar.YEAR),
                                    calendar.get(Calendar.MONTH),
                                    calendar.get(Calendar.DAY_OF_MONTH)
                                ).show()
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.secondaryContainer,
                                contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .padding(start = 4.dp)
                        ) {
                            Text(dateFormatter.format(Date(rangeState.endDate)))
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(onClick = {
                        onApply(selectedState)
                        onDismiss()
                    }) {
                        Text("Apply")
                    }
                }
            }
        }
    }
}

@Composable
fun DateFilterOption(
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(
            selected = selected,
            onClick = onClick
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(start = 8.dp)
        )
    }
}
