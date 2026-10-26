<div align="center">

<img src="app/src/main/res/drawable-nodpi/logo.png" alt="enWrite logo" width="84" />

# enWrite for Android

**A fast, minimal note-taking app that syncs with the enWrite web and desktop apps.**<br/>
Native Kotlin and Jetpack Compose, built for touch.

</div>

<p align="center">
  <img src="docs/screenshots/home-light.png" alt="Notes list" width="250" />
  <img src="docs/screenshots/editor-live.png" alt="Live editor" width="250" />
  <img src="docs/screenshots/preview-top.png" alt="Rendered preview" width="250" />
</p>

- **Package:** `com.srvraj311` · **Min SDK:** 24 (Android 7.0) · **Target SDK:** 36 · **Compile SDK:** 37
- **Backend:** Firebase project `enwrite-web` (Auth + Firestore), the same one the web app uses. Notes live at `users/{email}/note/{id}`, so every enWrite app shares them.

## Features

- **Sign in** with email and password, Google (Credential Manager), password reset, or try the app as a guest (notes stay on the device).
- **Notes list** with search, favourites, sort by time, title or colour, note colours, swipe to delete and a sync status chip.
- **Three ways to write:** Source (raw Markdown), Live (the Markdown styled as you type, markers dimmed) and Preview (rendered, with tappable checkboxes).
- **Formatting toolbar** above the keyboard: bold, italic, strikethrough, code, headings, lists, checklist, quote, code block, link, note link, image.
- **`/` insert menu** for headings, lists, checklist, quote, callout, code block, table, divider, note link, note card, image, web link, highlight and today's date.
- **Link notes together** with link chips or preview cards (`[Title](enwrite://note/<id> "card")`), compatible with the web app.
- **Rich rendering:** callouts (`> [!NOTE]`), `==highlights==`, tables, strikethrough and syntax-highlighted code.
- **Images:** pick from the gallery; images are shrunk and saved with the note (`img:<id>`). Tap one to open a full-screen viewer with pinch to zoom and a Download button.
- **Export:** save as Markdown, save as PDF (system print dialog) or share.
- **Realtime sync** with an offline cache. The status chip shows Synced, Syncing or Offline, and signing out saves your last edit and warns if changes are still unsynced.
- **Light, dark and system themes**, edge to edge, with a side-by-side layout on tablets.

## Screenshots

| Preview | Links, code and tables | Slash menu |
| :---: | :---: | :---: |
| <img src="docs/screenshots/preview-top.png" alt="Preview" width="230" /> | <img src="docs/screenshots/preview-links.png" alt="Links, code and tables" width="230" /> | <img src="docs/screenshots/slash-menu.png" alt="Slash insert menu" width="230" /> |

| Link to a note | Dark theme | Sign in |
| :---: | :---: | :---: |
| <img src="docs/screenshots/link-notes.png" alt="Note picker" width="230" /> | <img src="docs/screenshots/home-dark.png" alt="Dark theme" width="230" /> | <img src="docs/screenshots/login.png" alt="Sign in" width="230" /> |

<p align="center">
  <img src="docs/screenshots/preview-dark.png" alt="Dark preview" width="230" />
</p>

## Build

Requirements: Android Studio (or JDK 17+ and the Android SDK with platform 37), then:

```sh
./gradlew :app:assembleDebug        # debug APK
./gradlew :app:installDebug         # install on a connected device or emulator
```

### Release build

Create `keystore.properties` in the project root (it is git-ignored, never commit it):

```properties
storeFile=C:/path/to/your-keystore.jks
storePassword=...
keyAlias=your-alias
keyPassword=...
```

Then build the shrunk, signed release:

```sh
./gradlew :app:assembleRelease      # app/build/outputs/apk/release/app-release.apk
./gradlew :app:bundleRelease        # app/build/outputs/bundle/release/app-release.aab
```

Raise `versionCode` and `versionName` in `app/build.gradle.kts` for each new release.

## Project layout

```
app/src/main/java/com/srvraj311/
  data/        Note model, Firestore repository, auth, settings
  ui/          app root, home list, auth, settings
  ui/editor/   editor, Markdown renderer, live styling, slash menu, images, export
```

## Tech

Kotlin 2.4 · Jetpack Compose (Material 3) · Android Gradle Plugin 9.4 · Firebase Auth and Firestore · Credential Manager · CommonMark (rendering) · Coil (images) · DataStore (settings).

---

## Before v2: the original app

enWrite started in 2021 as a simple Java app, and this project is its complete rewrite. This is what the original release looked like and offered.

> enWrite is a simple and clean app for taking notes on the go and setting reminders for important tasks.
>
> - Note taking and real-time cloud backup
> - Reminders and alarms with a stylish date picker
> - Cross browser and platform support
>
> **Later additions:** a new display UI for saved notes, local synchronization for offline use, and a colour picker when creating a note.

<p align="center">
  <img src="https://res.cloudinary.com/srvraj311/image/upload/v1629315800/Screenshot_2021-08-18-22-23-13-281_com.srvraj311_x6f5ga.jpg" alt="Original app" width="190" />
  <img src="https://res.cloudinary.com/srvraj311/image/upload/v1629315800/Screenshot_2021-08-18-22-22-46-712_com.srvraj311_eyty8k.jpg" alt="Original app" width="190" />
  <img src="https://res.cloudinary.com/srvraj311/image/upload/v1629315800/Screenshot_2021-08-18-22-23-03-322_com.srvraj311_tqf8kk.jpg" alt="Original app" width="190" />
  <img src="https://res.cloudinary.com/srvraj311/image/upload/v1629315800/Screenshot_2021-08-18-22-22-55-060_com.srvraj311_qkpuyf.jpg" alt="Original app" width="190" />
</p>

### What changed in v2

| | Original (v1) | v2 |
| --- | --- | --- |
| Language and UI | Java, XML layouts, Android Views | Kotlin, Jetpack Compose |
| Notes | Plain text with colours | Markdown with Source, Live and Preview modes, `/` menu, note links and cards, images |
| Cloud sync | Firestore, one-off reads | Realtime listeners, offline cache, sync status, safe sign-out |
| Backend | Firebase project `enwrite-8ffba` | Shared `enwrite-web` project, so notes sync with the web and desktop apps |
| Colours and favourites | Colour picker, pin and unpin | Colour picker, favourites, search and sorting |
| Reminders and alarms | Yes | Not carried over yet |
| Themes | Light and dark | Light, dark and system, with an in-app switch |
| Build | Gradle 6.5, SDK 30 | Gradle 9.6, SDK 37 |
