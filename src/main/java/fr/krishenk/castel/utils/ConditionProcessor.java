package fr.krishenk.castel.utils;

import com.google.common.base.Strings;
import fr.krishenk.castel.data.Pair;
import fr.krishenk.castel.locale.MessageObjectBuilder;
import fr.krishenk.castel.locale.compiler.PlaceholderTranslationContext;
import fr.krishenk.castel.locale.messenger.LanguageEntryMessenger;
import fr.krishenk.castel.locale.messenger.Messenger;
import fr.krishenk.castel.locale.provider.MessageBuilder;
import fr.krishenk.castel.utils.compilers.ConditionalCompiler;
import fr.krishenk.castel.utils.compilers.PlaceholderContextProvider;
import fr.krishenk.castel.utils.config.ConfigSection;
import fr.krishenk.castel.utils.string.StringUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.function.Function;

public class ConditionProcessor implements Function<String, Object> {
    private final PlaceholderContextProvider placeholderContextProvider;
    private final MessageBuilder messageBuilder;

    public ConditionProcessor(PlaceholderContextProvider placeholderContextProvider) {
        this.placeholderContextProvider = placeholderContextProvider;
        this.messageBuilder = placeholderContextProvider instanceof MessageBuilder ? (MessageBuilder)placeholderContextProvider : null;
    }

    public static boolean process(ConditionalCompiler.LogicalOperand condition, PlaceholderContextProvider placeholderContextProvider) {
        try {
            return (Boolean)condition.eval(new ConditionProcessor(placeholderContextProvider));
        }
        catch (Throwable ex) {
            throw new RuntimeException("Error while evaluating condition: " + condition, ex);
        }
    }

    public static Collection<Pair<ConditionalCompiler.LogicalOperand, Messenger>> translatableConditions(ConfigSection section) {
        if (section == null) {
            return null;
        }
        ArrayList<Pair<ConditionalCompiler.LogicalOperand, Messenger>> conditions = new ArrayList<Pair<ConditionalCompiler.LogicalOperand, Messenger>>();
        for (String condition : section.getKeys()) {
            String messagePath = section.getString(condition);
            conditions.add(Pair.of(ConditionalCompiler.compile(condition).evaluate(), Strings.isNullOrEmpty((String)messagePath) ? null : new LanguageEntryMessenger(StringUtils.splitArray(messagePath, '.'))));
        }
        return conditions;
    }

    @Override
    public Object apply(String x) {
        Object local = this.placeholderContextProvider.processPlaceholder(x);
        if (local == null) {
            return null;
        }
        if (local instanceof PlaceholderTranslationContext) {
            PlaceholderTranslationContext ctx = (PlaceholderTranslationContext)local;
            local = ctx.getValue();
        }
        if (this.messageBuilder != null) {
            if (local instanceof Messenger) {
                local = ((Messenger)local).getMessageObject(this.messageBuilder.getLanguage());
            }
            if (local instanceof MessageObjectBuilder) {
                local = ((MessageObjectBuilder)local).buildPlain(this.messageBuilder);
            }
        }
        return local;
    }
}

