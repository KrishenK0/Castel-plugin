package fr.krishenk.castel.packet;

import com.google.gson.Gson;
import com.massivecraft.factions.Faction;
import fr.krishenk.castel.CastelPlugin;
import net.minecraft.server.v1_16_R3.MinecraftKey;
import net.minecraft.server.v1_16_R3.Packet;
import net.minecraft.server.v1_16_R3.PacketPlayOutCustomPayload;


public class FactionPeSCPacket extends NMSPacket {
    private final Packet<?> rawPacket;

    public FactionPeSCPacket(Faction faction) {
        PacketByteBuffer packetByteBuffer = PacketByteBuffer.get();

        packetByteBuffer.writeByte(4);
        packetByteBuffer.writeString(faction.getTag());
        packetByteBuffer.writeString(new Gson().toJson(faction.getPermissions()));
        this.rawPacket = new PacketPlayOutCustomPayload(new MinecraftKey(CastelPlugin.channel), packetByteBuffer);
    }

    @Override
    Packet<?> getRowPacket() {
        return rawPacket;
    }
}
