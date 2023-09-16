package fr.krishenk.castel.utils;

import fr.krishenk.castel.commands.CastelCommand;
import fr.krishenk.castel.commands.CastelParentCommand;
import fr.krishenk.castel.commands.CommandContext;
import fr.krishenk.castel.lang.Config;
import fr.krishenk.castel.lang.Lang;
import fr.krishenk.castel.locale.CastelLang;
import fr.krishenk.castel.locale.LanguageManager;
import fr.krishenk.castel.locale.SupportedLanguage;
import fr.krishenk.castel.locale.compiler.builders.MessageObjectLinker;
import fr.krishenk.castel.locale.provider.MessageBuilder;

import java.util.Arrays;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class CmdHelpPagination {
    private final CommandContext context;
    private final int itemsPerPage;
    private final Supplier<CastelCommand[]> supplier;

    public CmdHelpPagination(CommandContext context, int itemsPerPage, Supplier<CastelCommand[]> supplier) {
        this.context = context;
        this.itemsPerPage = itemsPerPage;
        this.supplier = supplier;
    }

    public int getPageNumbers(int commands) {
        return commands % this.itemsPerPage != 0 ? commands / this.itemsPerPage + 1 : commands / this.itemsPerPage;
    }

    public void execute() {
        String commandName = this.context.getCommand().getName();
        int page = 0;
        if (this.context.assertArgs(1)) {
            try {
                page = Integer.parseInt(this.context.arg(0)) - 1;
                if (page + 1 < 1) {
                    this.context.sendError(Lang.COMMAND_HELP_NEGATIVE_PAGES);
                    return;
                }
            }
            catch (NumberFormatException e) {
                this.context.sendError(CastelLang.COMMANDS_UNKNOWN_COMMAND, "cmd", commandName);
                return;
            }
        }
        CastelCommand[] commands = this.supplier.get();
        int eachPage = Config.HELP_COMMANDS.getInt();
        int maxPages = this.getPageNumbers(commands.length);
        List<CastelCommand> selectedCmds = Arrays.stream(commands).skip((long)page * (long)eachPage).limit(eachPage).collect(Collectors.toList());
        this.context.var("max_pages", maxPages).var("previous_page", page).var("page", page + 1).var("next_page", page + 2).var("command", commandName);
        if (selectedCmds.isEmpty()) {
            this.context.sendError(Lang.COMMAND_HELP_NO_MORE_PAGES);
            return;
        }
        this.context.sendMessage(Lang.COMMAND_HELP_HEADER);
        selectedCmds.forEach(cmd -> {
            StringBuilder name = new StringBuilder(cmd.getDisplayName().parse(this.context.getSettings()));
            for (CastelParentCommand group = cmd.getParent(); group != null; group = group.getParent()) {
                name.insert(0, group.getDisplayName().parse(this.context.getSettings()) + ' ');
            }
            String usage = cmd.getUsage().parse(this.context.getSender());
            if (usage == null) {
                usage = "";
            }
            this.context.var("cmd", name.toString()).var("usage", usage).var("command", commandName);
            this.context.getSettings().raw("description", cmd.getDescription() == null ? CastelLang.NONE : cmd.getDescription());
            this.context.sendMessage(Lang.COMMAND_HELP_COMMANDS);
        });
        MessageObjectLinker linker = new MessageObjectLinker();
        int footerPages = Config.HELP_FOOTER_PAGES.getInt();
        int startPages = 1;
        int endPages = maxPages;
        if (endPages * 2 + 1 > footerPages) {
            if (page - footerPages < 0 && endPages > footerPages + page) {
                endPages = Math.min(footerPages * 2, maxPages);
            } else if (page + footerPages > endPages && page - footerPages * 2 > 0) {
                startPages = endPages - footerPages * 2;
            } else {
                endPages = Math.min(endPages, page + 1 + footerPages);
                startPages = Math.max(1, page + 1 - footerPages);
            }
        }
        SupportedLanguage locale = LanguageManager.localeOf(this.context.getSender());
        for (int pages = startPages; pages <= endPages; ++pages) {
            MessageBuilder subSettings = new MessageBuilder().raws("number", pages, "previous_page", page, "page", page + 1, "next_page", page + 2, "max_pages", maxPages, "command", commandName);
            if (pages == page + 1) {
                linker.add(Lang.COMMAND_HELP_FOOTER_CURRENT_PAGE.getProvider(locale).getMessage(), subSettings);
            } else {
                linker.add(Lang.COMMAND_HELP_FOOTER_PAGE.getProvider(locale).getMessage(), subSettings);
                Lang.COMMAND_HELP_FOOTER_PAGE.getProvider(locale).getMessage();
            }
            linker.add(" ");
        }
        this.context.getSettings().raw("pages", linker);
        this.context.sendMessage(Lang.COMMAND_HELP_FOOTER);
    }
}

