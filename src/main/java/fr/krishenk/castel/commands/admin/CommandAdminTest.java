package fr.krishenk.castel.commands.admin;

import fr.krishenk.castel.commands.CastelCommand;
import fr.krishenk.castel.commands.CastelParentCommand;
import fr.krishenk.castel.commands.CommandContext;
import fr.krishenk.castel.commands.CommandTabContext;
import fr.krishenk.castel.locale.LanguageManager;
import fr.krishenk.castel.locale.MessageHandler;
import fr.krishenk.castel.locale.compiler.MessageCompiler;
import fr.krishenk.castel.locale.compiler.MessageCompilerSettings;
import fr.krishenk.castel.locale.compiler.MessageObject;
import fr.krishenk.castel.locale.compiler.placeholders.CastelPlaceholder;
import fr.krishenk.castel.locale.compiler.placeholders.StandardCastelPlaceholder;
import fr.krishenk.castel.locale.provider.MessageBuilder;
import fr.krishenk.castel.utils.string.StringUtils;
import net.md_5.bungee.api.chat.BaseComponent;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

public class CommandAdminTest extends CastelCommand {
    public CommandAdminTest(CastelParentCommand parent) {
        super("test", parent);
    }

    @Override
    public void execute(CommandContext context) {
        if (!context.requireArgs(1)) {
            String message = MessageHandler.replace(context.joinArgs(), "\\n", "\n");
            MessageCompiler compiler = new MessageCompiler(message.toCharArray(), new MessageCompilerSettings(true, false, true, true, true, null));
            MessageObject obj = compiler.compileObject();
            MessageBuilder settings = (new MessageBuilder()).withContext(context.getSender()).raw("test_int", ThreadLocalRandom.current().nextInt(4, 100)).raw("test_string", StringUtils.random(5, 20, "aldfoasdbnopu2910871054sdfjvnaoghwhserm;sdfo$)@#&*)%^&*!@)#")).raw("test", ThreadLocalRandom.current().nextInt(1, 100));
            context.getSender().sendMessage(MessageCompiler.compile("&2Compiled&8: &f%built%").buildPlain(new MessageBuilder().raw("built", compiler)));
            context.getSender().sendMessage(obj.buildPlain(settings));
            MessageHandler.sendPluginMessage(context.getSender(), "&7---------------- Complex:");
            BaseComponent[][] componentsBuild = obj.build(settings).create();
            for (BaseComponent[] components : componentsBuild) {
                context.getSender().spigot().sendMessage(components);
            }

            if (compiler.hasErrors()) {
                MessageHandler.sendPluginMessage(context.getSender(), "&8==================");
                MessageHandler.sendPluginMessage(context.getSender(), "&4Error(s)&8:");
                context.getSender().sendMessage(compiler.joinExceptions());
            }
        }
    }

    @Override
    public @NonNull List<String> tabComplete(@NonNull CommandTabContext context) {
        if (context.argsLengthEquals(0)) return emptyTab();
        String currentArg = context.currentArg();
        if (currentArg.isEmpty()) return emptyTab();
        else if (currentArg.equals("~")) {
            String[] testArgs = Arrays.stream(context.args).limit(context.args.length - 1L).toArray(String[]::new);
            return tabComplete(joinArgs(testArgs));
        } else if ("%guilds_%".startsWith(currentArg)) {
            return CastelPlaceholder.NAMES.keySet().stream().map(x -> "%guilds_" + x + "%").collect(Collectors.toList());
        } else if (currentArg.startsWith("%guilds_")) {
            return CastelPlaceholder.NAMES.keySet().stream().filter(x -> x.startsWith(currentArg.substring(10))).map(x -> "%guilds_" + x + "%").collect(Collectors.toList());
        } else if (currentArg.startsWith("{$")) {
            List<String> macros = new ArrayList<>(StandardCastelPlaceholder.getGlobalMacros().keySet());
            macros.addAll(LanguageManager.localeOf(context.getSender()).getMessages().keySet().stream().filter((x) -> x.getPath()[0].equals("variables")).map(x -> x.getPath()[1]).collect(Collectors.toList()));
            return macros.stream().filter(x -> currentArg.equals("{$") || x.toLowerCase(Locale.ENGLISH).contains(currentArg.substring(2).toLowerCase(Locale.ENGLISH))).map(x -> "{$" + x + '}').collect(Collectors.toList());
        } else if (currentArg.startsWith("{%")) {
            return tabComplete("{%colorBackRReferencePlaceholder [& colorIndex]}");
        } else if ("hover:{".startsWith(currentArg)) {
            return tabComplete("hover:{}");
        } else if (currentArg.startsWith("hover:{")) {
            return tabComplete("message", "hover", "action");
        } else {
            return "&#".startsWith(currentArg) ? tabComplete("&#<hex>") : emptyTab();
        }
    }
}
