package fr.krishenk.castel.constants.player;

import fr.krishenk.castel.constants.group.model.GuildRequest;
import fr.krishenk.castel.constants.namespace.Namespace;

import java.util.UUID;

public class GuildInvite extends GuildRequest {
    public static final Namespace NAMESPACE = Namespace.castel("INVITE");

    public GuildInvite(UUID sender, long acceptTime, long timestamp) {
        super(sender, acceptTime, timestamp);
    }

    public GuildInvite(UUID sender, long acceptTime) {
        this(sender, acceptTime, System.currentTimeMillis());
    }
}
