package fr.krishenk.castel.commands.general.misc;

import fr.krishenk.castel.CLogger;
import fr.krishenk.castel.commands.CastelCommand;
import fr.krishenk.castel.commands.CastelCommandHandler;
import fr.krishenk.castel.commands.CommandContext;
import fr.krishenk.castel.config.managers.NewConfigManager;
import fr.krishenk.castel.constants.group.model.relationships.GuildRelation;
import fr.krishenk.castel.constants.group.upgradable.Powerup;
import fr.krishenk.castel.constants.player.Rank;
import fr.krishenk.castel.data.DataManager;
import fr.krishenk.castel.lang.Lang;
import fr.krishenk.castel.locale.LanguageManager;
import fr.krishenk.castel.locale.MessageHandler;
import fr.krishenk.castel.locale.compiler.placeholders.StandardCastelPlaceholder;
import org.bukkit.Bukkit;
import org.bukkit.event.HandlerList;

public class CommandReload extends CastelCommand {
    private static boolean reloading;

    public CommandReload() {
        super("reload");
    }

    public static boolean isReloading() {
        return reloading;
    }

    @Override
    public void execute(CommandContext context) {
        if (reloading) {
            MessageHandler.sendPluginMessage(context.getSender(), "&4The plugin is still in the process of reloading... Please wait.");
            return;
        }
        reloading = true;
//        ConfigWatcher.setAccepting(false);
        CLogger.info("Performing a full reload for the plugin...");
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                CLogger.info("Setting up file manager...");
                NewConfigManager manager = new NewConfigManager(plugin);
                manager.createDataFolderIfMissing();
                CLogger.info("Reloading language files...");
                LanguageManager.loadAll();
//                CLogger.info("Reloading configs...");
//                TODO: Implements
                CLogger.info("Re-registering event listeners...");
                HandlerList.unregisterAll(plugin);
                plugin.registerAllEvents();
                CLogger.info("Re-registering commands...");
                CastelCommandHandler.reload();
                CLogger.info("Reinitializing other services...");
                Rank.init();
                GuildRelation.init();
                Powerup.init();
                StandardCastelPlaceholder.init();
                CLogger.info("Saving all data...");
                for (DataManager dataManager : plugin.getDataCenter().getAllDataManagers()) {
                    dataManager.saveAll(true);
                }
                context.sendMessage(Lang.COMMAND_RELOAD_SUCCESS);
                CLogger.info("Reloading done.");
                reloading = false;
//                ConfigWatcher.setAccepting(true);
            } catch (Throwable e) {
                reloading = false;
                context.fail(Lang.COMMAND_RELOAD_ERROR);
                e.printStackTrace();
            }
        });
    }
}
