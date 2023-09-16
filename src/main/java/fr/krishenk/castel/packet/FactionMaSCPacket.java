package fr.krishenk.castel.packet;

import com.google.gson.Gson;
import com.massivecraft.factions.FPlayer;
import com.massivecraft.factions.Faction;
import fr.krishenk.castel.CastelPlugin;
import fr.krishenk.castel.utils.FactionUtils;
import net.minecraft.server.v1_16_R3.MinecraftKey;
import net.minecraft.server.v1_16_R3.Packet;
import net.minecraft.server.v1_16_R3.PacketPlayOutCustomPayload;


public class FactionMaSCPacket extends NMSPacket {
    private final Packet<?> rawPacket;

    public FactionMaSCPacket(FPlayer fPlayer, Faction faction) {
        PacketByteBuffer packetByteBuffer = PacketByteBuffer.get();

        packetByteBuffer.writeByte(1);
        packetByteBuffer.writeString(faction.getTag());
        packetByteBuffer.writeInt(faction.getPowerRounded());
        packetByteBuffer.writeInt(faction.getPowerMaxRounded());
        packetByteBuffer.writeString(FactionUtils.getLeaderName(faction));
        packetByteBuffer.writeString(FactionUtils.getLeaderId(faction));
        packetByteBuffer.writeString(new Gson().toJson(faction.getPermissions()));
        packetByteBuffer.writeString(new Gson().toJson(FactionUtils.onlinePlayer(faction)));
        packetByteBuffer.writeString(new Gson().toJson(FactionUtils.offlinePlayer(faction)));
        packetByteBuffer.writeString(fPlayer.getRole().toString());
        this.rawPacket = new PacketPlayOutCustomPayload(new MinecraftKey(CastelPlugin.channel), packetByteBuffer);
    }

    @Override
    Packet<?> getRowPacket() {
        return rawPacket;
    }
}
