
package fr.krishenk.castel.libs.snakeyaml.api;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CodingErrorAction;
import java.nio.charset.StandardCharsets;

public class YamlUnicodeReader
extends Reader {
    private static final Charset UTF8 = StandardCharsets.UTF_8;
    private static final Charset UTF16BE = StandardCharsets.UTF_16BE;
    private static final Charset UTF16LE = StandardCharsets.UTF_16LE;
    private static final Charset UTF32BE = Charset.forName("UTF-32BE");
    private static final Charset UTF32LE = Charset.forName("UTF-32LE");
    final PushbackInputStream internalIn;
    InputStreamReader internalIn2;
    Charset encoding = UTF8;
    private static final int BOM_SIZE = 4;

    public YamlUnicodeReader(InputStream in) {
        this.internalIn = new PushbackInputStream(in, 4);
    }

    public Charset getEncoding() {
        return this.encoding;
    }

    protected void init() throws IOException {
        int unread;
        if (this.internalIn2 != null) {
            return;
        }
        byte[] bom = new byte[4];
        int n = this.internalIn.read(bom, 0, bom.length);
        if (bom[0] == 0 && bom[1] == 0 && bom[2] == -2 && bom[3] == -1) {
            this.encoding = UTF32BE;
            unread = n - 4;
        } else if (bom[0] == -1 && bom[1] == -2 && bom[2] == 0 && bom[3] == 0) {
            this.encoding = UTF32LE;
            unread = n - 4;
        } else if (bom[0] == -17 && bom[1] == -69 && bom[2] == -65) {
            this.encoding = UTF8;
            unread = n - 3;
        } else if (bom[0] == -2 && bom[1] == -1) {
            this.encoding = UTF16BE;
            unread = n - 2;
        } else if (bom[0] == -1 && bom[1] == -2) {
            this.encoding = UTF16LE;
            unread = n - 2;
        } else {
            this.encoding = UTF8;
            unread = n;
        }
        if (unread > 0) {
            this.internalIn.unread(bom, n - unread, unread);
        }
        CharsetDecoder decoder = this.encoding.newDecoder().onUnmappableCharacter(CodingErrorAction.REPORT);
        this.internalIn2 = new InputStreamReader((InputStream)this.internalIn, decoder);
    }

    @Override
    public void close() throws IOException {
        this.init();
        this.internalIn2.close();
    }

    @Override
    public int read(char[] cbuf, int off, int len) throws IOException {
        this.init();
        return this.internalIn2.read(cbuf, off, len);
    }
}

