# PocketCode

PocketCode adalah IDE Android ringan berbasis Kotlin dan Jetpack Compose untuk bekerja dengan workspace proyek, Agent AI, terminal shell, dan editor kode.

## Status

**Fase saat ini: UI dan fondasi runtime selesai; validasi build lokal tertahan karena environment belum memiliki Java.**

Fitur yang sudah tersedia:

- Agent chat dengan streaming response, riwayat prompt, thinking pulse, approval card, dan Copy untuk blok kode.
- Prompt bar sticky di bawah layar dan aman saat keyboard terbuka.
- Terminal shell berbasis `/system/bin/sh` dengan environment bergaya Termux.
- Prefix internal di `context.filesDir/usr`.
- Workspace picker memakai `ACTION_OPEN_DOCUMENT_TREE` dan persistable URI permission.
- Workspace drawer recursive dengan ikon dan warna berdasarkan tipe file.
- Model selector scrollable, bounded, dan clickable.
- Editor monospace dengan:
  - file tabs;
  - close tab;
  - active tab indicator;
  - breadcrumb path;
  - line numbers;
  - highlighting keyword, string, comment, function, dan braces;
  - save melalui `WorkspaceFileService`.
- Settings untuk endpoint AI, API key, model, workspace, dan approval guardrails.

## Stack

- Kotlin 2.0.21
- Android Gradle Plugin 8.5.2
- Gradle wrapper 8.7
- Android SDK 34
- `minSdk 26`, `targetSdk 34`
- Jetpack Compose BOM 2024.02.02
- Material 3
- OkHttp 4.12.0 + okhttp-sse
- Java target 17

## Struktur penting

```text
app/src/main/java/com/pocketcode/app/
├── MainActivity.kt
├── data/
│   ├── AgentEngine.kt
│   ├── SettingsRepository.kt
│   └── WorkspaceFileService.kt
├── terminal/
│   ├── RealTerminalBackend.kt
│   └── TerminalBackend.kt
└── ui/
    ├── DesignSystem.kt
    ├── EditorScreen.kt
    ├── Screens.kt
    └── theme/
```

## Terminal runtime

`RealTerminalBackend` membuat prefix internal berikut saat runtime:

```text
<filesDir>/usr/
├── bin/
├── lib/
├── etc/
├── tmp/
├── var/
└── home/
```

Environment shell:

```text
HOME=/sdcard
PREFIX=<filesDir>/usr
PATH=<filesDir>/usr/bin:/system/bin:/system/xbin
LD_LIBRARY_PATH=<filesDir>/usr/lib
```

Shell tetap `/system/bin/sh`. Command dijalankan pada workspace aktif jika tersedia; jika tidak, shell memakai prefix internal.

## Menjalankan build

Environment wajib:

- JDK 17
- Android SDK 34
- `ANDROID_HOME` atau konfigurasi SDK Android yang valid

Perintah:

```bash
chmod +x gradlew
./gradlew :app:compileDebugKotlin --no-daemon
./gradlew :app:assembleDebug --no-daemon
./gradlew :app:testDebugUnitTest --no-daemon
```

APK debug:

```text
app/build/outputs/apk/debug/app-debug.apk
```

Validasi lokal terakhir tertahan oleh environment:

```text
ERROR: JAVA_HOME is not set and no 'java' command could be found in your PATH.
```

## Workspace dan SAF

Gunakan tombol **Pick Folder** dari drawer atau Settings untuk membuka system directory picker.

- URI permission disimpan di `SettingsRepository.workspaceUri`.
- Primary storage dapat dipetakan ke path `/storage/emulated/0/...`.
- Provider SAF non-primary tetap disimpan sebagai URI dan belum menjadi backend `DocumentFile` penuh.
- File operations tetap melewati `WorkspaceFileService` dengan canonical path containment.

## Batasan saat ini

- Belum ada PTY penuh; terminal memakai proses shell biasa.
- Editor belum punya undo/redo, autocomplete, language server, atau parser syntax lengkap.
- Highlighting editor berbasis regex, bukan parser bahasa.
- Agent backend dan SSE perlu diuji pada endpoint nyata.
- Build, lint, unit test, dan runtime Android belum tervalidasi pada workspace ini karena JDK belum tersedia.
- SAF provider non-primary belum didukung penuh untuk operasi file langsung.

## Roadmap berikutnya

1. Sediakan JDK 17 dan Android SDK 34, lalu jalankan compile, lint, test, dan assemble.
2. Perbaiki error compile/runtime dari hasil CI atau device test.
3. Uji picker SAF pada internal storage dan provider non-primary.
4. Tambahkan test untuk terminal environment, path containment, settings, dan Agent state.
5. Lengkapi SSE Agent: timeout, retry, HTTP error, cancellation, dan streaming lifecycle.
6. Tambahkan PTY atau integrasi Termux resmi setelah artifact tervalidasi.
7. Persist settings dan session dengan storage yang lebih kuat bila kebutuhan bertambah.

## Dokumentasi kerja

- `PROGRES.md` — status implementasi dan pekerjaan berikutnya.
- `TODO.md` — backlog teknis.
- `docs/superpowers/specs/` — catatan desain perubahan besar.
- `agent.md` — aturan kerja repository.
