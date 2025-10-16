GSLC_PaperOnDesignPattern
=========================

Singkat (Bahasa Indonesia):
Proyek ini membandingkan dua pendekatan pembuatan objek Java: Prototype vs Constructor.
- Ada dua "experiment runners":
  - RunExperiments.PrototypeVsConstructor1 (eksperimen utama, ukuran kecil)
  - RunExperiments.PrototypeVsConstructor2 (eksperimen sekunder, heavy-data besar)
- `RunExperiments.LocChartAndReport` membuat chart perbandingan LOC dari contoh yang digenerate atau dari CSV di `charts/loc_results.csv`.

Struktur penting:
- src/RunExperiments/ : implementasi runner dan report (baru setelah refactor)
- src/Main/          : delegator untuk kompatibilitas (memanggil RunExperiments.*)
- src/SecItteration/ : delegator untuk kompatibilitas (memanggil RunExperiments.*)
- lib/xchart-3.8.1.jar : dependency untuk pembuatan chart (pastikan ada di folder lib)

Cara menjalankan (Windows, cmd.exe)
1) Compile (hasil ke folder bin). Pastikan Java 11+ dan file `lib\xchart-3.8.1.jar` ada.

javac -d bin --module-path lib -classpath lib\xchart-3.8.1.jar src\module-info.java src\RunExperiments\*.java src\Main\*.java src\SecItteration\*.java src\Main\generated\*.java

2) Jalankan salah satu runner (contoh menjalankan eksperimen sekunder):

java --module-path lib;bin -cp bin;lib\xchart-3.8.1.jar RunExperiments.PrototypeVsConstructor2

atau jalankan delegator "lama" jika Anda ingin nama lama:

java --module-path lib;bin -cp bin;lib\xchart-3.8.1.jar Main.ExperimentRunner

3) Jalankan pembuatan chart LOC (menggunakan data CSV di `charts/loc_results.csv` atau contoh yang digenerate):

java --module-path lib;bin -cp bin;lib\xchart-3.8.1.jar RunExperiments.LocChartAndReport

Catatan cepat
- Jika menggunakan IDE (Eclipse/IntelliJ), tambahkan `lib/xchart-3.8.1.jar` ke module-path/classpath dan jalankan kelas-kelas di atas.
- Jika `charts/loc_results.csv` tidak ada, `LocChartAndReport` akan mencoba menghitung LOC dari `src/Main/generated`.
- Setelah refactor ada delegator di `src/Main` dan `src/SecItteration` yang meneruskan panggilan ke `src/RunExperiments`.

Lisensi / Disclaimer
- Tidak ada lisensi khusus, gunakan sesuai kebutuhan.
