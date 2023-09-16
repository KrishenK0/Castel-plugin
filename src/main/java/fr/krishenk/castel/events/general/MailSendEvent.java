package fr.krishenk.castel.events.general;

import fr.krishenk.castel.abstraction.GroupOperator;
import fr.krishenk.castel.abstraction.PlayerOperator;
import fr.krishenk.castel.constants.group.Group;
import fr.krishenk.castel.constants.mails.Mail;
import fr.krishenk.castel.constants.mails.MailRecipientType;
import fr.krishenk.castel.constants.player.CastelPlayer;
import fr.krishenk.castel.events.CastelEvent;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Map;

public class MailSendEvent extends CastelEvent implements GroupOperator, PlayerOperator, Cancellable {
    private static final HandlerList handlers = new HandlerList();
    private final Group fromGroup;
    private final Player sender;
    private Map<Group, MailRecipientType> recipients;
    private final Mail replyTo;
    private List<String> message;
    private boolean cancelled;

    public MailSendEvent(Group fromGroup, Player sender, Map<Group, MailRecipientType> recipients, Mail replyTo, List<String> message) {
        this.fromGroup = fromGroup;
        this.sender = sender;
        this.recipients = recipients;
        this.replyTo = replyTo;
        this.message = message;
    }

    public static HandlerList getHandlersList() {
        return handlers;
    }

    @NotNull
    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public Group getFromGroup() {
        return fromGroup;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }

    public List<String> getMessage() {
        return message;
    }

    public void setMessage(List<String> message) {
        this.message = message;
    }

    public Map<Group, MailRecipientType> getRecipients() {
        return recipients;
    }

    public void setRecipients(Map<Group, MailRecipientType> recipients) {
        this.recipients = recipients;
    }

    public Mail getReplyTo() {
        return replyTo;
    }

    public Player getSender() {
        return sender;
    }

    @Override
    public Group getGroup() {
        return fromGroup;
    }

    @Override
    public CastelPlayer getPlayer() {
        return CastelPlayer.getCastelPlayer(sender);
    }
}
