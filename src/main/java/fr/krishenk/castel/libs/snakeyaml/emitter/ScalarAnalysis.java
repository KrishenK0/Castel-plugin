
package fr.krishenk.castel.libs.snakeyaml.emitter;

final class ScalarAnalysis {
    private final boolean empty;
    private final boolean multiline;
    private final boolean allowFlowPlain;
    private final boolean allowBlockPlain;
    private final boolean allowSingleQuoted;
    private final boolean allowBlock;

    public ScalarAnalysis(boolean empty, boolean multiline, boolean allowFlowPlain, boolean allowBlockPlain, boolean allowSingleQuoted, boolean allowBlock) {
        this.empty = empty;
        this.multiline = multiline;
        this.allowFlowPlain = allowFlowPlain;
        this.allowBlockPlain = allowBlockPlain;
        this.allowSingleQuoted = allowSingleQuoted;
        this.allowBlock = allowBlock;
    }

    public boolean isEmpty() {
        return this.empty;
    }

    public boolean isMultiline() {
        return this.multiline;
    }

    public boolean isAllowFlowPlain() {
        return this.allowFlowPlain;
    }

    public boolean isAllowBlockPlain() {
        return this.allowBlockPlain;
    }

    public boolean isAllowSingleQuoted() {
        return this.allowSingleQuoted;
    }

    public boolean isAllowBlock() {
        return this.allowBlock;
    }
}

