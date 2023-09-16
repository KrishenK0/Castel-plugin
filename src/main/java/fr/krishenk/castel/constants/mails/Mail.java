package fr.krishenk.castel.constants.mails;

import fr.krishenk.castel.constants.group.Group;
import fr.krishenk.castel.constants.group.GroupResolver;
import fr.krishenk.castel.constants.metadata.CastelObject;
import fr.krishenk.castel.data.CastelDataCenter;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;

import java.util.*;

public class Mail extends CastelObject<UUID> {
    protected transient UUID id;
    private UUID fromGroup;
    private UUID sender;
    private List<String> message;
    private String subject;
    private UUID inReplyTo;
    protected Map<UUID, MailRecipientType> recipients;
    protected long time;

    public Mail(UUID id, UUID from, UUID sender, List<String> message, String subject, UUID inReplyTo, Map<UUID, MailRecipientType> recipients) {
        this.id = Objects.requireNonNull(id);
        this.recipients = Objects.requireNonNull(recipients, "Recipients cannot be null");
        this.time = System.currentTimeMillis();
        this.sender = sender;
        this.fromGroup = Objects.requireNonNull(from, "The sender UUID cannot be null");
        this.subject = Objects.requireNonNull(subject, "Subject cannot be null");
        this.message = Objects.requireNonNull(message, "Subject cannot be null");
        this.inReplyTo = inReplyTo;
        CastelDataCenter.get().getMTG().loadAndSave(id, this);
    }

    public static Mail getMail(UUID id) {
        Objects.requireNonNull(id, "Mail ID cannot be null");
        return CastelDataCenter.get().getMTG().getData(id);
    }

    public CachedMail toCached(GroupResolver resolver) {
        Map<Group, MailRecipientType> recipients = new HashMap<>(this.recipients.size());
        for (Map.Entry<UUID, MailRecipientType> recipient : this.recipients.entrySet()) {
            recipients.put(resolver.getGroup(recipient.getKey()), recipient.getValue());
        }

        return new CachedMail(this.id, resolver.getGroup(this.fromGroup), this.getPlayerSender(), this.message, this.time, this.subject, recipients, this.getRepliedMail());
    }

    public UUID getFromGroup() {
        return fromGroup;
    }

    public UUID getSender() {
        return sender;
    }

    public OfflinePlayer getPlayerSender() {
        return Bukkit.getOfflinePlayer(this.sender);
    }

    public Mail getRepliedMail() {
        return this.inReplyTo == null ? null : getMail(this.inReplyTo);
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Mail)) return false;
        if (this == obj) return true;
        return this.id.equals(((Mail) obj).id);
    }

    public UUID getInReplyTo() {
        return inReplyTo;
    }

    public String getSubject() {
        return subject;
    }

    public List<String> getMessage() {
        return Collections.unmodifiableList(message);
    }

    @Override
    public UUID getDataKey() {
        return null;
    }

    public long getTime() {
        return time;
    }

    public Map<UUID, MailRecipientType> getRecipients() {
        return Collections.unmodifiableMap(recipients);
    }

    public List<UUID> getRecipientsOfType(MailRecipientType type) {
        List<UUID> ids = new ArrayList<>(type == MailRecipientType.PRIMARY ? 1 : 5);
        for (Map.Entry<UUID, MailRecipientType> recipient : this.recipients.entrySet()) {
            if (recipient.getValue() == type) ids.add(recipient.getKey());
        }
        return ids;
    }

    public UUID getId() {
        return id;
    }

    @Override
    public String getCompressedData() {
        return "";
    }
}
