# MDView for Android: build plan

> **Status (18 Oct 2026): built and tested.** All 8 build steps are done. The release APK is `release/MDView-1.2.0.apk` (5.3 MB).
> Tested on an Android 16 emulator:
> - rendering (maths, Mermaid, code, tables, callouts, front matter);
> - folder open, relative images, links and anchors between notes;
> - outline, themes, editor and save, live reload, find, folder search;
> - print / PDF, share-to-MDView, and the release (R8) build.
>
> Not yet tried on the emulator: Export as HTML, Split view on a tablet, opening from the Files app.

Goal: the same offline Markdown reader as MDView for Mac, as a native Android app.
It should render documents the same way, look at home on a phone and scale up to tablets.

## 1. Approach

The Mac app is SwiftUI around a WKWebView, and that web view runs a self-contained JavaScript renderer
(`MDView/Resources`: marked, KaTeX, mhchem, highlight.js, Mermaid, plus MDView's own
`renderer.js`, `math-protect.js` and `markdown-extensions.js`).

The Android app keeps **that exact renderer**. Gradle uses `../MDView/Resources` directly as the
app's assets folder, so both apps always ship the same engine. Rendering bugs get fixed once, for both.
Everything around the renderer is rewritten natively in **Kotlin + Jetpack Compose (Material 3)**.

| Mac (Swift) | Android (Kotlin) |
|---|---|
| `WKWebView` + `WKScriptMessageHandler` | `WebView` + `@JavascriptInterface` bridge. A tiny shim defines `window.webkit.messageHandlers`, so `renderer.js` runs unchanged |
| `mdview-doc://` scheme handler | `shouldInterceptRequest` on `https://appassets.androidplatform.net/{engine,relative,absolute}/…`, with byte ranges for video and audio |
| Content-Security-Policy with a script nonce | Same policy, same nonce scheme |
| `NSOpenPanel` / security-scoped bookmarks | Storage Access Framework (`OPEN_DOCUMENT`, `OPEN_DOCUMENT_TREE`, `CREATE_DOCUMENT`) with persisted URI permissions |
| `FileWatcher` (kqueue) | `ContentObserver` on the document, plus a re-check whenever the app returns to the foreground |
| `NSPrintOperation` → PDF | `PrintManager` + `WebView.createPrintDocumentAdapter` (Print or "Save as PDF") |
| `@AppStorage` / `UserDefaults` | `SharedPreferences` behind Compose state |
| `NSTextView` editor | `EditText` (no autocorrect, no smart quotes) in `AndroidView`, with scroll sync to the preview |
| `NavigationSplitView` sidebar | Modal drawer on phones, permanent side panel on tablets and foldables (≥ 840 dp) |

## 2. Feature list (parity with Mac 1.2.0)

**Rendering** (comes free with the shared engine). All maths syntaxes, AMS environments, mhchem and macros.
Mermaid loads on demand. Syntax highlighting, callouts, front-matter card, footnotes, task lists,
highlight, insert, sub and sup, sanitised HTML, local images, video and audio.

**Screens**
1. **Welcome**: icon, version, New document, Open…, Open folder…, Feature tour, recent documents.
2. **Document**
   - Top bar: title, "Edited" marker, layout switch (Preview / Editor, plus Split on wide screens), Find, Appearance and a ⋮ menu.
   - The ⋮ menu has Save, Save as, Export PDF, Export HTML, Print, Share and Close.
   - Bottom status bar: words, reading time, number of headings, text size.
3. **Sidebar** (drawer): **Outline** (follows scrolling, filter box), **Files** (open folder + recents, filter),
   **Search** (every Markdown file in the folder, results grouped by file with line numbers; tapping one
   opens the file and highlights the match).
4. **Appearance sheet**: theme (System, Light, Dark, Sepia, Nord, High Contrast), font (Sans, Serif, Mono),
   page width (4 sizes), text size (− / % / +, Reset).

**Behaviour**
- Find in document (WebView find, next and previous, match count).
- Live reload when the file changes. If there are unsaved edits, a "Reload / Keep my changes" bar appears instead.
- Relative images: if the folder isn't readable yet, an "Allow access" bar asks for it once and remembers the answer.
- Links to other `.md` files open in MDView. Web links open in the browser.
- Copy button on code blocks. It is always visible on touch screens, since there is no hover.
- Remembers the reading position (last heading) for each document, the last open document, and all settings.
- Asks Save / Don't save / Cancel before discarding edits (on Back, Close, New and Open).
- Opens `.md` files from other apps (Files, Drive, Gmail attachments, Telegram, …) through `VIEW` intents. Text shared to MDView opens as a new document.
- Back button: closes the drawer, then the find bar, then the document. On the welcome screen it exits.
- Privacy: no network permission at all. The app cannot make requests.

**Non-goals for v1**: the custom CSS themes folder, the command palette, tabs, and Word/PNG export
(those come from the Windows build).

## 3. Project layout

```
MDView/android/
  settings.gradle.kts, build.gradle.kts, gradle/libs.versions.toml, gradlew
  app/build.gradle.kts            assets.srcDirs("../../MDView/Resources") + src/main/assets
  app/src/main/assets/page.css    reader stylesheet (from MarkdownWebView.pageCSS) + mobile tweaks
  app/src/main/java/app/mdview/
    MainActivity.kt               intents, SAF launchers, back handling
    Models.kt                     themes/fonts/widths/zoom/layout, parseHeadings (port, unit-tested)
    Settings.kt                   persisted reader settings
    DocumentStore.kt              all document state and actions (ViewModel)
    Storage.kt                    SAF read/write, folders, search, relative-path resolution, recents
    RendererPage.kt               builds the renderer HTML once (CSP + nonce + bridge shim)
    ResourceServer.kt             serves engine files and document images (with byte ranges)
    PreviewController.kt          find, jump, scroll sync, print, PDF and HTML export
    MarkdownWebView.kt            Compose wrapper around WebView
    SampleContent.kt              feature tour
    ui/  Theme, WelcomeScreen, DocumentScreen, Sidebar, AppearanceSheet, Bars, Editor
```

## 4. Toolchain

- JDK: the JetBrains Runtime bundled with Android Studio.
- SDK: `/opt/homebrew/share/android-commandlinetools` (Platform 36, Build-Tools 36.0.0, emulator, Android 16 arm64 image).
- Gradle 9.8.0, AGP 9.4.1, Kotlin 2.4.20, Compose BOM 2026.09.00.
- minSdk 26 (Android 8.0), target/compile 36. The WebView updates through Play, so modern CSS such as
  `:has()`, `content-visibility` and `color-mix` works on old Android versions too.
- App id `app.mdview`, version 1.2.0 (same as Mac).

## 5. Build order

1. Gradle skeleton, assets wired to the shared Resources, renderer page and bridge. The feature tour renders.
2. Opening files (SAF + intents), recents, reading position, live reload.
3. Document screen: top bar, status bar, find, appearance sheet, themes.
4. Sidebar: outline (follows scrolling), folder files, folder search.
5. Editor and Split layout with scroll sync, then save / save as and the discard confirmation.
6. Relative images and media, the "Allow access" flow, and links between documents.
7. Print, PDF, HTML export and Share.
8. Icon, polish, release APK.

## 6. Testing

- JVM unit tests for the heading parser and anchors (they must match `renderer.js`).
- Headless emulator: push test documents (the feature tour, a maths-heavy document, a folder with images
  and links), drive the UI with `adb`, and take screenshots of every screen in light and dark.
- Check: maths, Mermaid and code render; outline jumps; find; theme switch; editing and saving; reload
  on external change; images after "Allow access"; PDF export; opening from the Files app.

## 7. Deliverables

- `MDView-1.2.0.apk` (debug-signed, installable by sideloading).
- Screenshots from the emulator.
- The project opens directly in Android Studio (`File ▸ Open ▸ MDView/android`).
