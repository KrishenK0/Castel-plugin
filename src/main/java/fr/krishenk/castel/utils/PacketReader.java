package fr.krishenk.castel.utils;

import com.massivecraft.factions.FPlayer;
import com.massivecraft.factions.FPlayers;
import com.massivecraft.factions.Faction;
import fr.krishenk.castel.CastelPlugin;
import fr.krishenk.castel.packet.*;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageDecoder;
import net.minecraft.server.v1_16_R3.MinecraftKey;
import net.minecraft.server.v1_16_R3.PacketDataSerializer;
import net.minecraft.server.v1_16_R3.PacketPlayInCustomPayload;
import org.bukkit.ChatColor;
import org.bukkit.craftbukkit.v1_16_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;

import java.lang.reflect.Field;
import java.util.List;

public class PacketReader {
    private final Player player;
    private int count = 0;

    public PacketReader(Player player) {
        this.player = player;
    }

    public void remove(Player player) {
        Channel channel = ((CraftPlayer) player).getHandle().playerConnection.networkManager.channel;
        channel.eventLoop().submit(() -> {
            channel.pipeline().remove(CastelPlugin.channel);
            return null;
        });
    }

    public boolean inject() {
        CraftPlayer nmsPlayer = (CraftPlayer) player;
        Channel channel = nmsPlayer.getHandle().playerConnection.networkManager.channel;
        if (channel.pipeline().get(CastelPlugin.channel) != null)
            return false;

        // IF we want to
        /*
        ChannelDuplexHandler channelDuplexHandler = new ChannelDuplexHandler() {
            @Override
            public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
                super.write(ctx, msg, promise);
            }

            @Override
            public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
                if (msg instanceof PacketPlayInCustomPayload) read((PacketPlayInCustomPayload) msg);
                super.channelRead(ctx, msg);
            }
        };*/

        channel.pipeline().addAfter("decoder", CastelPlugin.channel, new MessageToMessageDecoder<PacketPlayInCustomPayload>() {
            @Override
            protected void decode(ChannelHandlerContext channelHandlerContext, PacketPlayInCustomPayload packet, List<Object> list) throws Exception {
                list.add(packet);
                read(packet, list);
            }

        });

        // If we want to manage the packet sended
        /*
        channel.pipeline().addAfter("encoder", CastelPlugin.channel+":encoder", new MessageToMessageEncoder<PacketPlayOutCustomPayload>() {
            @Override
            protected void encode(ChannelHandlerContext channelHandlerContext, PacketPlayOutCustomPayload packet, List<Object> list) throws Exception {
                list.add(packet);
                CastelPlugin.getInstance().getLogger().info(ChatColor.AQUA+"WRITE : "+ChatColor.WHITE + packet);
            }
        });*/
        //channel.pipeline().addBefore("packet_handler", player.getName(), channelDuplexHandler);
        return true;
    }

    private void read(PacketPlayInCustomPayload packet, List<Object> list) {
        MinecraftKey mKey = (MinecraftKey) getValue(packet, "tag");
        CastelPlugin.getInstance().getLogger().info( ChatColor.GREEN +"READ : " +ChatColor.WHITE + mKey + " ("+ packet +")");
        PacketDataSerializer data = (PacketDataSerializer) getValue(packet, "data");
        if (!mKey.toString().equals(CastelPlugin.channel)) return;
        data.readByte();
        String command = data.e(32767);
        CastelPlugin.getInstance().getLogger().info("COMMAND : " + command);
        NMSPacket packet2 = null;
        FPlayer fPlayer = FPlayers.getInstance().getByPlayer(this.player);
        Faction faction = fPlayer.getFaction();
        if (command.equals("opengui-faction")) {
            String guiName = data.e(32767);
            switch (guiName) {
                case "main":
                    packet2 = new FactionMaSCPacket(fPlayer, faction);
                    break;
                case "bank":
                    packet2 = new FactionBaSCPacket(faction);
                    break;
                case "flag":
                    packet2 = new FactionFlSCPacket(faction);
                    break;
                case "perm":
                    if (faction.getFPlayerLeader() != fPlayer) return;
                    packet2 = new FactionPeSCPacket(faction);
                    break;
            }
            CastelPlugin.getInstance().getLogger().info(ChatColor.RED + "SEND BYTE");
            packet2.sendTo(player);
        } else if (command.equals("changeperm-faction")) {
            packet2 = new FactionChangePermCSPacket(faction, fPlayer, data);
            if (packet2.isHandlded()) new FactionPeSCPacket(faction).sendTo(player);
        }
        list.remove(packet);
    }

    private Object getValue(Object instance, String name) {
        Object result = null;
        try {
            Field field = instance.getClass().getField(name);
            field.setAccessible(true);
            result = field.get(instance);
            field.setAccessible(false);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

}
