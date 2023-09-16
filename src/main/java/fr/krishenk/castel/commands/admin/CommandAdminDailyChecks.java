package fr.krishenk.castel.commands.admin;

import fr.krishenk.castel.CastelPlugin;
import fr.krishenk.castel.commands.CastelCommand;
import fr.krishenk.castel.commands.CastelParentCommand;
import fr.krishenk.castel.commands.CommandContext;
import fr.krishenk.castel.commands.CommandTabContext;
import fr.krishenk.castel.lang.Lang;
import fr.krishenk.castel.managers.daily.DailyChecksManager;
import fr.krishenk.castel.managers.daily.TimeZoneHandler;
import fr.krishenk.castel.utils.time.TimeFormatter;
import fr.krishenk.castel.utils.time.TimeUtils;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Locale;

public class CommandAdminDailyChecks extends CastelCommand {
    public CommandAdminDailyChecks(CastelParentCommand parent) {
        super("dailychecks", parent);
    }

    @Override
    public void execute(CommandContext context) {
        if (context.assertArgs(1)) {
            switch (context.arg(0).toLowerCase(Locale.ENGLISH)) {
                case "run":
                    context.sendMessage(Lang.COMMAND_ADMIN_DAILYCHECKS_RUN);
                    CastelPlugin.taskScheduler().async().execute(() -> DailyChecksManager.getInstance().runAndRenew());
                    return;
                case "skip":
                    if (DailyChecksManager.getInstance().isSkipping()) {
                        context.sendError(Lang.COMMAND_ADMIN_DAILYCHECKS_SKIPPED);
                        return;
                    }
                    
                    context.sendMessage(Lang.COMMAND_ADMIN_DAILYCHECKS_SKIP);
                    DailyChecksManager.getInstance().setSkipped(true);
                    return;
                case "resume":
                    if (!DailyChecksManager.getInstance().isSkipping()) {
                        context.sendError(Lang.COMMAND_ADMIN_DAILYCHECKS_RESUMED);
                        return;
                    }

                    context.sendError(Lang.COMMAND_ADMIN_DAILYCHECKS_RESUME);
                    DailyChecksManager.getInstance().setSkipped(false);
                    return;
                default:
                    context.sendError(Lang.COMMAND_ADMIN_DAILYCHECKS_USAGE);
            }
        } else
            context.sendMessage(Lang.COMMAND_ADMIN_DAILYCHECKS_INFO, "next", TimeFormatter.of(DailyChecksManager.getInstance().untilNextChecks(ChronoUnit.MILLIS)), "daily_checks", TimeZoneHandler.DAILY_CHECKS.toString(), "time", TimeUtils.TIME_FORMAT.format(LocalTime.now(TimeZoneHandler.SERVER_TIME_ZONE)), "state", DailyChecksManager.getInstance().isSkipping() ? "&4Skip" : "&2Ongoing");
    }

    @Override
    public @NonNull List<String> tabComplete(@NonNull CommandTabContext context) {
        return context.isAtArg(0) ? tabComplete("run", "skip", "resume") : emptyTab();
    }
}
