package com.srvraj311.ui.editor

import android.app.Activity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.CheckBox
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.DataObject
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FormatBold
import androidx.compose.material.icons.filled.FormatItalic
import androidx.compose.material.icons.filled.FormatListBulleted
import androidx.compose.material.icons.filled.FormatListNumbered
import androidx.compose.material.icons.filled.FormatQuote
import androidx.compose.material.icons.filled.FormatStrikethrough
import androidx.compose.material.icons.filled.HorizontalRule
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.NoteAlt
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.srvraj311.data.EditorMode
import com.srvraj311.data.NOTE_COLOURS
import com.srvraj311.data.Note
import com.srvraj311.data.parseColour
import com.srvraj311.data.plainPreview
import com.srvraj311.data.timeAgo
import com.srvraj311.ui.MainViewModel
import com.srvraj311.ui.components.BrandBrush
import com.srvraj311.ui.components.ConfirmDialog
import com.srvraj311.ui.components.Logo
import com.srvraj311.ui.components.Wordmark
import kotlinx.coroutines.launch
import java.util.UUID

@Composable
fun EditorScreen(vm: MainViewModel, noteId: String?, showEmptyHint: Boolean) {
    val notes by vm.repo.notes.collectAsState()
    val note = notes.firstOrNull { it.id == noteId }
    if (note == null) {
        if (showEmptyHint) EmptyEditor(onNew = vm::newNote)
        return
    }
    key(note.id) { NoteEditor(vm, note.id, wide = showEmptyHint) }
}

@Composable
private fun EmptyEditor(onNew: () -> Unit) {
    Column(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.surface).padding(32.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
        Logo(84.dp)
        Spacer(Modifier.height(12.dp))
        Text("Pick a note, or start a new one", fontSize = 20.sp, fontWeight = FontWeight.Bold)
        Text("Everything you write is saved and synced automatically.", color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(top = 4.dp, bottom = 16.dp))
        com.srvraj311.ui.components.GradientButton("New note", onNew, icon = Icons.Filled.NoteAlt)
    }
}

private fun toggleTask(md: String, index: Int): String {
    var i = -1
    return Regex("^(\\s*(?:[-*+]|\\d+\\.)\\s+)\\[( |x|X)]", RegexOption.MULTILINE).replace(md) { m ->
        i++
        if (i == index) "${m.groupValues[1]}[${if (m.groupValues[2] == " ") "x" else " "}]" else m.value
    }
}

private class SlashState(val start: Int, val query: String)
private class PickerState(val start: Int, val end: Int, val card: Boolean)

@Composable
private fun NoteEditor(vm: MainViewModel, id: String, wide: Boolean) {
    val context = LocalContext.current
    val activity = context as Activity
    val scope = rememberCoroutineScope()
    val focus = LocalFocusManager.current
    val notes by vm.repo.notes.collectAsState()
    val note = notes.firstOrNull { it.id == id } ?: return
    val settings by vm.settings.settings.collectAsState()
    val colors = MaterialTheme.colorScheme

    var mode by rememberSaveable(id) { mutableStateOf(settings.editorMode) }
    var title by remember { mutableStateOf(note.title) }
    var body by remember { mutableStateOf(TextFieldValue(note.body)) }
    var slash by remember { mutableStateOf<SlashState?>(null) }
    var picker by remember { mutableStateOf<PickerState?>(null) }
    var viewer by remember { mutableStateOf<ImageRef?>(null) }
    var confirmDelete by remember { mutableStateOf(false) }
    var menuOpen by remember { mutableStateOf(false) }
    var colourOpen by remember { mutableStateOf(false) }
    val titleFocus = remember { FocusRequester() }
    val bodyFocus = remember { FocusRequester() }

    // Edits made on another device show up here.
    LaunchedEffect(note.title) { if (note.title != title) title = note.title }
    LaunchedEffect(note.body) {
        if (note.body != body.text) body = body.copy(text = note.body, selection = TextRange(body.selection.start.coerceAtMost(note.body.length)))
    }
    LaunchedEffect(Unit) { if (note.isBlank && mode != EditorMode.Preview) titleFocus.requestFocus() }

    fun setBody(v: TextFieldValue, detectSlash: Boolean = true) {
        body = v
        if (v.text != note.body) vm.repo.update(id) { it.copy(body = v.text) }
        slash = if (detectSlash && mode != EditorMode.Preview) Fmt.slash(v)?.let { SlashState(it.first, it.second) } else null
    }
    fun onBodyChange(new: TextFieldValue) = setBody(Fmt.continueList(body, new) ?: new)
    fun apply(f: (TextFieldValue) -> TextFieldValue) = setBody(f(body), detectSlash = false)

    // Photo picker: shrink the image and keep it with the note as img:<id>.
    var imageRange by remember { mutableStateOf<Pair<Int, Int>?>(null) }
    val pickImage = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri == null) return@rememberLauncherForActivityResult
        scope.launch {
            val img = ImageUtils.import(context, uri)
            if (img == null) { vm.toast("That image could not be added"); return@launch }
            val imgId = UUID.randomUUID().toString().take(8)
            val (s, e) = imageRange ?: (body.selection.min to body.selection.max)
            val updated = Fmt.insert(body, s.coerceAtMost(body.text.length), e.coerceAtMost(body.text.length), "![${img.name}](img:$imgId)\n\n", block = true)
            body = updated
            vm.repo.update(id) { it.copy(body = updated.text, images = it.images + (imgId to img.dataUri)) }
            vm.toast("Image added")
        }
    }
    fun addImage(start: Int = body.selection.min, end: Int = body.selection.max) {
        imageRange = start to end
        pickImage.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
    }

    val saveMd = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("text/markdown")) { uri ->
        if (uri == null) return@rememberLauncherForActivityResult
        val ok = runCatching { context.contentResolver.openOutputStream(uri)?.use { it.write(Export.markdown(note).toByteArray()) } }.isSuccess
        vm.toast(if (ok) "Saved as Markdown" else "Could not save the file")
    }

    fun pickSlash(item: SlashItem) {
        val s = slash ?: return
        slash = null
        val end = body.selection.start
        when (item.action) {
            SlashAction.NoteLink -> picker = PickerState(s.start, end, false)
            SlashAction.NoteCard -> picker = PickerState(s.start, end, true)
            SlashAction.Image -> addImage(s.start, end)
            SlashAction.Date -> setBody(Fmt.insert(body, s.start, end, java.text.DateFormat.getDateInstance(java.text.DateFormat.FULL).format(java.util.Date())), detectSlash = false)
            null -> setBody(Fmt.insert(body, s.start, end, item.md.orEmpty(), item.block), detectSlash = false)
        }
        if (item.action == null || item.action == SlashAction.Date) bodyFocus.requestFocus()
    }

    val resolve = { nid: String ->
        notes.firstOrNull { it.id == nid }?.let { LinkedNote(it.id, it.title, plainPreview(it.body).take(110), if (it.hasColour) parseColour(it.colour) else null) }
    }
    val live = LiveColors(colors.onSurface, colors.onSurfaceVariant.copy(alpha = .65f), colors.primary, colors.onSurface.copy(alpha = .08f), Color(0xFFFFD54A).copy(alpha = .45f))
    val words = remember(note.body) { note.body.trim().split(Regex("\\s+")).count { it.isNotEmpty() } }

    Column(Modifier.fillMaxSize().background(colors.background).statusBarsPadding()) {
        // Top bar
        Row(Modifier.fillMaxWidth().padding(horizontal = 4.dp).height(56.dp), verticalAlignment = Alignment.CenterVertically) {
            if (!wide) IconButton(onClick = { focus.clearFocus(); vm.close() }) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back to notes") }
            else Spacer(Modifier.width(12.dp))
            ModeSwitch(mode) { mode = it; slash = null; if (it == EditorMode.Preview) focus.clearFocus() }
            Spacer(Modifier.weight(1f))
            IconButton(onClick = { vm.repo.togglePin(id) }) {
                Icon(if (note.pinned) Icons.Filled.Star else Icons.Outlined.StarBorder, if (note.pinned) "Remove from favourites" else "Add to favourites", tint = if (note.pinned) colors.primary else colors.onSurfaceVariant)
            }
            Box {
                IconButton(onClick = { colourOpen = true }) {
                    Box(
                        Modifier.size(24.dp).clip(CircleShape)
                            .background(if (note.hasColour) parseColour(note.colour) else colors.surfaceVariant)
                            .border(1.5.dp, colors.outline, CircleShape),
                    )
                }
                DropdownMenu(colourOpen, onDismissRequest = { colourOpen = false }) {
                    FlowRow(Modifier.widthIn4().padding(12.dp), horizontalArrangement = Arrangement.spacedBy(10.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        NOTE_COLOURS.forEach { c ->
                            val selected = c.equals(note.colour, ignoreCase = true)
                            Box(
                                Modifier.size(40.dp).clip(CircleShape).background(if (c == "#FFFFFF") colors.surface else parseColour(c))
                                    .clickable { vm.repo.update(id, touch = false) { it.copy(colour = c) }; colourOpen = false },
                                contentAlignment = Alignment.Center,
                            ) {
                                if (selected) Icon(Icons.Filled.Check, "Selected", tint = if (c == "#FFFFFF") colors.primary else Color.White)
                                else if (c == "#FFFFFF") Text("-", color = colors.onSurfaceVariant)
                            }
                        }
                    }
                }
            }
            Box {
                IconButton(onClick = { menuOpen = true }) { Icon(Icons.Filled.MoreVert, "More actions") }
                DropdownMenu(menuOpen, onDismissRequest = { menuOpen = false }) {
                    DropdownMenuItem(text = { Text("Save as Markdown (.md)") }, leadingIcon = { Icon(Icons.Filled.Code, null) }, onClick = { menuOpen = false; saveMd.launch("${Export.fileName(note)}.md") })
                    DropdownMenuItem(text = { Text("Save as PDF") }, leadingIcon = { Icon(Icons.Filled.NoteAlt, null) }, onClick = { menuOpen = false; Export.pdf(activity, note) })
                    DropdownMenuItem(text = { Text("Share") }, leadingIcon = { Icon(Icons.Filled.Share, null) }, onClick = { menuOpen = false; Export.share(context, note) })
                    HorizontalDivider()
                    DropdownMenuItem(text = { Text("Delete note", color = colors.error) }, leadingIcon = { Icon(Icons.Filled.Delete, null, tint = colors.error) }, onClick = { menuOpen = false; confirmDelete = true })
                }
            }
        }

        // Document
        Column(Modifier.weight(1f).fillMaxWidth().verticalScroll(rememberScrollState()).padding(horizontal = 20.dp)) {
            Box(Modifier.fillMaxWidth().padding(top = 8.dp)) {
                if (title.isEmpty()) Text("Untitled", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = colors.onSurfaceVariant.copy(alpha = .5f))
                BasicTextField(
                    value = title,
                    onValueChange = { raw ->
                        // Enter in the title moves to the body instead of adding a line.
                        val entered = raw.contains('\n')
                        val v = raw.replace("\n", "")
                        title = v
                        vm.repo.update(id) { n -> n.copy(title = v) }
                        if (entered && mode != EditorMode.Preview) bodyFocus.requestFocus()
                    },
                    textStyle = TextStyle(color = colors.onSurface, fontSize = 28.sp, fontWeight = FontWeight.Bold, lineHeight = 34.sp),
                    cursorBrush = SolidColor(colors.primary),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                    keyboardActions = KeyboardActions(onNext = { if (mode != EditorMode.Preview) bodyFocus.requestFocus() }),
                    readOnly = mode == EditorMode.Preview,
                    modifier = Modifier.fillMaxWidth().focusRequester(titleFocus),
                )
            }
            Spacer(Modifier.height(10.dp))

            if (mode == EditorMode.Preview) {
                MarkdownView(
                    markdown = note.body, images = note.images, resolveNote = resolve,
                    onToggleTask = { i -> val t = toggleTask(note.body, i); body = body.copy(text = t); vm.repo.update(id) { it.copy(body = t) } },
                    onOpenNote = vm::open, onImage = { viewer = it },
                )
            } else {
                val source = mode == EditorMode.Source
                Box(Modifier.fillMaxWidth().heightIn(min = 360.dp)) {
                    if (body.text.isEmpty()) Text("Start writing...  Type / for commands", color = colors.onSurfaceVariant.copy(alpha = .6f), fontSize = if (source) 15.sp else 17.sp)
                    BasicTextField(
                        value = body, onValueChange = ::onBodyChange,
                        textStyle = TextStyle(
                            color = colors.onSurface, fontSize = if (source) 15.sp else 17.sp, lineHeight = if (source) 24.sp else 27.sp,
                            fontFamily = if (source) FontFamily.Monospace else FontFamily.Default,
                        ),
                        cursorBrush = SolidColor(colors.primary),
                        visualTransformation = if (source) VisualTransformation.None else LiveTransformation(live),
                        modifier = Modifier.fillMaxWidth().heightIn(min = 360.dp).focusRequester(bodyFocus),
                    )
                }
            }

            Spacer(Modifier.height(16.dp))
            Text("$words ${if (words == 1) "word" else "words"}  ·  ${(words / 220).coerceAtLeast(1)} min read  ·  Edited ${timeAgo(note.date)}", color = colors.onSurfaceVariant, fontSize = 12.sp)
            Spacer(Modifier.height(140.dp))
        }

        // Slash panel + formatting toolbar sit above the keyboard
        if (mode != EditorMode.Preview) {
            Column(Modifier.imePadding().navigationBarsPadding()) {
                val items = slash?.let { matchSlash(it.query) }.orEmpty()
                if (items.isNotEmpty()) SlashPanel(items, ::pickSlash)
                FormatToolbar(
                    onBold = { apply { Fmt.wrap(it, "**") } }, onItalic = { apply { Fmt.wrap(it, "*") } },
                    onStrike = { apply { Fmt.wrap(it, "~~") } }, onCode = { apply { Fmt.wrap(it, "`") } },
                    onH1 = { apply { Fmt.block(it, "h1") } }, onH2 = { apply { Fmt.block(it, "h2") } },
                    onBullets = { apply { Fmt.block(it, "ul") } }, onNumbers = { apply { Fmt.block(it, "ol") } },
                    onTask = { apply { Fmt.block(it, "task") } }, onQuote = { apply { Fmt.block(it, "quote") } },
                    onCodeBlock = { apply { Fmt.codeBlock(it) } }, onLink = { apply { Fmt.link(it) } },
                    onImage = { addImage() },
                    onSlash = {
                        val s = body.selection.min
                        val lead = if (s > 0 && !body.text[s - 1].isWhitespace()) " " else ""
                        bodyFocus.requestFocus()
                        setBody(Fmt.insert(body, s, body.selection.max, "$lead/"))
                    },
                    onNoteLink = { picker = PickerState(body.selection.min, body.selection.max, false) },
                )
            }
        }
    }

    picker?.let { p ->
        NotePickerSheet(
            notes = notes.filter { it.id != id && !it.isBlank }, initialCard = p.card, onDismiss = { picker = null },
            onPick = { n, card ->
                picker = null
                val title = n.title.ifBlank { "Untitled" }.replace(Regex("[\\[\\]]"), "")
                val md = if (card) "[$title](enwrite://note/${n.id} \"card\")\n\n" else "[$title](enwrite://note/${n.id})"
                setBody(Fmt.insert(body, p.start.coerceAtMost(body.text.length), p.end.coerceAtMost(body.text.length), md, block = card), detectSlash = false)
            },
        )
    }
    viewer?.let { ImageViewer(it) { viewer = null } }
    if (confirmDelete) {
        ConfirmDialog(
            title = "Delete note?", message = "\"${note.title.ifBlank { "Untitled" }}\" will be permanently removed from all your devices.",
            confirmLabel = "Delete", danger = true, onConfirm = { confirmDelete = false; vm.delete(id) }, onDismiss = { confirmDelete = false },
        )
    }
}

private fun Modifier.widthIn4() = this.then(Modifier.width(214.dp))

@Composable
private fun ModeSwitch(mode: EditorMode, onChange: (EditorMode) -> Unit) {
    Row(Modifier.clip(RoundedCornerShape(12.dp)).background(MaterialTheme.colorScheme.surfaceVariant).padding(3.dp)) {
        listOf(EditorMode.Source to "Source", EditorMode.Live to "Live", EditorMode.Preview to "Preview").forEach { (m, label) ->
            val on = m == mode
            Box(
                Modifier.height(36.dp).clip(RoundedCornerShape(10.dp)).background(if (on) MaterialTheme.colorScheme.surface else Color.Transparent)
                    .clickable { onChange(m) }.padding(horizontal = 12.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(label, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = if (on) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
private fun SlashPanel(items: List<SlashItem>, onPick: (SlashItem) -> Unit) {
    Column(Modifier.fillMaxWidth().clip(RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)).background(MaterialTheme.colorScheme.surface)) {
        Text("INSERT", fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 0.8.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(start = 20.dp, top = 12.dp, bottom = 4.dp))
        LazyColumn(Modifier.heightIn(max = 248.dp)) {
            items(items, key = { it.id }) { item ->
                Row(Modifier.fillMaxWidth().clickable { onPick(item) }.padding(horizontal = 16.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                    Box(Modifier.size(40.dp).clip(RoundedCornerShape(12.dp)).background(MaterialTheme.colorScheme.primary.copy(alpha = .12f)), contentAlignment = Alignment.Center) {
                        Icon(item.icon, null, tint = MaterialTheme.colorScheme.primary)
                    }
                    Column(Modifier.padding(start = 14.dp)) {
                        Text(item.label, fontWeight = FontWeight.SemiBold)
                        Text(item.hint, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 13.sp)
                    }
                }
            }
        }
    }
}

@Composable
private fun FormatToolbar(
    onBold: () -> Unit, onItalic: () -> Unit, onStrike: () -> Unit, onCode: () -> Unit, onH1: () -> Unit, onH2: () -> Unit,
    onBullets: () -> Unit, onNumbers: () -> Unit, onTask: () -> Unit, onQuote: () -> Unit, onCodeBlock: () -> Unit,
    onLink: () -> Unit, onImage: () -> Unit, onSlash: () -> Unit, onNoteLink: () -> Unit,
) {
    Row(
        Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.surface).horizontalScroll(rememberScrollState()).padding(horizontal = 6.dp, vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        ToolButton(null, "/", "Insert menu", onSlash, primary = true)
        ToolButton(Icons.Filled.FormatBold, null, "Bold", onBold)
        ToolButton(Icons.Filled.FormatItalic, null, "Italic", onItalic)
        ToolButton(Icons.Filled.FormatStrikethrough, null, "Strikethrough", onStrike)
        ToolButton(Icons.Filled.DataObject, null, "Inline code", onCode)
        ToolButton(null, "H1", "Heading 1", onH1)
        ToolButton(null, "H2", "Heading 2", onH2)
        ToolButton(Icons.Filled.FormatListBulleted, null, "Bulleted list", onBullets)
        ToolButton(Icons.Filled.FormatListNumbered, null, "Numbered list", onNumbers)
        ToolButton(Icons.Filled.CheckBox, null, "Checklist", onTask)
        ToolButton(Icons.Filled.FormatQuote, null, "Quote", onQuote)
        ToolButton(Icons.Filled.Code, null, "Code block", onCodeBlock)
        ToolButton(Icons.Filled.Link, null, "Web link", onLink)
        ToolButton(Icons.Filled.NoteAlt, null, "Link to a note", onNoteLink)
        ToolButton(Icons.Filled.AddPhotoAlternate, null, "Add image", onImage)
    }
}

@Composable
private fun ToolButton(icon: ImageVector?, label: String?, description: String, onClick: () -> Unit, primary: Boolean = false) {
    Box(Modifier.size(48.dp).clip(RoundedCornerShape(12.dp)).clickable(onClick = onClick), contentAlignment = Alignment.Center) {
        if (icon != null) Icon(icon, description, tint = MaterialTheme.colorScheme.onSurfaceVariant)
        else Text(label.orEmpty(), fontWeight = FontWeight.Bold, fontSize = if (primary) 22.sp else 15.sp, color = if (primary) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
