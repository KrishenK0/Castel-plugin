package fr.krishenk.castel.utils.config.adapters;

import fr.krishenk.castel.CastelPlugin;
import fr.krishenk.castel.config.ConfigAccessor;
import fr.krishenk.castel.libs.snakeyaml.api.Load;
import fr.krishenk.castel.libs.snakeyaml.api.LoadSettings;
import fr.krishenk.castel.libs.snakeyaml.composer.Composer;
import fr.krishenk.castel.libs.snakeyaml.nodes.MappingNode;
import fr.krishenk.castel.libs.snakeyaml.validation.NodeValidator;
import fr.krishenk.castel.libs.snakeyaml.validation.Validator;
import fr.krishenk.castel.utils.config.ConfigSection;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

public interface YamlContainer {
    ConfigSection getConfig();

    ConfigAccessor accessor();

    void saveConfig();

    default boolean isLoaded() {
        return this.getConfig() != null;
    }

    File getFile();

    default void createFile() {
        try {
            this.getFile().createNewFile();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    static MappingNode getRootOf(String name, InputStream is) {
        LoadSettings settings = new LoadSettings();
        settings.setLabel(name);
        Load load2 = new Load(settings);
        Composer composer = load2.createComposer(is);
        MappingNode root = composer.getRoot();
        load2.construct(root);
        return root;
    }

    static NodeValidator parseValidator(String label, String resourcePath) {
        InputStream is = CastelPlugin.getInstance().getResource(resourcePath);
        return YamlContainer.parseValidator(is, label);
    }

    static NodeValidator parseValidator(InputStream is, String label) {
        LoadSettings settings = new LoadSettings();
        settings.setLabel(label + " Schema");
        Load load2 = new Load(settings);
        Composer composer = load2.createComposer(is);
        MappingNode schemaYml = composer.getRoot();
        load2.construct(schemaYml);
        return Validator.parseSchema(schemaYml);
    }

    YamlContainer load();

    void reload();
}


