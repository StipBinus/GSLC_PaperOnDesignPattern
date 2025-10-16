package RunExperiments;

import Main.Car;
import Main.CarRegist;

import org.knowm.xchart.BitmapEncoder;
import org.knowm.xchart.SwingWrapper;
import org.knowm.xchart.XYChart;
import org.knowm.xchart.XYChartBuilder;
import org.knowm.xchart.style.markers.None;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class PrototypeVsConstructor2 {

    private static class Result {
        final long nanos;
        final long memoryBytes;
        Result(long nanos, long memoryBytes) {
            this.nanos = nanos;
            this.memoryBytes = memoryBytes;
        }
    }

    public static void main(String[] args) throws Exception {
        // Make heavy initialization much larger so constructing each Car is expensive
        Car.setHeavyDataSize(3_000_000); // ~3 MB per Car construction

        Car base = new Car(
                "Tesla",
                "Model S",
                "Red",
                2025,
                Arrays.asList("Autopilot", "Glass Roof", "Premium Sound")
        );

        CarRegist registry = new CarRegist();
        registry.addPrototype("ev", base);

        // Larger sizes to amplify difference
        int[] sizes = new int[] { 1, 50, 100, 200 };

        System.out.println("Running secondary iteration: heavyDataSize=" + Car.HEAVY_DATA_SIZE + " bytes");

        // Warm-up
        runPrototype(registry, "ev", 20);
        runNoPrototype(base, 20);

        // Prepare arrays to collect data for charting
        double[] x = Arrays.stream(sizes).asDoubleStream().toArray();
        double[] timeProtoMs = new double[sizes.length];
        double[] timeNoProtoMs = new double[sizes.length];
        double[] memProtoKB = new double[sizes.length];
        double[] memNoProtoKB = new double[sizes.length];

        for (int i = 0; i < sizes.length; i++) {
            int n = sizes[i];
            Result p = runPrototype(registry, "ev", n);
            Result c = runNoPrototype(base, n);

            double pMs = p.nanos / 1_000_000.0;
            double cMs = c.nanos / 1_000_000.0;
            double pKB = p.memoryBytes / 1024.0;
            double cKB = c.memoryBytes / 1024.0;

            timeProtoMs[i] = pMs;
            timeNoProtoMs[i] = cMs;
            memProtoKB[i] = pKB;
            memNoProtoKB[i] = cKB;

            System.out.printf("N=%d | Prototype: %.3f ms, %.1f KB | Constructor: %.3f ms, %.1f KB%n",
                    n, pMs, pKB, cMs, cKB);
        }

        // Build and display charts (time and memory) and save to charts/ folder
        XYChart timeChart = new XYChartBuilder()
                .width(800).height(600)
                .title("Secondary: Total Execution Time vs Number of Objects")
                .xAxisTitle("Number of objects (N)")
                .yAxisTitle("Time (ms)")
                .build();
        styleChart(timeChart);
        timeChart.addSeries("Prototype", x, timeProtoMs).setMarker(new None());
        timeChart.addSeries("Constructor", x, timeNoProtoMs).setMarker(new None());

        XYChart memChart = new XYChartBuilder()
                .width(800).height(600)
                .title("Secondary: Memory Used vs Number of Objects")
                .xAxisTitle("Number of objects (N)")
                .yAxisTitle("Memory (KB)")
                .build();
        styleChart(memChart);
        memChart.addSeries("Prototype", x, memProtoKB).setMarker(new None());
        memChart.addSeries("Constructor", x, memNoProtoKB).setMarker(new None());

        new SwingWrapper<>(timeChart).displayChart();
        new SwingWrapper<>(memChart).displayChart();
        saveChart(timeChart, "charts/sec_time_vs_n.png");
        saveChart(memChart, "charts/sec_memory_vs_n.png");

        System.out.println("Secondary charts saved to charts/sec_time_vs_n.png and charts/sec_memory_vs_n.png");

        System.out.println("Done.");
    }

    private static void styleChart(XYChart chart) {
        chart.getStyler().setLegendVisible(true);
        chart.getStyler().setMarkerSize(6);
        chart.getStyler().setDecimalPattern("#,###.##");
    }

    private static void saveChart(XYChart chart, String pathStr) {
        try {
            Path path = Path.of(pathStr);
            Path dir = path.getParent();
            if (dir != null) Files.createDirectories(dir);
            BitmapEncoder.saveBitmap(chart, path.toString(), BitmapEncoder.BitmapFormat.PNG);
        } catch (IOException e) {
            System.err.println("Failed to save chart: " + e.getMessage());
        }
    }

    private static Result runPrototype(CarRegist registry, String key, int n) throws InterruptedException {
        forceGC();
        long beforeMem = usedMemory();

        long t0 = System.nanoTime();
        List<Car> cars = new ArrayList<>(n);
        for (int i = 0; i < n; i++) {
            Car c = registry.getClone(key);
            // mutate a small field so objects are slightly different
            c.setColor(c.getColor() + "#" + i);
            cars.add(c);
        }
        long t1 = System.nanoTime();

        forceGC();
        long afterMem = usedMemory();
        return new Result(t1 - t0, Math.max(0, afterMem - beforeMem));
    }

    private static Result runNoPrototype(Car base, int n) throws InterruptedException {
        forceGC();
        long beforeMem = usedMemory();

        long t0 = System.nanoTime();
        List<Car> cars = new ArrayList<>(n);
        for (int i = 0; i < n; i++) {
            Car c = new Car(base);
            c.setColor(c.getColor() + "#" + i);
            cars.add(c);
        }
        long t1 = System.nanoTime();

        forceGC();
        long afterMem = usedMemory();
        return new Result(t1 - t0, Math.max(0, afterMem - beforeMem));
    }

    private static long usedMemory() {
        Runtime rt = Runtime.getRuntime();
        return rt.totalMemory() - rt.freeMemory();
    }

    private static void forceGC() throws InterruptedException {
        System.gc();
        Thread.sleep(100);
        System.gc();
        Thread.sleep(100);
    }
}
