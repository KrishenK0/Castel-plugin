package fr.krishenk.castel.constants.group;

import fr.krishenk.castel.constants.group.model.logs.AuditLog;
import fr.krishenk.castel.constants.group.model.relationships.GuildRelation;
import fr.krishenk.castel.constants.group.model.relationships.GuildRelationshipRequest;
import fr.krishenk.castel.constants.group.model.relationships.RelationAttribute;
import fr.krishenk.castel.constants.group.upgradable.Powerup;
import fr.krishenk.castel.constants.mails.Mail;
import fr.krishenk.castel.constants.mails.MailRecipientType;
import fr.krishenk.castel.constants.metadata.CastelObject;
import fr.krishenk.castel.constants.player.CastelPlayer;
import fr.krishenk.castel.constants.player.Rank;
import fr.krishenk.castel.constants.player.RankMap;
import fr.krishenk.castel.data.Pair;
import fr.krishenk.castel.events.general.*;
import fr.krishenk.castel.lang.Config;
import fr.krishenk.castel.managers.ResourcePointManager;
import fr.krishenk.castel.managers.mails.DraftMail;
import fr.krishenk.castel.utils.internal.nonnull.NonNullMap;
import fr.krishenk.castel.utils.time.TimeUtils;
import org.apache.commons.lang.Validate;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.awt.*;
import java.time.Duration;
import java.util.List;
import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;

public abstract class Group extends CastelObject<UUID> {
    protected final transient UUID id;
    protected UUID leader;
    protected String name;
    protected String tag;
    protected long since;
    protected final Set<UUID> members;
    protected RankMap ranks;
    protected Location home;
    protected double publicHomeCost;
    protected boolean publicHome;
    private Color color;
    protected double bank;
    protected String tax;
    private String flag;
    protected Map<UUID, GuildRelationshipRequest> relationshipRequests;
    protected Map<UUID, GuildRelation> relations;
    protected Map<GuildRelation, Set<RelationAttribute>> attributes;
    private transient long lastLogsExpirationCheck;
    protected long resourcePoints;
    protected boolean requiresInvite;
    protected boolean permanent;
    protected boolean hidden;
    private final Set<UUID> mails;
    private final LinkedList<AuditLog> logs;


    public Group(UUID id, UUID leader, String name, String tag, long since, Set<UUID> members, RankMap ranks, long resourcePoints, Location home, boolean publicHome, Color color, double bank, String tax, String flag, Map<UUID, GuildRelationshipRequest> relationshipRequests, Map<UUID, GuildRelation> relations, Map<GuildRelation, Set<RelationAttribute>> attributes, boolean requiresInvite, Set<UUID> mails, boolean permanent, LinkedList<AuditLog> logs) {
        super(new HashMap<>());
        this.id = id;
        this.leader = leader;
        this.name = name;
        this.tag = tag;
        this.since = since;
        this.members = members;
        this.ranks = Objects.requireNonNull(ranks, "Group ranks cannot be null");
        this.resourcePoints = resourcePoints;
        this.home = home;
        this.publicHome = publicHome;
        this.color = color;
        this.bank = bank;
        this.tax = tax;
        this.flag = flag;
        this.relationshipRequests = Objects.requireNonNull(relationshipRequests, "Group relationship requests cannot be null");
        this.relations = Objects.requireNonNull(relations, "Group relations cannot be null");
        this.attributes = attributes;
        this.requiresInvite = requiresInvite;
        this.mails = Objects.requireNonNull(mails, "Mails cannot be null");
        this.permanent = permanent;
        this.logs = Objects.requireNonNull(logs,"logs cannot be null");
    }

    public Group(UUID leader, String name) {
        super(new NonNullMap<>());
        this.id = UUID.randomUUID();
        this.leader = Objects.requireNonNull(leader, "Group leader cannot be null");
        this.name = Objects.requireNonNull(name, "Group name cannot be null");
        this.since = System.currentTimeMillis();
        this.publicHome = false;
        this.requiresInvite = true;
        this.ranks = Rank.copyDefaults();
        this.attributes = GuildRelation.copyDefaults();
        this.members = new HashSet<>();
        this.relations = new HashMap<>();
        this.mails = new LinkedHashSet<>();
        this.relationshipRequests = new HashMap<>();
        this.logs = new LinkedList<>();
    }

    public double getPublicHomeCost() {
        return publicHomeCost;
    }

    public void setPublicHomeCost(double publicHomeCost) {
        this.publicHomeCost = publicHomeCost;
    }

    public abstract String getTaxOrDefault();

    public static Comparator<Group> getTopComparator() {
        return Comparator.comparingDouble(Group::getMight);
    }

    @Override
    public String getCompressedData() {
        return this.name +
                compressString(this.tag) +
                compressString(this.tax) +
                compressUUID(this.leader) +
                compressString(this.flag) +
                compressColor(this.color) +
                this.resourcePoints +
                this.since +
                this.bank +
                compressLocation(this.home) +
                compressBoolean(this.permanent) +
                compressBoolean(this.requiresInvite) +
                compressBoolean(this.hidden) +
                compressCollection(this.ranks.getRanks().values(), Rank::getCompressedData) +
                this.relations.entrySet().stream().map((entry) -> compressUUID(entry.getKey()) + entry.getValue().ordinal()).collect(Collectors.joining()) +
                compressCollection(this.relationshipRequests.values(), Object::hashCode) +
                compressMap(this.attributes, this.attributes.size(), Enum::ordinal, (x) -> compressCollection(x, Object::hashCode)) +
                compressCollection(this.members, CastelObject::compressUUID) + compressCollection(this.metadata.values(), Object::hashCode) +
//                compressCollection(this.mails, Object::hashCode) +
                this.compressMetadata();
    }

    @Override
    public UUID getDataKey() {
        return this.id;
    }

    public boolean isHidden() {
        return hidden;
    }

    public GroupHiddenStateChangeEvent setHidden(boolean hidden, CastelPlayer player) {
        GroupHiddenStateChangeEvent event = new GroupHiddenStateChangeEvent(this, hidden, player);
        if (this.hidden == hidden) {
            event.setCancelled(true);
            return event;
        }
        Bukkit.getPluginManager().callEvent(event);
        if (event.isCancelled()) this.hidden = hidden;
        return event;
    }

    public String getFlag() {
        return flag;
    }

    public GroupFlagChangeEvent setFlag(String flag, CastelPlayer player) {
        GroupFlagChangeEvent event = new GroupFlagChangeEvent(flag, player, this);
        if (Objects.equals(this.flag, flag)) {
            event.setCancelled(true);
            return event;
        }
        Bukkit.getPluginManager().callEvent(event);
        if (event.isCancelled()) return event;
        this.flag = event.getNewFlag();
        return event;
    }

    public Color getColor() {
        return color;
    }

    public GroupColorChangeEvent setColor(Color color, CastelPlayer player) {
        GroupColorChangeEvent event = new GroupColorChangeEvent(color, player, this);
        if (Objects.equals(this.color, color)) {
            event.setCancelled(true);
            return event;
        }
        Bukkit.getPluginManager().callEvent(event);
        if (event.isCancelled()) return event;
        this.color = event.getNewColor();
        return event;
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName() + '[' + this.id + '|' + this.name + ']';
    }

    public double getBank() {
        return bank;
    }

    public void setBank(double bank) {
        this.bank = bank;
    }

    public String getTax() {
        return tax;
    }

    public void setTax(String tax) {
        this.tax = tax;
    }

    public abstract double getMight();

    public boolean hasMoney(double amount) {
        return this.bank >= amount;
    }

    public UUID getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        Validate.notEmpty(name, "Group name cannot be null or empty");
        this.name = name;
    }

    public Location getHome() {
        return home;
    }

    public boolean hasAttribute(Group other, RelationAttribute attribute) {
        return attribute.hasAttribute(this, other);
    }

    public Map<UUID, GuildRelation> getRelations() {
        return relations;
    }

    public void setRelations(Map<UUID, GuildRelation> relations) {
        this.relations = relations;
    }

    public RankMap getRanks() {
        return ranks;
    }

    public void setRanks(RankMap ranks) {
        this.ranks = Objects.requireNonNull(ranks, "Ranks cannot be null");
    }

    public abstract GroupRenameEvent rename(String name, CastelPlayer player);

    public Map<UUID, GuildRelationshipRequest> getRelationshipRequests() {
        return relationshipRequests;
    }

    public void setRelationshipRequests(Map<UUID, GuildRelationshipRequest> relationshipRequests) {
        this.relationshipRequests = Objects.requireNonNull(relationshipRequests, "Group relationship requests cannot be null");
    }

    public UUID getLeaderId() {
        return leader;
    }

    public boolean isMember(UUID id) {
        return this.members.contains(id);
    }

    public Set<UUID> getMembers() {
        return members;
    }

    public Set<UUID> unsafeGetMembers() {
        return members;
    }

    public double addBank(double amount) {
        return this.bank += amount;
    }

    public int countRelationships(GuildRelation relation) {
        int count = 0;
        for (GuildRelation rel : this.relations.values()) {
            if (rel != relation) continue;
            ++count;
        }
        return count;
    }

    public void setRelationShipWith(Group other, GuildRelation relation) {
        Objects.requireNonNull(other);
        if (relation == GuildRelation.SELF)
            throw new IllegalArgumentException("SELF relationship is not allowed");
        if (relation == null || relation == GuildRelation.NEUTRAL) {
            this.relations.remove(other.id);
            other.relations.remove(id);
        } else {
            this.relations.put(other.id, relation);
            other.relations.put(id, relation);
        }
    }

    public boolean isPermanent() {
        return permanent;
    }

    public void setPermanent(boolean permanent) {
        this.permanent = permanent;
    }

    public boolean requiresInvite() {
        return requiresInvite;
    }

    public void setRequiresInvite(boolean requiresInvite) {
        this.requiresInvite = requiresInvite;
    }

    public long getSince() {
        return since;
    }

    public void setSince(long since) {
        this.since = since;
    }

    @Override
    public int hashCode() {
        return this.id.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Group)) return false;
        Group g = (Group) obj;
        return this.id.equals(g.id);
    }

    public Map<GuildRelation, Set<RelationAttribute>> getAttributes() {
        return attributes;
    }

    public void setAttributes(Map<GuildRelation, Set<RelationAttribute>> attributes) {
        this.attributes = attributes;
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public GroupRenameTagEvent renameTag(String tag, CastelPlayer player) {
        GroupRenameTagEvent event = new GroupRenameTagEvent(tag, player, this);
        if (Objects.equals(this.tag, tag)) {
            event.setCancelled(true);
            return event;
        }
        Bukkit.getPluginManager().callEvent(event);
        if (event.isCancelled()) return event;
        this.tag = event.getNewTag();
        return event;
    }

    public abstract GroupDisband triggerDisbandEvent(GroupDisband.Reason reason);

    public abstract GroupDisband disband(GroupDisband.Reason reason);

    public abstract double calculateTax();

    public abstract int getMaxMembers();

    public boolean isHomePublic() {
        return publicHome;
    }

    public void setPublicHome(boolean publicHome) {
        this.publicHome = publicHome;
    }

    public abstract List<OfflinePlayer> getPlayerMembers();

    public abstract List<Player> getOnlineMembers();

    public abstract List<CastelPlayer> getCastelPlayers();

    public boolean isFull() {
        return this.members.size() >= this.getMaxMembers();
    }

    public void addResourcePoints(long amount) {
        this.resourcePoints += amount;
    }

    public boolean hasResourcePoints(long amount) {
        return this.resourcePoints >= amount;
    }

    public long getResourcePoints() {
        return resourcePoints;
    }

    public void setResourcePoints(long resourcePoints) {
        this.resourcePoints = resourcePoints;
    }

    public Pair<Long, List<ItemStack>> addToResourcePoints(Collection<ItemStack> donations, BiFunction<ItemStack, List<ItemStack>, Long> function) {
        Pair<Long, List<ItemStack>> pair = ResourcePointManager.convertToResourcePoints(donations, function);
        this.resourcePoints += pair.getKey();
        return pair;
    }

    public Mail sendMail(Player sender, DraftMail draft) {
        return this.sendMail(draft.getSubject(), sender, draft.getRecipients(), draft.getMessage(), draft.getInReplyTo());
    }

    public Mail sendMail(String subject, Player sender, Map<Group, MailRecipientType> recipients, List<String> message, UUID replyTo) {
        MailSendEvent event = new MailSendEvent(this, sender, recipients, replyTo == null ? null : Mail.getMail(replyTo), message);
        Bukkit.getPluginManager().callEvent(event);
        if (event.isCancelled()) return null;
        recipients = event.getRecipients();
        message = event.getMessage();
        Map<UUID, MailRecipientType> uuidRecipients = new HashMap<>(recipients.size());
        recipients.forEach((recipient, mailx) -> uuidRecipients.put(recipient.id, mailx));
        Mail mail = new Mail(UUID.randomUUID(), this.id, sender.getUniqueId(), message, subject, replyTo, uuidRecipients);
        this.mails.add(mail.getId());
        for (Group group : recipients.keySet()) {
            group.mails.add(mail.getId());
        }
        return mail;
    }

    public void log(AuditLog log) {
//        if (Config.AUDIT_)
        this.logs.add(log);
    }

    public LinkedList<AuditLog> getLogs() {
        long currentTime = System.currentTimeMillis();
        if (currentTime - this.lastLogsExpirationCheck < Duration.ofHours(1L).toMillis()) {
            return this.logs;
        }
        final long defaultExpiration = Config.AUDIT_LOGS_EXPIRATION_DEFAULT.getTimeMillis();
        final ConfigurationSection specifics = Config.AUDIT_LOGS_EXPIRATION.getSection();
        for (Iterator<AuditLog> iterator = this.logs.iterator(); iterator.hasNext(); ) {
            AuditLog log = iterator.next();
            Long expirationTime = TimeUtils.parseTime(specifics.getString(log.getProvider().getNamespace().getConfigOptionName()));
            if (expirationTime == null) expirationTime = defaultExpiration;
            final long time = log.getTime();
            final long diff = currentTime - time;
            if (diff >= expirationTime) iterator.remove();
        }
        this.lastLogsExpirationCheck = currentTime;
        return this.logs;
    }

    public <C extends AuditLog, T> T getNewestLog(final Class<C> base, final Function<C, T> transformer) {
        final Iterator<AuditLog> descending = this.logs.descendingIterator();
        while (descending.hasNext()) {
            final AuditLog log = descending.next();
            if (!base.isInstance(log)) continue;
            final T result = transformer.apply((C) log);
            if (result != null) return result;
        }
        return null;
    }

    public List<Mail> getSentMails() {
        List<Mail> sent = new ArrayList<>(this.mails.size() / 2);
        for (Mail mail : this.getMails().values()) {
            if (mail.getFromGroup().equals(this.id)) sent.add(mail);
        }
        return sent;
    }

    public List<Mail> getRecievedMails() {
        List<Mail> recieved = new ArrayList<>(this.mails.size() / 2);
        for (Mail mail : this.getMails().values()) {
            if (!mail.getFromGroup().equals(this.id)) recieved.add(mail);
        }
        return recieved;
    }

    public Map<UUID, Mail> getMails() {
        Map<UUID, Mail> mappedMails = new LinkedHashMap<>(this.mails.size());
        Iterator<UUID> iterator = this.mails.iterator();
        while (iterator.hasNext()) {
            UUID mailId = iterator.next();
            Mail mail = Mail.getMail(mailId);
            if (mail == null) iterator.remove();
            else mappedMails.put(mailId, mail);
        }
        return Collections.unmodifiableMap(mappedMails);
    }
}
