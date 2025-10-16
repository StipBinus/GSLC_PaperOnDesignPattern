package Main.generated;

import Main.Car;

public class ConstructorUsage_1 {
    public static void main(String[] args) {
        // create base (simplified)
        Car base = new Car("Make", "Model", "Color", 2025, java.util.Arrays.asList("F"));

        Car c0 = new Car(base);
        c0.setColor(c0.getColor() + "#f0" );
        c0.setColor(c0.getColor() + "#f1" );
        c0.setColor(c0.getColor() + "#f2" );
        c0.setColor(c0.getColor() + "#f3" );
        c0.setColor(c0.getColor() + "#f4" );
        c0.setColor(c0.getColor() + "#f5" );
        c0.setColor(c0.getColor() + "#f6" );
        c0.setColor(c0.getColor() + "#f7" );
        c0.setColor(c0.getColor() + "#f8" );
        c0.setColor(c0.getColor() + "#f9" );
        c0.setColor(c0.getColor() + "#f10" );
        c0.setColor(c0.getColor() + "#f11" );
    }
}
