package fr.krishenk.castel.locale.compiler;

import java.util.function.Consumer;

public class MessageCompilerSettings {
    protected MessageTokenHandler[] tokenHandlers;
    protected boolean validate;
    protected boolean colorize;
    protected boolean plainOnly = true;
    protected boolean translatePlaceholders;
    protected boolean allowNewLines;
    protected Consumer<MessageCompiler> errorHandler;

    public MessageCompilerSettings() {
    }

    public MessageCompilerSettings(boolean validate, boolean plainOnly, boolean colorize, boolean translatePlaceholders, boolean allowNewLines, MessageTokenHandler[] tokenHandlers) {
        this.tokenHandlers = tokenHandlers;
        this.validate = validate;
        this.plainOnly = plainOnly;
        this.colorize = colorize;
        this.allowNewLines = allowNewLines;
        this.translatePlaceholders = translatePlaceholders;
    }

    public static MessageCompilerSettings all() {
        return new MessageCompilerSettings(true, false, true, true, true, null);
    }

    public static MessageCompilerSettings none() {
        return new MessageCompilerSettings(false, true, false, false, false, null);
    }

    public MessageCompilerSettings validate() {
        this.validate = true;
        return this;
    }

    public MessageCompilerSettings colorize() {
        this.colorize = true;
        return this;
    }

    public MessageCompilerSettings hovers() {
        this.plainOnly = false;
        return this;
    }

    public MessageCompilerSettings translatePlaceholders() {
        this.translatePlaceholders = true;
        return this;
    }

    public MessageCompilerSettings allowNewLines() {
        this.allowNewLines = true;
        return this;
    }

    public MessageCompilerSettings withTokenHandlers(MessageTokenHandler[] tokenHandlers) {
        if (this.tokenHandlers != null) {
            throw new IllegalStateException("Overriding token handlers");
        }
        this.tokenHandlers = tokenHandlers;
        return this;
    }

    public MessageCompilerSettings withErrorHandler(Consumer<MessageCompiler> errorHandler) {
        this.errorHandler = errorHandler;
        return this;
    }

    public MessageCompilerSettings withTokenHandler(MessageTokenHandler tokenHandler) {
        return this.withTokenHandlers(new MessageTokenHandler[]{tokenHandler});
    }

    public MessageTokenHandler[] getTokenHandlers() {
        return this.tokenHandlers;
    }
}
