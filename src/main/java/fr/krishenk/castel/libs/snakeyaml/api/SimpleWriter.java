
package fr.krishenk.castel.libs.snakeyaml.api;

import java.io.IOException;
import java.io.Writer;

public class SimpleWriter
implements StreamDataWriter {
    private final Writer writer;

    public SimpleWriter(Writer writer) {
        this.writer = writer;
    }

    @Override
    public void write(String str) {
        try {
            this.writer.write(str);
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void write(String str, int off, int len) {
        try {
            this.writer.write(str, off, len);
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void closeWriter() {
        try {
            this.writer.close();
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}

