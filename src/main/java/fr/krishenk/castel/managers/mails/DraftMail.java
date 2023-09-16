package fr.krishenk.castel.managers.mails;

import fr.krishenk.castel.constants.group.Group;
import fr.krishenk.castel.constants.group.Guild;
import fr.krishenk.castel.constants.mails.Mail;
import fr.krishenk.castel.constants.mails.MailRecipientType;
import fr.krishenk.castel.utils.internal.nonnull.NonNullMap;

import java.util.*;

public class DraftMail {
    private final Group fromGroup;
    private final List<String> message;
    private String subject;
    private final Map<UUID, MailRecipientType> recipients;
    private UUID inReplyTo;
    private final Class<? extends Group> communicationProtocol;

    public DraftMail(Group fromGroup, List<String> message, String subject, Map<UUID, MailRecipientType> recipients, UUID inReplyTo, Class<? extends Group> communicationProtocol) {
        this.fromGroup = fromGroup;
        this.message = message;
        this.subject = subject;
        this.recipients = recipients;
        this.inReplyTo = inReplyTo;
        this.communicationProtocol = communicationProtocol;
    }

    public DraftMail(Group fromGroup) {
        this(fromGroup, new ArrayList<>(), null, new HashMap<>(), null, fromGroup.getClass());
    }

    public Group getTo() {
        List<Group> primary = this.getRecipientsOfType(MailRecipientType.PRIMARY);
        return primary.isEmpty() ? null : primary.get(0);
    }

    public Group getFromGroup() {
        return fromGroup;
    }

    public Mail getRepliedMail() {
        return this.inReplyTo == null ? null : Mail.getMail(this.inReplyTo);
    }

    public Map<UUID, MailRecipientType> getRecipientsRaw() {
        return recipients;
    }

    public Map<Group, MailRecipientType> getRecipients() {
        if (this.recipients == null) return new HashMap<>();
        Map<Group, MailRecipientType> recipients = new NonNullMap<>(this.recipients.size());
        Iterator<Map.Entry<UUID, MailRecipientType>> iterator = this.recipients.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<UUID, MailRecipientType> recipient = iterator.next();
            UUID id = recipient.getKey();
            Group group = this.communicationProtocol == Guild.class ? Guild.getGuild(id) : null;
            if (group == null) iterator.remove();
            else recipients.put(group, recipient.getValue());
        }
        return recipients;
    }

    public Class<? extends Group> getCommunicationProtocol() {
        return communicationProtocol;
    }

    public List<Group> getRecipientsOfType(MailRecipientType type) {
        List<Group> ids = new ArrayList<>(type == MailRecipientType.PRIMARY ? 1 : 5);
        for (Map.Entry<Group, MailRecipientType> recipient : this.getRecipients().entrySet()) {
            if (recipient.getValue() == type) ids.add(recipient.getKey());
        }
        return ids;
    }

    public UUID getInReplyTo() {
        return inReplyTo;
    }

    public void setInReplyTo(UUID inReplyTo) {
        this.inReplyTo = inReplyTo;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public List<String> getMessage() {
        return message;
    }
}
