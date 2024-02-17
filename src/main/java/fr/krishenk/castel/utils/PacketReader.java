package fr.krishenk.castel.utils;

import fr.krishenk.castel.CastelPlugin;
import fr.krishenk.castel.constants.group.Guild;
import fr.krishenk.castel.constants.player.CastelPlayer;
import fr.krishenk.castel.constants.player.StandardGuildPermission;
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
        if (!mKey.toString().equals(CastelPlugin.channel)) return;
        CastelPlugin.getInstance().getLogger().info( ChatColor.GREEN +"READ : " +ChatColor.WHITE + mKey + " ("+ packet +")");
        PacketDataSerializer data = (PacketDataSerializer) getValue(packet, "data");
        data.readByte();
        String command = data.e(32767);
        CastelPlugin.getInstance().getLogger().info(ChatColor.GOLD +"COMMAND : " +ChatColor.WHITE + command);
        NMSPacket packet2 = null;
        CastelPlayer cPlayer = CastelPlayer.getCastelPlayer(this.player);
        Guild guild = cPlayer.getGuild();
        if (command.equals("opengui-faction")) {
            String guiName = data.e(32767);
            System.out.println(ChatColor.YELLOW +"ARG : " +ChatColor.WHITE + guiName);
            switch (guiName) {
                case "main":
                    packet2 = new GuildMaSCPacket(cPlayer, guild);
                    break;
                case "bank":
                    packet2 = new GuildBaSCPacket(guild);
                    break;
                case "flag":
                    packet2 = new GuildFlSCPacket(guild);
                    break;
                case "invite":
                    packet2 = new GuildInSCPacket(guild, cPlayer);
                    break;
                case "perm":
                    if (guild.getLeader() != cPlayer) return;
                    packet2 = new GuildPeSCPacket(guild);
                    break;
                case "claims":
                    if (!cPlayer.hasPermission(StandardGuildPermission.CLAIM)) return;
                    packet2 = new GuildClSCPacket(cPlayer, guild);
                    break;
            }
        } else if (command.equals("update-faction")) {
            String param = data.e(32767);
            System.out.println(ChatColor.YELLOW +"ARG : " +ChatColor.WHITE + param);
            switch (param) {
                case "perm":
                    packet2 = new GuildPeCSPacket(guild, cPlayer, data);
                    if (packet2.isHandled()) new GuildPeSCPacket(guild).sendTo(player);
                    break;
                case "flag":
                    packet2 = new GuildFlCSPacket(guild, cPlayer, data);
                    if (packet2.isHandled()) new GuildFlSCPacket(guild).sendTo(player);
                    break;
            }
        }
        CastelPlugin.getInstance().getLogger().info(ChatColor.RED + "SEND BYTE");
        packet2.sendTo(player);
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
