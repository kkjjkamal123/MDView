# Changelog

All notable changes to MDView for Mac.

## 1.3.0 (build 2) — 2026-11-06

### New
- **Command palette** (⇧⌘P): fuzzy search over every command, the document's
  headings, recent documents and the files in the open folder.
- **Back and Forward** (⌘[ / ⌘], toolbar buttons) between documents shown in a
  window, returning to the section you were reading. Links such as
  `notes.md#setup` open the file at that heading.
- **Find options**: Match Case, Whole Words and Regular Expression, with a
  "3 of 12" match count and an "Invalid pattern" hint. Matches are drawn with
  the CSS Custom Highlight API, so finding never changes or relayouts the
  document; highlights stay current while you edit in Split layout.
- ⌘F / ⌘G / ⇧⌘G search the source in the Editor layout.
- **File ▸ Open Recent** menu.
- **Live reload**: a file changed by another app updates immediately; with
  unsaved edits a bar offers Reload or Keep My Changes. The open folder's file
  list follows additions and removals too.
- **Reading position** is remembered per document.
- **Export as HTML** (⌥⇧⌘E): one self-contained file — local images are
  embedded, diagrams drawn, maths fonts included, no scripts.
- Extended Markdown: GitHub alerts, `:::` admonitions, footnotes, YAML front
  matter card, `==mark==`, `++ins++`, `~sub~`, `^sup^`, document-wide
  `\newcommand` macros and `\ce{}` chemistry.
- Plain-text editor without smart quotes or dash substitution, with
  editor-to-preview scroll sync in Split layout.
- Local video and audio play (byte-range responses); media never autoplays.
- Next / Previous Section (⌥⌘↓ / ⌥⌘↑).

### Fixed
- Closing a window with unsaved edits discarded them silently. The window now
  asks to save (close button and ⌘W), and Quit no longer asks again about a
  window you already closed.
- ⌘W did nothing: replacing the File menu's save group had removed the
  standard Close item. *File ▸ Close Window* (⌘W) is back.
- Printing and PDF export could fail under the App Sandbox: the app was missing
  the printing entitlement.
- Undo after a document was reloaded from disk could apply edits to the wrong
  text, and undo could bring back the previous document's text after opening
  another file. A reload is now one undoable step and each document starts with
  a fresh undo history.
- Recent documents were per window: two windows overwrote each other's list.
  The list is now shared and always up to date.
- Folder search kept scanning for every superseded query; it now stops, skips
  package contents and `node_modules`, and no longer piles up work while typing.
- Two Swift concurrency warnings (main-actor functions passed as nonisolated
  closures).

### Security
- Raw HTML using `class="math"` could skip the HTML sanitiser (the sanitiser
  trusted that class to mark KaTeX output). Typeset maths is now marked with a
  per-page secret instead.
- The Content Security Policy allowed any `mdview-doc:` script, including files
  next to the document; only the bundled diagram engine may load now.
- SVG `<animate>`/`<set>` values containing `javascript:` URLs are stripped.
- A link in a document to an application, script or other executable is
  revealed in Finder instead of being launched with one click.
- KaTeX updated from 0.16.8 to 0.16.47 (with the matching mhchem), fixing
  CVE-2024-28243 and CVE-2024-28244 (a document could hang the preview with
  runaway macro expansion).

### Performance
- Rendering is incremental: an edit re-renders only the changed blocks; maths
  is typeset once and cached; Mermaid (3 MB) loads only for documents with a
  diagram and draws diagrams as they come into view; off-screen blocks skip
  layout and paint; file watching uses kernel events, not polling.
- Open-ended media range requests are answered in 8 MB chunks instead of
  reading a whole film into memory.
- Idle with a 200-section maths document open: about 0.15 % CPU for the app and
  0.2 % for its web content process (measured over 60 s).

## 1.2.0 — 2026-09-27

- Native SwiftUI app for macOS: sidebar with outline, files and folder search;
  preview, split and editor layouts; six themes; find; export as PDF; print.
