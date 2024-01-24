package fr.krishenk.castel.packet;

import com.google.gson.Gson;
import fr.krishenk.castel.CastelPlugin;
import fr.krishenk.castel.constants.group.Guild;
import net.minecraft.server.v1_16_R3.MinecraftKey;
import net.minecraft.server.v1_16_R3.Packet;
import net.minecraft.server.v1_16_R3.PacketPlayOutCustomPayload;


public class GuildPeSCPacket extends NMSPacket {
    private final Packet<?> rawPacket;

    public GuildPeSCPacket(Guild guild) {
        PacketByteBuffer packetByteBuffer = PacketByteBuffer.get();

        packetByteBuffer.writeByte(4);
        packetByteBuffer.writeString(guild.getTag());
        packetByteBuffer.writeString(new Gson().toJson(guild.getRanks().getRanks()));
        this.rawPacket = new PacketPlayOutCustomPayload(new MinecraftKey(CastelPlugin.channel), packetByteBuffer);
    }

    @Override
    Packet<?> getRowPacket() {
        return rawPacket;
    }
}
