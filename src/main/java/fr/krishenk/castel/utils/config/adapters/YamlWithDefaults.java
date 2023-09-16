package fr.krishenk.castel.utils.config.adapters;

import fr.krishenk.castel.CLogger;
import fr.krishenk.castel.config.ConfigAccessor;
import fr.krishenk.castel.config.implementation.YamlConfigAccessor;
import fr.krishenk.castel.config.managers.ConfigManager;
import fr.krishenk.castel.libs.snakeyaml.api.*;
import fr.krishenk.castel.libs.snakeyaml.exceptions.ComposerException;
import fr.krishenk.castel.libs.snakeyaml.exceptions.ParserException;
import fr.krishenk.castel.libs.snakeyaml.exceptions.ScannerException;
import fr.krishenk.castel.libs.snakeyaml.nodes.MappingNode;
import fr.krishenk.castel.libs.snakeyaml.validation.NodeValidator;
import fr.krishenk.castel.libs.snakeyaml.validation.ValidationFailure;
import fr.krishenk.castel.libs.snakeyaml.validation.Validator;
import fr.krishenk.castel.utils.config.ConfigSection;
import fr.krishenk.castel.utils.config.CustomConfigValidators;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.StandardOpenOption;
import java.util.List;
import java.util.Objects;

public abstract class YamlWithDefaults implements YamlContainer {
    private static final int DEFAULT_BUFFER_SIZE = 8192;
    protected final @Nullable File file;
    protected ConfigSection config;
    protected ConfigSection defaults;
    protected NodeValidator validator;

    protected YamlWithDefaults(@Nullable File file) {
        this.file = file;
    }

    public List<ValidationFailure> validate() {
        Objects.requireNonNull(this.validator, () -> "Cannot validate config with no validator attached: " + this.file.getPath());
        Objects.requireNonNull(this.config, () -> "Cannot validate config that isn't loaded yet: " + this.file.getPath());
        return Validator.validate(this.config.getNode(), this.validator, CustomConfigValidators.getValidators());
    }

    protected static void transferTo(InputStream in, OutputStream out) throws IOException {
        int read;
        byte[] buffer = new byte[8192];
        while ((read = in.read(buffer)) >= 0) {
            out.write(buffer, 0, read);
        }
    }

    @Override
    public void createFile() {
        Objects.requireNonNull(this.file, "No file path specified to generate the config");
        if (!this.file.exists()) {
            this.saveDefaultConfig();
        }
    }

    @Override
    public final ConfigSection getConfig() {
        return this.config;
    }

    @Override
    public final ConfigAccessor accessor() {
        return new YamlConfigAccessor(this.config, this.defaults);
    }

    @Override
    public void saveConfig() {
        Objects.requireNonNull(this.file, "No file path specified to save the config");
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
    public final @Nullable File getFile() {
        return this.file;
    }

    protected abstract InputStream getDefaultsInputStream();

    protected abstract String getDefaultsPath();

    protected static InputStream inputStreamOf(File file) {
        try {
            return new FileInputStream(file);
        }
        catch (FileNotFoundException e) {
            return null;
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public boolean saveDefaultConfig() {
        block22: {
            Objects.requireNonNull(this.file, "No file path specified to save the default config");
            InputStream in = this.getDefaultsInputStream();
            if (in == null) {
                return false;
            }
            File outDir = this.file.getParentFile();
            if (!outDir.exists()) {
                outDir.mkdirs();
            } else if (this.file.exists()) {
                return false;
            }
            ConfigManager.beforeWrite(this);
            try {
                boolean bl;
                block21: {
                    OutputStream out = Files.newOutputStream(this.file.toPath(), new OpenOption[0]);
                    try {
                        YamlWithDefaults.transferTo(in, out);
                        bl = true;
                        if (out == null) break block21;
                    }
                    catch (Throwable throwable) {
                        try {
                            if (out != null) {
                                try {
                                    out.close();
                                }
                                catch (Throwable throwable2) {
                                    throwable.addSuppressed(throwable2);
                                }
                            }
                            throw throwable;
                        }
                        catch (IOException ex) {
                            ex.printStackTrace();
                            break block22;
                        }
                    }
                    out.close();
                }
                return bl;
            } catch (IOException e) {
                throw new RuntimeException(e);
            } finally {
                try {
                    in.close();
                }
                catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return false;
    }

    protected abstract InputStream getSchemaInputStream();

    protected void loadSchema() {
        if (this.defaults == null) {
            return;
        }
        InputStream is = this.getSchemaInputStream();
        if (is == null) {
            return;
        }
        MappingNode schema = YamlContainer.getRootOf(this.file.getName(), is);
        this.validator = Validator.parseSchema(schema);
        try {
            is.close();
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void setSchema(NodeValidator validator) {
        this.validator = validator;
    }

    @Override
    public YamlWithDefaults load() {
        this.loadDefaults();
        this.loadSchema();
        this.reload();
        return this;
    }

    protected void loadDefaults() {
        try (InputStream res = this.getDefaultsInputStream()){
            if (res != null) {
                try {
                    this.defaults = new ConfigSection(null, YamlContainer.getRootOf("reader", res));
                }
                catch (UnsupportedOperationException | ComposerException | ParserException | ScannerException ex) {
                    CLogger.error("Failed to load defaults for config '" + this.file.getAbsolutePath() + "' from '" + this.getDefaultsPath() + "':");
                    ex.printStackTrace();
                }
            } else if (this.file == null) {
                throw new IllegalStateException("Internal config not found");
            }
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void reload() {
        if (this.file == null) {
            this.config = this.defaults;
            return;
        }
        if (this.file.exists()) {
            try {
                InputStream is = YamlWithDefaults.inputStreamOf(this.file);
                this.config = new ConfigSection(null, YamlContainer.getRootOf(this.file.getName(), is));
                is.close();
            }
            catch (UnsupportedOperationException | ComposerException | ParserException | ScannerException ex) {
                this.config = this.defaults;
                CLogger.error("Invalid config when loading '" + this.file.getAbsolutePath() + "':");
                ex.printStackTrace();
            }
            catch (IOException e) {
                this.config = this.defaults;
                CLogger.error("Error when loading config '" + this.file.getAbsolutePath() + "':");
                throw new RuntimeException(e);
            }
        } else {
            this.createFile();
            this.config = this.defaults;
        }
    }

    public YamlWithDefaults createEmptyConfigIfNull() {
        if (this.config == null) {
            this.createFile();
            this.config = new ConfigSection(null, new MappingNode());
        }
        return this;
    }

    public boolean isDefault() {
        return this.config == this.defaults;
    }

    public final ConfigSection getDefaults() {
        return this.defaults;
    }

    public void update() {
        UpdateResult result;
        if (this.isDefault()) {
            return;
        }
        if (this.defaults == null) {
            throw new IllegalStateException("The config " + this.file.getName() + " cannot be updated because there's no default config for it");
        }
        ConfigManager.beforeWrite(this);
        try {
            result = Updater.updateConfig(this.config.getCurrentNode(), this.defaults.getNode().clone(), this.validator, this.file.toPath(), new Dump(new DumpSettings()));
        }
        catch (Throwable e) {
            CLogger.error("An error occurred while attempting to update " + this.file.getAbsolutePath() + ": " + e.getMessage() + (e.getMessage().contains("another process") ? ". This is probably caused by your text editor" : ""));
            return;
        }
        for (UpdateResult.Change change : result.getChanges()) {
            StringBuilder path = new StringBuilder();
            int paths = change.getPath().size();
            int i = 0;
            for (String key : change.getPath()) {
                path.append(key);
                if (++i == paths) continue;
                path.append(" -> ");
            }
            CLogger.warn("Added missing config option to " + this.file.getName() + ": " + path);
        }
        this.load();
    }
}
