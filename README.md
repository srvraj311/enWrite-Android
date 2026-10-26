# enWrite for Android

A fast, minimal note-taking app that syncs with the enWrite web and desktop apps. Native Kotlin and Jetpack Compose, built for touch.

- **Package:** `com.srvraj311` · **Min SDK:** 24 (Android 7.0) · **Target SDK:** 36 · **Compile SDK:** 37
- **Backend:** Firebase project `enwrite-web` (Auth + Firestore), the same one the web app uses. Notes live at `users/{email}/note/{id}`, so everything syncs.

## Features

- **Sign in** with email and password, Google (Credential Manager), password reset, or try the app as a guest (notes stay on the device).
- **Notes list** with search, favourites, sort by time, title or colour, note colours, swipe to delete and a sync status chip.
- **Three ways to write:** Source (raw Markdown), Live (the Markdown styled as you type, markers dimmed) and Preview (rendered, with tappable checkboxes).
- **Formatting toolbar** above the keyboard: bold, italic, strikethrough, code, headings, lists, checklist, quote, code block, link, note link, image.
- **`/` insert menu** for headings, lists, checklist, quote, callout, code block, table, divider, note link, note card, image, web link, highlight and today's date.
- **Link notes together** with link chips or preview cards (`[Title](enwrite://note/<id> "card")`), compatible with the web app.
- **Rendering:** callouts (`> [!NOTE]`), `==highlights==`, tables, strikethrough, syntax-highlighted code.
- **Images:** pick from the gallery; images are shrunk and saved with the note (`img:<id>`). Tap one to open a full-screen viewer with pinch to zoom and a Download button.
- **Export:** save as Markdown, save as PDF (system print dialog) or share.
- **Realtime sync** with an offline cache. The status chip shows Synced, Syncing or Offline, and signing out saves your last edit and warns if changes are still unsynced.
- **Light, dark and system themes**, edge to edge, tablet layout (list and editor side by side on wide screens).

## Build

Requirements: Android Studio (or JDK 17+ and the Android SDK with platform 37), then:

```sh
./gradlew :app:assembleDebug        # debug APK
./gradlew :app:installDebug         # install on a connected device or emulator
```

## Release (Google Play)

1. Create `keystore.properties` in the project root (it is git-ignored, never commit it):

   ```properties
   storeFile=C:/path/to/enwrite-upload.jks
   storePassword=...
   keyAlias=enwrite-upload
   keyPassword=...
   ```

2. Build the signed bundle and upload `app/build/outputs/bundle/release/app-release.aab` to Play Console:

   ```sh
   ./gradlew :app:bundleRelease
   ```

3. Raise `versionCode` (and `versionName`) in `app/build.gradle.kts` for every upload.
4. Google sign-in: after your first upload, copy the **App signing key certificate** SHA-1 from Play Console (Release → Setup → App signing) and add it to the Android app in the Firebase console. The upload key and debug key fingerprints are already registered.

## Project layout

```
app/src/main/java/com/srvraj311/
  data/        Note model, Firestore repository, auth, settings
  ui/          app root, home list, auth, settings
  ui/editor/   editor, Markdown renderer, live styling, slash menu, images, export
```
