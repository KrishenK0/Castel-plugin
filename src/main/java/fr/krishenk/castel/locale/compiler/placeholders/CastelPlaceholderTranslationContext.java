package fr.krishenk.castel.locale.compiler.placeholders;

import fr.krishenk.castel.constants.group.Guild;
import fr.krishenk.castel.constants.player.CastelPlayer;
import fr.krishenk.castel.locale.provider.MessageBuilder;
import fr.krishenk.castel.utils.CastelBukkitExtensions;
import org.bukkit.OfflinePlayer;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.jetbrains.annotations.NotNull;

public class CastelPlaceholderTranslationContext {
    @NotNull
    private final PlaceholderContextBuilder placeholderContext;
    @NotNull
    private final Placeholder placeholder;

    public CastelPlaceholderTranslationContext(@NotNull PlaceholderContextBuilder placeholderContext, @NotNull Placeholder placeholder) {
        this.placeholderContext = placeholderContext;
        this.placeholder = placeholder;
    }

    @NotNull
    public final PlaceholderContextBuilder getPlaceholderContext() {
        return this.placeholderContext;
    }

    @NotNull
    public final Placeholder getPlaceholder() {
        return this.placeholder;
    }

    @NotNull
    public final MessageBuilder asMessaegeBuilder() {
        return this.placeholderContext instanceof MessageBuilder ? (MessageBuilder)this.placeholderContext : new MessageBuilder();
    }

    @Nullable
    public final CastelPlayer getPlayer() {
        Object object = this.placeholderContext.main;
        CastelPlayer castelPlayer = object instanceof CastelPlayer ? (CastelPlayer)object : null;
        if (castelPlayer == null) {
            Object object2 = this.placeholderContext.main;
            OfflinePlayer offlinePlayer = object2 instanceof OfflinePlayer ? (OfflinePlayer)object2 : null;
            castelPlayer = offlinePlayer != null ? CastelBukkitExtensions.INSTANCE.asCastelPlayer(offlinePlayer) : null;
        }
        return castelPlayer;
    }

    @Nullable
    public final Guild getGuild() {
        Object object = this.placeholderContext.main;
        Guild guild = object instanceof Guild ? (Guild)object : null;
        if (guild == null) {
            CastelPlayer kingdomPlayer = this.getPlayer();
            guild = kingdomPlayer != null ? kingdomPlayer.getGuild() : null;
        }
        return guild;
    }

    @Nullable
    public final CastelPlayer getOtherPlayer() {
        Object object = this.placeholderContext.relationalSecond;
        CastelPlayer castelPlayer = object instanceof CastelPlayer ? (CastelPlayer)object : null;
        if (castelPlayer == null) {
            Object object2 = this.placeholderContext.relationalSecond;
            OfflinePlayer offlinePlayer = object2 instanceof OfflinePlayer ? (OfflinePlayer)object2 : null;
            castelPlayer = offlinePlayer != null ? CastelBukkitExtensions.INSTANCE.asCastelPlayer(offlinePlayer) : null;
        }
        return castelPlayer;
    }

    @Nullable
    public final Guild getOtherGuild() {
        Object object = this.placeholderContext.relationalSecond;
        Guild guild = object instanceof Guild ? (Guild)object : null;
        if (guild == null) {
            CastelPlayer kingdomPlayer = this.getOtherPlayer();
            guild = kingdomPlayer != null ? kingdomPlayer.getGuild() : null;
        }
        return guild;
    }
}

