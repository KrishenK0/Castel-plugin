package fr.krishenk.castel.constants.group.model.logs.misc;

import fr.krishenk.castel.abstraction.PlayerOperator;
import fr.krishenk.castel.constants.group.model.logs.AuditLog;
import fr.krishenk.castel.constants.land.SerializationContext;
import fr.krishenk.castel.constants.player.CastelPlayer;
import fr.krishenk.castel.data.dataproviders.SectionableDataSetter;
import fr.krishenk.castel.locale.provider.MessageBuilder;
import org.bukkit.Bukkit;

import java.util.UUID;

public abstract class LogPlayerOperator extends AuditLog implements PlayerOperator {
    private UUID player;

    protected LogPlayerOperator() {
    }

    public LogPlayerOperator(UUID player) {
        this.player = player;
    }

    @Override
    public void serialize(SerializationContext<SectionableDataSetter> context) {
        super.serialize(context);
        context.getDataProvider().setUUID("player", this.player);
    }

    @Override
    public void addEdits(MessageBuilder builder) {
        super.addEdits(builder);
        builder.withContext(Bukkit.getOfflinePlayer(this.player));
    }

    @Override
    public CastelPlayer getPlayer() {
        return CastelPlayer.getCastelPlayer(this.player);
    }

    public UUID getPlayerId() {
        return this.player;
    }
}
