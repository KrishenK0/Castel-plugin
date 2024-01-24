package fr.krishenk.castel.packet;

import com.google.gson.Gson;
import fr.krishenk.castel.CastelPlugin;
import fr.krishenk.castel.constants.group.Guild;
import fr.krishenk.castel.constants.player.CastelPlayer;
import fr.krishenk.castel.lang.Config;
import fr.krishenk.castel.utils.GuildUtils;
import net.minecraft.server.v1_16_R3.MinecraftKey;
import net.minecraft.server.v1_16_R3.Packet;
import net.minecraft.server.v1_16_R3.PacketPlayOutCustomPayload;


public class GuildMaSCPacket extends NMSPacket {
    private final Packet<?> rawPacket;

    public GuildMaSCPacket(CastelPlayer cPlayer, Guild guild) {
        System.out.println("cPlayer = " + cPlayer + ", guild = " + guild);
        PacketByteBuffer packetByteBuffer = PacketByteBuffer.get();

        packetByteBuffer.writeByte(1);
        try {
            packetByteBuffer.writeString(guild.getName());
            packetByteBuffer.writeInt((int) guild.getPower());
            packetByteBuffer.writeInt(Config.Powers.POWER_FACTION_MAX.getManager().getInt());
            packetByteBuffer.writeString(GuildUtils.getLeaderName(guild));
            packetByteBuffer.writeString(guild.getLeaderId().toString());
            packetByteBuffer.writeString(new Gson().toJson(cPlayer.getRank().copyPermissions()));
            packetByteBuffer.writeString(new Gson().toJson(GuildUtils.onlinePlayer(guild)));
            packetByteBuffer.writeString(new Gson().toJson(GuildUtils.offlinePlayer(guild)));
            packetByteBuffer.writeString(cPlayer.getRank().getName());
        } catch (Exception e) {
            e.printStackTrace();
        }

        this.rawPacket = new PacketPlayOutCustomPayload(new MinecraftKey(CastelPlugin.channel), packetByteBuffer);
    }

    @Override
    Packet<?> getRowPacket() {
        return rawPacket;
    }
}
