package fr.krishenk.castel.packet;

import com.massivecraft.factions.Faction;
import fr.krishenk.castel.CastelPlugin;
import net.minecraft.server.v1_16_R3.MinecraftKey;
import net.minecraft.server.v1_16_R3.Packet;
import net.minecraft.server.v1_16_R3.PacketPlayOutCustomPayload;


public class FactionFlSCPacket extends NMSPacket {
    private final Packet<?> rawPacket;

    public FactionFlSCPacket(Faction faction) {
        PacketByteBuffer packetByteBuffer = PacketByteBuffer.get();

        packetByteBuffer.writeByte(3);
        packetByteBuffer.writeString(faction.getTag());
        this.rawPacket = new PacketPlayOutCustomPayload(new MinecraftKey(CastelPlugin.channel), packetByteBuffer);
    }

    @Override
    Packet<?> getRowPacket() {
        return rawPacket;
    }
}
