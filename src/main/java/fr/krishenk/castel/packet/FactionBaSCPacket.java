package fr.krishenk.castel.packet;

import com.google.gson.Gson;
import com.massivecraft.factions.Faction;
import fr.krishenk.castel.CastelPlugin;
import fr.krishenk.castel.utils.FactionUtils;
import net.minecraft.server.v1_16_R3.MinecraftKey;
import net.minecraft.server.v1_16_R3.Packet;
import net.minecraft.server.v1_16_R3.PacketPlayOutCustomPayload;


public class FactionBaSCPacket extends NMSPacket {
    private final Packet<?> rawPacket;

    public FactionBaSCPacket(Faction faction) {
        PacketByteBuffer packetByteBuffer = PacketByteBuffer.get();

        packetByteBuffer.writeByte(2);
        packetByteBuffer.writeString(new Gson().toJson(FactionUtils.onlinePlayer(faction)));
        packetByteBuffer.writeString(new Gson().toJson(FactionUtils.offlinePlayer(faction)));
        this.rawPacket = new PacketPlayOutCustomPayload(new MinecraftKey(CastelPlugin.channel), packetByteBuffer);
    }

    @Override
    Packet<?> getRowPacket() {
        return rawPacket;
    }
}
