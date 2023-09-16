package fr.krishenk.castel.utils.config.adapters;

import java.io.File;
import java.io.InputStream;


public class YamlFiledDefaults
        extends YamlWithDefaults {
    protected final File defaultsFile;

    public YamlFiledDefaults(File file, File defaultsFile) {
        super(file);
        this.defaultsFile = defaultsFile;
    }

    @Override
    protected InputStream getDefaultsInputStream() {
        return YamlFiledDefaults.inputStreamOf(this.defaultsFile);
    }

    @Override
    protected String getDefaultsPath() {
        return this.defaultsFile.toString();
    }

    @Override
    protected InputStream getSchemaInputStream() {
        return null;
    }
}