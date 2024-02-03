package fr.krishenk.castel.packet;

import net.minecraft.server.v1_16_R3.Packet;
import org.bukkit.craftbukkit.v1_16_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;

public abstract class NMSPacket {
    private boolean handled = false;
    public void sendTo(Player player) {
        ((CraftPlayer) player).getHandle().playerConnection.sendPacket(getRowPacket());
    }

    abstract Packet<?> getRowPacket();

    void setHandled(boolean value) { this.handled = value; }

    public boolean isHandled() { return this.handled; }

}
