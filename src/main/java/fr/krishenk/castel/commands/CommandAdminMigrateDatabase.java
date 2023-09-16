package fr.krishenk.castel.commands;

import fr.krishenk.castel.CLogger;
import fr.krishenk.castel.CastelPlugin;
import fr.krishenk.castel.data.CastelDataCenter;
import fr.krishenk.castel.data.DataManager;
import fr.krishenk.castel.data.Database;
import fr.krishenk.castel.lang.Config;
import fr.krishenk.castel.lang.Lang;
import fr.krishenk.castel.utils.internal.Fn;
import org.bukkit.Bukkit;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.jetbrains.annotations.NotNull;

import java.time.Duration;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

public class CommandAdminMigrateDatabase extends CastelCommand {
    private static long executionCooldown;
    private static long migrationCooldown;
    private static final String oldDbType = Config.DATABASE_METHOD.getString();
    private static boolean wait;

    public CommandAdminMigrateDatabase(CastelParentCommand parent) {
        super("database", parent);
    }

    public void loadAllData() {
        CastelDataCenter dataCenter = plugin.getDataCenter();
        CLogger.info("Loading all data... This might take several minutes.");

        for (DataManager<?, ?> dataManager : dataCenter.getAllDataManagers()) {
            CLogger.info("Loading " + dataManager.getDisplayName() + " data...");
            dataManager.loadAllData();
            CLogger.info("Loaded " + dataManager.getDisplayName() + " data.");
        }
    }

    public void saveAll(CastelDataCenter dataCenter) {
        CLogger.info("Saving all data... This might take several minutes");

        for (DataManager<?, ?> dataManager : dataCenter.getAllDataManagers()) {
            CLogger.info("Saving " + dataManager.getDisplayName() + " data...");
            dataManager.saveAll(false);
            CLogger.info("Saved " + dataManager.getDisplayName() + " data.");
        }
    }

    public void createNewDataCenterAndSaveAll() {
        CLogger.info("Creating new data center...");
        for (DataManager<?, ?> dataManager : plugin.getDataCenter().getAllDataManagers()) {
            dataManager.setSavingState(false);
        }

        Database.ranSchema = false;
        CastelDataCenter newDataCenter = new CastelDataCenter(plugin);
        CLogger.info("Transferring data between data centers...");

        List<DataManager<?, ?>> dataManagers = plugin.getDataCenter().getAllDataManagers();
        for (DataManager<?, ?> dataManager : dataManagers) {
            dataManagers = newDataCenter.getAllDataManagers();
            Iterator<DataManager<?, ?>> it = dataManagers.iterator();

            DataManager<?, ?> copyDataManager;
            while (true) {
                if (!it.hasNext()) {
                    copyDataManager = null;
                    break;
                }

                DataManager<?, ?> tempDataManager = it.next(); // No idea how to name the variable
                if (Objects.equals(tempDataManager.getClass(), tempDataManager.getClass())) {
                    copyDataManager = tempDataManager;
                    break;
                }
            }
            dataManager.copyCacheTo((DataManager) Fn.cast(copyDataManager));
        }
    }

    @Override
    public @NotNull CommandResult executeX(@NonNull CommandContext context) {
        if (context.isPlayer()) return context.fail(Lang.COMMAND_ADMIN_MIGRATE_DATABASE_CONSOLE_ONLY);
        else if (wait) return context.fail(Lang.COMMAND_ADMIN_MIGRATE_DATABASE_PLEASE_WAIT);
        else {
            long now = System.currentTimeMillis();
            if (migrationCooldown != 0L && now - migrationCooldown <= Duration.ofMinutes(5L).toMillis()) {
//                Config.MAIN.reload();
                if (oldDbType.equals(Config.DATABASE_METHOD.getString())) {
                    context.var("method", Config.DATABASE_METHOD.getString());
                    return context.fail(Lang.COMMAND_ADMIN_MIGRATE_DATABASE_SAME_DATABASE_TYPE);
                } else {
                    wait = true;
                    CastelPlugin.taskScheduler().executeAsync(() -> {
                        try {
                            this.createNewDataCenterAndSaveAll();
                            context.sendMessage(Lang.COMMAND_ADMIN_MIGRATE_DATABASE_DONE);
                            CastelPlugin.taskScheduler().syncLater(Bukkit::shutdown, Duration.ofSeconds(10L));
                        } catch (Throwable e) {
                            context.sendMessage(Lang.COMMAND_ADMIN_MIGRATE_DATABASE_ERROR_LOADING_NEW_DATABASE);
                            e.getStackTrace();
                        }
                    });
                    return CommandResult.SUCCESS;
                }
            } else if (now - executionCooldown >= Duration.ofSeconds(5L).toMillis()) {
                executionCooldown = now;
                context.sendMessage(Lang.COMMAND_ADMIN_MIGRATE_DATABASE_COOLDOWN);
                return CommandResult.PARTIAL;
            } else {
                wait = true;
                CastelPlugin.taskScheduler().executeAsync(() -> {
                    this.saveAll(plugin.getDataCenter());
                    this.loadAllData();
                    migrationCooldown = now;
                    context.sendMessage(Lang.COMMAND_ADMIN_MIGRATE_DATABASE_START);
                    wait = false;
                });
                return CommandResult.SUCCESS;
            }
        }
    }
}
