package fr.krishenk.castel.packet;

import fr.krishenk.castel.constants.group.Guild;
import fr.krishenk.castel.constants.player.CastelPlayer;
import net.minecraft.server.v1_16_R3.Packet;
import net.minecraft.server.v1_16_R3.PacketDataSerializer;

public class GuildChangePermCSPacket extends NMSPacket {
    private final Packet<?> rawPacket = null;
    private final int permissable;
    private final int relation;
    private final String permAction;
    private final boolean access;

    public GuildChangePermCSPacket(Guild guild, CastelPlayer cPlayer, PacketDataSerializer data) {
        this.permissable = data.readInt();
        this.relation = data.readInt();
        this.permAction = data.e(32767);
        this.access = data.readBoolean();
        if (guild.getLeader().equals(cPlayer)) {
            handle(guild);
            this.isHandled(true);
        }

    }

    private void handle(Guild guild) {
        System.out.println("guild = " + guild + ", permissable = " + this.permissable + ", permAction = " + this.permAction + ", access = " + this.access);
//        guild.setPermission(permissableFromString(this.permissable), PermissableAction.fromString(this.permAction), (!this.access) ? Access.ALLOW : Access.DENY);
    }

//    private Permissable permissableFromString(int permissableValue) {
//        return this.relation == 0 ? getRelationByValue(permissableValue) :  Role.getByValue(permissableValue);
//    }
//
//    private Permissable getRelationByValue(int relationValue) {
//        for (Relation r : Relation.values()) {
//            if (r.value == relationValue) return r;
//        }
//        return null;
//    }

    @Override
    Packet<?> getRowPacket() {
        return null;
    }
}
