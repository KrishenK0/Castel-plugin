
package fr.krishenk.castel.libs.snakeyaml.api;

import fr.krishenk.castel.libs.snakeyaml.emitter.Emitter;
import fr.krishenk.castel.libs.snakeyaml.nodes.Node;
import fr.krishenk.castel.libs.snakeyaml.representer.BaseRepresenter;
import fr.krishenk.castel.libs.snakeyaml.representer.StandardRepresenter;
import fr.krishenk.castel.libs.snakeyaml.serializer.Serializer;

import java.io.IOException;
import java.io.StringWriter;
import java.util.Collections;
import java.util.Iterator;
import java.util.Objects;

public class Dump {
    protected final DumpSettings settings;
    protected final BaseRepresenter representer;

    public Dump(DumpSettings settings) {
        this(settings, new StandardRepresenter(settings));
    }

    public Dump(DumpSettings settings, BaseRepresenter representer) {
        this.settings = Objects.requireNonNull(settings, "DumpSettings cannot be null");
        this.representer = Objects.requireNonNull(representer, "Representer cannot be null");
    }

    public void dumpAll(Iterator<?> instancesIterator, StreamDataWriter streamDataWriter) {
        Serializer serializer = new Serializer(this.settings, new Emitter(this.settings, streamDataWriter));
        while (instancesIterator.hasNext()) {
            Object instance = instancesIterator.next();
            Node node = this.representer.represent(instance);
            serializer.serializeDocument(node);
        }
    }

    public void dump(Object yaml, StreamDataWriter streamDataWriter) {
        Iterator<Object> iter = Collections.singleton(yaml).iterator();
        this.dumpAll(iter, streamDataWriter);
    }

    public String dumpAllToString(Iterator<?> instancesIterator) {
        StreamToStringWriter writer = new StreamToStringWriter();
        this.dumpAll(instancesIterator, writer);
        return writer.toString();
    }

    public String dumpToString(Object yaml) {
        StreamToStringWriter writer = new StreamToStringWriter();
        this.dump(yaml, writer);
        return writer.toString();
    }

    public void dumpNode(Node node, StreamDataWriter streamDataWriter) {
        Serializer serializer = new Serializer(this.settings, new Emitter(this.settings, streamDataWriter));
        serializer.serializeDocument(node);
    }

    public static final class StreamToStringWriter
    extends StringWriter
    implements StreamDataWriter {
        @Override
        public void closeWriter() {
            try {
                this.close();
            }
            catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }
}

