package RunExperiments;

import org.knowm.xchart.BitmapEncoder;
import org.knowm.xchart.CategoryChart;
import org.knowm.xchart.CategoryChartBuilder;
import org.knowm.xchart.style.Styler;
import org.knowm.xchart.SwingWrapper;

import java.awt.GraphicsEnvironment;
import java.io.IOException;
import java.nio.file.AccessDeniedException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/*
 Ringkasan singkat (Bahasa Indonesia):
 Program ini membuat chart perbandingan LOC (Lines Of Code) untuk contoh "Prototype" vs "Constructor".
 Alur utama:
  - Cek apakah ada berkas CSV hasil pengukuran di folder `charts/`.
  - Jika tidak ada, hitung LOC dari file contoh di `src/Main/generated`.
  - Bangun chart kategori (bar chart) dari data dan simpan ke `charts/loc_chart.png`.
  - Jika lingkungan grafis tersedia, tampilkan chart di jendela Swing.

 Bagian penting (komentar singkat):
  - Pembuatan direktori charts: pastikan lokasi bisa ditulis, gunakan fallback bila perlu.
  - Parsing CSV: format diharapkan N,prototypeLOC,constructorLOC (header di baris pertama).
  - Fallback ke folder `src/Main/generated`: membaca file `PrototypeUsage_*` dan `ConstructorUsage_*`.
  - `countEffectiveLines`: menghitung LOC efektif (mengabaikan komentar dan baris kosong).
*/

public class LocChartAndReport {

    public static void main(String[] args) throws Exception {
        // Tentukan lokasi folder charts (relatif ke working directory proyek)
        Path chartsDir = Path.of(System.getProperty("user.dir"), "charts");
        try {
            // Pastikan direktori charts ada; buat bila belum ada
            Files.createDirectories(chartsDir);
        } catch (AccessDeniedException ade) {
            // Jika tidak punya izin, gunakan direktori fallback di home user
            chartsDir = fallbackChartsDir();
            Files.createDirectories(chartsDir);
            System.err.println("Permission denied creating project charts directory; using fallback: " + chartsDir);
        } catch (IOException ioe) {
            // Penanganan IO umum (mis. menjalankan dari lokasi terproteksi), juga gunakan fallback
            chartsDir = fallbackChartsDir();
            Files.createDirectories(chartsDir);
            System.err.println("Unable to create project charts directory; using fallback: " + chartsDir + " (" + ioe.getMessage() + ")");
        }

        // Lokasi CSV hasil pengukuran LOC (jika tersedia)
        Path csv = chartsDir.resolve("loc_results.csv");

        List<String> categories = new ArrayList<>();
        List<Integer> proto = new ArrayList<>();
        List<Integer> cons = new ArrayList<>();

        if (Files.exists(csv)) {
            // Jika CSV ditemukan, baca dan parsing isinya (abaikan header di baris pertama)
            List<String> lines = Files.readAllLines(csv);
            if (lines.size() < 2) {
                System.err.println("CSV contains no data");
                return;
            }

            for (int i = 1; i < lines.size(); i++) {
                String line = lines.get(i).trim();
                if (line.isEmpty()) continue;
                String[] parts = line.split(",");
                if (parts.length < 3) continue;
                categories.add(parts[0]);
                proto.add(Integer.parseInt(parts[1]));
                cons.add(Integer.parseInt(parts[2]));
            }
        } else {
            // Jika CSV tidak ada, coba hitung dari contoh sumber yang di-generate
            Path genDir = Path.of(System.getProperty("user.dir"), "src", "Main", "generated");
            if (Files.exists(genDir) && Files.isDirectory(genDir)) {
                Map<String, Integer> protoMap = new HashMap<>();
                Map<String, Integer> consMap = new HashMap<>();

                // Iterasi file contoh di folder generated
                try (DirectoryStream<Path> ds = Files.newDirectoryStream(genDir, "*.java")) {
                    for (Path p : ds) {
                        String name = p.getFileName().toString();
                        if (name.startsWith("PrototypeUsage_") && name.endsWith(".java")) {
                            String n = name.substring("PrototypeUsage_".length(), name.length() - 5);
                            int loc = countEffectiveLines(p); // Hitung LOC efektif (tanpa komentar/baris kosong)
                            protoMap.put(n, loc);
                        } else if (name.startsWith("ConstructorUsage_") && name.endsWith(".java")) {
                            String n = name.substring("ConstructorUsage_".length(), name.length() - 5);
                            int loc = countEffectiveLines(p);
                            consMap.put(n, loc);
                        }
                    }
                }

                // Gabungkan kunci (N) dari kedua map dan urutkan secara numerik bila memungkinkan
                List<String> keys = new ArrayList<>(protoMap.keySet());
                for (String k : consMap.keySet()) if (!keys.contains(k)) keys.add(k);
                keys.sort((a,b) -> {
                    try { return Integer.compare(Integer.parseInt(a), Integer.parseInt(b)); }
                    catch (NumberFormatException e) { return a.compareTo(b); }
                });

                // Bangun daftar kategori dan nilai berdasarkan map
                for (String k : keys) {
                    categories.add(k);
                    proto.add(protoMap.getOrDefault(k, 0));
                    cons.add(consMap.getOrDefault(k, 0));
                }

                if (categories.isEmpty()) {
                    System.err.println("No generated sample files found in " + genDir.toString());
                    System.err.println("Either run ModularityLOC to generate loc_results.csv or provide the CSV at " + csv.toString());
                    return;
                }

            } else {
                // Jika tidak ada CSV maupun contoh generated, informasikan pengguna dan keluar
                System.err.println("CSV not found: " + csv.toString());
                System.err.println("And generated samples directory not found: " + genDirPathInfo());
                System.err.println("Run ModularityLOC first to produce charts/loc_results.csv, or place a CSV at that path.");
                return;
            }
        }

        // Bangun chart kategori (bar chart) dengan judul dan label sumbu
        CategoryChart chart = new CategoryChartBuilder()
                .width(800).height(600)
                .title("LOC: Prototype vs Constructor")
                .xAxisTitle("N")
                .yAxisTitle("Lines of Code")
                .build();

        chart.getStyler().setLegendPosition(Styler.LegendPosition.InsideNW);

        // Tambahkan series untuk Prototype dan Constructor
        chart.addSeries("Prototype", categories, proto);
        chart.addSeries("Constructor", categories, cons);

        // Simpan chart ke file PNG di folder charts
        Path outPng = chartsDir.resolve("loc_chart.png");
        BitmapEncoder.saveBitmap(chart, outPng.toString(), BitmapEncoder.BitmapFormat.PNG);

        // Tampilkan chart di jendela Swing jika lingkungan grafis tersedia
        if (!GraphicsEnvironment.isHeadless()) {
            new SwingWrapper<>(chart).displayChart();
        } else {
            System.out.println("Headless environment detected: chart image saved to " + outPng.toString());
        }

        System.out.println("Chart saved to: " + outPng.toString());
    }

    // Direktori fallback bila tidak bisa membuat ./charts di working directory
    private static Path fallbackChartsDir() {
        return Path.of(System.getProperty("user.home"), "GSLC_PaperOnDesignPattern", "charts");
    }

    // Info path untuk generated samples (digunakan dalam pesan error)
    private static String genDirPathInfo() {
        return Path.of(System.getProperty("user.dir"), "src", "Main", "generated").toString();
    }

    // Hitung LOC efektif: abaikan komentar blok, komentar baris, dan baris kosong
    private static int countEffectiveLines(Path file) throws IOException {
        List<String> lines = Files.readAllLines(file);
        boolean inBlock = false;
        int count = 0;
        for (String raw : lines) {
            String line = raw.trim();
            if (line.isEmpty()) continue;
            if (!inBlock && line.startsWith("/*")) {
                inBlock = true;
                if (line.endsWith("*/") && !line.equals("/*")) {
                    inBlock = false;
                }
                continue;
            }
            if (inBlock) {
                if (line.endsWith("*/")) inBlock = false;
                continue;
            }
            if (line.startsWith("//")) continue;
            count++;
        }
        return count;
    }
}