package fr.krishenk.castel.packet;

import fr.krishenk.castel.constants.group.Guild;
import net.minecraft.server.v1_16_R3.Packet;
import net.minecraft.server.v1_16_R3.PacketDataSerializer;

public class GuildRiCSPacket extends NMSPacket {
    private final Packet<?> rawPacket = null;
    public GuildRiCSPacket(Guild guild, PacketDataSerializer data) {
        guild.setRequiresInvite(data.readBoolean());
        this.setHandled(true);
    }

    @Override
    Packet<?> getRowPacket() {
        return rawPacket;
    }
}
