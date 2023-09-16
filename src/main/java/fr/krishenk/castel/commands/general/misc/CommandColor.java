package fr.krishenk.castel.commands.general.misc;

import fr.krishenk.castel.commands.CastelCommand;
import fr.krishenk.castel.commands.CommandContext;
import fr.krishenk.castel.commands.CommandTabContext;
import fr.krishenk.castel.constants.group.Guild;
import fr.krishenk.castel.lang.Config;
import fr.krishenk.castel.lang.Lang;
import fr.krishenk.castel.utils.ColorUtils;
import org.bukkit.configuration.ConfigurationSection;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.awt.*;
import java.util.List;

public class CommandColor extends CastelCommand {
    public CommandColor() {
        super("color", true);
    }

    @Override
    public void execute(CommandContext context) {
        if (!context.assertPlayer()) {
            if (!context.assertHasGuild()) {
                Guild guild = context.getGuild();
                if (context.argsLengthEquals(0)) {
                    // Implement getColorPicker
                } else {
                    String colorStr = context.joinArgs();
                    context.var("color", colorStr);
                    Color color = ColorUtils.parseColor(colorStr);
                    if (color == null) context.sendError(Lang.NEXUS_SETTINGS_COLOR_WRONG_HEX);
                    else {
                        context.var("r", color.getRed()).var("b", color.getBlue()).var("g", color.getGreen()).var("hex", color.getRGB()).var("color", ColorUtils.toHexString(color));
                        if (!isColorInRange(context.isAdmin(), color)) context.sendError(Lang.NEXUS_SETTINGS_COLOR_RANGE_BLACKLISTED);
                        else {
                            context.sendMessage(Lang.NEXUS_SETTINGS_COLOR_SET);
                            guild.setColor(color, context.getCastelPlayer());
                        }
                    }
                }
            }
        }
    }

    public static boolean isColorInRange(boolean admin, Color color) {
        if (!admin && Config.COLOR_RANGE_ENABLED.getManager().getBoolean()) {
            float[] hsb = ColorUtils.getHSB(color);
            float hue = hsb[0];
            float saturation = hsb[1];
            float brightness = hsb[2];
            if (saturation > 40.0F && brightness > 40.0F) {
                boolean blacklist = Config.COLOR_RANGE_BLACKLIST.getManager().getBoolean();
                ConfigurationSection section = Config.COLOR_RANGE_COLORS.getManager().getSection();

                boolean matches = false;
                for (String colors : section.getKeys(true)) {
                    List<Integer> list = section.getIntegerList(colors);
                    if (!list.isEmpty()) {
                        float minHue = list.get(0);
                        float maxHue = list.get(1);
                        if (hue >= minHue && hue <= maxHue) {
                            matches = true;
                            break;
                        }
                    }
                }

                return blacklist == matches;
            }
        }

        return true;
    }

    @Override
    public @NonNull List<String> tabComplete(@NonNull CommandTabContext context) {
        return context.isAtArg(0) ? tabComplete(Lang.COMMAND_COLOR_TAB_COMPLETE_HEX.parse(context.getSender()), Lang.COMMAND_COLOR_TAB_COMPLETE_RGB.parse(context.getSender())) : emptyTab();
    }
}
