package com.srvraj311.ui.home

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Smartphone
import androidx.compose.material.icons.filled.Sort
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material.icons.outlined.NoteAlt
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import com.srvraj311.data.AppUser
import com.srvraj311.data.Note
import com.srvraj311.data.SyncState
import com.srvraj311.data.parseColour
import com.srvraj311.data.timeAgo
import com.srvraj311.ui.Filter
import com.srvraj311.ui.MainViewModel
import com.srvraj311.ui.Sort
import com.srvraj311.ui.components.BrandBrush
import com.srvraj311.ui.components.ConfirmDialog
import com.srvraj311.ui.components.Logo
import com.srvraj311.ui.components.Wordmark

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun HomeScreen(vm: MainViewModel, user: AppUser, onOpenSettings: () -> Unit, modifier: Modifier = Modifier) {
    val notes by vm.visible.collectAsState()
    val all by vm.repo.notes.collectAsState()
    val loading by vm.repo.loading.collectAsState()
    val query by vm.query.collectAsState()
    val filter by vm.filter.collectAsState()
    val sort by vm.sort.collectAsState()
    val favourites by vm.favouriteCount.collectAsState()
    val selected by vm.selectedId.collectAsState()
    var pendingDelete by remember { mutableStateOf<Note?>(null) }
    val haptic = LocalHapticFeedback.current

    Box(modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        Column(Modifier.fillMaxSize().statusBarsPadding()) {
            // Header
            Row(Modifier.fillMaxWidth().padding(start = 18.dp, end = 6.dp, top = 10.dp), verticalAlignment = Alignment.CenterVertically) {
                Logo(34.dp)
                Spacer(Modifier.width(8.dp))
                Wordmark(26.sp)
                Spacer(Modifier.weight(1f))
                SyncChip(vm.repo.sync.collectAsState().value, user.guest)
                AccountButton(user, onOpenSettings, vm::requestSignOut)
            }

            // Search
            SearchField(query, vm.query::value::set, Modifier.padding(horizontal = 16.dp, vertical = 10.dp))

            // Filters + sort
            Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp), verticalAlignment = Alignment.CenterVertically) {
                FilterPill("All", all.size, filter == Filter.All) { vm.filter.value = Filter.All }
                Spacer(Modifier.width(8.dp))
                FilterPill("Favourites", favourites, filter == Filter.Favourites) { vm.filter.value = Filter.Favourites }
                Spacer(Modifier.weight(1f))
                SortMenu(sort) { vm.sort.value = it }
            }

            // List
            when {
                loading && all.isEmpty() -> Box(Modifier.weight(1f).fillMaxWidth().padding(16.dp)) { SkeletonList() }
                notes.isEmpty() -> EmptyState(query.isNotBlank(), filter == Filter.Favourites, onNew = vm::newNote, modifier = Modifier.weight(1f))
                else -> LazyColumn(
                    Modifier.weight(1f).fillMaxWidth(),
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 120.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    items(notes, key = { it.id }) { note ->
                        SwipeableNote(
                            note = note,
                            selected = note.id == selected,
                            snippet = vm.snippet(note),
                            onClick = { vm.open(note.id) },
                            onStar = { vm.repo.togglePin(note.id); haptic.performHapticFeedback(HapticFeedbackType.ContextClick) },
                            onDelete = { pendingDelete = note },
                        )
                    }
                }
            }
        }

        // New note button
        Box(
            Modifier.align(Alignment.BottomEnd).navigationBarsPadding().padding(20.dp)
                .clip(RoundedCornerShape(20.dp)).background(BrandBrush)
                .combinedClickable(onClick = vm::newNote)
                .padding(horizontal = 20.dp, vertical = 16.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(Icons.Filled.Add, null, tint = Color.White)
                Text("New note", color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
            }
        }
    }

    pendingDelete?.let { n ->
        ConfirmDialog(
            title = "Delete note?",
            message = "\"${n.title.ifBlank { "Untitled" }}\" will be permanently removed from all your devices.",
            confirmLabel = "Delete", danger = true,
            onConfirm = { vm.delete(n.id); pendingDelete = null },
            onDismiss = { pendingDelete = null },
        )
    }
}

@Composable
private fun SearchField(value: String, onChange: (String) -> Unit, modifier: Modifier = Modifier) {
    Row(
        modifier.fillMaxWidth().height(52.dp).clip(RoundedCornerShape(16.dp)).background(MaterialTheme.colorScheme.surface)
            .padding(start = 14.dp, end = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(Icons.Filled.Search, null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
        Box(Modifier.weight(1f).padding(horizontal = 10.dp)) {
            if (value.isEmpty()) Text("Search notes", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 16.sp)
            BasicTextField(
                value = value, onValueChange = onChange, singleLine = true,
                textStyle = androidx.compose.ui.text.TextStyle(color = MaterialTheme.colorScheme.onSurface, fontSize = 16.sp),
                cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                modifier = Modifier.fillMaxWidth(),
            )
        }
        if (value.isNotEmpty()) IconButton(onClick = { onChange("") }) { Icon(Icons.Filled.Close, "Clear search", tint = MaterialTheme.colorScheme.onSurfaceVariant) }
    }
}

@Composable
private fun FilterPill(label: String, count: Int, active: Boolean, onClick: () -> Unit) {
    val bg by animateColorAsState(if (active) MaterialTheme.colorScheme.primary.copy(alpha = .14f) else MaterialTheme.colorScheme.surface, label = "pill")
    Row(
        Modifier.height(40.dp).clip(RoundedCornerShape(20.dp)).background(bg).combinedClickable(onClick = onClick).padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(label, fontWeight = FontWeight.SemiBold, color = if (active) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface)
        Spacer(Modifier.width(6.dp))
        Text("$count", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 13.sp)
    }
}

@Composable
private fun SortMenu(current: Sort, onPick: (Sort) -> Unit) {
    var open by remember { mutableStateOf(false) }
    Box {
        IconButton(onClick = { open = true }) { Icon(Icons.Filled.Sort, "Sort notes", tint = MaterialTheme.colorScheme.onSurfaceVariant) }
        DropdownMenu(open, onDismissRequest = { open = false }) {
            Sort.entries.forEach { s ->
                DropdownMenuItem(
                    text = { Text("Sort by ${s.label.lowercase()}", fontWeight = if (s == current) FontWeight.Bold else FontWeight.Normal, color = if (s == current) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface) },
                    onClick = { onPick(s); open = false },
                )
            }
        }
    }
}

@Composable
private fun AccountButton(user: AppUser, onSettings: () -> Unit, onSignOut: () -> Unit) {
    var open by remember { mutableStateOf(false) }
    Box {
        IconButton(onClick = { open = true }, modifier = Modifier.size(52.dp)) {
            Box(Modifier.size(36.dp).clip(CircleShape).background(BrandBrush), contentAlignment = Alignment.Center) {
                Text((user.name.ifBlank { user.email }.firstOrNull() ?: '?').uppercase(), color = Color.White, fontWeight = FontWeight.Bold)
            }
        }
        DropdownMenu(open, onDismissRequest = { open = false }) {
            Column(Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                Text(user.name.ifBlank { "Welcome" }, fontWeight = FontWeight.Bold)
                Text(if (user.guest) "Guest - notes stay on this device" else user.email, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 13.sp)
            }
            DropdownMenuItem(text = { Text("Settings") }, leadingIcon = { Icon(Icons.Filled.Settings, null) }, onClick = { open = false; onSettings() })
            DropdownMenuItem(text = { Text(if (user.guest) "Leave guest mode" else "Sign out") }, leadingIcon = { Icon(Icons.AutoMirrored.Filled.Logout, null) }, onClick = { open = false; onSignOut() })
        }
    }
}

@Composable
fun SyncChip(state: SyncState, guest: Boolean) {
    val (icon, label, tint) = when {
        guest -> Triple(Icons.Filled.Smartphone, "On device", MaterialTheme.colorScheme.onSurfaceVariant)
        state == SyncState.Error -> Triple(Icons.Filled.ErrorOutline, "Sync error", MaterialTheme.colorScheme.error)
        state == SyncState.Offline -> Triple(Icons.Filled.CloudOff, "Offline", Color(0xFFD97706))
        state == SyncState.Saving -> Triple(Icons.Filled.CloudSync, "Syncing", MaterialTheme.colorScheme.primary)
        else -> Triple(Icons.Filled.CloudDone, "Synced", MaterialTheme.colorScheme.onSurfaceVariant)
    }
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp), modifier = Modifier.padding(end = 2.dp)) {
        Icon(icon, null, tint = tint, modifier = Modifier.size(18.dp))
        Text(label, color = tint, fontSize = 12.sp, fontWeight = FontWeight.Medium)
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun SwipeableNote(note: Note, selected: Boolean, snippet: String, onClick: () -> Unit, onStar: () -> Unit, onDelete: () -> Unit) {
    val state = rememberSwipeToDismissBoxState(confirmValueChange = { v ->
        if (v == SwipeToDismissBoxValue.EndToStart) onDelete()
        false // snap back; the confirm dialog decides
    })
    SwipeToDismissBox(
        state = state,
        enableDismissFromStartToEnd = false,
        backgroundContent = {
            Box(Modifier.fillMaxSize().clip(RoundedCornerShape(18.dp)).background(MaterialTheme.colorScheme.error), contentAlignment = Alignment.CenterEnd) {
                Icon(Icons.Filled.Delete, "Delete", tint = Color.White, modifier = Modifier.padding(end = 24.dp))
            }
        },
    ) { NoteCard(note, selected, snippet, onClick, onStar, onDelete) }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun NoteCard(note: Note, selected: Boolean, snippet: String, onClick: () -> Unit, onStar: () -> Unit, onLongPress: () -> Unit) {
    val shape = RoundedCornerShape(18.dp)
    Row(
        Modifier.fillMaxWidth().height(IntrinsicSize.Min).clip(shape)
            .background(if (selected) MaterialTheme.colorScheme.primary.copy(alpha = .10f) else MaterialTheme.colorScheme.surface)
            .combinedClickable(onClick = onClick, onLongClick = onLongPress),
    ) {
        Box(Modifier.width(5.dp).fillMaxHeight().background(if (note.hasColour) parseColour(note.colour) else Color.Transparent))
        Column(Modifier.weight(1f).padding(start = 14.dp, top = 12.dp, bottom = 6.dp, end = 4.dp)) {
            Text(note.title.ifBlank { "Untitled" }, fontWeight = FontWeight.Bold, fontSize = 17.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(
                snippet.ifBlank { "No additional text" }, maxLines = 2, overflow = TextOverflow.Ellipsis,
                color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 14.sp, lineHeight = 20.sp, modifier = Modifier.padding(top = 2.dp),
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(timeAgo(note.date), color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = .8f), fontSize = 12.sp, modifier = Modifier.weight(1f))
                IconButton(onClick = onStar, modifier = Modifier.size(44.dp)) {
                    Icon(
                        if (note.pinned) Icons.Filled.Star else Icons.Outlined.StarBorder, if (note.pinned) "Remove from favourites" else "Add to favourites",
                        tint = if (note.pinned) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(22.dp),
                    )
                }
            }
        }
    }
}

@Composable
private fun EmptyState(searching: Boolean, favourites: Boolean, onNew: () -> Unit, modifier: Modifier = Modifier) {
    Column(modifier.fillMaxWidth().padding(32.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
        Icon(if (searching) Icons.Filled.Search else Icons.Outlined.NoteAlt, null, Modifier.size(52.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = .6f))
        Spacer(Modifier.height(12.dp))
        Text(
            when { searching -> "No notes match your search"; favourites -> "No favourites yet.\nTap the star on a note to keep it here."; else -> "Your notes will appear here" },
            color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = androidx.compose.ui.text.style.TextAlign.Center,
        )
        if (!searching && !favourites) {
            Spacer(Modifier.height(16.dp))
            com.srvraj311.ui.components.GradientButton("Create your first note", onNew, icon = Icons.Filled.Add)
        }
    }
}

@Composable
private fun SkeletonList() {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        repeat(4) { Box(Modifier.fillMaxWidth().height(92.dp).clip(RoundedCornerShape(18.dp)).background(MaterialTheme.colorScheme.surface)) }
    }
}
