<div align="center">

# DocVault

**Private document scanner and vault for Android.**
Scan, extract text, search, export. Everything stays on your phone.

![Platform](https://img.shields.io/badge/Platform-Android%207.0%2B-3DDC84?style=flat-square&logo=android&logoColor=white)
![Kotlin](https://img.shields.io/badge/Kotlin-Jetpack%20Compose-7F52FF?style=flat-square&logo=kotlin&logoColor=white)
![Version](https://img.shields.io/badge/Version-1.0.0-5B61F6?style=flat-square)
![Offline](https://img.shields.io/badge/Works-Offline-10B981?style=flat-square)

[Features](#features) · [Install](#install) · [Security](#security-model) · [Build](#build-from-source) · [Architecture](#architecture) · [Roadmap](#roadmap)

</div>

---

## What it does

1. **Scan** a document with automatic edge detection and perspective correction. Multi-page supported.
2. **Extract** the text with on-device OCR. Edit it before saving.
3. **Store** it in an encrypted local vault.
4. **Find** it later with full-text search across names and content.
5. **Export** to PDF or TXT and share.

No account. No server. No analytics. Nothing leaves the device unless you export it.

---

## Features

| Area | What you get |
|---|---|
| **Scanning** | ML Kit Document Scanner: auto edge detection, crop, cleanup, up to 20 pages per document |
| **OCR** | ML Kit Text Recognition, fully on-device. Per-page text, editable before save |
| **Vault** | Thumbnails, page count, text snippet, search, sort (newest / oldest / A-Z / Z-A), rename, delete |
| **Viewer** | Document View and Text View tabs, page thumbnail strip, selectable text |
| **PDF export** | Image-only, or images plus extracted text as a final page. Images downsampled to 1600px to keep files small |
| **TXT export** | Plain text of the extracted content |
| **Exports** | Files saved to `Downloads/DocVault`, visible in any file manager. In-app list with ALL / PDF / TXT filter, share, delete |
| **Home** | Live stats (documents, exports, storage, scanned today) and recent documents |
| **Settings** | Export defaults, storage usage, clear exports, delete all documents |
| **UI** | Dark Material 3 theme, glass-style cards, pill bottom navigation, animated scanner hand-off |

---

## Install

### From a release APK

1. Open the **Releases** page of this repository.
2. Download `docvault-1.0.0.apk` from the latest release.
3. Open the file on your phone. Allow **Install unknown apps** for your browser or file manager when asked.
4. Launch DocVault.

**Requirements**
- Android 7.0 (API 24) or newer
- Google Play services installed (the ML Kit document scanner is delivered through it)

**Auto-updates:** add this repository's URL to [Obtainium](https://github.com/ImranR98/Obtainium) and new releases install from there.

---

## Security model

| Data | Protection | Notes |
|---|---|---|
| Document metadata and OCR text | SQLCipher AES-256 | Whole database encrypted at rest |
| Database passphrase | Android Keystore AES-256/GCM | Random per install, never in source, never stored in plaintext |
| Page images | App-private storage | Protected by the Android app sandbox. **Not** encrypted at file level |
| Exported PDF / TXT | Plain files in `Downloads/DocVault` | Readable by other apps with storage access. Delete them from the Exports tab when done |
| Network | None | The scanner and OCR run on-device. No analytics, no ads, no telemetry |

**Known limits in v1**
- There is no app lock. Anyone who can unlock your phone can open the app.
- Image files are not individually encrypted. A rooted device or physical extraction could read them.
- Cloud sync and account login are not part of v1.

---

## Build from source

**Requirements:** Android Studio (current stable), JDK 17, Android SDK 36.

```bash
git clone https://github.com/<your-username>/DocVault.git
cd DocVault
./gradlew assembleDebug
```

Debug APK output: `app/build/outputs/apk/debug/app-debug.apk`

### Signed release build

1. Create a keystore once and back it up. Losing it means you cannot ship updates that install over existing ones.
   ```bash
   keytool -genkey -v -keystore docvault-release.jks -keyalg RSA -keysize 2048 -validity 10000 -alias docvault
   ```
2. Android Studio: **Build > Generate Signed App Bundle / APK > APK > release**.
3. Output: `app/release/app-release.apk`.
4. Never commit the `.jks` file or its passwords.

---

## Architecture

```
MVVM + Repository

UI (Compose screens)
   -> ViewModel (StateFlow)
      -> Repository
         -> Room DAO -> SQLCipher database
```

```
com.example.documentscanner/
├── data/
│   ├── dao/               DocumentDao
│   ├── database/          DocumentDatabase (Room + SQLCipher)
│   └── entity/            ScannedDocument, pageList()
├── domain/repository/     DocumentRepository
├── ui/
│   ├── components/        GlassmorphicCard, DocVaultIcon
│   ├── navigation/        NavGraph, BottomNavBar
│   ├── screens/           Home, Camera, Detail, DocumentList, DocumentView, Exports, Settings
│   ├── theme/             Color, Theme
│   └── viewmodel/         DocumentViewModel, DocumentListViewModel
└── utils/
    ├── DocumentScannerLauncher   ML Kit scanner config
    ├── OCRProcessor              ML Kit text recognition
    ├── KeystoreManager           Keystore-backed passphrase
    ├── ExportManager             PDF / TXT to Downloads via MediaStore
    └── ShareUtils                FileProvider share sheet
```

### Stack

| Layer | Library |
|---|---|
| Language | Kotlin, Coroutines, Flow |
| UI | Jetpack Compose, Material 3, Navigation Compose, Coil |
| Scan / OCR | ML Kit Document Scanner, ML Kit Text Recognition |
| Storage | Room, SQLCipher, Android Keystore |
| Export | `PdfDocument`, MediaStore, FileProvider |
| Min / target SDK | 24 / 36 |

---

## Roadmap

**Shipped in v1.0**
- [x] Multi-page scan with edge detection
- [x] On-device OCR with editing
- [x] Encrypted local vault, search, sort, rename
- [x] PDF and TXT export to `Downloads/DocVault`
- [x] Dashboard, exports manager, settings

**Planned**
- [ ] Optional account login with passkeys
- [ ] Optional cross-device sync (Supabase backend scaffolded, not enabled)
- [ ] App lock backed by a biometric-gated Keystore key
- [ ] Encrypt page images at file level
- [ ] Multi-select and merge into one PDF
- [ ] Tags and folders
- [ ] Performance pass with Perfetto traces (launch time, screen transitions)

---

## License

Add a `LICENSE` file before publishing. MIT is a common choice for a portfolio project.

<div align="center">

Built with Kotlin and Jetpack Compose.

</div>