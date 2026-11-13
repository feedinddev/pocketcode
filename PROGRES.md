# Progres PocketCode

Tanggal pembaruan: 2026-11-13

## Status ringkas

**Fondasi runtime dan UI utama selesai. Validasi build lokal belum bisa dilakukan karena environment tidak memiliki JDK/Java.**

## Selesai

### Fondasi proyek

- Struktur Android native Kotlin + Jetpack Compose.
- Android SDK 34, target SDK 34, min SDK 26.
- Kotlin 2.0.21, AGP 8.5.2, Gradle 8.7, Java target 17.
- Compose Kotlin plugin 2.0; konfigurasi Compose Compiler legacy tidak dipakai.
- Material 3, Material Icons Extended, Navigation Compose.
- OkHttp dan okhttp-sse.
- GitHub Actions build debug APK.
- `MainActivity` memakai edge-to-edge dengan system bar hitam.
- Apple dark theme di `ui/theme/`.

### Agent

- Agent screen dengan streaming state dan status `Ready`/`Streaming`/`Error`.
- Riwayat prompt user sebagai bubble/card sebelum response.
- Response tersimpan per turn, tidak lagi bergabung menjadi satu output global.
- Thinking pulse saat Agent streaming.
- Output kode dibungkus dark card dengan border tipis dan font monospace.
- Tombol `Copy` untuk output kode memakai clipboard Compose.
- Approval card untuk command/diff tetap tersedia.
- Log inspect/index tetap tersedia.
- Prompt bar sticky di bawah.
- Prompt bar memakai `imePadding` agar tetap terlihat saat keyboard terbuka.

### Terminal backend

- `RealTerminalBackend` menggunakan `$PREFIX` di `context.filesDir/usr`.
- Environment shell:

  ```text
  HOME=/sdcard
  PREFIX=<filesDir>/usr
  PATH=<filesDir>/usr/bin:/system/bin:/system/xbin
  LD_LIBRARY_PATH=<filesDir>/usr/lib
  ```

- Runtime directories dibuat otomatis: `bin`, `lib`, `etc`, `tmp`, `var`, `home`.
- Fallback working directory memakai prefix internal.
- Shell memakai `/system/bin/sh`.
- Process cancel dan command history tersedia.

### Workspace dan storage

- `ACTION_OPEN_DOCUMENT_TREE` tersedia dari drawer dan Settings.
- Persistable URI permission disimpan.
- `workspaceUri` disimpan di `SettingsRepository`.
- Primary storage URI dapat dipetakan ke `/storage/emulated/0/...`.
- Workspace drawer recursive untuk folder bertingkat.
- File click membuka file di Editor.
- Ikon dan warna file tersedia untuk TypeScript, React, JavaScript, CSS, SCSS, JSON, Kotlin, Java, HTML, Markdown, dan Git.
- `WorkspaceFileService` tidak lagi meminta `MANAGE_EXTERNAL_STORAGE`.
- Canonical path containment tetap dipakai.

### Settings

- Endpoint AI, API key, model, workspace path.
- Approval toggles.
- Model list dibungkus Card dengan `heightIn(max = 200.dp)`.
- Daftar model vertical scroll.
- Model row clickable.
- Model terpilih langsung disimpan sebagai `activeModel`.
- Daftar model tertutup setelah pilihan dibuat.

### Editor

- Tab Editor di navbar utama.
- File tabs horizontal.
- Active tab indicator.
- Close tab.
- Buffer lokal per file path.
- Breadcrumb path.
- Line-number gutter.
- Syntax highlighting regex untuk keyword, string, comment, function, dan braces.
- Save eksplisit melalui `WorkspaceFileService`.
- Normalisasi path absolute/relative dan validasi batas workspace.
- File extension matching case-insensitive.

## Validasi yang sudah dilakukan

- `git diff --check` lulus.
- Source inspection untuk env terminal, Agent animation, clipboard, model selector, recursive tree, editor tabs, breadcrumb, line numbers, dan syntax highlighting lulus.

## Validasi tertahan

Perintah berikut belum dapat menyelesaikan karena Java tidak tersedia:

```bash
./gradlew :app:compileDebugKotlin --no-daemon
./gradlew :app:assembleDebug --no-daemon
./gradlew :app:testDebugUnitTest --no-daemon
```

Error:

```text
ERROR: JAVA_HOME is not set and no 'java' command could be found in your PATH.
```

Belum ada validasi runtime pada device/emulator.

## Batasan teknis

- Terminal belum PTY penuh; masih proses `/system/bin/sh` biasa.
- Editor belum mendukung undo/redo, autocomplete, language server, atau parser syntax lengkap.
- Syntax highlighting editor berbasis regex.
- SAF provider non-primary belum memakai `DocumentFile` penuh.
- Agent SSE, timeout, retry, cancellation, dan error lifecycle perlu diuji endpoint nyata.
- Filesystem scan drawer masih terjadi saat composition untuk folder yang dibuka.

## Next

### Prioritas 1 — Build dan device validation

- [ ] Sediakan JDK 17 dan Android SDK 34.
- [ ] Jalankan `./gradlew :app:compileDebugKotlin --no-daemon`.
- [ ] Jalankan `./gradlew :app:assembleDebug --no-daemon`.
- [ ] Jalankan `./gradlew :app:testDebugUnitTest --no-daemon`.
- [ ] Jalankan lint.
- [ ] Install APK ke device/emulator dan uji Agent, Terminal, SAF, Settings, drawer, dan Editor.

### Prioritas 2 — Stabilitas data dan storage

- [ ] Tambahkan test `RealTerminalBackend` untuk env dan directory creation.
- [ ] Tambahkan test `WorkspaceFileService` untuk traversal/symlink/path boundary.
- [ ] Tambahkan test `SettingsRepository`.
- [ ] Pindahkan persistence penting dari SharedPreferences ke DataStore bila diperlukan.
- [ ] Tambahkan dukungan `DocumentFile` untuk SAF provider non-primary.
- [ ] Pindahkan filesystem scan drawer ke worker/state cache.

### Prioritas 3 — Agent dan terminal production

- [ ] Validasi request SSE OkHttp pada endpoint nyata.
- [ ] Tambahkan timeout, retry, cancellation, HTTP error, dan reconnect state.
- [ ] Persist chat/session bila diperlukan.
- [ ] Tambahkan process lifecycle dan output streaming yang lebih kuat.
- [ ] Evaluasi PTY atau integrasi Termux resmi.
- [ ] Hubungkan approval terminal langsung ke semua execution path.

### Prioritas 4 — Editor

- [ ] Tambahkan dirty-state indicator per tab.
- [ ] Tambahkan undo/redo.
- [ ] Sinkronkan line-number scroll dengan editor scroll.
- [ ] Tambahkan parser syntax jika regex tidak cukup.
- [ ] Tambahkan autocomplete dan file search bila kebutuhan sudah jelas.

## File dokumentasi

- `README.md` — cara memahami, membangun, dan memakai proyek.
- `TODO.md` — backlog teknis lama.
- `docs/superpowers/specs/` — desain perubahan besar.
- `agent.md` — aturan kerja repository.
