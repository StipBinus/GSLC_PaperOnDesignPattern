package Main;

import java.util.HashMap;
import java.util.Map;

public class CarRegist {
    private final Map<String, Car> prototypes = new HashMap<>();

    public void addPrototype(String key, Car car) {
        prototypes.put(key, car);
    }

    public Car getClone(String key) {
        Car proto = prototypes.get(key);
        if (proto == null) {
            throw new IllegalArgumentException("No prototype registered for key: " + key);
        }
        return proto.clone();
    }
}