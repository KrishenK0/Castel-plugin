package fr.krishenk.castel.commands;

import fr.krishenk.castel.locale.SupportedLanguage;
import fr.krishenk.castel.locale.enums.CommandLang;
import fr.krishenk.castel.utils.internal.enumeration.QuickEnumMap;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.*;

public class CastelParentCommand extends CastelCommand {
    protected final Map<SupportedLanguage, Map<String, CastelCommand>> children = new QuickEnumMap(SupportedLanguage.values());

    public CastelParentCommand(@NonNull String name, @Nullable CastelParentCommand parent) {
        super(name, parent, null);
    }

    public CastelParentCommand(@NonNull String name, boolean playerCmd) {
        super(name, playerCmd);
    }

    public CastelParentCommand(@NonNull String name) {
        super(name, false);
    }

    public final @NonNull Collection<CastelCommand> getChildren(SupportedLanguage language) {
        return Objects.requireNonNull(this.children.get(language), () -> "Null children for command '" + this.name + "' for language: " + language).values();
    }

    public Map<SupportedLanguage, Map<String, CastelCommand>> getChildren() {
        return this.children;
    }

    @Override
    public @NonNull List<String> tabComplete(CommandTabContext context) {
        return context.isAtArg(0) ? TabCompleteManager.getSubCommand(context.getSender(), this, context.args) : CastelParentCommand.emptyTab();
    }

    @Override
    public void execute(CommandContext context) {
        Set filtered = Collections.newSetFromMap(new IdentityHashMap(30));
        boolean isAdmin = context.isPlayer() && context.getCastelPlayer().isAdmin();
        CastelCommand[] subCommands = this.getChildren(context.getSettings().getLanguage()).stream().filter(filtered::add).filter(c -> isAdmin || c.hasPermission(context.getSender())).toArray(CastelCommand[]::new);
        context.sendMessage(CommandLang.COMMAND_HELP_GROUPED_HEADER, "group", this.getDisplayName());
        for (CastelCommand cmd : subCommands) {
            StringBuilder name = new StringBuilder(cmd.getDisplayName().parse(context.getSettings()));
            for (CastelParentCommand group = cmd.getParent(); group != null; group = group.getParent()) {
                name.insert(0, group.getDisplayName().parse(context.getSettings()) + ' ');
            }
            context.sendMessage(CommandLang.COMMAND_HELP_GROUPED_COMMANDS, "cmd", name, "description", cmd.getDescription().parse(context.getSender()));
        }
    }
}


