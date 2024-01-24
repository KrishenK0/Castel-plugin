package fr.krishenk.castel.packet;

import fr.krishenk.castel.CastelPlugin;
import fr.krishenk.castel.constants.group.Guild;
import net.minecraft.server.v1_16_R3.MinecraftKey;
import net.minecraft.server.v1_16_R3.Packet;
import net.minecraft.server.v1_16_R3.PacketPlayOutCustomPayload;


public class GuildFlSCPacket extends NMSPacket {
    private final Packet<?> rawPacket;

    public GuildFlSCPacket(Guild guild) {
        PacketByteBuffer packetByteBuffer = PacketByteBuffer.get();

        packetByteBuffer.writeByte(3);
        packetByteBuffer.writeString(guild.getName());
        this.rawPacket = new PacketPlayOutCustomPayload(new MinecraftKey(CastelPlugin.channel), packetByteBuffer);
    }

    @Override
    Packet<?> getRowPacket() {
        return rawPacket;
    }
}
