package fr.krishenk.castel.services.placeholders;

import fr.krishenk.castel.CLogger;
import fr.krishenk.castel.locale.MessageObjectBuilder;
import fr.krishenk.castel.locale.compiler.placeholders.Placeholder;
import fr.krishenk.castel.locale.compiler.placeholders.PlaceholderParser;
import fr.krishenk.castel.locale.messenger.Messenger;
import fr.krishenk.castel.locale.provider.MessageBuilder;
import fr.krishenk.castel.services.Service;
import fr.krishenk.castel.utils.debugging.CastelDebug;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import me.clip.placeholderapi.expansion.Relational;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public class ServicePlaceholderAPI extends PlaceholderExpansion implements Relational, Service {
    public ServicePlaceholderAPI() {}

    @Override
    public boolean canRegister() {
        return true;
    }

    @Override
    public boolean persist() {
        return true;
    }

    @Override
    public @NotNull String getIdentifier() {
        return "castel";
    }

    @Override
    public @NotNull String getAuthor() {
        return "KrishenK";
    }

    @Override
    public @NotNull String getVersion() {
        return "1.1.0";
    }

    @Override
    public @Nullable String onRequest(OfflinePlayer player, @NotNull String params) {
        return request(player, params);
    }

    private static String request(OfflinePlayer player, String params) {
        return requestInternal(player, null, params);
    }

    private static String requestInternal(OfflinePlayer player, Player other, String params) {
        Placeholder ph = PlaceholderParser.parsePlaceholder(params);
        if (ph == null) {
            CLogger.debug(CastelDebug.UNKNOWN$PLACEHOLDER, "Requested null placeholder: " + params);
            return null;
        } else {
            MessageBuilder settings = (new MessageBuilder()).withContext(player).other(other);
            Object translated = ph.request(settings);
            if (translated instanceof MessageObjectBuilder) translated = ((MessageObjectBuilder) translated).buildPlain(settings);
            else if (translated instanceof Messenger) translated = ((Messenger) translated).parse(settings);

            return Objects.toString(translated);
        }
    }

    @Override
    public void enable() {
        this.register();
    }

    @Override
    public String onPlaceholderRequest(Player player, Player player1, String s) {
        return requestInternal(player, player1, s);
    }
}
