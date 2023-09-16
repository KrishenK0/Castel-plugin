package fr.krishenk.castel.locale.compiler.placeholders;

import fr.krishenk.castel.locale.compiler.MessageCompiler;
import fr.krishenk.castel.locale.compiler.MessageObject;
import fr.krishenk.castel.locale.compiler.PlaceholderTranslationContext;
import fr.krishenk.castel.locale.provider.MessageBuilder;
import fr.krishenk.castel.services.SoftService;
import fr.krishenk.castel.utils.compilers.PlaceholderContextProvider;
import me.clip.placeholderapi.PlaceholderAPIPlugin;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import me.clip.placeholderapi.expansion.Relational;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Locale;
import java.util.Map;

public interface PlaceholderType {
    @NotNull Companion Companion = new Companion();

     Object request(@NotNull PlaceholderContextBuilder var1);

    @NotNull String rebuild();
    
    abstract class ModifiablePlaceholder implements PlaceholderType {
        
        private final Placeholder.Modifier modifier;

        protected ModifiablePlaceholder( Placeholder.Modifier modifier) {
            this.modifier = modifier;
        }

        
        public final Placeholder.Modifier getModifier() {
            return this.modifier;
        }

        @NotNull
        protected final String modifyString(@NotNull String str) {
            String string;
            if (this.modifier == null) {
                string = '%' + str + '%';
            } else {
                StringBuilder stringBuilder = new StringBuilder().append('%');
                String string2 = this.modifier.name().toLowerCase(Locale.ROOT);
                string = stringBuilder.append(string2).append('_').append(str).append('%').toString();
            }
            return string;
        }

        
        protected final Object modify( Object translated) {
            return this.modifier == null || translated == null ? translated : Placeholder.modify(this.modifier, translated);
        }
    }

    
    final class Permission extends ModifiablePlaceholder {
        @NotNull
        private final String permission;

        public Permission(Placeholder.Modifier modifier, @NotNull String permission) {
            super(modifier);
            this.permission = permission;
        }

        @Override
        
        public Object request(@NotNull PlaceholderContextBuilder contextProvider) {
            if (!(contextProvider.main instanceof Player)) {
                return null;
            }
            Object object = contextProvider.main;
            return this.modify(((Player)object).hasPermission(this.permission));
        }

        @Override
        @NotNull
        public String rebuild() {
            return this.modifyString("perm_" + this.permission.replace('.', '_'));
        }
    }

    
    final class ExternalOrLocal
            implements PlaceholderType {
        private final boolean relational;
        @NotNull
        private final String identifier;
        @NotNull
        private final String parameter;

        public ExternalOrLocal(boolean relational, @NotNull String identifier, @NotNull String parameter) {
           
           
            this.relational = relational;
            this.identifier = identifier;
            this.parameter = parameter;
        }

        @Override
        
        public Object request(@NotNull PlaceholderContextBuilder contextProvider) {
            String string;
            Map<String, Object> map;

            Map<String, Object> map2 = map = contextProvider.placeholders;
            if (map2 != null) {

                Object object = map.get(this.identifier + '_' + this.parameter);
                if (object != null) {
                    return object;
                }
            }
            Map<String, PlaceholderContextProvider> groupedPlaceholders = contextProvider.groupedPlaceholders;
            if (groupedPlaceholders != null) {
                PlaceholderContextProvider context = groupedPlaceholders.get(this.identifier);
                if (context != null && context.processPlaceholder(this.parameter) != null) {
                    return context;
                }
            }
            if (!SoftService.PLACEHOLDERAPI.isAvailable()) {
                return null;
            }
            if (!(contextProvider.main instanceof OfflinePlayer)) {
                return null;
            }

            PlaceholderExpansion placeholderExpansion = PlaceholderAPIPlugin.getInstance().getLocalExpansionManager().getExpansion(this.identifier);
            if (placeholderExpansion == null) {
                return null;
            }

            if (contextProvider.canHandleRelational() && placeholderExpansion instanceof Relational && contextProvider.relationalSecond instanceof Player && contextProvider.main instanceof Player) {
                string = ((Relational) placeholderExpansion).onPlaceholderRequest((Player) contextProvider.main, (Player) contextProvider.relationalSecond, this.parameter);
            } else if (contextProvider.main instanceof OfflinePlayer) {
                string = placeholderExpansion.onRequest((OfflinePlayer) contextProvider.main, this.parameter);
            } else {
                return null;
            }
            String translated = string;
            return Companion.wrapWithDefaultContextProvider(translated);
        }

        @Override
        @NotNull
        public String rebuild() {
            char c = '%';
            String string = this.relational ? "rel_" : "";
            return c + string + this.identifier + '_' + this.parameter + '%';
        }

        @NotNull
        public String toString() {
            return "ExternalOrLocal{ " + (this.relational ? "Relational " : "") + this.identifier + ':' + this.parameter + " }";
        }
    }

    
    final class Local
            extends ModifiablePlaceholder {
        @NotNull
        private final String identifier;

        public Local(Placeholder.Modifier modifier, @NotNull String identifier) {
           
            super(modifier);
            this.identifier = identifier;
        }

        @Override
        
        public Object request(@NotNull PlaceholderContextBuilder contextProvider) {
            int index;
            Object it;
           
            Map<String, Object> map = contextProvider.placeholders;
            if (map != null && (it = map.get(this.identifier)) != null) {
                return this.modify(it);
            }
            if (contextProvider.groupedPlaceholders != null && (index = this.identifier.indexOf('_')) > 0) {
                String id = this.identifier.substring(0, index);
                String param = this.identifier.substring(index + 1);

                PlaceholderContextProvider handler = contextProvider.groupedPlaceholders.get(id);
                if (handler != null) {
                    return this.modify(handler.processPlaceholder(param));
                }
            }
            return null;
        }

        @NotNull
        public String toString() {
            return "Local{ " + (this.getModifier() == null ? "" : String.valueOf(this.getModifier()) + '_') + this.identifier + " }";
        }

        @Override
        @NotNull
        public String rebuild() {
            return this.modifyString(this.identifier);
        }
    }

    
    final class Macro
            implements PlaceholderType {
        @NotNull
        private final String name;

        public Macro(@NotNull String name) {
           
            this.name = name;
        }

        @Override
        
        public Object request(@NotNull PlaceholderContextBuilder contextProvider) {
           
            if (!(contextProvider instanceof MessageBuilder)) {
                return MessageCompiler.compile("&cUnsupported provided context: " + contextProvider);
            }
            MessageObject messageObject = ((MessageBuilder)contextProvider).getLanguage().getVariable((MessageBuilder)contextProvider, this.name);
            if (messageObject == null) {
                messageObject = MessageCompiler.compile("&8(&4Unknown variable &e'" + this.name + "'&8)");
            }
            return messageObject;
        }

        @Override
        @NotNull
        public String rebuild() {
            return "{$" + this.name + '}';
        }

        @NotNull
        public String toString() {
            return "Macro { " + this.name + " }";
        }
    }

    
    final class Internal
            implements PlaceholderType {
        private final boolean relational;
        @NotNull
        private final Placeholder placeholder;

        public Internal(boolean relational, @NotNull Placeholder placeholder) {
           
            this.placeholder = placeholder;
            this.relational = relational || placeholder.isPointerOther();
        }

        @Override
        
        public Object request(@NotNull PlaceholderContextBuilder contextProvider) {
           
            return Companion.wrapWithDefaultContextProvider(this.placeholder.request(contextProvider));
        }

        @NotNull
        public String toString() {
            return "Internal{ " + (this.relational ? "Relational " : "") + this.placeholder + " }";
        }

        @Override
        @NotNull
        public String rebuild() {
            StringBuilder str = new StringBuilder('%' + (this.relational ? "rel_" : "") + "guilds_");
            if (this.placeholder.modifier != null) {
                String string = this.placeholder.modifier.name().toLowerCase(Locale.ROOT);
               
                str.append(string).append('_');
            }
            String string = this.placeholder.identifier.getName().toLowerCase(Locale.ROOT);
           
            str.append(string);
            if (this.placeholder.fn != null) {
                if (this.placeholder.hasFormat()) {
                    str.append('@').append(this.placeholder.fn);
                } else {
                    str.append(':').append(this.placeholder.fn).append(' ');
                    Map<String, String> map = this.placeholder.parameters;
                   
                    for (Map.Entry<String, String> entry : map.entrySet()) {
                        String key = entry.getKey();
                        String value = entry.getValue();
                        str.append(key).append('=').append(value).append(", ");
                    }
                    Map<String, String> map2 = this.placeholder.parameters;
                   
                    if (!map2.isEmpty()) {
                        str.setLength(str.length() - 2);
                    }
                }
            }
            return String.valueOf(str) + '%';
        }
    }

    
    final class Companion {
        private Companion() {
        }

        
        public Object wrapWithDefaultContextProvider( Object placeholder) {
            return !(placeholder instanceof String) ? placeholder : PlaceholderTranslationContext.withDefaultContext(placeholder);
        }
    }
}

