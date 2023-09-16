package fr.krishenk.castel.managers;

import com.google.common.base.Enums;
import com.google.common.base.Strings;
import fr.krishenk.castel.CLogger;
import fr.krishenk.castel.CastelPlugin;
import fr.krishenk.castel.constants.group.Guild;
import fr.krishenk.castel.constants.player.CastelPlayer;
import fr.krishenk.castel.data.Pair;
import fr.krishenk.castel.lang.Config;
import fr.krishenk.castel.locale.provider.MessageBuilder;
import fr.krishenk.castel.locale.provider.MessageContextProvider;
import fr.krishenk.castel.utils.ConditionProcessor;
import fr.krishenk.castel.utils.commands.ConfigCommand;
import fr.krishenk.castel.utils.compilers.ConditionalCompiler;
import fr.krishenk.castel.utils.debugging.CastelDebug;
import fr.krishenk.castel.utils.internal.JavaParser;
import fr.krishenk.castel.utils.internal.arrays.ArrayUtils;
import fr.krishenk.castel.utils.string.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventException;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.plugin.EventExecutor;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.stream.Collectors;

public class GeneralizedEventWatcher implements Listener {
    private static final Listener INSTANCE = new GeneralizedEventWatcher();

    private GeneralizedEventWatcher(){}

    private static boolean isClassOneOf(Class<?> first, Class<?>... others) {
        return Arrays.stream(others).anyMatch(x -> x == first);
    }

    public static void init() {
        ConfigurationSection events = Config.EVENTS.getManager().getSection();
        for (String event : events.getKeys(false)) {
            Class eventClass;
            try {
                Class<?> clazz = Class.forName(StringUtils.replace(event, "-", "."));
                if (!Event.class.isAssignableFrom(clazz)) {
                    CLogger.error("The specified class '" + event + "' is not an event.");
                    continue;
                }
                eventClass = clazz;
            } catch (ClassNotFoundException e) {
                CLogger.error("Unknown event: " + event);
                continue;
            }

            ConfigurationSection section = events.getConfigurationSection(event);
            String priorityName = section.getString("priority");
            boolean ignoreCancelled = !section.isSet("ignore-cancelled") || section.getBoolean("ignore-cancelled");
            EventPriority priority = Strings.isNullOrEmpty(priorityName) ? EventPriority.NORMAL : Enums.getIfPresent(EventPriority.class, priorityName.toUpperCase(Locale.ENGLISH)).or(EventPriority.NORMAL);

            List<Pair<ConditionalCompiler.LogicalOperand, List<ConfigCommand>>> commands = new ArrayList<>();
            ConfigurationSection cmdSection = section.getConfigurationSection("commands");
            if (cmdSection != null) {
                for (String condition : cmdSection.getKeys(false)) {
                    commands.add(Pair.of(ConditionalCompiler.compile(condition).evaluate(), ConfigCommand.parse(cmdSection.getStringList(condition))));
                }
            } else {
                commands.add(Pair.of(ConditionalCompiler.ConstantLogicalOperand.TRUE, ConfigCommand.parse(section.getStringList("commands"))));
            }

            List<Pair<String, MethodChain>> variables = new ArrayList<>();
            MethodChain primaryCtx = null;
            MethodChain secondaryCtx = null;
            ConfigurationSection contextSection = section.getConfigurationSection("context");
            if (contextSection != null) {
                ConfigurationSection vars = contextSection.getConfigurationSection("variables");
                if (vars != null) {
                    for (String varName : vars.getKeys(false)) {
                        variables.add(Pair.of(varName, JavaParser.parse(eventClass, vars.getString(varName))));
                    }
                    String primaryCtxStr = contextSection.getString("primary");
                    String secondaryCtxStr = contextSection.getString("secondary");
                    primaryCtx = primaryCtxStr == null ? null : JavaParser.parse(eventClass, primaryCtxStr);
                    secondaryCtx = secondaryCtxStr == null ? null : JavaParser.parse(eventClass, secondaryCtxStr);
                    if (primaryCtx != null && !isClassOneOf(primaryCtx.getLastReturnType(), Player.class, OfflinePlayer.class, Guild.class, CastelPlayer.class)) {
                        throw new IllegalArgumentException("The primary context method chain is wrong: " + primaryCtx);
                    }

                    if (secondaryCtx != null && !isClassOneOf(secondaryCtx.getLastReturnType(), Player.class, OfflinePlayer.class, Guild.class, CastelPlayer.class)) {
                        throw new IllegalArgumentException("The secondary context method chain is wrong: " + secondaryCtx);
                    }
                }
            }

            Object executor;
            if (MessageContextProvider.class.isAssignableFrom(eventClass)) {
                executor = new ExactEventExecutor(commands, variables, primaryCtx, secondaryCtx);
            } else {
                List<Method> nativePlayerMethods = new ArrayList<>();
                List<Method> castelPlayerMethods = new ArrayList<>();
                List<Method> guildMethods = new ArrayList<>();
//                    List<Method> nationMethods = new ArrayList<>();
                Method[] methods = eventClass.getMethods();

                for (int i = 0; i < methods.length; i++) {
                    Method method = methods[i];
                    if (method.getParameterCount() <= 0 && !Modifier.isStatic(method.getModifiers())) {
                        Class<?> returnType = method.getReturnType();
                        List<Method> addTo;
                        if (returnType.isAssignableFrom(Player.class)) {
                            addTo = nativePlayerMethods;
                        } else if (!returnType.isAssignableFrom(OfflinePlayer.class) && !returnType.isAssignableFrom(CastelPlayer.class)) {
                            if (returnType.isAssignableFrom(Guild.class)) addTo = guildMethods;
                            else {
//                                    if (!returnType.isAssignableFrom(Nation.class)) continue;
//                                    addTo = nationMethods;
                                continue;
                            }
                        } else addTo = castelPlayerMethods;

                        addTo.add(method);
                    }
                }
                CLogger.debug(CastelDebug.GENERALIZED$EVENT$WATCHER, "Watching event '" + eventClass.getName() + "' with methods:\n   native: " + nativePlayerMethods + '\n' + "    Castel Player: " + castelPlayerMethods + '\n' + "    Guild: " + guildMethods + '\n' /*+ "    Nation: " + nationMethods*/);
                executor = new ReflectiveEventExecutor(commands, variables, primaryCtx, secondaryCtx, nativePlayerMethods, castelPlayerMethods, guildMethods);
            }
            Bukkit.getPluginManager().registerEvent(eventClass, INSTANCE, priority, (EventExecutor) executor, CastelPlugin.getInstance(), ignoreCancelled);
        }
        CLogger.info("End Event Watcher");
    }
    
    public static Method findMethodWithName(Class<?> clazz, String name) {
        Method[] methods = clazz.getMethods();
        for (int i = 0; i < methods.length; i++) {
            Method method = methods[i];
            if (method.getName().equals(name)) return method;
        }
        return null;
    }

    public static class MethodChain {
        private final List<Method> methods;

        public MethodChain(List<Method> methods) {
            this.methods = methods;
        }

        public Class<?> getLastReturnType() {
            return ArrayUtils.getLast(this.methods).getReturnType();
        }

        @Override
        public String toString() {
            return this.methods.get(0).getDeclaringClass() + " -> " + this.methods.stream().map(Method::toString).collect(Collectors.joining(" -> "));
        }

        public Object call(Object instance) {
            for (Method method : this.methods) {
                try {
                    instance = method.invoke(instance);
                } catch (InvocationTargetException | IllegalAccessException e) {
                    throw new RuntimeException("Error while invoking method '" + method.getName() + "' in chain: " + this, e);
                }
            }
            return instance;
        }
    }

    private abstract static class GeneralEventExecutor implements EventExecutor {
        private final List<Pair<ConditionalCompiler.LogicalOperand, List<ConfigCommand>>> commands;
        private final List<Pair<String, MethodChain>> methodChains;
        private final MethodChain mainContext;
        private final MethodChain relationalSecondContext;

        public GeneralEventExecutor(List<Pair<ConditionalCompiler.LogicalOperand, List<ConfigCommand>>> commands, List<Pair<String, MethodChain>> methodChains, MethodChain mainContext, MethodChain relationalSecondContext) {
            this.commands = commands;
            this.methodChains = methodChains;
            this.mainContext = mainContext;
            this.relationalSecondContext = relationalSecondContext;
        }

        protected abstract MessageBuilder getMessageContext(Event event);

        @Override
        public void execute(@NotNull Listener listener, @NotNull Event event) throws EventException {
            try {
                MessageBuilder settings = this.getMessageContext(event);
                if (this.mainContext != null) settings.main = this.mainContext.call(event);

                if (this.relationalSecondContext != null) settings.relationalSecond = this.relationalSecondContext.call(event);

                Player player = settings.main instanceof Player ? (Player) settings.main : null;
                for (Pair<String, MethodChain> method : this.methodChains) {
                    settings.parse(method.getKey(), method.getValue().call(event));
                }

                for (Pair<ConditionalCompiler.LogicalOperand, List<ConfigCommand>> command : this.commands) {
                    if (ConditionProcessor.process(command.getKey(), settings)) {
                        ConfigCommand.execute(player, command.getValue(), settings, true);
                    }
                }
            } catch (Throwable e) {
                throw new EventException(e, "An error occured while handled generalized event: " + event.getClass().getName());
            }
        }
    }

    private static class ExactEventExecutor extends GeneralEventExecutor {
        public ExactEventExecutor(List<Pair<ConditionalCompiler.LogicalOperand, List<ConfigCommand>>> commands, List<Pair<String, MethodChain>> methodChains, MethodChain mainContext, MethodChain relationalSecondContext) {
            super(commands, methodChains, mainContext, relationalSecondContext);
        }

        @Override
        protected MessageBuilder getMessageContext(Event event) {
            return ((MessageContextProvider) event).getMessageContext();
        }
    }

    private static class ReflectiveEventExecutor extends GeneralEventExecutor {
        private final Method main;
        private final Method relationalSecond;

        public ReflectiveEventExecutor(List<Pair<ConditionalCompiler.LogicalOperand, List<ConfigCommand>>> commands, List<Pair<String, GeneralizedEventWatcher.MethodChain>> methodChains, GeneralizedEventWatcher.MethodChain mainContext, GeneralizedEventWatcher.MethodChain relationalSecondContext, List<Method> nativePlayerMethods, List<Method> castelPlayerMethods, List<Method> guildMethods/*, List<Method> nationMethods*/) {
            super(commands, methodChains, mainContext, relationalSecondContext);
            Method main = null;
            Method relationalSecond = null;
            List<Method> all = new ArrayList<>();
            all.addAll(nativePlayerMethods);
            all.addAll(castelPlayerMethods);
            all.addAll(guildMethods);

            Method meth;
            for (Iterator<Method> it = all.iterator(); it.hasNext(); main = meth) {
                meth = it.next();
                if (main != null) {
                    relationalSecond = meth;
                    break;
                }
            }
            this.main = main;
            this.relationalSecond = relationalSecond;
        }

        @Override
        protected MessageBuilder getMessageContext(Event event) {
            MessageBuilder settings = new MessageBuilder();

            try {
                settings.main = this.main == null ? null : this.main.invoke(event);
                settings.relationalSecond = this.relationalSecond == null ? null : this.relationalSecond.invoke(event);
            } catch (InvocationTargetException | IllegalAccessException e) {
                throw new RuntimeException(e);
            }

            Player player;
            if (settings.main instanceof Player) {
                player = (Player) settings.main;
            } else if (settings.main instanceof OfflinePlayer) {
                player = ((OfflinePlayer)settings.main).getPlayer();
                if (player == null) settings.withContext((OfflinePlayer) settings.main);
            } else if (settings.main instanceof CastelPlayer) {
                player = ((CastelPlayer) settings.main).getPlayer();
            } else player = null;

            if (player != null) settings.withContext(player);

            return settings;
        }
    }
}
