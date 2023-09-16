package fr.krishenk.castel.locale.compiler.placeholders;

import fr.krishenk.castel.constants.group.Group;
import fr.krishenk.castel.constants.group.Guild;
import fr.krishenk.castel.locale.compiler.PlaceholderTranslationContext;
import fr.krishenk.castel.utils.compilers.PlaceholderContextProvider;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class PlaceholderContextBuilder implements PlaceholderContextProvider, Cloneable {
    protected Map<String, Object> placeholders;
    protected Map<String, PlaceholderContextProvider> groupedPlaceholders;
    public Object main;
    public Object relationalSecond;

    @Override
    public Object processPlaceholder(String placeholder) {
        PlaceholderType parsed = PlaceholderParser.parseType(placeholder);
        Object translated = parsed.request(this);
        if (translated instanceof Supplier) {
            translated = ((Supplier<?>)translated).get();
        }
        return translated;
    }

    public void switchContext() {
        Object main = this.main;
        this.main = this.relationalSecond;
        this.relationalSecond = main;
    }

    public PlaceholderContextBuilder clone() {
        PlaceholderContextBuilder ctx = new PlaceholderContextBuilder();
        ctx.main = this.main;
        ctx.relationalSecond = this.relationalSecond;
        ctx.placeholders = this.placeholders;
        ctx.groupedPlaceholders = this.groupedPlaceholders;
        return ctx;
    }

    public boolean canHandleRelational() {
        return this.relationalSecond != null && this.main instanceof Player;
    }

    public void addGroupedPlaceholder(String id, PlaceholderContextProvider provider) {
        if (this.groupedPlaceholders == null) {
            this.groupedPlaceholders = new HashMap<String, PlaceholderContextProvider>(1);
        }
        this.groupedPlaceholders.put(id, provider);
    }

    public PlaceholderContextBuilder placeholders(Object ... edits) {
        if (edits.length == 0) {
            return this;
        }
        PlaceholderContextBuilder.validateLength(edits);
        if (this.placeholders != null) {
            PlaceholderParser.serializeVariablesIntoContext(this.placeholders, edits);
        } else {
            this.placeholders = PlaceholderParser.serializeVariablesIntoContext(edits);
        }
        return this;
    }

    private static void validateLength(Object[] edits) {
        if (edits.length % 2 == 1) {
            throw new IllegalArgumentException("Missing variable/replacement for one of edits, possibly: " + edits[edits.length - 1]);
        }
    }

    public PlaceholderContextBuilder inheritPlaceholders(PlaceholderContextBuilder other) {
        this.addAllIfAbsent(other.placeholders);
        if (other.groupedPlaceholders != null) {
            this.groupedPlaceholders = other.groupedPlaceholders;
        }
        return this;
    }

    public PlaceholderContextBuilder inheritContext(PlaceholderContextBuilder other) {
        if (this.main == null) {
            this.main = other.main;
        }
        if (this.relationalSecond == null) {
            this.relationalSecond = other.relationalSecond;
        }
        return this;
    }

    public PlaceholderContextBuilder addAll(Map<String, Object> placeholders) {
        if (this.placeholders == null) {
            this.placeholders = placeholders;
            return this;
        }
        this.placeholders.putAll(placeholders);
        return this;
    }

    public PlaceholderContextBuilder addAllIfAbsent(Map<String, Object> placeholders) {
        if (placeholders == null) {
            return this;
        }
        if (this.placeholders == null) {
            this.placeholders = placeholders;
            return this;
        }
        for (Map.Entry<String, Object> placeholder : placeholders.entrySet()) {
            this.placeholders.putIfAbsent(placeholder.getKey(), placeholder.getValue());
        }
        return this;
    }

    public PlaceholderContextBuilder raws(Object ... edits) {
        if (edits.length == 0) {
            return this;
        }
        PlaceholderContextBuilder.validateLength(edits);
        if (this.placeholders != null) {
            PlaceholderParser.serializeVariables(this.placeholders, edits);
        } else {
            this.placeholders = PlaceholderParser.serializeVariables(edits);
        }
        return this;
    }

    public Map<String, Object> getPlaceholders() {
        return this.placeholders;
    }

    public PlaceholderContextBuilder withContext(OfflinePlayer player) {
        if (player == null) {
            return this;
        }
        this.main = player;
        return this;
    }

    public boolean hasContext() {
        return this.main != null;
    }

    public PlaceholderContextBuilder withContext(Player player) {
        if (player == null) {
            return this;
        }
        return this.withContext((OfflinePlayer)player);
    }

    public PlaceholderContextBuilder withContext(CommandSender sender) {
        Objects.requireNonNull(sender);
        if (sender instanceof Player) {
            return this.withContext((Player)sender);
        }
        if (sender instanceof OfflinePlayer) {
            return this.withContext((OfflinePlayer)sender);
        }
        return this;
    }

    public PlaceholderContextBuilder withContext(Guild guild) {
        this.main = guild;
        return this;
    }

    public PlaceholderContextBuilder withContext(Group group) {
        if (group == null) {
            return this;
        }
        if (group instanceof Guild) {
            return this.withContext((Guild) group);
        }
        throw new IllegalArgumentException("Unknown placeholder context for group: " + group.getClass() + " -> " + group);
    }

    public PlaceholderContextBuilder other(Player other) {
        this.relationalSecond = other;
        return this;
    }

    public PlaceholderContextBuilder other(Guild other) {
        this.relationalSecond = other;
        return this;
    }

    void createPlaceholdersIfNull() {
        if (this.placeholders == null) {
            this.placeholders = new HashMap<String, Object>(12);
        }
    }

    public PlaceholderContextBuilder resetPlaceholders() {
        this.placeholders = null;
        return this;
    }

    public PlaceholderContextBuilder parse(String variable, Object replacement) {
        if (replacement == null) {
            return this;
        }
        this.createPlaceholdersIfNull();
        this.placeholders.put(variable, PlaceholderTranslationContext.withDefaultContext(replacement));
        return this;
    }

    public PlaceholderContextBuilder raw(String variable, Object replacement) {
        if (replacement == null) {
            return this;
        }
        this.createPlaceholdersIfNull();
        this.placeholders.put(variable, replacement);
        return this;
    }

    public Object getPlaceholder(String str) {
        if (this.placeholders != null) {
            return this.placeholders.get(str);
        }
        return null;
    }

    public String toString() {
        return "MessageBuilder{ context=" + this.main + ", other=" + this.relationalSecond + ", placeholders=" + (this.placeholders == null ? "{}" : this.placeholders.entrySet().stream().map(entry -> String.valueOf(entry.getKey()) + '=' + entry.getValue()).collect(Collectors.toList())) + " }";
    }
}


