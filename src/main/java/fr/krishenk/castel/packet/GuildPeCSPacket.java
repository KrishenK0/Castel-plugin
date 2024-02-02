package fr.krishenk.castel.packet;

import fr.krishenk.castel.CastelPlugin;
import fr.krishenk.castel.constants.group.Guild;
import fr.krishenk.castel.constants.namespace.Namespace;
import fr.krishenk.castel.constants.player.CastelPlayer;
import fr.krishenk.castel.constants.player.GuildPermission;
import fr.krishenk.castel.constants.player.Rank;
import fr.krishenk.castel.constants.player.StandardGuildPermission;
import net.minecraft.server.v1_16_R3.Packet;
import net.minecraft.server.v1_16_R3.PacketDataSerializer;

import java.util.Set;

public class GuildPeCSPacket extends NMSPacket {
    private final Packet<?> rawPacket = null;
    private final String permAction;
    private final boolean access;
    private final Rank rank;

    public GuildPeCSPacket(Guild guild, CastelPlayer cPlayer, PacketDataSerializer data) {
        this.rank = guild.getRanks().get(data.readInt());
        this.permAction = data.e(32767);
        this.access = data.readBoolean();

        if (guild.getLeader().equals(cPlayer) && this.rank.getPriority() >= cPlayer.getRank().getPriority()) {
            handle();
            this.isHandled(true);
        }
    }

    private void handle() {
        Set<GuildPermission> perms =  rank.copyPermissions();
        StandardGuildPermission perm = (StandardGuildPermission) CastelPlugin.getInstance().getPermissionRegistry().getRegistered(Namespace.fromString(this.permAction));

        if (this.access) perms.remove(perm);
        else perms.add(perm);

        this.rank.setPermissions(perms);
    }

    @Override
    Packet<?> getRowPacket() {
        return null;
    }
}
