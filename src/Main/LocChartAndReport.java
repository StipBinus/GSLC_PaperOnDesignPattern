package Main;

import org.knowm.xchart.BitmapEncoder;
import org.knowm.xchart.CategoryChart;
import org.knowm.xchart.CategoryChartBuilder;
import org.knowm.xchart.style.Styler;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LocChartAndReport {

    public static void main(String[] args) throws Exception {
        Path chartsDir = Path.of(System.getProperty("user.dir"), "charts");
        Files.createDirectories(chartsDir);

        Path csv = chartsDir.resolve("loc_results.csv");

        List<String> categories = new ArrayList<>();
        List<Integer> proto = new ArrayList<>();
        List<Integer> cons = new ArrayList<>();

        if (Files.exists(csv)) {
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
            // Fallback: try to compute LOC from generated sources
            Path genDir = Path.of(System.getProperty("user.dir"), "src", "Main", "generated");
            if (Files.exists(genDir) && Files.isDirectory(genDir)) {
                // Map N -> counts
                Map<String, Integer> protoMap = new HashMap<>();
                Map<String, Integer> consMap = new HashMap<>();

                try (DirectoryStream<Path> ds = Files.newDirectoryStream(genDir, "*.java")) {
                    for (Path p : ds) {
                        String name = p.getFileName().toString();
                        if (name.startsWith("PrototypeUsage_") && name.endsWith(".java")) {
                            String n = name.substring("PrototypeUsage_".length(), name.length() - 5);
                            int loc = countEffectiveLines(p);
                            protoMap.put(n, loc);
                        } else if (name.startsWith("ConstructorUsage_") && name.endsWith(".java")) {
                            String n = name.substring("ConstructorUsage_".length(), name.length() - 5);
                            int loc = countEffectiveLines(p);
                            consMap.put(n, loc);
                        }
                    }
                }

                // Build sorted categories from keys present in either map
                List<String> keys = new ArrayList<>(protoMap.keySet());
                for (String k : consMap.keySet()) if (!keys.contains(k)) keys.add(k);
                keys.sort((a,b) -> {
                    try { return Integer.compare(Integer.parseInt(a), Integer.parseInt(b)); }
                    catch (NumberFormatException e) { return a.compareTo(b); }
                });

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
                System.err.println("CSV not found: " + csv.toString());
                System.err.println("And generated samples directory not found: " + genDirPathInfo());
                System.err.println("Run ModularityLOC first to produce charts/loc_results.csv, or place a CSV at that path.");
                return;
            }
        }

        // Build category chart (bar chart)
        CategoryChart chart = new CategoryChartBuilder()
                .width(800).height(600)
                .title("LOC: Prototype vs Constructor")
                .xAxisTitle("N")
                .yAxisTitle("Lines of Code")
                .build();

        chart.getStyler().setLegendPosition(Styler.LegendPosition.InsideNW);

        chart.addSeries("Prototype", categories, proto);
        chart.addSeries("Constructor", categories, cons);

        Path outPng = chartsDir.resolve("loc_chart.png");
        BitmapEncoder.saveBitmap(chart, outPng.toString(), BitmapEncoder.BitmapFormat.PNG);

        System.out.println("Chart saved to: " + outPng.toString());
    }

    private static String genDirPathInfo() {
        return Path.of(System.getProperty("user.dir"), "src", "Main", "generated").toString();
    }

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