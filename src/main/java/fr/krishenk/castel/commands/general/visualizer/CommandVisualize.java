package fr.krishenk.castel.commands.general.visualizer;

import fr.krishenk.castel.CastelPluginPermission;
import fr.krishenk.castel.commands.CastelParentCommand;
import fr.krishenk.castel.commands.CommandContext;
import fr.krishenk.castel.constants.land.Land;
import fr.krishenk.castel.constants.land.ProtectionSign;
import fr.krishenk.castel.constants.land.location.SimpleChunkLocation;
import fr.krishenk.castel.constants.land.location.SimpleLocation;
import fr.krishenk.castel.constants.player.CastelPlayer;
import fr.krishenk.castel.lang.Lang;
import fr.krishenk.castel.locale.compiler.MessageCompilerSettings;
import fr.krishenk.castel.locale.compiler.PlaceholderTranslationContext;
import fr.krishenk.castel.locale.provider.MessageBuilder;
import fr.krishenk.castel.managers.land.indicator.LandVisualizer;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

public class CommandVisualize extends CastelParentCommand {
    public CommandVisualize() {
        super("visualize", true);
        if (!this.isDisabled()) {
            new CommandVisualizePermanent(this);
            new CommandVisualizeToggle(this);
            new CommandVisualizeMarkers(this);
            new CommandVisualizeAll(this);
        }
    }

    @Override
    public void execute(CommandContext context) {
        if (!context.assertPlayer()) {
            Player player = context.senderAsPlayer();
            Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
                SimpleChunkLocation chunk = SimpleChunkLocation.of(player.getLocation());
                Land land = chunk.getLand();
                CastelPlayer cp = CastelPlayer.getCastelPlayer(player);
                (new LandVisualizer()).forPlayer(player, cp).forLand(land, chunk.toChunk()).display(true);
                Lang lang;
                if (!context.hasPermission(CastelPluginPermission.COMMAND_VISUALIZE_DETAILS, true)) {
                    lang = Lang.COMMAND_VISUALIZE_DISPLAY;
                } else {
                    lang = Lang.COMMAND_VISUALIZE_ADMIN_DISPLAY;
                }
                StringBuilder protectionSigns = new StringBuilder();
                if (land != null) {
                    for (ProtectionSign protection : land.getProtectedBlocks().values()) {
                        SimpleLocation location = protection.getLocation();
                        OfflinePlayer owner = Bukkit.getOfflinePlayer(protection.getOwner());
                        protectionSigns.append("&2").append(owner.getName()).append("&8| &5").append(location.getX()).append("&7, &6").append(location.getY()).append("&7, &6").append(location.getZ()).append(";&2Teleport;/tp").append(location.getX()).append(' ').append(location.getY()).append(' ').append(location.getZ()).append('}').append(" &8| &2").append(protection.getProtectionType().getDisplayname()).append('\n');
                    }
                }

                if (protectionSigns.length() == 0) {
                    protectionSigns.append("None");
                }
                MessageBuilder settings = (new MessageBuilder()).raws("protection-signs", new PlaceholderTranslationContext(protectionSigns, MessageCompilerSettings.all()), "guild", land != null && land.getGuild() != null ? land.getGuild().getName() : "wilderness", "x", chunk.getX(), "z", chunk.getZ());
                lang.getMessageObject(cp.getLanguage()).getSimpleProvider().send(player, settings);
            });
        }
    }
}
