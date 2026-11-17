# MDView for Android

The MDView Markdown reader as a native Android app (Kotlin + Jetpack Compose). It renders with the
**same engine as MDView for Mac**. Gradle reads `../MDView/Resources` directly, so maths, diagrams and
code look identical on both, and engine fixes land in both apps at once.

## Install

Sideload `release/MDView-1.2.0.apk` (Android 8.0 or later, about 5 MB). It is signed with a debug key,
so Android asks you to allow installs from the app you open it with. For the Play Store, sign it with
a real upload key.

## What it does

- **Renders everything the Mac app does**:
  - all LaTeX delimiters and AMS environments, mhchem and macros (KaTeX);
  - Mermaid, loaded only when a document has a diagram;
  - syntax highlighting, callouts, front-matter card, footnotes and task lists;
  - local images, video and audio.
- **Opens files from anywhere**: Open…, Open Folder…, or "Open with MDView" from Files, Drive, mail and chat apps. Text shared to MDView opens as a new document.
- **Sidebar** (☰): an outline that follows your reading, the folder's files with recents, and search across every file in the folder.
- **Reading**: six themes (System, Light, Dark, Sepia, Nord, High Contrast), three fonts, four page widths and text size, all in the **Aa** sheet. Also find in document, next and previous section, and the reading position is remembered for each document.
- **Editing**:
  - an editor with no autocorrect or smart quotes;
  - side-by-side Split view with scroll sync on tablets;
  - Save / Save as, with a "Save changes?" prompt before anything is discarded;
  - unsaved edits survive Android stopping the app in the background.
- **Live reload** when another app changes the open file. If you have unsaved edits, it asks first.
- **Relative images and links** work for documents inside a folder you've opened. For a single file, an "Allow access" bar asks for its folder once.
- **Print / Save as PDF**, **Export as HTML** (one standalone file, fonts embedded) and **Share**.
- **Private**: no INTERNET permission. Documents can't run scripts (CSP with a nonce, plus sanitising), and only the app's own engine files load.

## Build

```sh
export JAVA_HOME="/Applications/Android Studio.app/Contents/jbr/Contents/Home"
./gradlew assembleRelease        # app/build/outputs/apk/release/app-release.apk
./gradlew testDebugUnitTest      # heading/anchor parity with renderer.js, paths, encodings
```

Or open this folder in Android Studio. Toolchain: Gradle 9.8.0, AGP 9.4.1, Kotlin 2.4.20,
Compose BOM 2026.06.01, compile/target SDK 36, min SDK 26.

## How it fits together

| File | Role |
|---|---|
| `RendererPage.kt` | Builds the renderer page from the shared engine files, with its CSP and a bridge shim that gives `renderer.js` the `window.webkit.messageHandlers` it expects |
| `ResourceServer.kt` | Answers every request from the page: the page itself, Mermaid, KaTeX fonts, and document images (with byte ranges for media). It plays the part of the Mac app's `mdview-doc://` handler |
| `PreviewController.kt` | Owns the WebView: render, settings, find, outline jumps, scroll sync, print, HTML export |
| `DocumentStore.kt` | All document state and actions (port of `DocumentStore.swift`) |
| `Storage.kt` | Storage Access Framework: reading and writing, folders, search, relative paths, recents |
| `Models.kt` | Themes, widths, fonts, zoom, layouts, and the heading parser (port of `Models.swift`) |
| `ui/` | Welcome screen, document screen, sidebar, editor, appearance sheet |
| `assets/page.css` | The reader stylesheet from `MarkdownWebView.pageCSS`, plus phone tweaks |

`page.css` is a copy of the Mac app's `pageCSS`. If that changes, copy it across, or move it into
`MDView/Resources` so both apps read the same file.
