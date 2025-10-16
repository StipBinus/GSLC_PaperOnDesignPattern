package Main;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Car implements Cloneable {
    private String make;
    private String model;
    private String color;
    private int year;
    private List<String> features;

    // Configurable expensive initialization size (bytes)
    public static volatile int HEAVY_DATA_SIZE = 1_000_000;

    // Simulate expensive initialization (e.g. large immutable resource)
    private final byte[] heavyData;

    public Car(String make, String model, String color, int year, List<String> features) {
        this.make = make;
        this.model = model;
        this.color = color;
        this.year = year;
        // defensive copy to avoid external mutation
        this.features = new ArrayList<>(features);

        // Expensive initialization: allocate and fill a large array to simulate heavy setup
        this.heavyData = new byte[HEAVY_DATA_SIZE];
        for (int i = 0; i < heavyData.length; i++) {
            // simple deterministic work to prevent JIT from optimizing away
            heavyData[i] = (byte) (i % 127);
        }
    }

    public static void setHeavyDataSize(int size) {
        HEAVY_DATA_SIZE = Math.max(0, size);
    }

    // Copy constructor (used for the non-prototype baseline)
    public Car(Car other) {
        this(other.make, other.model, other.color, other.year, other.features);
    }

    @Override
    public Car clone() {
        try {
            Car copy = (Car) super.clone();
            // deep copy mutable list
            copy.features = new ArrayList<>(this.features);
            // Note: do NOT clone heavyData — it is immutable-like and expensive to recreate.
            // The prototype will reuse the same heavyData reference which is the point of the benchmark.
            return copy;
        } catch (CloneNotSupportedException e) {
            throw new AssertionError("Clone should be supported", e);
        }
    }

    // Getters & setters
    public String getMake() { return make; }
    public String getModel() { return model; }
    public String getColor() { return color; }
    public int getYear() { return year; }
    public List<String> getFeatures() { return features; }

    public void setMake(String make) { this.make = make; }
    public void setModel(String model) { this.model = model; }
    public void setColor(String color) { this.color = color; }
    public void setYear(int year) { this.year = year; }
    public void setFeatures(List<String> features) { this.features = new ArrayList<>(features); }

    @Override
    public String toString() {
        return "Car{" +
                "make='" + make + '\'' +
                ", model='" + model + '\'' +
                ", color='" + color + '\'' +
                ", year=" + year +
                ", features=" + features +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Car)) return false;
        Car c = (Car) o;
        return year == c.year &&
               Objects.equals(make, c.make) &&
               Objects.equals(model, c.model) &&
               Objects.equals(color, c.color) &&
               Objects.equals(features, c.features);
    }

    @Override
    public int hashCode() {
        return Objects.hash(make, model, color, year, features);
    }
}