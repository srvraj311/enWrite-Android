package com.srvraj311.ui.editor

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.NoteAlt
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.clickable
import com.srvraj311.data.Note
import com.srvraj311.data.parseColour
import com.srvraj311.data.plainPreview

/** Bottom sheet to search the user's notes and insert either a link chip or a preview card. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotePickerSheet(notes: List<Note>, initialCard: Boolean, onPick: (Note, Boolean) -> Unit, onDismiss: () -> Unit) {
    var query by remember { mutableStateOf("") }
    val focus = remember { FocusRequester() }
    val results = remember(notes, query) {
        val q = query.trim().lowercase()
        notes.filter { q.isEmpty() || it.title.lowercase().contains(q) || it.body.lowercase().contains(q) }.sortedByDescending { it.date }.take(40)
    }
    LaunchedEffect(Unit) { focus.requestFocus() }

    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true), containerColor = MaterialTheme.colorScheme.surface) {
        Column(Modifier.imePadding().navigationBarsPadding().padding(horizontal = 16.dp)) {
            Text("Link to a note", fontWeight = FontWeight.Bold, fontSize = 20.sp)
            Text(if (initialCard) "Tap a note to add a preview card, or the link button for a simple link." else "Tap a note to add a link, or the card button for a preview card.",
                color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 13.sp, modifier = Modifier.padding(top = 2.dp, bottom = 12.dp))
            Row(Modifier.fillMaxWidth().height(50.dp).clip(RoundedCornerShape(14.dp)).background(MaterialTheme.colorScheme.surfaceVariant).padding(horizontal = 14.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Filled.Search, null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                Box(Modifier.weight(1f).padding(start = 10.dp)) {
                    if (query.isEmpty()) Text("Search notes", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    BasicTextField(
                        query, { query = it }, singleLine = true, modifier = Modifier.fillMaxWidth().focusRequester(focus),
                        textStyle = TextStyle(color = MaterialTheme.colorScheme.onSurface, fontSize = 16.sp), cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                    )
                }
            }
            Spacer(Modifier.height(8.dp))
            if (results.isEmpty()) {
                Text("No notes found", color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(vertical = 32.dp).fillMaxWidth(), textAlign = androidx.compose.ui.text.style.TextAlign.Center)
            } else LazyColumn(Modifier.height(380.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                items(results, key = { it.id }) { n ->
                    Row(
                        Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).clickable { onPick(n, initialCard) }.padding(start = 8.dp, top = 6.dp, bottom = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Box(Modifier.size(10.dp).clip(CircleShape).background(if (n.hasColour) parseColour(n.colour) else MaterialTheme.colorScheme.outline))
                        Column(Modifier.weight(1f).padding(horizontal = 12.dp)) {
                            Text(n.title.ifBlank { "Untitled" }, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                            Text(plainPreview(n.body).take(80).ifBlank { "No additional text" }, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 13.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        }
                        PickButton(Icons.Filled.Link, "Insert a link", active = !initialCard) { onPick(n, false) }
                        Spacer(Modifier.width(6.dp))
                        PickButton(Icons.Filled.NoteAlt, "Insert a preview card", active = initialCard) { onPick(n, true) }
                    }
                }
            }
            Spacer(Modifier.height(12.dp))
        }
    }
}

@Composable
private fun PickButton(icon: androidx.compose.ui.graphics.vector.ImageVector, description: String, active: Boolean, onClick: () -> Unit) {
    Box(
        Modifier.size(44.dp).clip(RoundedCornerShape(12.dp))
            .background(if (active) MaterialTheme.colorScheme.primary.copy(alpha = .14f) else Color.Transparent).clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) { Icon(icon, description, tint = if (active) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant) }
}
