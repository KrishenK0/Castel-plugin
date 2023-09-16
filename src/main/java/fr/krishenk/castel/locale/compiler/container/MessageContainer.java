package fr.krishenk.castel.locale.compiler.container;

import fr.krishenk.castel.CLogger;
import fr.krishenk.castel.config.ConfigAccessor;
import fr.krishenk.castel.config.KeyedConfigAccessor;
import fr.krishenk.castel.config.NewKeyedConfigAccessor;
import fr.krishenk.castel.data.Pair;
import fr.krishenk.castel.locale.compiler.MessageCompiler;
import fr.krishenk.castel.locale.compiler.MessageObject;
import fr.krishenk.castel.locale.provider.MessageBuilder;
import fr.krishenk.castel.utils.config.ConfigSection;
import org.bukkit.configuration.ConfigurationSection;

import java.util.ArrayList;
import java.util.List;

import static fr.krishenk.castel.utils.compilers.ConditionalCompiler.LogicalOperand;
import static fr.krishenk.castel.utils.compilers.ConditionalCompiler.compile;

public interface MessageContainer {
    MessageObject get(MessageBuilder var1);

    static List<Pair<LogicalOperand, String>> parseRaw(KeyedConfigAccessor accessor) {
        ArrayList<Pair<LogicalOperand, String>> list2 = new ArrayList<Pair<LogicalOperand, String>>();
        if (!accessor.isSet()) {
            return list2;
        }
        ConfigAccessor variableSection = accessor.getSection();
        if (variableSection != null) {
            ConfigSection varSection = variableSection.getSection();
            for (String conds : variableSection.getKeys()) {
                list2.add(Pair.of(compile(conds).evaluate(), varSection.getString(conds)));
            }
        } else {
            list2.add(Pair.of(null, accessor.getString()));
        }
        return list2;
    }

    static MessageContainer parse(NewKeyedConfigAccessor accessor) {
        MessageContainer container;
        ConfigurationSection variableSection = accessor.getSection();

        if (variableSection != null) {
//            ConfigurationSection varSection = variableSection.getDefaultSection();
            ArrayList<Pair<LogicalOperand, MessageObject>> messages = new ArrayList<>(2);
            container = new ConditionalMessageContainer(messages);

            for (String conds : variableSection.getKeys(false)) {
                LogicalOperand condition = compile(conds).evaluate();
                MessageObject msg = MessageCompiler.compile(variableSection.getString(conds), MessageCompiler.defaultSettingsWithErroHandler(exes -> {
//                    Mark mark = varSection.getNode(conds).getStartMark();
                    CLogger.warn("While parsing '" + accessor.getDynamicOption() + "' macro, line " + /*mark.getLine() +*/ ":\n" + exes.joinExceptions());
                }));
                messages.add(Pair.of(condition, msg));
            }
        } else {
            container = new SimpleMessageContainer(MessageCompiler.compile(accessor.getString(), MessageCompiler.defaultSettingsWithErroHandler(exes -> {
//                Mark mark = accessor.getNode().getStartMark();
                CLogger.warn("While parsing '" + accessor.getDynamicOption() + "' macro, line " + /*mark.getLine() +*/ ":\n" + exes.joinExceptions());
            })));
        }
        return container;
    }
}


