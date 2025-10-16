package SecItteration;

import Main.Car;
import Main.CarRegist;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

// XChart imports
import org.knowm.xchart.BitmapEncoder;
import org.knowm.xchart.SwingWrapper;
import org.knowm.xchart.XYChart;
import org.knowm.xchart.XYChartBuilder;
import org.knowm.xchart.style.markers.None;

public class PrototypeVsConstructor {

    private static class Result {
        final long nanos;
        final long memoryBytes;
        Result(long nanos, long memoryBytes) {
            this.nanos = nanos;
            this.memoryBytes = memoryBytes;
        }
    }

    public static void main(String[] args) throws Exception {

        Car.setHeavyDataSize(3_000_000); 

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
        int[] sizes = new int[] { 50, 100, 200 };

        System.out.println("Running secondary iteration: heavyDataSize=" + Car.HEAVY_DATA_SIZE + " bytes");

        // Warm-up
        runPrototype(registry, "ev", 20);
        runNoPrototype(base, 20);

        // Collect results into arrays for plotting
        double[] x = Arrays.stream(sizes).asDoubleStream().toArray();
        double[] timeProtoMs = new double[sizes.length];
        double[] timeConstructorMs = new double[sizes.length];
        double[] memProtoKB = new double[sizes.length];
        double[] memConstructorKB = new double[sizes.length];

        for (int i = 0; i < sizes.length; i++) {
            int n = sizes[i];
            Result p = runPrototype(registry, "ev", n);
            Result c = runNoPrototype(base, n);

            timeProtoMs[i] = p.nanos / 1_000_000.0;
            timeConstructorMs[i] = c.nanos / 1_000_000.0;
            memProtoKB[i] = p.memoryBytes / 1024.0;
            memConstructorKB[i] = c.memoryBytes / 1024.0;

            System.out.printf("N=%d | Prototype: %.3f ms, %.1f KB | Constructor: %.3f ms, %.1f KB%n",
                    n, timeProtoMs[i], memProtoKB[i], timeConstructorMs[i], memConstructorKB[i]);
        }

        // Build and display charts
        XYChart timeChart = new XYChartBuilder()
                .width(800).height(600)
                .title("Secondary Iteration: Time vs Number of Objects")
                .xAxisTitle("Number of objects (N)")
                .yAxisTitle("Time (ms)")
                .build();
        styleChart(timeChart);
        timeChart.addSeries("Prototype", x, timeProtoMs).setMarker(new None());
        timeChart.addSeries("Constructor", x, timeConstructorMs).setMarker(new None());

        XYChart memChart = new XYChartBuilder()
                .width(800).height(600)
                .title("Secondary Iteration: Memory vs Number of Objects")
                .xAxisTitle("Number of objects (N)")
                .yAxisTitle("Memory (KB)")
                .build();
        styleChart(memChart);
        memChart.addSeries("Prototype", x, memProtoKB).setMarker(new None());
        memChart.addSeries("Constructor", x, memConstructorKB).setMarker(new None());

        new SwingWrapper<>(timeChart).displayChart();
        new SwingWrapper<>(memChart).displayChart();
        saveChart(timeChart, "charts/sec_time_vs_n.png");
        saveChart(memChart, "charts/sec_memory_vs_n.png");

        System.out.println("Charts saved to charts/sec_time_vs_n.png and charts/sec_memory_vs_n.png");

        System.out.println("Done.");
    }

    private static Result runPrototype(CarRegist registry, String key, int n) throws InterruptedException {
        forceGC();
        long beforeMem = usedMemory();

        long t0 = System.nanoTime();
        List<Car> cars = new ArrayList<>(n);
        for (int i = 0; i < n; i++) {
            Car c = registry.getClone(key);
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

    private static void styleChart(XYChart chart) {
        chart.getStyler().setLegendVisible(true);
        chart.getStyler().setMarkerSize(6);
        chart.getStyler().setDecimalPattern("#,###.##");
    }

    private static void saveChart(XYChart chart, String pathStr) {
        try {
            java.nio.file.Path path = java.nio.file.Path.of(pathStr);
            java.nio.file.Path dir = path.getParent();
            if (dir != null) java.nio.file.Files.createDirectories(dir);
            BitmapEncoder.saveBitmap(chart, path.toString(), BitmapEncoder.BitmapFormat.PNG);
        } catch (java.io.IOException e) {
            System.err.println("Failed to save chart: " + e.getMessage());
        }
    }
}