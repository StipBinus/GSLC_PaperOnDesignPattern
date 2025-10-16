package Main.generated;

import Main.Car;
import Main.CarRegist;

public class PrototypeUsage_2 {
    public static void main(String[] args) {
        // create base and registry (simplified)
        Car base = new Car("Make", "Model", "Color", 2025, java.util.Arrays.asList("F"));
        CarRegist registry = new CarRegist();
        registry.addPrototype("key", base);

        Car c0 = registry.getClone("key");
        c0.setColor(c0.getColor() + "#" + 0);
        Car c1 = registry.getClone("key");
        c1.setColor(c1.getColor() + "#" + 1);
    }
}
