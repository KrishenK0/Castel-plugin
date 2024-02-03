package fr.krishenk.castel.packet;

import com.google.gson.Gson;
import fr.krishenk.castel.CastelPlugin;
import fr.krishenk.castel.constants.group.Guild;
import fr.krishenk.castel.utils.PacketUtils;
import net.minecraft.server.v1_16_R3.MinecraftKey;
import net.minecraft.server.v1_16_R3.Packet;
import net.minecraft.server.v1_16_R3.PacketPlayOutCustomPayload;


public class GuildInSCPacket extends NMSPacket {
    private final Packet<?> rawPacket;

    public GuildInSCPacket(Guild guild) {
        PacketByteBuffer packetByteBuffer = PacketByteBuffer.get();

        packetByteBuffer.writeByte(7);
        packetByteBuffer.writeBoolean(guild.getGroup().requiresInvite());
        packetByteBuffer.writeString(new Gson().toJson(PacketUtils.Server.onlinePlayer()));
        packetByteBuffer.writeString(new Gson().toJson(PacketUtils.GUILD.getInvitationSent(guild)));
        this.rawPacket = new PacketPlayOutCustomPayload(new MinecraftKey(CastelPlugin.channel), packetByteBuffer);
    }

    @Override
    Packet<?> getRowPacket() {
        return rawPacket;
    }
}
