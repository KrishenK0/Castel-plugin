
package fr.krishenk.castel.libs.snakeyaml.api;

public interface StreamDataWriter {
    default public void flush() {
    }

    public void write(String var1);

    public void write(String var1, int var2, int var3);

    public void closeWriter();
}

