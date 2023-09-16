package fr.krishenk.castel.utils;

import fr.krishenk.castel.CastelPlugin;
import io.netty.channel.Channel;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_16_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRegisterChannelEvent;

public class TCListener implements Listener {

    private final CastelPlugin tcPlugin;
    //private final TCSender tcSender;

    public TCListener(CastelPlugin plugin)
    {
        this.tcPlugin = plugin;
        //this.tcSender = new TCSender(plugin);
        Bukkit.getServer().getPluginManager().registerEvents(this, plugin);
    }

    public static void addChannel(Player player, String channelName) {
        CraftPlayer nmsPlayer = (CraftPlayer) player;
        Channel channel = nmsPlayer.getHandle().playerConnection.networkManager.channel;
        System.out.println(channel.pipeline().get(channelName));
        if (channel.pipeline().get(channelName) != null || nmsPlayer.getListeningPluginChannels().contains(channelName))
            return;
        nmsPlayer.addChannel(channelName);
    }


    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        PacketReader pr = new PacketReader((event.getPlayer()));
        pr.inject();
    }

    @EventHandler
    public void onLeave(PlayerQuitEvent event) {
        PacketReader pr = new PacketReader((event.getPlayer()));
        pr.remove(event.getPlayer());
    }

    @EventHandler
    public void onPlayerRegisterChannel(PlayerRegisterChannelEvent event)
    {
        // Sends custom colors on join
        if (event.getChannel().equals(CastelPlugin.channel))
        {
            System.out.println(event.getPlayer().getDisplayName() + " registered to channel " + CastelPlugin.channel);
            //tcSender.send(event.getPlayer());
        }
    }

    @EventHandler
    public void onPlayerChangedWorld(PlayerChangedWorldEvent event)
    {
        // Resends the packet so that the biomes are right again
        Player player = event.getPlayer();
        if (player.getListeningPluginChannels().contains(CastelPlugin.channel))
        {
            System.out.println(event.getPlayer().getDisplayName() + " re-registered to channel " + CastelPlugin.channel);
            //tcSender.send(player);
        }
    }
}
