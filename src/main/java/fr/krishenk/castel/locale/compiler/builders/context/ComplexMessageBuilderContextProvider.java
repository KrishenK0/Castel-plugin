package fr.krishenk.castel.locale.compiler.builders.context;

import fr.krishenk.castel.locale.compiler.MessageCompilerSettings;
import fr.krishenk.castel.locale.compiler.MessagePiece;
import fr.krishenk.castel.locale.provider.MessageBuilder;
import fr.krishenk.castel.utils.XComponentBuilder;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.TextComponent;

public class ComplexMessageBuilderContextProvider extends MessageBuilderContextProvider {
    private final XComponentBuilder builder;
    private TextComponent component;
    private int jsonLength;

    public ComplexMessageBuilderContextProvider(XComponentBuilder builder, TextComponent first, MessageBuilder settings, MessageCompilerSettings compilerSettings) {
        super(compilerSettings, settings);
        this.builder = builder;
        this.component = new TextComponent(first);
        this.component.setText("");
    }

    public XComponentBuilder getBuilder() {
        return this.builder;
    }

    public TextComponent getComponent() {
        return this.component;
    }

    public void newComponent(BaseComponent[] components) {
        BaseComponent lastComponent = components[components.length - 1];
        TextComponent newComp = new TextComponent();
        XComponentBuilder.copyFormatting(newComp, lastComponent, ComponentBuilder.FormatRetention.FORMATTING);
        this.builder.append(this.component, ComponentBuilder.FormatRetention.NONE);
        for (BaseComponent comp : components) {
            this.builder.append(comp, ComponentBuilder.FormatRetention.NONE);
        }
        this.component = newComp;
    }

    public TextComponent newComponent(ComponentBuilder.FormatRetention formatRetention) {
        TextComponent component = new TextComponent();
        XComponentBuilder.copyFormatting(component, this.component, formatRetention);
        this.builder.append(this.component, ComponentBuilder.FormatRetention.NONE);
        this.component = component;
        return this.component;
    }

    public void appendRemaining() {
        this.builder.append(this.component, ComponentBuilder.FormatRetention.NONE);
    }

    @Override
    public void build(MessagePiece messagePiece) {
        messagePiece.build(this);
        this.jsonLength += messagePiece.jsonLength();
        if (this.jsonLength >= 212144) {
            this.builder.newPacket();
            this.jsonLength = 0;
        }
    }
}

