# TODO

## Build and Release

- [ ] Jalankan `./gradlew assembleDebug` pada JDK 17 + Android SDK 34.
- [ ] Push ke branch `main` dan verifikasi GitHub Actions menghasilkan `pocketcode-debug-apk`.
- [ ] Tambahkan signing config hanya jika release APK dibutuhkan.

## Agent Backend

- [ ] Hubungkan `Base URL`, `API Key`, dan `Active Model` ke application state atau DataStore.
- [ ] Implementasikan request LLM streaming memakai OkHttp SSE.
- [ ] Tambahkan loading, success, timeout, HTTP error, dan retry states.
- [ ] Ganti dummy Agent response dengan response API nyata.
- [ ] Implementasikan diff model, preview, apply, discard, dan error rollback.
- [ ] Minta approval sebelum tool yang bukan read-only berjalan.

## Terminal

- [ ] Implementasikan `TerminalBackend` dengan backend nyata.
- [ ] Pilih integrasi Termux yang valid setelah artifact/repository resmi terverifikasi.
- [ ] Hubungkan `Require approval for terminal` ke command execution.
- [ ] Tambahkan process lifecycle, cancellation, exit code, stderr, dan output streaming.
- [ ] Tambahkan command history yang persisten bila diperlukan.

## Workspace and Storage

- [ ] Ganti path dummy dengan file tree nyata.
- [ ] Implementasikan folder picker atau SAF.
- [ ] Tentukan apakah `MANAGE_EXTERNAL_STORAGE` benar-benar diperlukan; hapus permission luas jika tidak.
- [ ] Persist recent sessions.
- [ ] Implementasikan create/resume session.

## Settings and Quality

- [ ] Persist settings dengan DataStore.
- [ ] Tambahkan validasi URL dan API key.
- [ ] Tambahkan unit test untuk command parser dan settings validation.
- [ ] Tambahkan Compose UI tests untuk tab switch, drawer, diff action, terminal commands, dan storage permission action.
- [ ] Jalankan lint, compile, dan test di CI.

## Design Reference

- [ ] Isi `design_reference.html` dengan desain final jika source HTML tersedia.
- [ ] Reconcile token/layout Compose terhadap HTML final.
