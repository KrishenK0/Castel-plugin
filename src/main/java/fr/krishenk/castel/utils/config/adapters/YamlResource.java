package fr.krishenk.castel.utils.config.adapters;

import fr.krishenk.castel.CastelPlugin;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.io.InputStream;

public class YamlResource extends YamlWithDefaults {
    protected final Plugin plugin;
    protected String resourcePath;

    public YamlResource(Plugin plugin, File file, String resourcePath) {
        super(file);
        this.resourcePath = resourcePath;
        this.plugin = plugin;
    }

    public YamlResource(File file, String resourcePath) {
        this(CastelPlugin.getInstance(), file, resourcePath);
    }

    public YamlResource(File file) {
        this(file, file.getName());
    }

    @Override
    protected InputStream getDefaultsInputStream() {
        return this.resourcePath == null ? null : this.plugin.getResource(this.resourcePath);
    }

    @Override
    protected String getDefaultsPath() {
        return this.resourcePath;
    }

    @Override
    public YamlResource load() {
        super.load();
        return this;
    }

    @Override
    protected InputStream getSchemaInputStream() {
        return this.plugin.getResource("schemas/" + this.resourcePath);
    }
}