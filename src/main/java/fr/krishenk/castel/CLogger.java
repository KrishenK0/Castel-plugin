package fr.krishenk.castel;

import fr.krishenk.castel.lang.Config;
import fr.krishenk.castel.locale.MessageHandler;
import fr.krishenk.castel.utils.cooldown.Cooldown;
import fr.krishenk.castel.utils.debugging.DebugNS;
import fr.krishenk.castel.utils.debugging.DebugSettings;
import fr.krishenk.castel.utils.debugging.StacktraceSettings;
import fr.krishenk.castel.utils.string.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;

import java.time.Duration;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;
import java.util.logging.Logger;

public class CLogger {
    private static final String DEBUG = "&4[&5DEBUG&7] &6";
    private static final Logger LOGGER = CastelPlugin.getInstance().getLogger();
    private static final Set<String> CONDITIONALS = new HashSet<>();
    private static final AtomicInteger ID_GEN = new AtomicInteger();
    private static final Cooldown<String> COOLDOWN = new Cooldown<>();
    private boolean ignore;
    private int id;
    private final DebugNS ns;

    public CLogger(DebugNS ns) {
        this(ns, true, null, null);
    }

    public CLogger(DebugNS ns, boolean printHeader, Object cooldownObject, Duration cooldown) {
        this.ns = ns;
        if (!isDebugging()) return;
        if (cooldownObject != null) {
            if (CLogger.COOLDOWN.isInCooldown(cooldown.toString())) {
                this.ignore = true;
                return;
            }
            CLogger.COOLDOWN.add(cooldownObject.toString(), cooldown);
        }
        this.id = CLogger.ID_GEN.getAndIncrement();
        if (printHeader) {
            this.log("Debug Info&8:");
            this.log("   &7- &2Platform&8: &9" + Bukkit.getVersion() + " &8- &9" + Bukkit.getBukkitVersion());
            this.log("   &7- &2Plugin Version&8: &9" + CastelPlugin.getInstance().getDescription().getVersion());
            final StackTraceElement stack = this.getFirstStackTrace();
            this.log("   &7- &2Source&8: &9" + stack.getClassName() + "&8.&9" + stack.getMethodName() + "&8: &5" + stack.getLineNumber());
        }
    }

    public static void info(Object str) {
        CLogger.LOGGER.info(ChatColor.translateAlternateColorCodes('&', (str == null ? "null" : str.toString())));
    }

    @Deprecated
    public static void temp(Object str) {
        info(str);
    }

    public static boolean calledFrom(Class<?> clazz) {
        for (StackTraceElement stack : Thread.currentThread().getStackTrace()) {
            if (stack.getClassName().equals(clazz.getName())) return true;
        }
        return false;
    }

    public static void cond(String condition, String msg) {
        if (CLogger.CONDITIONALS.contains(condition))
            info('[' + condition + "] " + msg);
    }

    public static boolean isCond(String cond) {
        return CLogger.CONDITIONALS.contains(cond);
    }

    public static void debug(DebugNS ns, Supplier<Object> obj) {
        if (isDebugging()) {
            String nsStr = ns == null ? "" : "&8[&5" + ns.namespace() + "&8] &6";
            String str = obj.get().toString();
            String msg = ChatColor.translateAlternateColorCodes('&', "&7[&5DEBUG&7] &6" + nsStr + str);
            MessageHandler.sendConsolePluginMessage(msg);
            Bukkit.getOnlinePlayers().stream().filter(x -> CastelPluginPermission.DEBUG.hasPermission(x, true)).filter(x -> ns == null || DebugSettings.getSettings(x).isWhitelist() == DebugSettings.getSettings(x).getList().contains(ns)).forEach(x -> MessageHandler.sendPlayerPluginMessage(x, msg));
            if (ns != null && StacktraceSettings.isWhitelist == StacktraceSettings.list.contains(ns)) {
                StringUtils.printStackTrace();
            }
        }
    }

    public static boolean isDebugging() {
        return Config.DEBUG.getBoolean();
    }

    public static void debug(DebugNS ns, Object str) {
        debug(ns, () -> str);
    }

    public static void cond(String cond, Object msg) {
        if (isCond(cond)) info(msg);
    }

    public static void end(String cond) {
        CLogger.CONDITIONALS.remove(cond);
    }

    public static void warn(Object str) {
        CLogger.LOGGER.warning(str.toString());
    }

    public static void error(Object str) {
        CLogger.LOGGER.severe(str.toString());
    }

    public CLogger property(Object name, boolean bool) {
        this.property(name, (bool ? "&2" : "&c") + bool);
        return this;
    }

    public CLogger property(Object name, Object value) {
        this.log(name + "&8: &2" + value);
        return this;
    }

    private StackTraceElement getFirstStackTrace() {
        return Arrays.stream(Thread.currentThread().getStackTrace()).skip(2L).filter(x -> !x.getClassName().startsWith(this.getClass().getName())).findFirst().orElseThrow(() -> new IllegalStateException("Cannot obtain first stack trace"));
    }

    public void end() {
        this.log("---------------------------------------------------------");
    }

    public void log(Object str) {
        this.log(this.ns, str);
    }

    public void log(DebugNS ns, Object str) {
        this.log(ns, () -> str);
    }

    public void log(final Supplier<Object> str) {
        this.log(this.ns, str);
    }

    public void log(final DebugNS ns, final Supplier<Object> str) {
        if (this.ignore) {
            return;
        }
        debug((DebugNS)null, () -> "&8[&5" + ns.namespace() + "&8][&7" + this.id + "&8] &6" + str.get());
    }
}
