package fr.krishenk.castel.packet;

import com.google.common.base.Strings;
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

    void writeVarIntArray(int[] array) {
//        writeVarInt(1);
//        writeVarInt(i);
        writeVarInt(array.length);
        for (int i : array) {
            writeVarInt(i);
        }
    }

    void writeUUID(UUID uuid) {
        super.a(uuid);
    }

    void writeString(String string) {
        super.a(!Strings.isNullOrEmpty(string) ? string : "null");
    }

    String readString() {
        return super.e(32767);
    }
}
