
package fr.krishenk.castel.libs.snakeyaml.api;

import fr.krishenk.castel.libs.snakeyaml.composer.Composer;
import fr.krishenk.castel.libs.snakeyaml.constructor.BaseConstructor;
import fr.krishenk.castel.libs.snakeyaml.constructor.StandardConstructor;
import fr.krishenk.castel.libs.snakeyaml.nodes.MappingNode;
import fr.krishenk.castel.libs.snakeyaml.parser.ParserImpl;
import fr.krishenk.castel.libs.snakeyaml.scanner.StreamReader;

import java.io.InputStream;
import java.io.Reader;
import java.util.Objects;

public final class Load {
    private final LoadSettings settings;
    private final BaseConstructor constructor;

    public Load(LoadSettings settings) {
        this(settings, new StandardConstructor(settings));
    }

    public Load(LoadSettings settings, BaseConstructor constructor) {
        this.settings = Objects.requireNonNull(settings, "LoadSettings cannot be null");
        this.constructor = Objects.requireNonNull(constructor, "BaseConstructor cannot be null");
    }

    public Composer createComposer(StreamReader streamReader) {
        return new Composer(this.settings, new ParserImpl(this.settings, streamReader));
    }

    public Composer createComposer(InputStream yamlStream) {
        return this.createComposer(new StreamReader(this.settings, new YamlUnicodeReader(yamlStream)));
    }

    public Composer createComposer(String yaml) {
        return this.createComposer(new StreamReader(this.settings, yaml));
    }

    public Composer createComposer(Reader yamlReader) {
        return this.createComposer(new StreamReader(this.settings, yamlReader));
    }

    protected Object loadOne(Composer composer) {
        return this.constructor.construct(composer.getRoot());
    }

    public void construct(MappingNode root) {
        this.constructor.construct(root);
    }

    public Object loadFromInputStream(InputStream yamlStream) {
        Objects.requireNonNull(yamlStream, "InputStream cannot be null");
        return this.loadOne(this.createComposer(yamlStream));
    }

    public Object loadFromReader(Reader yamlReader) {
        Objects.requireNonNull(yamlReader, "Reader cannot be null");
        return this.loadOne(this.createComposer(yamlReader));
    }

    public Object loadFromString(String yaml) {
        Objects.requireNonNull(yaml, "String cannot be null");
        return this.loadOne(this.createComposer(yaml));
    }
}

