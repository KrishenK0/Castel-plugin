package fr.krishenk.castel.commands.admin;

import fr.krishenk.castel.commands.CastelCommand;
import fr.krishenk.castel.commands.CastelParentCommand;
import fr.krishenk.castel.commands.CommandAdminMigrate;
import fr.krishenk.castel.commands.CommandContext;
import fr.krishenk.castel.commands.admin.claim.CommandAdminClaim;
import fr.krishenk.castel.commands.admin.claim.CommandAdminUnclaim;
import fr.krishenk.castel.commands.admin.debugging.*;
import fr.krishenk.castel.commands.admin.debugging.debug.CommandAdminDebug;
import fr.krishenk.castel.commands.admin.item.CommandAdminItem;
import fr.krishenk.castel.constants.player.CastelPlayer;
import fr.krishenk.castel.lang.Config;
import fr.krishenk.castel.utils.CmdHelpPagination;
import org.bukkit.OfflinePlayer;

import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Stream;

public class CommandAdmin extends CastelParentCommand {
    private static CommandAdmin instance;

    public static CommandAdmin getInstance() {
        return instance;
    }

    public static void setInstance(CommandAdmin instance) {
        CommandAdmin.instance = instance;
    }

    public CommandAdmin() {
        super("admin");
        if (!this.isDisabled()) {
            new CommandAdminToggle(this);
            new CommandAdminToggles(this);
            new CommandAdminSpy(this);
            new CommandAdminJoin(this);
            new CommandAdminTest(this);
            new CommandAdminSound(this);
            new CommandAdminUnclaim(this);
            new CommandAdminDisband(this);
            new CommandAdminResourcePoints(this);
            new CommandAdminClaim(this);
//            new CommandAdminNexus(this);
            new CommandAdminRank(this);
//            new CommandAdminNation(this);
            new CommandAdminMaxLandModifier(this);
//            new CommandAdminShield(this);
//            new CommandAdminMasswar(this);
            new CommandAdminKick(this);
            new CommandAdminBank(this);
            new CommandAdminHologram(this);
            new CommandAdminEvaluate(this);
            new CommandAdminDailyChecks(this);
            new CommandAdminLand(this);
            new CommandAdminPlayer(this);
            new CommandAdminHome(this);
            new CommandAdminCreate(this);
            new CommandAdminRename(this);
            new CommandAdminPacifism(this);
            new CommandAdminExecute(this);
//            new CommandAdminGUI(this);
            new CommandAdminEntity(this);
            new CommandAdminItem(this);
//            new CommandAdminTurret(this);
            new CommandAdminPermanent(this);
            new CommandAdminCommand(this);
            new CommandAdminCommands(this);
            new CommandAdminTrack(this);
//            new CommandAdminAddons(this);
//            new CommandAdminLanguagePack(this);
            new CommandAdminPurge(this);
            new CommandAdminResetConfigs(this);
            new CommandAdminOpenFile(this);
            new CommandAdminFiles(this);
//            new CommandAdminMissingGUIs(this);
//            new CommandAdminSearchConfig(this);
            new CommandAdminDebug(this);
            new CommandAdminMigrate(this);
            new CommandAdminFSCK(this);
        }
    }

    @Override
    public void execute(CommandContext context) {
        Set<CastelCommand> filtered = Collections.newSetFromMap(new IdentityHashMap<>(this.children.size()));
        boolean admin = context.isPlayer() && CastelPlayer.getCastelPlayer((OfflinePlayer) context.getSender()).isAdmin();
        Stream<CastelCommand> stream = this.getChildren(context.getSettings().getLanguage()).stream();
        Objects.requireNonNull(filtered);
        CastelCommand[] subCommands = stream.filter(filtered::add).filter(c -> admin || c.hasPermission(context.getSender())).toArray(x -> filtered.toArray(new CastelCommand[0]));
        (new CmdHelpPagination(context, Config.HELP_COMMANDS.getInt(), () -> subCommands)).execute();
    }
}
