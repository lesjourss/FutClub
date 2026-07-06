# FutClub - Android App (Java)

Aplikasi komunitas olahraga untuk UAS Mobile Programming (PG119) - Kelompok AL.

2411501865 - Zahfandhika Fauzan Maldini
2411501642 - R. Ezra Rahmaditya

## Cara Buka Project
1. Buka Android Studio → **Open** → pilih folder `FutClub` ini.
2. Tunggu Gradle sync selesai (pertama kali agak lama karena download dependency).

## WAJIB dilakukan sebelum project bisa jalan

### 1. Setup Firebase (untuk Login Google)
1. Buka https://console.firebase.google.com → **Add Project** → beri nama misal "FutClub".
2. Di dashboard project, klik ikon Android → daftarkan app dengan package name:
   ```
   com.futclub.app
   ```
3. Untuk **SHA-1 fingerprint** (wajib diisi supaya Google Sign-In bisa jalan), jalankan di terminal Android Studio:
   ```
   ./gradlew signingReport
   ```
   Copy nilai SHA1 dari bagian `debugAndroidTest` atau `debug`, paste di form Firebase.
4. Download file **`google-services.json`** yang disediakan Firebase, lalu **taruh di folder `app/`**
   (sejajar dengan `build.gradle` milik app, bukan di root project).
5. Di Firebase Console → **Authentication** → **Sign-in method** → aktifkan **Google**.
6. Masih di halaman yang sama, copy **Web client ID** (bentuknya panjang, diakhiri
   `.apps.googleusercontent.com`), lalu paste ke:
   ```
   app/src/main/res/values/strings.xml → default_web_client_id
   ```

### 2. Sambungkan ke Backend PHP
Pastikan backend `futclub-backend` (yang sebelumnya sudah aku buatkan) sudah jalan di XAMPP.
Cek/ubah `base_url` di `app/src/main/res/values/strings.xml`:
- Emulator Android Studio → `http://10.0.2.2/futclub-backend/api/` (default, biasanya tidak perlu diubah)
- HP fisik → ganti `10.0.2.2` dengan IP address laptop kamu

### 3. Jalankan
Klik tombol Run (▶) di Android Studio, pilih emulator atau HP yang tersambung.

## Struktur Alur Aplikasi (untuk dokumentasi laporan)

**Activity:**
1. `LoginActivity` - Login Google (Firebase Auth)
2. `CategorySelectionActivity` - Pilih kategori olahraga (multi-select, wajib min 1)
3. `RoleSelectionActivity` - Pilih role: Admin Komunitas / Olahragawan
4. `CreateCommunityActivity` - Form buat komunitas baru (khusus admin), validasi input lengkap
5. `MainActivity` - Home: RecyclerView daftar komunitas + filter kategori + bottom nav glass
6. `CommunityDetailActivity` - Detail komunitas: gallery, RecyclerView member, join WhatsApp
7. `EditCommunityActivity` - Edit komunitas & tambah galeri (khusus admin pemilik)
8. `ProfileActivity` - Edit profil (nama, foto), logout

**Intent yang dipakai:**
- LoginActivity → CategorySelectionActivity (setelah login sukses)
- CategorySelectionActivity → RoleSelectionActivity (setelah pilih kategori)
- RoleSelectionActivity → CreateCommunityActivity (jika pilih Admin) / MainActivity (jika pilih Olahragawan)
- MainActivity → CommunityDetailActivity (tap item komunitas di RecyclerView, kirim `community_id`)
- MainActivity → ProfileActivity (tap foto profil di header / menu bottom nav)
- MainActivity → CreateCommunityActivity (tap FAB, khusus admin)
- CommunityDetailActivity → EditCommunityActivity (tap tombol Edit, khusus admin pemilik, kirim `community_id`)
- CommunityDetailActivity → (implicit intent) buka WhatsApp via `Intent.ACTION_VIEW`
- ProfileActivity → LoginActivity (setelah logout)

**RecyclerView yang dipakai (syarat wajib minimal 1):**
- `rvCommunities` di MainActivity (daftar komunitas)
- `rvMembers` di CommunityDetailActivity (daftar anggota komunitas)

**Widget yang dipakai:** TextInputLayout/EditText, MaterialButton, ChipGroup+Chip, Spinner,
CircleImageView, RecyclerView, CardView, SwipeRefreshLayout, BottomNavigationView, FloatingActionButton.

**Library tambahan & alasan pemakaian:**
- **Retrofit + Gson** - komunikasi REST API ke backend PHP (parsing JSON otomatis)
- **OkHttp Logging Interceptor** - debugging request/response API di Logcat
- **Glide** - load gambar dari URL (foto profil, foto komunitas, galeri) dengan cache otomatis
- **Firebase Auth + Google Sign-In** - fitur bonus login Google
- **CircleImageView** - menampilkan foto profil berbentuk bulat
- `SharedPreferences` (lewat class `SessionManager`) hanya dipakai untuk menyimpan status login
  (siapa yang sedang login) di HP, bukan sebagai database aplikasi.
- Upload foto saat ini masih berupa input URL manual (misal dari Google Drive/Imgur/ImgBB).
  Kalau mau upgrade ke upload file langsung dari galeri HP, bilang aja nanti dibuatkan endpoint upload-nya.
