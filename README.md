
<div align="center">

# 🔐 DocVault

### Secure Document Scanner & Vault for Android

[![Android](https://img.shields.io/badge/Platform-Android-3DDC84?style=for-the-badge&logo=android&logoColor=white)](https://android.com)
[![Kotlin](https://img.shields.io/badge/Language-Kotlin-7F52FF?style=for-the-badge&logo=kotlin&logoColor=white)](https://kotlinlang.org)
[![Jetpack Compose](https://img.shields.io/badge/UI-Jetpack%20Compose-4285F4?style=for-the-badge&logo=jetpackcompose&logoColor=white)](https://developer.android.com/jetpack/compose)
[![Supabase](https://img.shields.io/badge/Backend-Supabase-3ECF8E?style=for-the-badge&logo=supabase&logoColor=white)](https://supabase.com)
[![License](https://img.shields.io/badge/License-MIT-yellow?style=for-the-badge)](LICENSE)

**Scan. Extract. Secure. Sync.**

*A privacy-first document management app with on-device OCR, AES-256 encryption, and optional cross-device cloud sync via Google Passkey.*

[Features](#-features) • [Architecture](#-architecture) • [Screenshots](#-screenshots) • [Setup](#-setup) • [Security](#-security) • [Roadmap](#-roadmap)

---

</div>

## ✨ Features

### 📸 Document Scanning
- **ML Kit Document Scanner** — real edge detection, perspective correction, auto-cleanup
- **Multi-page capture** — scan multi-page documents as a single vault entry
- **Animated scanner transition** — smooth branded loading before scanner opens

### 🔍 OCR Text Extraction
- **On-device ML Kit OCR** — 100% offline, no data sent to servers
- **Editable OCR text** — correct extraction errors before saving
- **Multi-page text extraction** — extracts and labels text per page
- **Full-text search** — search across all document content and filenames

### 🔐 Security & Privacy
- **SQLCipher AES-256** — entire database encrypted at rest
- **Android Keystore** — database passphrase generated and protected by hardware-backed AES-256/GCM key
- **App-private storage** — documents inaccessible to other apps
- **100% offline capable** — works with zero internet connection

### ☁️ Cloud Sync (Optional)
- **Google Passkey login** — fingerprint / face / PIN, no password, no OTP
- **Supabase backend** — PostgreSQL + encrypted storage
- **User-controlled sync** — toggle sync on/off, sync on demand
- **Cross-device access** — documents available on all signed-in devices

### 📤 Export
- **PDF export** — image only, or image + OCR text as final page
- **TXT export** — plain text of extracted content
- **Saves to Downloads/DocVault** — visible in Files app
- **Android share sheet** — share to WhatsApp, email, Drive, etc.

### 🎨 UI/UX
- **Dark glassmorphism design** — Electric Indigo `#5B61F6` + Emerald `#10B981`
- **Material 3** — ElevatedCards, SecondaryTabRow, NavigationBar
- **Pill bottom navigation** — Home / Vault / Exports / Profile
- **Dashboard home screen** — live stats + recent documents

---

## 🏗️ Architecture

```
DocVault/
├── data/
│   ├── dao/                    # Room DAO queries
│   ├── database/               # SQLCipher-encrypted Room DB
│   └── entity/                 # ScannedDocument entity + pageList()
├── domain/
│   └── repository/             # DocumentRepository
├── ui/
│   ├── components/             # GlassmorphicCard, DocVaultIcon
│   ├── navigation/             # NavGraph, BottomNavBar
│   ├── screens/                # All screen composables
│   ├── theme/                  # Color, Theme (Material 3)
│   └── viewmodel/              # DocumentViewModel, DocumentListViewModel
└── utils/
    ├── AuthRepository          # Google Sign-In session management
    ├── DocumentScannerLauncher # ML Kit scanner config
    ├── ExportManager           # PDF + TXT export to Downloads
    ├── KeystoreManager         # Android Keystore AES-256/GCM
    ├── OCRProcessor            # ML Kit text recognition
    ├── ShareUtils              # FileProvider share sheet
    ├── SupabaseManager         # Supabase client + session persistence
    └── SyncRepository          # Document upload/fetch/delete
```

### Pattern
```
MVVM + Repository
UI (Compose) → ViewModel (StateFlow) → Repository → Room / Supabase
```

---

## 📱 Screens

| Screen | Description |
|--------|-------------|
| **Home** | Dashboard with live stats (docs, exports, storage, today's scans) + recent documents |
| **Camera** | ML Kit document scanner with edge detection and multi-page capture |
| **Detail** | Post-scan review — image preview, OCR extraction, editable text, save to vault |
| **Vault (DocumentList)** | Searchable, sortable document list with thumbnail previews and overflow menu |
| **DocumentView** | Read-only viewer — tabbed Document View / Text View with export actions |
| **Exports** | All exported PDFs and TXTs with filter (ALL/PDF/TXT), share and delete |
| **Profile** | Google Sign-In, cloud sync toggle, sync-now, sign out |
| **Settings** | Biometric lock toggle, auto-lock timer, export defaults, storage info, about |

---

## 🔒 Security

```
┌─────────────────────────────────────────────────┐
│                   DocVault                       │
│                                                  │
│  Photos ──→ App-private storage (sandbox)        │
│                                                  │
│  Metadata ──→ Room + SQLCipher (AES-256)         │
│                    ↑                             │
│              Passphrase                          │
│                    ↑                             │
│         Android Keystore Key (AES-256/GCM)       │
│         [Hardware-backed, never leaves device]   │
└─────────────────────────────────────────────────┘
```

| Layer | Method | Strength |
|-------|--------|----------|
| Database | SQLCipher AES-256 | ✅ Strong |
| Passphrase storage | Android Keystore AES-256/GCM | ✅ Hardware-backed |
| File storage | App sandbox | ⚠️ Sandbox only |
| Auth | Google Passkey (FIDO2) | ✅ No password/OTP |
| Transport | HTTPS (Supabase) | ✅ TLS |

---

## 🛠️ Tech Stack

| Category | Technology |
|----------|-----------|
| Language | Kotlin |
| UI | Jetpack Compose + Material 3 |
| Camera/Scan | ML Kit Document Scanner |
| OCR | ML Kit Text Recognition (on-device) |
| Database | Room + SQLCipher |
| Encryption | Android Keystore + Tink |
| Auth | Google Credential Manager (Passkey) |
| Backend | Supabase (Auth + PostgreSQL + Storage) |
| Navigation | Jetpack Navigation Compose |
| Image loading | Coil |
| Async | Kotlin Coroutines + Flow |
| Min SDK | API 24 (Android 7.0) |

---

## ⚙️ Setup

### Prerequisites
- Android Studio Hedgehog or later
- Android device / emulator API 24+
- Google Cloud Console account (for Sign-In)
- Supabase account (for cloud sync)

### 1. Clone
```bash
git clone https://github.com/yourusername/DocVault.git
cd DocVault
```

### 2. Supabase Setup
1. Create project at [supabase.com](https://supabase.com)
2. Run the SQL in `supabase/schema.sql` in the SQL Editor
3. Enable Google provider: **Authentication → Providers → Google**
4. Copy your `Project URL` and `anon key`
5. Update `SupabaseManager.kt`:
```kotlin
supabaseUrl = "YOUR_PROJECT_URL"
supabaseKey = "YOUR_ANON_KEY"
```

### 3. Google Sign-In Setup
1. Go to [console.cloud.google.com](https://console.cloud.google.com)
2. Create OAuth 2.0 credentials (Web + Android)
3. Add redirect URI: `https://YOUR_PROJECT.supabase.co/auth/v1/callback`
4. Update `AuthRepository.kt`:
```kotlin
private const val WEB_CLIENT_ID = "YOUR_WEB_CLIENT_ID"
```

### 4. Build
```bash
./gradlew assembleDebug
```

---

## 🗄️ Database Schema

```sql
-- User profiles (auto-created on signup)
profiles (id, email, created_at)

-- Document metadata
documents (
  id, user_id, local_id,
  file_name, extracted_text,
  date_created, date_modified, page_count
)
```

Row Level Security enabled — users can only access their own data.

---

## 📦 Key Dependencies

```kotlin
// Supabase
implementation("io.github.jan-tennert.supabase:auth-kt:3.1.4")
implementation("io.github.jan-tennert.supabase:postgrest-kt:3.1.4")
implementation("io.github.jan-tennert.supabase:storage-kt:3.1.4")
implementation("io.github.jan-tennert.supabase:compose-auth:3.1.4")

// ML Kit
implementation("com.google.mlkit:text-recognition:16.0.0")
implementation("com.google.android.gms:play-services-mlkit-document-scanner:16.0.0-beta1")

// Room + SQLCipher
implementation("androidx.room:room-runtime:2.7.0")
implementation("net.zetetic:android-database-sqlcipher:4.5.4")

// Google Passkey
implementation("androidx.credentials:credentials:1.3.0")
implementation("com.google.android.gms:play-services-auth:21.2.0")
```

---

## 🗺️ Roadmap

- [x] Camera capture with ML Kit edge detection
- [x] On-device OCR text extraction
- [x] AES-256 encrypted local database
- [x] Multi-page document support
- [x] PDF + TXT export to Downloads
- [x] Dark glassmorphism UI
- [x] Google Passkey authentication
- [x] Supabase cloud sync (optional)
- [x] Sort, rename, search documents
- [x] Settings screen
- [ ] Batch operations (multi-select, merge PDFs)
- [ ] Per-document encryption (image files)
- [ ] Document categories / tags
- [ ] Widget for quick scan
- [ ] WebAuthn passkey (native, no Google dependency)
- [ ] iPad / tablet layout

---

## 🤝 Contributing

1. Fork the repo
2. Create a feature branch: `git checkout -b feature/your-feature`
3. Commit: `git commit -m "Add your feature"`
4. Push: `git push origin feature/your-feature`
5. Open a Pull Request

---

## 📄 License

```
MIT License — see LICENSE file for details
```

---

<div align="center">

Built with ❤️ using Kotlin + Jetpack Compose

**Secure by default. Synced by choice.**

</div>
EOF
echo "Done"
