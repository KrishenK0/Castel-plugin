package fr.krishenk.castel.locale.compiler.placeholders;

import fr.krishenk.castel.constants.group.Guild;
import fr.krishenk.castel.constants.player.CastelPlayer;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.intellij.lang.annotations.Pattern;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public abstract class CastelPlaceholder {
    @NotNull
    private final String name;
    @NotNull
    private final Object defaultObj;
    @Nullable
    private Object configuredDefaultValue;
    @NotNull
    public static final String NAME_PATTERN = "[a-z]";

    @NotNull
    public static final Map<String, CastelPlaceholder> NAMES = new HashMap(50);

    public CastelPlaceholder(@Pattern(value="[a-z]") @NotNull String name, @NotNull Object object) {
        this.name = name;
        this.defaultObj = object;
    }

    @NotNull
    public final String getName() {
        return this.name;
    }

    @NotNull
    public final Object getDefault() {
        return this.defaultObj;
    }

    @Nullable
    public final Object getConfiguredDefaultValue() {
        return this.configuredDefaultValue;
    }

    public final void setConfiguredDefaultValue(@Nullable Object object) {
        this.configuredDefaultValue = object;
    }

    @Nullable
    public abstract Object translate(@NotNull CastelPlaceholderTranslationContext var1);

    @NotNull
    public String toString() {
        return this.getClass().getSimpleName() + "{ " + this.name + " }";
    }

    public static void register(@NotNull CastelPlaceholder placeholder) {
        if (NAMES.containsKey(placeholder.getName())) {
            IllegalArgumentException illegalArgumentException = new IllegalArgumentException("Previously registered: " + placeholder.getName());
            throw new IllegalArgumentException(((Object)illegalArgumentException).toString());
        }
        NAMES.put(placeholder.getName(), placeholder);
    }


    @Nullable
    public static CastelPlaceholder getByName(@NotNull String name) {
        return NAMES.get(name);
    }


    public static void of(@Pattern(value="[a-z]") @NotNull String name, @NotNull Object object, @NotNull Function<? super CastelPlaceholderTranslationContext, ? extends Object> translator) {
        register(new CastelPlaceholder(name, object){
            @Nullable
            public Object translate(@NotNull CastelPlaceholderTranslationContext context) {
                return translator.apply(context);
            }
        });
    }


    @NotNull
    public Function<CastelPlaceholderTranslationContext, Object> ofPlayer(@NotNull Function<? super CastelPlayer, ? extends Object> translator) {
        return new Function<CastelPlaceholderTranslationContext, Object>(){
            @Nullable
            public Object apply(@NotNull CastelPlaceholderTranslationContext data) {
                Object object;
                CastelPlayer castelPlayer = data.getPlayer();
                if (castelPlayer != null) {
                    object = translator.apply(castelPlayer);
                } else {
                    object = null;
                }
                return object;
            }
        };
    }


    @NotNull
    public Function<CastelPlaceholderTranslationContext, Object> ofGuild(@Nullable Object object, @NotNull Function<? super Guild, ? extends Object> translator) {
        return new Function<CastelPlaceholderTranslationContext, Object>() {
            @Nullable
            public Object apply(@NotNull CastelPlaceholderTranslationContext data) {
                Object object;
                Guild guild = data.getGuild();
                if (guild != null) {
                    object = translator.apply(guild);
                } else {
                    object = null;
                }
                return object;
            }
        };
    }
}


