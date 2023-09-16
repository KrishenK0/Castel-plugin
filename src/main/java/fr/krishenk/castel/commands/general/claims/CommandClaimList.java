package fr.krishenk.castel.commands.general.claims;

import fr.krishenk.castel.CastelPluginPermission;
import fr.krishenk.castel.commands.CastelCommand;
import fr.krishenk.castel.commands.CastelParentCommand;
import fr.krishenk.castel.commands.CommandContext;
import fr.krishenk.castel.commands.CommandResult;
import fr.krishenk.castel.constants.group.Guild;
import fr.krishenk.castel.constants.land.location.SimpleChunkLocation;
import fr.krishenk.castel.lang.Lang;
import fr.krishenk.castel.locale.compiler.MessageCompiler;
import fr.krishenk.castel.locale.compiler.MessageCompilerSettings;
import fr.krishenk.castel.locale.compiler.MessageObject;
import fr.krishenk.castel.locale.compiler.MessagePiece;
import fr.krishenk.castel.locale.messenger.StaticMessenger;
import fr.krishenk.castel.locale.provider.MessageProvider;
import fr.krishenk.castel.utils.LocationUtils;
import fr.krishenk.castel.utils.string.tree.StringTree;
import fr.krishenk.castel.utils.string.tree.TreeBuilder;
import fr.krishenk.castel.utils.string.tree.TreeColorScheme;
import fr.krishenk.castel.utils.string.tree.TreeStyle;

import java.util.*;
import java.util.stream.Collectors;

public class CommandClaimList extends CastelCommand {
    public static final MessageCompilerSettings COMPILER_SETTINGS = MessageCompilerSettings.none().colorize().translatePlaceholders().hovers();

    public CommandClaimList(CastelParentCommand parent) {
        super("list", parent);
    }

    @Override
    public CommandResult executeX(CommandContext context) {
        if (context.assertPlayer() || context.assertHasGuild()) {
            return CommandResult.FAILED;
        }
            Guild guild = context.getGuild();
            Map<String, List<SimpleChunkLocation>> mappedLands = new HashMap<>();

            for (SimpleChunkLocation location : guild.getLandLocations()) {
                mappedLands.compute(location.getWorld(), (key, value) -> {
                    List<SimpleChunkLocation> list = value;
                    if (list == null) {
                        list = new ArrayList<>();
                    }
                    list.add(location);
                    return list;
                });
            }

            if (mappedLands.isEmpty()) {
                context.sendError(Lang.COMMAND_CLAIM_LIST_NO_CLAIMS);
                return CommandResult.FAILED;
            } else {
                Comparator<SimpleChunkLocation> comparator = Comparator.comparingInt(SimpleChunkLocation::getX)
                        .thenComparingInt(SimpleChunkLocation::getZ);
                boolean allowTp = context.hasPermission(CastelPluginPermission.TELEPORT_TO_CLAIMS);
                Lang description = allowTp ? Lang.COMMAND_CLAIM_LIST_TELEPORT_ADMIN_DESCRIPTION : Lang.COMMAND_CLAIM_LIST_TELEPORT_DESCRIPTION;
                Map<String, List<String>> destination = new LinkedHashMap<>();

                for (Map.Entry<String, List<SimpleChunkLocation>> entry : mappedLands.entrySet()) {
                    MessageObject messageObject = LocationUtils.translateWorld(entry.getKey()).getProvider(context.getSettings().getLanguage()).getMessage();
                    Objects.requireNonNull(messageObject);
                    List<String> sortedLocations = entry.getValue().stream()
                            .sorted(comparator)
                            .map(land -> land.getX() + "&7, {$p}" + land.getZ())
                            .collect(Collectors.toList());
                    destination.put(messageObject.buildPlain(context.getSettings()), sortedLocations);
                }

                List<StringBuilder> lines = (new TreeBuilder(destination)).parse(generateTreeStyle()).print().getLines();

                for (StringBuilder line : lines) {
                    context.sendMessage(new StaticMessenger(new MessageProvider(MessageCompiler.compile(line.toString(), COMPILER_SETTINGS))));
                }

                return CommandResult.SUCCESS;
            }

    }

    public TreeStyle generateTreeStyle() {
        EnumMap<TreeColorScheme, String> colorScheme = new EnumMap<>(TreeColorScheme.class);
        colorScheme.put(TreeColorScheme.INDICATORS, "{$sep}");
        colorScheme.put(TreeColorScheme.PATH_SEPARATORS, "{$sep}");
        colorScheme.put(TreeColorScheme.ENTRIES, "{$p}");
        Map<TreeColorScheme, String> colorSet = Collections.unmodifiableMap(colorScheme);
        TreeStyle treeStyle = new TreeStyle(StringTree.getUTF_CHARACTER_SET(), colorSet);
        treeStyle.setFlatten(false);
        treeStyle.setIndentation(1);
        treeStyle.setMaxColumns(4);
        treeStyle.setColumizeFromLevel(1);
        treeStyle.setColumnSpaceModifier(line -> {
            MessagePiece[] pieces = MessageCompiler.compile(line, CommandClaimList.COMPILER_SETTINGS).getPieces();
            int totalLength = 0;

            for (MessagePiece piece : pieces) {
                if (piece instanceof MessagePiece.Plain) {
                    totalLength += piece.length();
                } else if (piece instanceof MessagePiece.Hover) {
                    MessagePiece[] normalMessage = ((MessagePiece.Hover) piece).getNormalMessage();
                    int sum = Arrays.stream(normalMessage)
                            .filter(MessagePiece.Plain.class::isInstance)
                            .mapToInt(MessagePiece::length)
                            .sum();
                    totalLength += sum;
                }
            }

            return totalLength;
        });

        treeStyle.setEntryModifier((nestLevel, str) -> {
            String modifiedStr;
            if (nestLevel != null && nestLevel == 1) {
                modifiedStr = "{$p}" + str;
            } else if (nestLevel != null && nestLevel == 2) {
                modifiedStr = " " + str;
            } else {
                modifiedStr = str;
            }
            return modifiedStr;
        });

        return treeStyle;
    }
}
