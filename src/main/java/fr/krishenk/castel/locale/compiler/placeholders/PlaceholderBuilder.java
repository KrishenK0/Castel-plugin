package fr.krishenk.castel.locale.compiler.placeholders;

public class PlaceholderBuilder {
    private static final char CLOSURE = '%';
    private static final int PLACEHOLDER_ID_APPROX_LENGTH = 10;
    private static final int PLACEHOLDER_PARAM_APPROX_LENGTH = 20;
    private final char[] str;
    private final int len;
    private int start;
    private final StringBuilder idBuilder = new StringBuilder(10);
    private final StringBuilder paramBuilder = new StringBuilder(20);
    private boolean idIsLocal = false;
    private RuntimeException used;

    public int getStopIndex() {
        return this.start;
    }

    public PlaceholderType build() {
        return PlaceholderParser.parseType(this.idIsLocal, this.idBuilder.toString(), this.paramBuilder.toString());
    }

    public PlaceholderBuilder(int start, char[] str) {
        this.str = str;
        this.start = start;
        this.len = str.length;
    }

    public boolean evaluate() {
        if (this.used != null) {
            throw new IllegalStateException("This placeholder builder was already used", this.used);
        }
        this.used = new RuntimeException("Used here");
        boolean isParsingId = true;
        if (this.start == this.len) {
            return false;
        }
        if (this.str[this.start] == '%') {
            return false;
        }
        while (this.start < this.len) {
            char ch = this.str[this.start];
            if (ch == '%') {
                return this.idBuilder.length() != 0;
            }
            if (ch == '_') {
                if (this.idIsLocal) {
                    this.idBuilder.append('_');
                } else if (isParsingId) {
                    isParsingId = false;
                } else {
                    this.paramBuilder.append('_');
                }
            } else if (this.idIsLocal) {
                this.idBuilder.append(ch);
            } else if (isParsingId) {
                if (!(ch >= 'a' && ch <= 'z' || ch >= 'A' && ch <= 'Z')) {
                    if (ch == '-' || ch >= '0' && ch <= '9') {
                        this.idIsLocal = true;
                    } else {
                        return false;
                    }
                }
                this.idBuilder.append(ch);
            } else {
                this.paramBuilder.append(ch);
            }
            ++this.start;
        }
        return false;
    }
}


