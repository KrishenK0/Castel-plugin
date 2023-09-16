package fr.krishenk.castel.utils.config.adapters;

import fr.krishenk.castel.CLogger;
import fr.krishenk.castel.config.ConfigAccessor;
import fr.krishenk.castel.config.implementation.YamlConfigAccessor;
import fr.krishenk.castel.config.managers.ConfigManager;
import fr.krishenk.castel.libs.snakeyaml.api.Dump;
import fr.krishenk.castel.libs.snakeyaml.api.DumpSettings;
import fr.krishenk.castel.libs.snakeyaml.api.SimpleWriter;
import fr.krishenk.castel.libs.snakeyaml.exceptions.ScannerException;
import fr.krishenk.castel.utils.config.ConfigSection;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.Objects;

public class YamlFile implements YamlContainer {
    protected @NonNull File file;
    protected ConfigSection config;

    public YamlFile(File file) {
        this.file = Objects.requireNonNull(file);
    }

    @Override
    public ConfigSection getConfig() {
        return this.config;
    }

    @Override
    public ConfigAccessor accessor() {
        return new YamlConfigAccessor(this.config, this.config);
    }

    @Override
    public void saveConfig() {
        Dump dumper = new Dump(new DumpSettings());
        ConfigManager.beforeWrite(this);
        try (BufferedWriter writer = Files.newBufferedWriter(this.file.toPath(), StandardOpenOption.CREATE, StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING);){
            dumper.dumpNode(this.config.getNode(), new SimpleWriter(writer));
        }
        catch (IOException e) {
            CLogger.error("Error while attempting to save configuration file " + this.file.getName() + ": ");
            e.printStackTrace();
        }
    }

    @Override
    public File getFile() {
        return this.file;
    }

    @Override
    public YamlFile load() {
        this.reload();
        return this;
    }

    @Override
    public void reload() {
        if (this.file.exists()) {
            try {
                try (FileInputStream fis = new FileInputStream(this.file);){
                    this.config = new ConfigSection(null, YamlContainer.getRootOf(this.file.getName(), fis));
                }
                catch (IOException e) {
                    throw new AssertionError((Object)e);
                }
            }
            catch (ScannerException ex) {
                this.config = ConfigSection.empty();
                CLogger.error("Failed to load config '" + this.file.getAbsolutePath() + "':");
                ex.printStackTrace();
            }
        } else {
            this.config = ConfigSection.empty();
        }
    }
}

