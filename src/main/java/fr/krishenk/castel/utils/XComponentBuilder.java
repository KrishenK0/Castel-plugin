package fr.krishenk.castel.utils;

import fr.krishenk.castel.libs.xseries.ReflectionUtils;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.TextComponent;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class XComponentBuilder {
    public static final int MAXIMUM_JSON_CHAT_PACKET_SIZE = 262144;
    public static final int SUGGESTED_JSON_CHAT_PACKET_SIZE_RENEWAL = 212144;
    private final List<List<BaseComponent>> parts = new ArrayList<>(1);
    private List<BaseComponent> currentPart = new ArrayList<>();
    private BaseComponent current;

    public static TextComponent duplicate(TextComponent component) {
        if (ReflectionUtils.supports(15)) {
            return component.duplicate();
        }
        return new TextComponent(component);
    }

    public XComponentBuilder(String text) {
        this();
        this.current = new TextComponent(text);
    }

    public XComponentBuilder() {
        this.parts.add(this.currentPart);
    }

    public static void copyFormatting(BaseComponent to, BaseComponent from, ComponentBuilder.FormatRetention retention) {
        if (retention == ComponentBuilder.FormatRetention.NONE) {
            return;
        }
        if (retention == ComponentBuilder.FormatRetention.EVENTS || retention == ComponentBuilder.FormatRetention.ALL) {
            if (to.getClickEvent() == null) {
                to.setClickEvent(from.getClickEvent());
            }
            if (to.getHoverEvent() == null) {
                to.setHoverEvent(from.getHoverEvent());
            }
        }
        if (retention == ComponentBuilder.FormatRetention.FORMATTING || retention == ComponentBuilder.FormatRetention.ALL) {
            if (to.getColorRaw() == null) {
                to.setColor(from.getColorRaw());
            }
            if (to.isBoldRaw() == null) {
                to.setBold(from.isBoldRaw());
            }
            if (to.isItalicRaw() == null) {
                to.setItalic(from.isItalicRaw());
            }
            if (to.isUnderlinedRaw() == null) {
                to.setUnderlined(from.isUnderlinedRaw());
            }
            if (to.isStrikethroughRaw() == null) {
                to.setStrikethrough(from.isStrikethroughRaw());
            }
            if (to.isObfuscatedRaw() == null) {
                to.setObfuscated(from.isObfuscatedRaw());
            }
            if (to.getInsertion() == null) {
                to.setInsertion(from.getInsertion());
            }
            if (ReflectionUtils.supports(16) && to.getFontRaw() == null) {
                to.setFont(from.getFont());
            }
        }
    }

    public XComponentBuilder append(BaseComponent component) {
        return this.append(component, ComponentBuilder.FormatRetention.ALL);
    }

    public XComponentBuilder append(BaseComponent component, ComponentBuilder.FormatRetention retention) {
        Objects.requireNonNull(component, "Cannot append null component");
        if (this.current == null) {
            this.current = component;
            return this;
        }
        this.currentPart.add(this.current);
        BaseComponent previous = this.current;
        this.current = component;
        XComponentBuilder.copyFormatting(this.current, previous, retention);
        return this;
    }

    public void newPacket() {
        this.parts.add(this.currentPart);
        this.currentPart = new ArrayList<BaseComponent>(10);
    }

    public XComponentBuilder append(String text) {
        return this.append(text, ComponentBuilder.FormatRetention.ALL);
    }

    public XComponentBuilder append(String text, ComponentBuilder.FormatRetention retention) {
        this.append((BaseComponent)new TextComponent(text), retention);
        return this;
    }

    public BaseComponent[] createSingular() {
        if (this.parts.size() != 1) {
            throw new IllegalStateException("The component is too large: " + this.parts.size());
        }
        return this.create()[0];
    }

    public BaseComponent[][] create() {
        if (this.currentPart.isEmpty()) {
            if (this.current == null) {
                return new BaseComponent[0][0];
            }
            return new BaseComponent[][]{{this.current}};
        }
        Objects.requireNonNull(this.current);
        BaseComponent[][] result = new BaseComponent[this.parts.size()][];
        for (int i = 0; i < this.parts.size(); ++i) {
            List<BaseComponent> part = this.parts.get(i);
            int size = part.size();
            result[i] = part.toArray(new BaseComponent[size + 1]);
            result[i][size] = this.current;
        }
        return result;
    }

    public TextComponent buildTextComponent() {
        TextComponent component = new TextComponent();
        if (this.current != null) {
            this.currentPart.add(this.current);
        }
        component.setExtra(this.currentPart);
        return component;
    }

    @Deprecated
    public static BaseComponent[] fromLegacyText(String message) {
        ArrayList<TextComponent> components = new ArrayList<TextComponent>();
        StringBuilder builder = new StringBuilder();
        TextComponent component = new TextComponent();
        for (int i = 0; i < message.length(); ++i) {
            char c = message.charAt(i);
            if (c == '\u00a7') {
                ChatColor format;
                if (++i >= message.length()) break;
                c = message.charAt(i);
                if (c >= 'A' && c <= 'Z') {
                    c = (char)(c + 32);
                }
                if (c == 'x' && i + 12 < message.length()) {
                    StringBuilder hex = new StringBuilder("#");
                    for (int j = 0; j < 6; ++j) {
                        hex.append(message.charAt(i + 2 + j * 2));
                    }
                    try {
                        format = ChatColor.of((String)hex.toString());
                    }
                    catch (IllegalArgumentException ex) {
                        format = null;
                    }
                    i += 12;
                } else {
                    format = ChatColor.getByChar((char)c);
                }
                if (format == null) continue;
                if (builder.length() > 0) {
                    TextComponent old = component;
                    component = new TextComponent(old);
                    old.setText(builder.toString());
                    builder.setLength(0);
                    components.add(old);
                }
                if (format == ChatColor.BOLD) {
                    component.setBold(Boolean.valueOf(true));
                    continue;
                }
                if (format == ChatColor.ITALIC) {
                    component.setItalic(Boolean.valueOf(true));
                    continue;
                }
                if (format == ChatColor.UNDERLINE) {
                    component.setUnderlined(Boolean.valueOf(true));
                    continue;
                }
                if (format == ChatColor.STRIKETHROUGH) {
                    component.setStrikethrough(Boolean.valueOf(true));
                    continue;
                }
                if (format == ChatColor.MAGIC) {
                    component.setObfuscated(Boolean.valueOf(true));
                    continue;
                }
                if (format == ChatColor.RESET) {
                    format = ChatColor.WHITE;
                    component = new TextComponent();
                    component.setColor(format);
                    continue;
                }
                component = new TextComponent();
                component.setColor(format);
                continue;
            }
            builder.append(c);
        }
        component.setText(builder.toString());
        components.add(component);
        return components.toArray(new BaseComponent[0]);
    }

    public static void resetFormats(BaseComponent component) {
        component.setBold(Boolean.valueOf(false));
        component.setItalic(Boolean.valueOf(false));
        component.setUnderlined(Boolean.valueOf(false));
        component.setStrikethrough(Boolean.valueOf(false));
        component.setObfuscated(Boolean.valueOf(false));
    }
}
