# PocketCode Workspace, Terminal, Agent, Editor

## Intent

Buat PocketCode terasa seperti IDE Android ringan: shell punya prefix internal bergaya Termux, agent tetap usable saat keyboard terbuka, workspace bisa dipilih dari storage system, drawer menampilkan tree proyek, dan tab Editor memberi pembacaan/edit teks monospace yang terstruktur.

## Goals

- `RealTerminalBackend` memakai `context.filesDir/usr` sebagai `$PREFIX`, membuat direktori runtime, dan mengirim environment shell yang konsisten.
- Agent memakai layout `Column` + riwayat scroll + input sticky di bawah; prompt user muncul sebelum output agent.
- Output kode/teks agent tampil dalam `Card` gelap, monospace, border tipis.
- Workspace dipilih melalui `Intent.ACTION_OPEN_DOCUMENT_TREE`; URI permission disimpan agar folder dapat dipakai ulang.
- Terminal memaksimalkan area konsol, mempertahankan input command satu baris dan shortcut kecil tanpa panel input besar.
- Drawer tree recursive dengan ikon dan warna berdasarkan tipe file.
- Navbar punya tab Agent, Terminal, Editor; Editor menampilkan file aktif dan highlighting keyword/string/comment sederhana.

## Non-goals

- PTY penuh dengan cursor/color escape interaktif.
- Parser syntax lengkap, autocomplete, multi-file tabs, undo/redo, atau language server.
- Migrasi seluruh file service ke Storage Access Framework URI; path lokal tetap dipakai untuk workspace yang dapat diakses sebagai `File`.
- Dependency baru.

## Anti-goals

- Jangan meminta `MANAGE_EXTERNAL_STORAGE` sebagai syarat utama folder picker.
- Jangan menjalankan filesystem scan besar berulang pada setiap recomposition.
- Jangan menutupi prompt user atau output streaming.

## Constraints

- Existing app Compose Material 3, minSdk 26, target SDK 34.
- Navigation saat ini integer tab di `Screens.kt`; pertahankan struktur ini.
- `WorkspaceFileService` dan `AgentEngine` mengharapkan root `File`.
- SAF URI tidak selalu punya filesystem path. Implementasi menyimpan URI dan memakai path filesystem bila URI mengarah ke lokasi lokal yang bisa di-resolve; bila tidak, tampilkan URI dan pertahankan workspace lama daripada menebak path.

## Approaches

1. **Native minimal, dipilih.** Gunakan environment `ProcessBuilder`, `ACTION_OPEN_DOCUMENT_TREE` + `takePersistableUriPermission`, Compose layout existing, recursive composable tree, `AnnotatedString` regex highlighting. Paling kecil dan tanpa dependency.
2. **Path langsung.** Lebih sedikit state, tetapi rapuh terhadap scoped storage Android modern dan tidak memenuhi pemilihan directory system.
3. **Editor penuh + abstraction URI.** Lebih kuat jangka panjang, tetapi scope jauh melebihi kebutuhan dan perlu model dokumen/file provider baru.

## Design

### Terminal backend

Ubah constructor agar menerima `Context` atau menerima root prefix yang dibuat caller; pilih bentuk paling kecil yang memungkinkan backend membuat `context.filesDir/usr`. Pada init, buat `bin`, `lib`, `etc`, `tmp`, `var`, `home`, dan root prefix. Set environment:

- `HOME=/sdcard`
- `PREFIX=<filesDir>/usr`
- `PATH=<PREFIX>/bin:/system/bin:/system/xbin`
- `LD_LIBRARY_PATH=<PREFIX>/lib`

Shell tetap `/system/bin/sh`; working directory memakai workspace valid atau prefix fallback. `execute` tetap blocking dan dipanggil dari IO dispatcher.

### Agent

`LazyColumn` hanya memegang riwayat. Setiap submit menambah item user lebih dulu, lalu streaming agent menggabungkan output ke item agent aktif. Input berada di `bottomBar`/slot bawah `Scaffold` dan memakai `imePadding` agar tetap terlihat di atas keyboard. Blok output memakai dark `Card`, thin border, `FontFamily.Monospace`; teks biasa tetap readable.

### Workspace picker

Tambahkan launcher `rememberLauncherForActivityResult(ActivityResultContracts.OpenDocumentTree())`. Pada hasil, persist read/write URI permission, simpan URI string baru di `SettingsRepository`, dan refresh drawer/settings. Path text field tetap tersedia untuk kompatibilitas. Picker dibuka dari drawer dan Settings.

### Terminal screen

Gunakan root `Column` dengan header kecil, output `LazyColumn` berbobot penuh, lalu command row ringkas dan shortcut horizontal. Hindari `imePadding` pada seluruh layar; pasang hanya pada command row supaya output tidak ikut melompat saat keyboard muncul.

### Drawer

Recursive `TreeNode`/composable memakai `File.listFiles`, filter `.git` dan `node_modules`, sort folder dulu lalu nama. State folder terbuka keyed by absolute path. Icon mapping berdasarkan extension/name: folder, `.kt`, `.java`, `.ts/.tsx`, `.js/.jsx`, `.css/.scss`, `.html`, `.json`, `.md`, `.git*`, default file. Warna icon membedakan kelompok tipe.

### Editor

Buat `ui/EditorScreen.kt`. Pilih file aktif dari workspace tree atau default file pertama yang terbaca. Header menampilkan nama file/path. `OutlinedTextField` multiline monospace berisi teks; overlay/transform `AnnotatedString` memberi warna keyword, string, dan komentar tanpa parser penuh. Simpan perubahan ke `WorkspaceFileService` lewat tombol Save. Editor tab menerima workspace path dari `MainScaffold`.

## Verification

- `./gradlew :app:assembleDebug`.
- `./gradlew :app:testDebugUnitTest`.
- Inspect compiled source for required env keys, picker contract, third tab, recursive tree, sticky bottom layout.
- Manual Android check: open picker from `/sdcard`, type prompt with keyboard open, submit and observe prompt before stream, run terminal command, expand nested folders, open/save editor file.

## Builder calls

- Keep diff confined to backend, settings, Screens, new EditorScreen, and only required manifest/import changes.
- Preserve existing agent approval behavior.
- Treat unavailable SAF-to-File conversion as explicit limitation; do not silently map arbitrary URI to unsafe path.
