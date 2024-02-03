package fr.krishenk.castel.packet;

import fr.krishenk.castel.CastelPlugin;
import fr.krishenk.castel.constants.group.Guild;
import fr.krishenk.castel.constants.player.CastelPlayer;
import net.minecraft.server.v1_16_R3.Packet;
import net.minecraft.server.v1_16_R3.PacketDataSerializer;
import org.bukkit.Bukkit;


public class GuildFlCSPacket extends NMSPacket {
    private final Packet<?> rawPacket = null;

    public GuildFlCSPacket(Guild guild, CastelPlayer cPlayer, PacketDataSerializer data) {
        String flag = data.e(32767);
        Bukkit.getScheduler().runTaskLater(CastelPlugin.getInstance(), () -> {
            guild.setFlag(flag, cPlayer);
            this.setHandled(true);
        }, 0L);
    }

    @Override
    Packet<?> getRowPacket() {
        return rawPacket;
    }
}
