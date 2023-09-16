package fr.krishenk.castel.commands.admin.debugging;

import fr.krishenk.castel.commands.CastelCommand;
import fr.krishenk.castel.commands.CastelParentCommand;
import fr.krishenk.castel.commands.CommandContext;
import fr.krishenk.castel.commands.CommandTabContext;
import fr.krishenk.castel.data.Pair;
import fr.krishenk.castel.lang.Lang;
import fr.krishenk.castel.locale.MessageHandler;
import fr.krishenk.castel.locale.compiler.placeholders.CastelPlaceholder;
import fr.krishenk.castel.locale.compiler.placeholders.Placeholder;
import fr.krishenk.castel.locale.compiler.placeholders.PlaceholderParser;
import fr.krishenk.castel.locale.provider.MessageBuilder;
import fr.krishenk.castel.utils.MathUtils;
import fr.krishenk.castel.utils.compilers.MathCompiler;
import fr.krishenk.castel.utils.string.StringUtils;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class CommandAdminEvaluate extends CastelCommand {
    private static final Pattern HEXADECIMAL = Pattern.compile("0x[A-Za-z0-9]+");
    private static final Pattern SHORT_DECIMAL = Pattern.compile("[^\\d]+(\\.\\d+)");
    private static final Pattern VARIABLE_BINDINGS = Pattern.compile("(?:let|var|const|val|where)\\s+\\w+\\s*=");
    private static final Pattern EXPLICIT_POS_SIGN = Pattern.compile("[^A-Za-z0-9})\\]\\s]\\s*\\+\\s*\\d+");
    private static final String[] NAMES = new String[MathCompiler.getConstants().size() + MathCompiler.getFunctions().size()];
    public static final double DEFAULT_VAR_VAL = 10.0;
    private static final List<Pair<String, String>> OPERATIONS = Arrays.asList(Pair.of("+", "Addition"), Pair.of("-", "Subtraction"), Pair.of("*", "Multipication"), Pair.of("/", "Division"), Pair.of("^", "Exponentiation"), Pair.of("%", "Remainder"), Pair.of("#", "Bit Rotation Right"), Pair.of("@", "Bit Rotation Left"), Pair.of("|", "Bitwise OR"), Pair.of("&", "Bitwise AND"), Pair.of("!", "Bitwise XOR"), Pair.of(">", "Bitwise shift right"), Pair.of("<", "Bitwise shift left"));

    private static String getParams(String meth) {
        for (Method method : Math.class.getMethods()) {
            if (method.getName().equals(meth)) {
                return '(' + StringUtils.join(Arrays.stream(method.getParameters()).map(Parameter::getName).toArray(String[]::new), ",") + ')';
            }
        }
        return "{}";
    }

    private static StringBuilder argsOf(int count) {
        StringBuilder args = new StringBuilder("(");
        for (int i = 0; i < count; i++) {
            args.append("arg").append("i");
            if (i < count - 1) args.append(',');
        }
        return args.append(')');
    }

    public CommandAdminEvaluate(CastelParentCommand parent) {
        super("evaluate", parent);
    }

    @Override
    public void execute(CommandContext context) {
        if (!context.requireArgs(1)) {
            String message = context.joinArgs();
            compile(context.getSender(), message);
        }
    }

    public static MathCompiler.Expression compile(CommandSender sender, String message) {
        MathCompiler.Expression result;
        try {
            result = MathCompiler.compile(message);
        } catch (Exception e) {
            Lang.COMMAND_ADMIN_EVALUATE_FAILED.sendError(sender, new MessageBuilder().raws("translated", message, "result", e.getMessage()));
            if (sender instanceof Player)
                MessageHandler.sendPluginMessage(sender, "&eWarning&8: &6Due to the nature of Minecraft's font typeface, some error pointers may point to inaccurate characters within the expression, to see the correct location, go to &2Options &7-> &2Language &7-> &2Force Unicode Font: ON\n&6or use this command from your console.");

            if (message.contains("**"))
                MessageHandler.sendMessage(sender, "&eWarning&8: &2^ &6is used as the exponentiation operator not &2**");

            Matcher matcher = HEXADECIMAL.matcher(message);
            if (matcher.find())
                MessageHandler.sendMessage(sender, "&eWarning&8: &6Hexadecimal numbers &2" + matcher.group() + "&2 &6are not supported.");

            matcher = SHORT_DECIMAL.matcher(message);
            if (matcher.find())
                MessageHandler.sendMessage(sender, "&eWarning&8: &6Short decimal notations &2'" + matcher.group(1) + "&2' &6are not supported.");

            matcher = EXPLICIT_POS_SIGN.matcher(message);
            if (matcher.find())
                MessageHandler.sendMessage(sender, "&eWarning&8: &6Explicit positive signs &2'" + matcher.group() + "&2' &6are not supported.");

            matcher = VARIABLE_BINDINGS.matcher(message);
            if (matcher.find())
                MessageHandler.sendMessage(sender, "&eWarning&8: &6Variable bindings &2'" + matcher.group() + "&2' &6are not supported.");

            return null;
        }

        double evaluated = result.eval(x -> {
            if (sender instanceof Player && x.startsWith("guilds")) {
                Placeholder parsed = PlaceholderParser.parsePlaceholder(x.substring("guilds".length()+1));
                Object res = parsed.request(new MessageBuilder().withContext(sender));

                try {
                    if (res == null) {
                        MessageHandler.sendMessage(sender, "&cError&8: &6Failed to parse &e'" + x + "&e' &6placeholder, defaulting to &e10");
                        return 10.0;
                    } else {
                        try {
                            return MathUtils.expectDouble(x, result);
                        } catch (IllegalArgumentException e) {
                            return Double.parseDouble(res.toString());
                        }
                    }
                } catch (NumberFormatException e) {
                    String hint;
                    if (parsed.modifier == null) hint = "";
                    else hint = "(&9hint&8: &7Consider removing the &2" + parsed.modifier + " &7placeholder modifier";

                    MessageHandler.sendMessage(sender, "&cError&8: &6The &e'" + x + "&e' &6placeholder isn't a number placeholder, defaulting to &e10 " + hint);
                    return 10.0;
                }
            } else {
                Object global = new MessageBuilder().withContext(sender).processPlaceholder(x);
                if (global != null) {
                    return MathUtils.expectDouble(x, global);
                } else {
                    MessageHandler.sendMessage(sender, "&eWarning&8: &6Variable &e'" + x + "' &6is unrecognized. Defaulting to &210.0 &6for evaluation to succeed.");
                    return 10.0;
                }
            }
        });
        Lang.COMMAND_ADMIN_EVALUATE_EVALUATED.sendMessage(sender, "translated", message, "result", evaluated, "object-code", result);
        if (evaluated == Double.POSITIVE_INFINITY || evaluated == Double.NEGATIVE_INFINITY || Double.isNaN(evaluated))
            MessageHandler.sendMessage(sender, "&eWarning&8: &2Infinity/NaN &6answer means you've performed an &2undefined &6mathematical operation such as dividing by zero.");
        return result;
    }

    @Override
    public @NonNull List<String> tabComplete(@NonNull CommandTabContext context) {
        if (!context.assertArgs(1)) return Arrays.asList(NAMES);
        String arg = context.currentArg().toLowerCase();
        if (context.isNumber(context.args.length - 1)) return emptyTab();
        if (arg.startsWith("c"))
            return CastelPlaceholder.NAMES.keySet().stream().filter(x -> !arg.startsWith("guilds_") || x.startsWith(StringUtils.remove(arg, "guilds_"))).map(x -> "guilds_" + x).collect(Collectors.toList());
        if (arg.startsWith("[")) return tabComplete("[<number> <days|hours|mins|secs>]");
        if (arg.startsWith("{")) return tabComplete("{variable}");
        if (arg.startsWith("(")) return tabComplete("Open subexpression");
        if (arg.startsWith(")")) return tabComplete("Close subexpression");
        return OPERATIONS.stream().anyMatch(x -> x.getKey().equals(arg)) ? OPERATIONS.stream().map(x -> x.getKey() + ' ' + x.getValue()).collect(Collectors.toList()) : context.suggest(context.args.length - 1, NAMES);
    }

    static {
        int i = 0;
        for (String s : MathCompiler.getConstants().keySet()) {
            NAMES[i++] = s;
        }

        for (Map.Entry<String, MathCompiler.Function> stringFunctionEntry : MathCompiler.getFunctions().entrySet()) {
            String params = getParams(stringFunctionEntry.getKey());
            NAMES[i++] = stringFunctionEntry.getKey() + (params.equals("()") ? argsOf(stringFunctionEntry.getValue().getArgCount()) : params);
        }
    }
}
