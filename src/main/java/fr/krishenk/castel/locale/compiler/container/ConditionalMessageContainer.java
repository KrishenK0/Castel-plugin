package fr.krishenk.castel.locale.compiler.container;

import fr.krishenk.castel.data.Pair;
import fr.krishenk.castel.locale.compiler.MessageObject;
import fr.krishenk.castel.locale.provider.MessageBuilder;
import fr.krishenk.castel.utils.ConditionProcessor;
import fr.krishenk.castel.utils.compilers.ConditionalCompiler;

import java.util.List;

public class ConditionalMessageContainer implements MessageContainer {
    private final List<Pair<ConditionalCompiler.LogicalOperand, MessageObject>> variables;

    public ConditionalMessageContainer(List<Pair<ConditionalCompiler.LogicalOperand, MessageObject>> variables) {
        this.variables = variables;
    }

    @Override
    public MessageObject get(MessageBuilder settings) {
        for (Pair<ConditionalCompiler.LogicalOperand, MessageObject> variable : this.variables) {
            if (!ConditionProcessor.process(variable.getKey(), settings)) continue;
            return variable.getValue();
        }
        return null;
    }
}
