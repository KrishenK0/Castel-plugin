package fr.krishenk.castel.locale.provider;

import fr.krishenk.castel.constants.group.Group;
import fr.krishenk.castel.constants.group.Guild;
import fr.krishenk.castel.constants.player.CastelPlayer;
import fr.krishenk.castel.locale.LanguageManager;
import fr.krishenk.castel.locale.SupportedLanguage;
import fr.krishenk.castel.locale.compiler.placeholders.PlaceholderContextBuilder;
import fr.krishenk.castel.services.ServiceVault;
import fr.krishenk.castel.services.SoftService;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

public class MessageBuilder extends PlaceholderContextBuilder {
    public boolean ignoreColors = false;
    public Boolean usePrefix;
    private @NonNull SupportedLanguage language = LanguageManager.getDefaultLanguage();
    public static final MessageBuilder DEFAULT = new MessageBuilder();

    @Override
    public MessageBuilder placeholders(Object ... edits) {
        super.placeholders(edits);
        return this;
    }

    public MessageBuilder viewer(CastelPlayer kp) {
        this.lang(kp.getLanguage());
        return this;
    }

    public MessageBuilder lang(SupportedLanguage lang) {
        this.language = Objects.requireNonNull(lang);
        return this;
    }

    public @NonNull SupportedLanguage getLanguage() {
        return this.language;
    }

    @Override
    public MessageBuilder inheritPlaceholders(PlaceholderContextBuilder other) {
        super.inheritPlaceholders(other);
        return this;
    }

    @Override
    public MessageBuilder inheritContext(PlaceholderContextBuilder other) {
        super.inheritContext(other);
        return this;
    }

    @Override
    public MessageBuilder addAll(Map<String, Object> placeholders) {
        super.addAll(placeholders);
        return this;
    }

    public MessageBuilder addAll(PlaceholderContextBuilder ctx) {
        this.addAll((Map)ctx.getPlaceholders());
        return this;
    }

    @Override
    public MessageBuilder addAllIfAbsent(Map<String, Object> placeholders) {
        super.addAllIfAbsent(placeholders);
        return this;
    }

    @Override
    public MessageBuilder raws(Object ... edits) {
        super.raws(edits);
        return this;
    }

    public MessageBuilder usePrefix() {
        return this.usePrefix(true);
    }

    public MessageBuilder usePrefix(boolean usePrefix) {
        this.usePrefix = usePrefix;
        return this;
    }

    public MessageBuilder ignoreColors() {
        this.ignoreColors = true;
        return this;
    }

    public MessageBuilder dontIgnoreColors() {
        this.ignoreColors = false;
        return this;
    }

    @Override
    public MessageBuilder withContext(OfflinePlayer player) {
        if (player == null) {
            return this;
        }
        super.withContext(player);
        this.raw("player", player.getName());
        return this;
    }

    @Override
    public MessageBuilder withContext(Player player) {
        if (player == null) {
            return this;
        }
        super.withContext(player);

        String displayName = SoftService.VAULT.isAvailable() && ServiceVault.isAvailable(ServiceVault.Component.CHAT) ? ServiceVault.getDisplayName(player) : player.getDisplayName();
        this.parse("displayname", displayName);

        Objects.requireNonNull(player);
        this.parse("pure-displayname", player.getDisplayName());
        return this;
    }

    @Override
    public MessageBuilder withContext(CommandSender sender) {
        super.withContext(sender);
        return this;
    }

    @Override
    public MessageBuilder withContext(Guild guild) {
        super.withContext(guild);
        return this;
    }

    @Override
    public MessageBuilder withContext(Group group) {
        super.withContext(group);
        return this;
    }

    @Override
    public MessageBuilder other(Player other) {
        super.other(other);
        if (other != null) {
            this.raw("other_player", other.getName());
        }
        return this;
    }

    @Override
    public MessageBuilder other(Guild other) {
        super.other(other);
        return this;
    }

    @Override
    public MessageBuilder resetPlaceholders() {
        super.resetPlaceholders();
        return this;
    }

    @Override
    public MessageBuilder parse(String variable, Object replacement) {
        super.parse(variable, replacement);
        return this;
    }

    @Override
    public MessageBuilder raw(String variable, Object replacement) {
        super.raw(variable, replacement);
        return this;
    }

    @Override
    public String toString() {
        return "MessageBuilder{ context=" + this.main + ", ignoreColors=" + this.ignoreColors + ", prefix=" + this.usePrefix + ", other=" + this.relationalSecond + ", placeholders=" + (this.placeholders == null ? "{}" : this.placeholders.entrySet().stream().map(entry -> String.valueOf(entry.getKey()) + '=' + entry.getValue()).collect(Collectors.toList())) + " }";
    }

    @Override
    public MessageBuilder clone() {
        MessageBuilder builder = new MessageBuilder();
        builder.main = this.main;
        builder.relationalSecond = this.relationalSecond;
        builder.usePrefix = this.usePrefix;
        builder.ignoreColors = this.ignoreColors;
        if (this.placeholders != null) {
            builder.placeholders = new HashMap(this.placeholders);
        }
        if (this.groupedPlaceholders != null) {
            builder.groupedPlaceholders = this.groupedPlaceholders;
        }
        return builder;
    }
}

