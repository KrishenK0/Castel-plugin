package fr.krishenk.castel.packet;

import io.netty.buffer.Unpooled;
import net.minecraft.server.v1_16_R3.PacketDataSerializer;

import java.util.UUID;

public class PacketByteBuffer extends PacketDataSerializer {

    private static final PacketByteBuffer INSTANCE = new PacketByteBuffer();

    static PacketByteBuffer get() {
        INSTANCE.clear();
        return INSTANCE;
    }

    private PacketByteBuffer() {
        super(Unpooled.buffer());
    }

    void writeVarInt(int i) {
        super.d(i);
    }

    void writeVarIntArray(int i) {
        writeVarInt(1);
        writeVarInt(i);
    }

    void writeUUID(UUID uuid) {
        super.a(uuid);
    }

    void writeString(String string) {
        super.a(string);
    }

    String readString() {
        return super.e(32767);
    }
}
