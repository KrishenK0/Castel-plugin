package fr.krishenk.castel.constants.mails;

import fr.krishenk.castel.constants.group.Group;
import org.bukkit.OfflinePlayer;

import java.util.*;

public class CachedMail {
    private final UUID id;
    private final Group fromGroup;
    private final OfflinePlayer sender;
    private final List<String> message;
    private final long sent;
    private final String subject;
    private final Map<Group, MailRecipientType> recipients;
    private final Mail inReplyTo;

    public CachedMail(UUID id, Group fromGroup, OfflinePlayer sender, List<String> message, long sent, String subject, Map<Group, MailRecipientType> recipients, Mail inReplyTo) {
        this.id = id;
        this.fromGroup = fromGroup;
        this.sender = sender;
        this.message = message;
        this.sent = sent;
        this.subject = subject;
        this.recipients = recipients;
        this.inReplyTo = inReplyTo;
    }

    public UUID getId() {
        return id;
    }

    public Group getFromGroup() {
        return fromGroup;
    }

    public OfflinePlayer getSender() {
        return sender;
    }

    public List<String> getMessage() {
        return message;
    }

    public long getSent() {
        return sent;
    }

    public Map<Group, MailRecipientType> getRecipients() {
        return Collections.unmodifiableMap(recipients);
    }

    public Mail getInReplyTo() {
        return inReplyTo;
    }

    public List<Group> getRecipientsOfType(MailRecipientType type) {
        List<Group> ids = new ArrayList<>(5);
        for (Map.Entry<Group, MailRecipientType> recipient : this.recipients.entrySet()) {
            if (recipient.getValue() == type) ids.add(recipient.getKey());
        }
        return ids;
    }

    public String getSubject() {
        return subject;
    }
}
