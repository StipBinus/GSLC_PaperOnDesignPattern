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

public class PrototypeVsConstructor1 {

    private static class Result {
        final long nanos;
        final long memoryBytes;
        Result(long nanos, long memoryBytes) {
            this.nanos = nanos;
            this.memoryBytes = memoryBytes;
        }
    }

    public static void main(String[] args) throws Exception {
        // Use a small heavy data size so prototype vs constructor look identical on the chart
        Car.setHeavyDataSize(1);
        System.out.println("PrototypeVsConstructor1: HEAVY_DATA_SIZE=" + Car.HEAVY_DATA_SIZE + " bytes");

        Car base = new Car(
                "Toyota",
                "Camry",
                "Blue",
                2022,
                Arrays.asList("ABS", "Airbags", "Bluetooth", "Cruise Control", "Lane Assist",
                        "Backup Camera", "Heated Seats", "Keyless Entry", "Sunroof", "Android Auto")
        );

        CarRegist registry = new CarRegist();
        registry.addPrototype("sedan", base);

        // Use same sizes as PrototypeVsConstructor
        int[] sizes = new int[] { 1, 50, 100, 200 };

        double[] x = Arrays.stream(sizes).asDoubleStream().toArray();
        double[] timeProtoMs = new double[sizes.length];
        double[] timeNoProtoMs = new double[sizes.length];
        double[] memProtoKB = new double[sizes.length];
        double[] memNoProtoKB = new double[sizes.length];

        // Warm-up to reduce JIT noise (match PrototypeVsConstructor warm-up)
        runPrototype(registry, "sedan", 20);
        runNoPrototype(base, 20);

        for (int i = 0; i < sizes.length; i++) {
            int n = sizes[i];

            Result r1 = runPrototype(registry, "sedan", n);
            Result r2 = runNoPrototype(base, n);

            timeProtoMs[i] = r1.nanos / 1_000_000.0;
            timeNoProtoMs[i] = r2.nanos / 1_000_000.0;
            memProtoKB[i] = r1.memoryBytes / 1024.0;
            memNoProtoKB[i] = r2.memoryBytes / 1024.0;

            System.out.printf("N=%d | Prototype: %.3f ms, %.1f KB | No-Prototype: %.3f ms, %.1f KB%n",
                    n, timeProtoMs[i], memProtoKB[i], timeNoProtoMs[i], memNoProtoKB[i]);
        }

        XYChart timeChart = new XYChartBuilder()
                .width(800).height(600)
                .title("Total Execution Time vs Number of Objects")
                .xAxisTitle("Number of objects (N)")
                .yAxisTitle("Time (ms)")
                .build();
        styleChart(timeChart);
        timeChart.addSeries("Prototype", x, timeProtoMs).setMarker(new None());
        timeChart.addSeries("No Prototype", x, timeNoProtoMs).setMarker(new None());

        XYChart memChart = new XYChartBuilder()
                .width(800).height(600)
                .title("Memory Used vs Number of Objects")
                .xAxisTitle("Number of objects (N)")
                .yAxisTitle("Memory (KB)")
                .build();
        styleChart(memChart);
        memChart.addSeries("Prototype", x, memProtoKB).setMarker(new None());
        memChart.addSeries("No Prototype", x, memNoProtoKB).setMarker(new None());

        new SwingWrapper<>(timeChart).displayChart();
        new SwingWrapper<>(memChart).displayChart();
        saveChart(timeChart, "charts/time_vs_n.png");
        saveChart(memChart, "charts/memory_vs_n.png");

        System.out.println("Charts saved to charts/time_vs_n.png and charts/memory_vs_n.png");
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
        Thread.sleep(50);
        System.gc();
        Thread.sleep(50);
    }
}
