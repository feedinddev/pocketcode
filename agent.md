# Agent Guide

## Project

PocketCode: aplikasi Android native Kotlin + Jetpack Compose untuk coding workspace dengan Agent, Terminal, Workspace Drawer, dan Settings.

## Stack

- Android SDK 34
- Kotlin 1.9.22
- Jetpack Compose Material 3
- Navigation Compose
- OkHttp 4.12.0 + okhttp-sse 4.12.0
- Gradle 8.2
- JDK 17

## Source Layout

- `app/src/main/java/com/pocketcode/app/MainActivity.kt`: entry point, edge-to-edge, system bars.
- `app/src/main/java/com/pocketcode/app/ui/Screens.kt`: Compose screens and local UI state.
- `app/src/main/java/com/pocketcode/app/ui/theme/`: color, typography, Material theme.
- `app/src/main/java/com/pocketcode/app/terminal/TerminalBackend.kt`: seam untuk backend terminal nyata atau Termux.
- `app/src/main/AndroidManifest.xml`: permissions and launcher activity.
- `.github/workflows/build-apk.yml`: debug APK CI build.

## Design Rules

- Gunakan Apple Dark palette:
  - Pure black `#000000`
  - Grouped surface `#1C1C1E`
  - Elevated surface `#2C2C2E`
  - Hairline `#38383A`
  - Blue `#0A84FF`
  - Green `#30D158`
  - Red `#FF453A`
- UI text memakai `FontFamily.SansSerif`.
- Kode, path, log, dan terminal memakai `FontFamily.Monospace`.
- Pertahankan accessibility: content description untuk icon actions, kontras, keyboard handling.
- Pertahankan state Compose lokal sampai state holder/application layer dibuat.

## Workflow

1. Baca `PROGRES.md` dan `TODO.md` sebelum mengubah kode.
2. Baca `design_reference.html`; file saat ini kosong, jadi spesifikasi user menjadi fallback source of truth.
3. Ubah file sesedikit mungkin.
4. Jangan menambah dependency baru tanpa kebutuhan jelas.
5. Jalankan `./gradlew assembleDebug` setelah perubahan jika JDK 17 dan Android SDK tersedia.
6. Jika build tidak bisa jalan, catat command dan error exact di `PROGRES.md`.
7. Jangan mengklaim terminal, Agent API, diff apply, atau storage sebagai fitur nyata jika masih mock.

## Build

```bash
chmod +x gradlew
./gradlew assembleDebug
```

APK output:

```text
app/build/outputs/apk/debug/app-debug.apk
```

CI memakai Ubuntu runner, JDK 17, Android SDK platform/build-tools 34, dan upload artifact `pocketcode-debug-apk`.

## Change Policy

- Jangan menghapus permission atau dependency yang diminta user tanpa mencatat alasan.
- Termux artifacts tidak dipakai langsung karena koordinat JitPack tidak terbukti resolvable; gunakan `TerminalBackend` seam untuk integrasi berikutnya.
- Untuk perubahan database/backend/API, tambahkan error state dan validasi input.
- Untuk perubahan UI, pertahankan dark visual system dan state interaction yang sudah ada.
