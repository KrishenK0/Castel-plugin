package fr.krishenk.castel.locale.compiler;

import fr.krishenk.castel.libs.xseries.ReflectionUtils;
import fr.krishenk.castel.locale.MessageObjectBuilder;
import fr.krishenk.castel.locale.compiler.builders.context.ComplexMessageBuilderContextProvider;
import fr.krishenk.castel.locale.compiler.builders.context.PlainMessageBuilderContextProvider;
import fr.krishenk.castel.locale.compiler.placeholders.PlaceholderType;
import fr.krishenk.castel.locale.messenger.Messenger;
import fr.krishenk.castel.locale.provider.MessageBuilder;
import fr.krishenk.castel.utils.ColorUtils;
import fr.krishenk.castel.utils.XComponentBuilder;
import net.md_5.bungee.api.chat.*;
import org.bukkit.ChatColor;

import java.util.*;
import java.util.function.Supplier;

public abstract class MessagePiece {
    public abstract void build(ComplexMessageBuilderContextProvider var1);

    public abstract void build(PlainMessageBuilderContextProvider var1);

    public abstract int length();

    public abstract int jsonLength();

    public static final class Hover
            extends MessagePiece {
        private final ClickEvent.Action clickActionType;
        private final MessagePiece[] normalMessage;
        private final MessagePiece[] hoverMessage;
        private final MessagePiece[] clickAction;
        private static final int JSON_LEN = "\"clickEvent\":{\"action\":\"ACTION\",\"value\":\"VALUE\"},\"hoverEvent\":{\"action\":\"show_text\",\"contents\":[]}".length();

        public Hover(ClickEvent.Action clickActionType, MessagePiece[] normalMessage, MessagePiece[] hoverMessage, MessagePiece[] clickAction) {
            this.clickActionType = clickActionType;
            this.normalMessage = Objects.requireNonNull(normalMessage);
            this.hoverMessage = hoverMessage;
            this.clickAction = clickAction;
        }

        @Override
        public void build(ComplexMessageBuilderContextProvider context) {
            PlainMessageBuilderContextProvider actionProvider = new PlainMessageBuilderContextProvider(context.getSettings(), context.getCompilerSettings());
            for (MessagePiece piece : this.clickAction) {
                piece.build(actionProvider);
            }
            ClickEvent clickEvent = this.clickActionType == null ? null : new ClickEvent(this.clickActionType, actionProvider.merge());
            ComplexMessageBuilderContextProvider msgProvider = new ComplexMessageBuilderContextProvider(new XComponentBuilder(), context.getComponent(), context.getSettings(), context.getCompilerSettings());
            for (MessagePiece piece : this.normalMessage) {
                msgProvider.build(piece);
            }
            msgProvider.appendRemaining();
            ComplexMessageBuilderContextProvider hoverProvider = new ComplexMessageBuilderContextProvider(new XComponentBuilder(), new TextComponent(), context.getSettings(), context.getCompilerSettings());
            for (MessagePiece piece : this.hoverMessage) {
                hoverProvider.build(piece);
            }
            hoverProvider.appendRemaining();
            HoverEvent hoverEvent = MessageCompiler.constructHoverEvent(hoverProvider.getBuilder().createSingular());
            context.newComponent(ComponentBuilder.FormatRetention.NONE);
            TextComponent parent = new TextComponent();
            parent.setHoverEvent(hoverEvent);
            if (clickEvent != null) {
                parent.setClickEvent(clickEvent);
            }
            parent.addExtra(msgProvider.getBuilder().buildTextComponent());
            context.getBuilder().append(parent);
        }

        @Override
        public void build(PlainMessageBuilderContextProvider context) {
            Arrays.stream(this.normalMessage).forEach(x -> x.build(context));
        }

        public MessagePiece[] getHoverMessage() {
            return this.hoverMessage;
        }

        public MessagePiece[] getClickAction() {
            return this.clickAction;
        }

        public MessagePiece[] getNormalMessage() {
            return this.normalMessage;
        }

        @Override
        public int length() {
            return Arrays.stream(this.normalMessage).mapToInt(MessagePiece::length).sum();
        }

        @Override
        public int jsonLength() {
            return JSON_LEN + this.length();
        }

        public String toString() {
            String actionStr = "No Action";
            if (this.clickActionType != null) {
                actionStr = this.clickActionType + " -> " + Arrays.toString(this.clickAction);
            }
            return "Hover{ length=" + this.length() + " | " + Arrays.toString(this.normalMessage) + ';' + Arrays.toString(this.hoverMessage) + ';' + actionStr + " }";
        }
    }

    public static final class ColorAccessor
            extends Color {
        private final int index;
        private final Variable variable;

        public ColorAccessor(int index, Variable variable) {
            this.index = index;
            this.variable = variable;
        }

        public List<MessagePiece> getLastColors(MessageBuilder context) {
            MessageObject obj;
            ArrayList<MessagePiece> pieces = new ArrayList<MessagePiece>(3);
            Object translated = this.variable.getPlaceholder(context);
            if (translated == null) {
                return Collections.singletonList(new Plain('{' + this.variable.placeholder.rebuild() + " & " + this.index + '}'));
            }
            if (translated instanceof MessageObject) {
                obj = (MessageObject)translated;
            } else if (translated instanceof PlaceholderTranslationContext) {
                obj = this.variable.getCompiled(translated);
            } else {
                pieces.add(new Plain('{' + this.variable.placeholder.rebuild() + " & " + this.index + " (this special placeholder which is of type" + translated.getClass() + " -> " + translated + " is not supported for color accessors}"));
                return pieces;
            }
            return obj.findColorPieces(Math.abs(this.index), this.index < 0);
        }

        @Override
        public void build(ComplexMessageBuilderContextProvider context) {
            for (MessagePiece piece : this.getLastColors(context.getSettings())) {
                context.build(piece);
            }
        }

        @Override
        public void build(PlainMessageBuilderContextProvider context) {
            for (MessagePiece piece : this.getLastColors(context.getSettings())) {
                context.build(piece);
            }
        }

        @Override
        public int jsonLength() {
            return 5;
        }

        @Override
        public int length() {
            return 4;
        }

        public String toString() {
            return "ColorAccessor{ " + this.variable.toString() + " & " + this.index + " }";
        }
    }

    public static final class Variable
            extends MessagePiece {
        private final PlaceholderType placeholder;

        public Variable(PlaceholderType placeholder) {
            this.placeholder = placeholder;
        }

        @Override
        public void build(ComplexMessageBuilderContextProvider context) {
            Object translated = this.getPlaceholder(context.getSettings());
            if (translated == null) {
                context.newComponent(ComponentBuilder.FormatRetention.FORMATTING);
                context.getBuilder().append(this.placeholder.rebuild());
                return;
            }
            context.getSettings().usePrefix(false);
            if (translated instanceof MessageObjectBuilder) {
                MessageObjectBuilder obj = (MessageObjectBuilder)translated;
                XComponentBuilder build = obj.build(context.getComponent(), context.getSettings());
                BaseComponent[] comps = build.createSingular();
                context.newComponent(comps);
                return;
            }
            if (!(translated instanceof PlaceholderTranslationContext)) {
                TextComponent placeholderComp = context.newComponent(ComponentBuilder.FormatRetention.ALL);
                placeholderComp.setText(translated.toString());
                return;
            }
            MessageObject varObj = this.getCompiled(translated);
            XComponentBuilder varBuilder = varObj.build(context.getComponent(), context.getSettings());
            context.newComponent(varBuilder.createSingular());
        }

        public PlaceholderType getPlaceholder() {
            return this.placeholder;
        }

        MessageObject getCompiled(Object translated) {
            PlaceholderTranslationContext settings = (PlaceholderTranslationContext)translated;
            translated = settings.getValue();
            MessageCompilerSettings compilerSettings = settings.getSettings();
            return MessageCompiler.compile(translated.toString(), compilerSettings);
        }

        @Override
        public void build(PlainMessageBuilderContextProvider context) {
            Object translated = this.getPlaceholder(context.getSettings());
            if (translated == null) {
                context.getBuilder().append(this.placeholder.rebuild());
                return;
            }
            context.getSettings().usePrefix(false);
            if (translated instanceof MessageObjectBuilder) {
                MessageObjectBuilder obj = (MessageObjectBuilder)translated;
                String build = obj.buildPlain(context.getSettings());
                context.getBuilder().append(build);
                return;
            }
            if (!(translated instanceof PlaceholderTranslationContext)) {
                context.getBuilder().append(translated);
                return;
            }
            MessageObject varObj = this.getCompiled(translated);
            context.getBuilder().append(varObj.buildPlain(context.getSettings()));
        }

        public Object getPlaceholder(MessageBuilder context) {
            Object translated = this.placeholder.request(context);
            if (translated == null) {
                return null;
            }
            if (translated instanceof Supplier) {
                translated = ((Supplier)translated).get();
            }
            if (translated instanceof Messenger) {
                translated = ((Messenger)translated).getMessageObject(context.getLanguage());
            }
            return translated;
        }

        @Override
        public int length() {
            return this.placeholder.rebuild().length();
        }

        @Override
        public int jsonLength() {
            return 10;
        }

        public String toString() {
            return "Variable{ " + this.placeholder + " }";
        }
    }

    public static final class NewLine
            extends MessagePiece {
        @Override
        public void build(ComplexMessageBuilderContextProvider context) {
            TextComponent comp = context.getComponent();
            comp.setText(comp.getText() + '\n');
        }

        @Override
        public void build(PlainMessageBuilderContextProvider context) {
            context.getBuilders().add(context.getBuilder());
            context.newBuilder();
        }

        @Override
        public int length() {
            return 1;
        }

        @Override
        public int jsonLength() {
            return 0;
        }

        public String toString() {
            return "Newline";
        }
    }

    public static final class HexColor
            extends Color {
        private final java.awt.Color color;
        private final net.md_5.bungee.api.ChatColor componentColor;
        private static final int JSON_LEN = "\"color\":\"\",".length() + 12;

        public java.awt.Color getColor() {
            return this.color;
        }

        public HexColor(java.awt.Color color) {
            this.color = color;
            this.componentColor = ReflectionUtils.supports(16) ? net.md_5.bungee.api.ChatColor.of(color) : null;
        }

        @Override
        public void build(ComplexMessageBuilderContextProvider context) {
            if (this.componentColor == null) {
                ChatColor translated = ColorUtils.hexColorToLegacy(this.color);
                context.build(new SimpleColor(translated));
                return;
            }
            TextComponent comp = context.newComponent(ComponentBuilder.FormatRetention.NONE);
            comp.setColor(this.componentColor);
        }

        @Override
        public void build(PlainMessageBuilderContextProvider context) {
            if (this.componentColor == null) {
                ChatColor translated = ColorUtils.hexColorToLegacy(this.color);
                new SimpleColor(translated).build(context);
                return;
            }
            context.getBuilder().append(this.componentColor);
        }

        @Override
        public int length() {
            return 12;
        }

        @Override
        public int jsonLength() {
            return JSON_LEN;
        }

        public String toString() {
            return "Hex{ " + ColorUtils.toHexString(this.color) + " }";
        }
    }

    public static final class SimpleColor
            extends Color {
        private final ChatColor color;
        private static final int JSON_LEN = "\"color\":\"\",".length();

        public SimpleColor(ChatColor color) {
            this.color = color;
        }

        public boolean equals(Object obj) {
            if (!(obj instanceof SimpleColor)) {
                return false;
            }
            return this.color == ((SimpleColor)obj).color;
        }

        @Override
        public void build(ComplexMessageBuilderContextProvider context) {
            if (this.color.isColor()) {
                TextComponent comp = context.newComponent(ComponentBuilder.FormatRetention.NONE);
                comp.setColor(this.color.asBungee());
                return;
            }
            if (this.color == ChatColor.RESET) {
                context.newComponent(ComponentBuilder.FormatRetention.NONE);
                return;
            }
            TextComponent component = context.getComponent().getText().isEmpty() ? context.getComponent() : context.newComponent(ComponentBuilder.FormatRetention.FORMATTING);
            switch (this.color) {
                case BOLD: {
                    component.setBold(Boolean.valueOf(true));
                    break;
                }
                case ITALIC: {
                    component.setItalic(Boolean.valueOf(true));
                    break;
                }
                case UNDERLINE: {
                    component.setUnderlined(Boolean.valueOf(true));
                    break;
                }
                case STRIKETHROUGH: {
                    component.setStrikethrough(Boolean.valueOf(true));
                    break;
                }
                case MAGIC: {
                    component.setObfuscated(Boolean.valueOf(true));
                    break;
                }
                default: {
                    throw new AssertionError();
                }
            }
        }

        @Override
        public void build(PlainMessageBuilderContextProvider context) {
            context.getBuilder().append('\u00a7').append(this.color.getChar());
        }

        public ChatColor getColor() {
            return this.color;
        }

        @Override
        public int length() {
            return 2;
        }

        @Override
        public int jsonLength() {
            return JSON_LEN + this.color.name().length();
        }

        public String toString() {
            return "SimpleColor{ " + this.color.name() + " }";
        }
    }

    public static abstract class Color
            extends MessagePiece {
    }

    public static final class Plain
            extends MessagePiece {
        private final String message;
        private static final int JSON_LEN = "\"text\":\"\",".length();

        public Plain(String message) {
            this.message = message;
        }

        public String getMessage() {
            return this.message;
        }

        @Override
        public void build(ComplexMessageBuilderContextProvider context) {
            TextComponent comp = context.getComponent();
            comp.setText(comp.getText() + this.message);
        }

        @Override
        public void build(PlainMessageBuilderContextProvider context) {
            context.getBuilder().append(this.message);
        }

        @Override
        public int length() {
            return this.message.length();
        }

        @Override
        public int jsonLength() {
            return JSON_LEN + this.length();
        }

        public String toString() {
            return "Plain{ \"" + this.message + "\", length=" + this.length() + " }";
        }
    }
}

