package fr.krishenk.castel.packet;

import com.massivecraft.factions.FPlayer;
import com.massivecraft.factions.Faction;
import com.massivecraft.factions.struct.Relation;
import com.massivecraft.factions.struct.Role;
import com.massivecraft.factions.zcore.fperms.Access;
import com.massivecraft.factions.zcore.fperms.Permissable;
import com.massivecraft.factions.zcore.fperms.PermissableAction;
import net.minecraft.server.v1_16_R3.Packet;
import net.minecraft.server.v1_16_R3.PacketDataSerializer;

public class FactionChangePermCSPacket extends NMSPacket {
    private final Packet<?> rawPacket = null;
    private final int permissable;
    private final int relation;
    private final String permAction;
    private final boolean access;

    public FactionChangePermCSPacket(Faction faction, FPlayer fPlayer, PacketDataSerializer data) {
        this.permissable = data.readInt();
        this.relation = data.readInt();
        this.permAction = data.e(32767);
        this.access = data.readBoolean();
        if (faction.getFPlayerLeader().equals(fPlayer)) {
            handle(faction);
            this.isHandled(true);
        }
    }

    private void handle(Faction faction) {
        faction.setPermission(permissableFromString(this.permissable), PermissableAction.fromString(this.permAction), (!this.access) ? Access.ALLOW : Access.DENY);
    }

    private Permissable permissableFromString(int permissableValue) {
        return this.relation == 0 ? getRelationByValue(permissableValue) :  Role.getByValue(permissableValue);
    }

    private Permissable getRelationByValue(int relationValue) {
        for (Relation r : Relation.values()) {
            if (r.value == relationValue) return r;
        }
        return null;
    }

    @Override
    Packet<?> getRowPacket() {
        return null;
    }
}
