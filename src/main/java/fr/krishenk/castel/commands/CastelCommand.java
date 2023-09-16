package fr.krishenk.castel.commands;

import fr.krishenk.castel.CLogger;
import fr.krishenk.castel.CastelPlugin;
import fr.krishenk.castel.constants.player.CastelPlayer;
import fr.krishenk.castel.libs.xseries.XMaterial;
import fr.krishenk.castel.locale.CastelLang;
import fr.krishenk.castel.locale.MessageHandler;
import fr.krishenk.castel.locale.SupportedLanguage;
import fr.krishenk.castel.locale.compiler.MessageObject;
import fr.krishenk.castel.locale.messenger.DefaultedMessenger;
import fr.krishenk.castel.locale.messenger.LanguageEntryMessenger;
import fr.krishenk.castel.locale.messenger.Messenger;
import fr.krishenk.castel.locale.messenger.StaticMessenger;
import fr.krishenk.castel.locale.provider.MessageBuilder;
import fr.krishenk.castel.utils.PlayerUtils;
import fr.krishenk.castel.utils.config.ConfigPath;
import fr.krishenk.castel.utils.internal.arrays.ArrayUtils;
import fr.krishenk.castel.utils.internal.nonnull.NonNullMap;
import fr.krishenk.castel.utils.string.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionDefault;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public abstract class CastelCommand {
    protected static final CastelPlugin plugin = CastelPlugin.getInstance();
    protected static final boolean COLORIZE_TAB_COMPLETES = XMaterial.supports(13);
    protected final Permission permission;
    protected final Permission bypassCooldownPermission;
    protected final Permission bypassDisabledWorldPermission;
    protected final String name;
    protected final String[] path;
    protected final CastelParentCommand parent;
    protected final Messenger description;
    protected final Set<String> disabledWorlds;
    protected final Map<SupportedLanguage, List<String>> aliases;
    protected final long cooldown;
    private static final String PATTERN = "[a-zA-Z0-9]+";
    private static final Pattern COMMAND_NODE_PATTERN = Pattern.compile(PATTERN);

    public CastelCommand(String name, CastelParentCommand parent, PermissionDefault permissionDefault) {
        Objects.requireNonNull(name, "Command name cannot be null");
        if (!COMMAND_NODE_PATTERN.matcher(name).matches()) {
            throw new IllegalArgumentException("Command name doesn't match pattern: " + COMMAND_NODE_PATTERN.pattern());
        }
        this.name = name.toLowerCase(Locale.ENGLISH);
        this.parent = parent;
        String identifier = CastelCommand.calculatePermission(parent, this.name);
        this.path = ArrayUtils.merge(new String[]{"command"}, StringUtils.splitArray(identifier, '.'));
        this.description = this.lang("description");
        this.permission = new Permission("castel.command." + identifier, "castel command");//this.description.parse(new Object[0]), permissionDefault == null ? (parent == null ? PermissionDefault.OP : parent.getPermission().getDefault()) : permissionDefault)
        this.bypassDisabledWorldPermission = new Permission("castel.command." + identifier + ".bypass.disabled-worlds", "Permission to use this command even when it's disabled in a certain world.");
        this.bypassCooldownPermission = new Permission("castel.command" + identifier + ".bypass.cooldown", "Permission to bypass the command cooldown");
//        ConfigSection section = CastelConfig.COMMANDS.getSection().getSection().getSection(CastelCommand.calculateConfigEntry(identifier));
//        if (section != null) {
//            if (section.getBoolean("disabled")) {
//                this.cooldown = 0L;
//                this.disabledWorlds = new HashSet<>();
//                this.aliases = null;
//                return;
//            }
//            this.aliases = new NonNullMap<>();
//            String cd = section.getString("cooldown");
//            this.cooldown = Strings.isNullOrEmpty(cd) ? 0L : Optional.ofNullable(TimeUtils.parseTime(cd, TimeUnit.SECONDS)).orElseThrow(() -> new IllegalArgumentException("Invalid time format for command cooldown " + this.name + ": " + cd));
//            this.disabledWorlds = new HashSet<>(section.getStringList("disabled-worlds"));
//        } else {
//        }
            this.cooldown = 0L;
            this.aliases = new NonNullMap<>();
            this.disabledWorlds = new HashSet<>();
        if (this instanceof Listener)
            Bukkit.getPluginManager().registerEvents((Listener) this, CastelPlugin.getInstance());
        if (permissionDefault == null && parent != null)
            this.permission.setDefault(parent.getPermission().getDefault());
        Bukkit.getPluginManager().addPermission(this.permission);
        Bukkit.getPluginManager().addPermission(this.bypassCooldownPermission);
        Bukkit.getPluginManager().addPermission(this.bypassDisabledWorldPermission);
        Map<SupportedLanguage, Map<String, CastelCommand>> map = parent == null ? CastelCommandHandler.COMMANDS : parent.children;
        for (SupportedLanguage lang : SupportedLanguage.getInstalled()) {
            Map<String, CastelCommand> cmdMap = map.computeIfAbsent(lang, k -> new HashMap<>());
            try {
                MessageObject aliasesObj = this.lang("aliases").getMessageObject(lang);
                String mainName = this.getDisplayName().getMessageObject(lang).buildPlain(MessageBuilder.DEFAULT);
                cmdMap.put(mainName.toLowerCase(lang.getLocale()), this);
                if (lang == SupportedLanguage.EN)
                    cmdMap.put(name.toLowerCase(lang.getLocale()), this);
                if (aliasesObj != null) {
                    String[] aliases = StringUtils.splitArray(aliasesObj.buildPlain(MessageBuilder.DEFAULT), ' ');
                    this.aliases.put(lang, Arrays.asList(aliases));
                    for (String alias : aliases) {
                        CastelCommand previous = cmdMap.put(alias.toLowerCase(lang.getLocale()), this);
                        if (previous == null) continue;
                        CLogger.warn("The alias '" + alias + "' for command '" + identifier + "' has overridden command '" + previous.name + '\'');
                    }
                    continue;
                }
                this.aliases.put(lang, Collections.emptyList());
            } catch (IllegalStateException e) {}
        }
    }

    public Map<SupportedLanguage, List<String>> getAliases() {
        return Collections.unmodifiableMap(this.aliases);
    }

    public void setPermissionDefault(PermissionDefault permissionDefault) {
        this.permission.setDefault(permissionDefault);
    }

    public CastelCommand(String name) {
        this(name, false);
    }

    public CastelCommand(String name, boolean playerCmd) {
        this(name, null, (playerCmd ? PermissionDefault.TRUE : null));
    }

    public CastelCommand(String name, CastelParentCommand parent) {
        this(name, parent, null);
    }

    public void unregisterPermissions() {
        for (Permission perm : Arrays.asList(this.permission, this.bypassCooldownPermission, this.bypassDisabledWorldPermission)) {
            Bukkit.getPluginManager().removePermission(perm);
        }
    }

    private static String[] calculateConfigEntry(String identifier) {
        return ConfigPath.buildRaw(StringUtils.replace(identifier, '.', ".commands.").toString());
    }

    public static String joinArgs(String[] args) {
        return String.join(" ", args);
    }

    public static String joinArgs(String[] args, int from) {
        return Arrays.stream(args).skip(from).collect(Collectors.joining(" "));
    }

    public boolean canBypassDisabledWorlds(CommandSender sender) {
        return sender.hasPermission(this.bypassDisabledWorldPermission);
    }

    public boolean canBypassCooldown(CommandSender sender) {
        return sender.hasPermission(this.bypassCooldownPermission);
    }

    private static String processTabMessage(String str) {
        return COLORIZE_TAB_COMPLETES ? MessageHandler.colorize(str) : MessageHandler.stripColors(MessageHandler.colorize(str), true);
    }

    public static List<String> tabComplete(String completion) {
        return Collections.singletonList(CastelCommand.processTabMessage(completion));
    }

    public static List<String> tabComplete(String ... completions) {
        for (int i = 0; i < completions.length; ++i) {
            completions[i] = CastelCommand.processTabMessage(completions[i]);
        }
        return Arrays.asList(completions);
    }

    public Messenger getUsage() {
        return this.lang("usage");
    }

    public static List<String> tabComplete(Collection<String> completions) {
        ArrayList<String> tabs = new ArrayList<String>(completions.size());
        for (String str : completions) {
            tabs.add(CastelCommand.processTabMessage(str));
        }
        return tabs;
    }

    protected static OfflinePlayer getPlayer(CommandSender sender, String name) {
        OfflinePlayer player = PlayerUtils.getOfflinePlayer(name);
        if (player == null) {
            CastelLang.NOT_FOUND_PLAYER.sendError(sender);
        }
        return player;
    }

    public static List<String> emptyTab() {
        return Collections.emptyList();
    }

    public static Boolean parseBool(String str) {
        if (str == null) {
            return null;
        }
        if ((str = str.toLowerCase(Locale.ENGLISH)).equals("true") || str.equals("t") || str.equals("y")) {
            return true;
        }
        if (str.equals("false") || str.equals("f") || str.equals("n")) {
            return false;
        }
        return null;
    }

    public @NonNull Set<String> getDisabledWorlds() {
        return this.disabledWorlds;
    }

    public long getCooldown() {
        return this.cooldown;
    }

    private static String calculatePermission(CastelParentCommand parent, String name) {
        CastelParentCommand lastGroup = parent;
        StringBuilder perms = new StringBuilder(50);
        while (lastGroup != null) {
            perms.insert(0, lastGroup.name + '.');
            lastGroup = lastGroup.parent;
        }
        return perms.append(name).toString();
    }

    protected boolean isDisabled() {
        return this.aliases == null;
    }

    public @NonNull Permission getPermission() {
        return this.permission;
    }

    public boolean hasPermission(@NonNull CommandSender sender) {
        return sender.hasPermission(this.permission);
    }

    public boolean hasPermission(CommandSender sender, String perm) {
        if (sender.hasPermission(perm)) {
            return true;
        }
        return sender instanceof Player && CastelPlayer.getCastelPlayer((OfflinePlayer) sender).isAdmin();
    }

    public String toString() {
        CastelParentCommand lastGroupChk = this.parent;
        StringBuilder command = new StringBuilder(this.name);
        while (lastGroupChk != null) {
            command.insert(0, lastGroupChk.name + ' ');
            lastGroupChk = lastGroupChk.parent;
        }
        return command.toString();
    }

    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof CastelCommand)) {
            return false;
        }
        CastelCommand cmd = (CastelCommand)obj;
        return this.name.equals(cmd.name) && this.parent == cmd.parent;
    }

    public int hashCode() {
        int prime = 31;
        int result = 15;
        result = prime * result + this.name.hashCode();
        result = prime * result + (this.parent == null ? 0 : this.parent.hashCode());
        return result;
    }

    public Messenger lang(String ... path) {
        return new LanguageEntryMessenger(ArrayUtils.merge(this.path, path));
    }

    public void execute(CommandContext context) {
        throw new UnsupportedOperationException();
    }

    @NotNull
    public CommandResult executeX(@NonNull CommandContext context) {
        return CommandResult.NOT_IMPLEMENTED;
    }

    @Deprecated
    public List<String> tabComplete(@NonNull CommandSender sender, @NonNull String[] args) {
        return null;
    }

    public @NonNull List<String> tabComplete(@NonNull CommandTabContext context) {
        return new ArrayList<>();
    }

    public final @NonNull Messenger getDisplayName() {
        return new DefaultedMessenger(this.lang("name"), new StaticMessenger(this.name));
    }

    public final @NonNull String getName() {
        return this.name;
    }

    public final @Nullable CastelParentCommand getParent() {
        return this.parent;
    }

    public final @Nullable Messenger getDescription() {
        return this.description;
    }
}
