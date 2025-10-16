package Main.generated;

import Main.Car;

public class ConstructorUsage_2 {
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
        Car c1 = new Car(base);
        c1.setColor(c1.getColor() + "#f0" );
        c1.setColor(c1.getColor() + "#f1" );
        c1.setColor(c1.getColor() + "#f2" );
        c1.setColor(c1.getColor() + "#f3" );
        c1.setColor(c1.getColor() + "#f4" );
        c1.setColor(c1.getColor() + "#f5" );
        c1.setColor(c1.getColor() + "#f6" );
        c1.setColor(c1.getColor() + "#f7" );
        c1.setColor(c1.getColor() + "#f8" );
        c1.setColor(c1.getColor() + "#f9" );
        c1.setColor(c1.getColor() + "#f10" );
        c1.setColor(c1.getColor() + "#f11" );
    }
}
