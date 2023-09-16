package fr.krishenk.castel.locale.provider;

public interface MessageContextProvider {
    void addMessageContextEdits(MessageBuilder builder);

    default MessageBuilder getMessageContext() {
        MessageBuilder builder = new MessageBuilder();
        this.addMessageContextEdits(builder);
        return builder;
    }
}
