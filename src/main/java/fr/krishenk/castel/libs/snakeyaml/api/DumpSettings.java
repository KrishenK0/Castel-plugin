
package fr.krishenk.castel.libs.snakeyaml.api;

import fr.krishenk.castel.libs.snakeyaml.common.FlowStyle;
import fr.krishenk.castel.libs.snakeyaml.serializer.AnchorGenerator;
import fr.krishenk.castel.libs.snakeyaml.serializer.NumberAnchorGenerator;

public final class DumpSettings {
    private final AnchorGenerator anchorGenerator;
    private final boolean useUnicodeEncoding;
    private final int indent;
    private final int indicatorIndent;
    private final int width;
    private final boolean indentWithIndicator;

    public DumpSettings(AnchorGenerator anchorGenerator, FlowStyle defaultFlowStyle, boolean useUnicodeEncoding, int indent, int indicatorIndent, int width, String bestLineBreak, boolean indentWithIndicator) {
        this.anchorGenerator = anchorGenerator;
        this.useUnicodeEncoding = useUnicodeEncoding;
        this.indent = indent;
        this.indicatorIndent = indicatorIndent;
        this.width = width;
        this.indentWithIndicator = indentWithIndicator;
    }

    public DumpSettings() {
        this.anchorGenerator = new NumberAnchorGenerator(0);
        this.useUnicodeEncoding = true;
        this.indent = 2;
        this.indicatorIndent = 2;
        this.width = 300;
        this.indentWithIndicator = false;
    }

    public AnchorGenerator getAnchorGenerator() {
        return this.anchorGenerator;
    }

    public boolean isUseUnicodeEncoding() {
        return this.useUnicodeEncoding;
    }

    public int getIndent() {
        return this.indent;
    }

    public int getIndicatorIndent() {
        return this.indicatorIndent;
    }

    public int getWidth() {
        return this.width;
    }

    public boolean getIndentWithIndicator() {
        return this.indentWithIndicator;
    }
}

