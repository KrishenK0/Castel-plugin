package fr.krishenk.castel.locale.compiler.builders.context;

import fr.krishenk.castel.locale.compiler.MessageCompilerSettings;
import fr.krishenk.castel.locale.compiler.MessagePiece;
import fr.krishenk.castel.locale.provider.MessageBuilder;

import java.util.ArrayList;
import java.util.List;

public class PlainMessageBuilderContextProvider extends MessageBuilderContextProvider {
    private final List<StringBuilder> builders = new ArrayList<>(3);
    private StringBuilder currentBuilder = new StringBuilder(10);

    public PlainMessageBuilderContextProvider(MessageBuilder settings, MessageCompilerSettings compilerSettings) {
        super(compilerSettings, settings);
    }

    public StringBuilder getBuilder() {
        return this.currentBuilder;
    }

    public void newBuilder() {
        this.currentBuilder = new StringBuilder();
    }

    public String merge() {
        this.builders.add(this.currentBuilder);
        StringBuilder first = this.builders.get(0);
        for (StringBuilder builder : this.builders) {
            if (first == builder) continue;
            first.append('\n').append((CharSequence)builder);
        }
        return first.toString();
    }

    public List<StringBuilder> getBuilders() {
        return this.builders;
    }

    @Override
    public void build(MessagePiece messagePiece) {
        messagePiece.build(this);
    }
}

