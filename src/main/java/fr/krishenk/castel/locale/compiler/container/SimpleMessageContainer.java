package fr.krishenk.castel.locale.compiler.container;

import fr.krishenk.castel.locale.compiler.MessageObject;
import fr.krishenk.castel.locale.provider.MessageBuilder;

public class SimpleMessageContainer implements MessageContainer {
    private final MessageObject variable;

    public SimpleMessageContainer(MessageObject variable) {
        this.variable = variable;
    }

    @Override
    public MessageObject get(MessageBuilder settings) {
        return this.variable;
    }
}