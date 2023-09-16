package fr.krishenk.castel.dependencies.relocation;

import fr.krishenk.castel.dependencies.DependencyManager;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;

public class RelocationHandler {
    private static final String JAR_RELOCATOR_CLASS = JarRelocator.class.getName();
    private static final String JAR_RELOCATOR_RUN_METHOD = "run";

    public RelocationHandler(DependencyManager dependencyManager) {
    }

    public void remap(Path input, Path output, List<SimpleRelocation> relocations) throws Exception {
        HashMap<String, String> mappings = new HashMap<String, String>();
        for (SimpleRelocation relocation : relocations) {
            mappings.put(relocation.getPattern(), relocation.getRelocatedPattern());
        }
        JarRelocator instance = new JarRelocator(input.toFile(), output.toFile(), mappings);
        instance.run();
    }
}
