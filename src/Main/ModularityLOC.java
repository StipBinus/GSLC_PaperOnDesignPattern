package Main;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.List;

public class ModularityLOC {

    public static void main(String[] args) throws Exception {
        int[] ns = new int[] { 1, 2, 5 };

        Path outDir = Path.of(System.getProperty("user.dir"), "src", "Main", "generated");
        Files.createDirectories(outDir);

        StringBuilder csv = new StringBuilder();
        csv.append("N,Prototype_LOC,Constructor_LOC\n");

        for (int n : ns) {
            String protoName = "PrototypeUsage_" + n;
            String consName = "ConstructorUsage_" + n;

            Path protoFile = outDir.resolve(protoName + ".java");
            Path consFile = outDir.resolve(consName + ".java");

            String protoSrc = generatePrototypeSource(protoName, n);
            String consSrc = generateConstructorSource(consName, n);

            Files.writeString(protoFile, protoSrc, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
            Files.writeString(consFile, consSrc, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);

            int protoLOC = countEffectiveLines(protoFile);
            int consLOC = countEffectiveLines(consFile);

            System.out.printf("N=%d -> Prototype LOC=%d, Constructor LOC=%d%n", n, protoLOC, consLOC);
            csv.append(String.format("%d,%d,%d\n", n, protoLOC, consLOC));
        }

        Path chartsDir = Path.of(System.getProperty("user.dir"), "charts");
        Files.createDirectories(chartsDir);
        Path outCsv = chartsDir.resolve("loc_results.csv");
        Files.writeString(outCsv, csv.toString(), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);

        System.out.println("LOC results saved to " + outCsv.toString());
    }

    private static String generatePrototypeSource(String className, int n) {
        StringBuilder sb = new StringBuilder();
        sb.append("package Main.generated;\n\n");
        sb.append("import Main.Car;\n");
        sb.append("import Main.CarRegist;\n\n");
        sb.append("public class ").append(className).append(" {\n");
        sb.append("    public static void main(String[] args) {\n");
        sb.append("        // create base and registry (simplified)\n");
        sb.append("        Car base = new Car(\"Make\", \"Model\", \"Color\", 2025, java.util.Arrays.asList(\"F\"));\n");
        sb.append("        CarRegist registry = new CarRegist();\n");
        sb.append("        registry.addPrototype(\"key\", base);\n\n");
        for (int i = 0; i < n; i++) {
            sb.append("        Car c").append(i).append(" = registry.getClone(\"key\");\n");
            sb.append("        c").append(i).append(".setColor(c").append(i).append(".getColor() + \"#\" + ").append(i).append(");\n");
        }
        sb.append("    }\n");
        sb.append("}\n");
        return sb.toString();
    }

    private static String generateConstructorSource(String className, int n) {
        // Simulate an object with many fields and verbose per-instance setup to show LOC cost
        int extraFields = 12; // number of per-field setter lines to generate per object
        StringBuilder sb = new StringBuilder();
        sb.append("package Main.generated;\n\n");
        sb.append("import Main.Car;\n\n");
        sb.append("public class ").append(className).append(" {\n");
        sb.append("    public static void main(String[] args) {\n");
        sb.append("        // create base (simplified)\n");
        sb.append("        Car base = new Car(\"Make\", \"Model\", \"Color\", 2025, java.util.Arrays.asList(\"F\"));\n\n");
        for (int i = 0; i < n; i++) {
            sb.append("        Car c").append(i).append(" = new Car(base);\n");
            // add many setter lines to simulate many fields being tweaked per instance
            for (int f = 0; f < extraFields; f++) {
                sb.append("        c").append(i).append(".setColor(c").append(i).append(".getColor() + \"#f").append(f).append("\" );\n");
            }
        }
        sb.append("    }\n");
        sb.append("}\n");
        return sb.toString();
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
            // exclude package and import lines if you want purely code lines, but keep them for transparency
            count++;
        }
        return count;
    }
}