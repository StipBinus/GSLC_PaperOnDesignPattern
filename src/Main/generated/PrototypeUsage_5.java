package Main.generated;

import Main.Car;
import Main.CarRegist;

public class PrototypeUsage_5 {
    public static void main(String[] args) {
        // create base and registry (simplified)
        Car base = new Car("Make", "Model", "Color", 2025, java.util.Arrays.asList("F"));
        CarRegist registry = new CarRegist();
        registry.addPrototype("key", base);

        Car c0 = registry.getClone("key");
        c0.setColor(c0.getColor() + "#" + 0);
        Car c1 = registry.getClone("key");
        c1.setColor(c1.getColor() + "#" + 1);
        Car c2 = registry.getClone("key");
        c2.setColor(c2.getColor() + "#" + 2);
        Car c3 = registry.getClone("key");
        c3.setColor(c3.getColor() + "#" + 3);
        Car c4 = registry.getClone("key");
        c4.setColor(c4.getColor() + "#" + 4);
    }
}
