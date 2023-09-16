package fr.krishenk.castel.managers.land.claiming;

import fr.krishenk.castel.constants.group.Guild;
import fr.krishenk.castel.constants.land.location.SimpleChunkLocation;
import fr.krishenk.castel.constants.player.CastelPlayer;
import fr.krishenk.castel.lang.Lang;
import fr.krishenk.castel.locale.provider.MessageBuilder;
import org.bukkit.command.CommandSender;

import java.util.Objects;

public abstract class AbstractClaimProcessor {
    protected final MessageBuilder contextHolder = new MessageBuilder();
    protected final SimpleChunkLocation chunk;
    protected Lang issue;
    protected final CastelPlayer cp;
    protected final Guild guild;
    protected boolean auto;
    protected long rp;
    protected double money;

    public AbstractClaimProcessor(SimpleChunkLocation chunk, CastelPlayer cp, Guild guild) {
        this.chunk = Objects.requireNonNull(chunk, "Cannot process null chunk");
        this.cp = Objects.requireNonNull(cp, "Player performer cannot be null");
        this.guild = Objects.requireNonNull(guild, "Guild cannot be null");
        this.var("x", chunk.getX());
        this.var("z", chunk.getZ());
    }

    public void asAuto() {
        this.auto = true;
    }

    public abstract AbstractClaimProcessor recompile();

    protected final void var(String variable, Object replacement) {
        this.contextHolder.raw(variable, replacement);
    }

    protected abstract Lang checkConstants();

    protected abstract Lang processIssue();

    public abstract AbstractClaimProcessor process();

    public void finalizeRequest() {
        if (!this.cp.isAdmin()) {
            if (this.money != 0.0) this.guild.addBank(this.money);
            if (this.rp != 0L) this.guild.addResourcePoints(this.rp);
        }
    }

    public boolean isSuccessful() {
        return this.issue == null;
    }

    public long getResourcePoints() {
        return rp;
    }

    public double getMoney() {
        return money;
    }

    public MessageBuilder getContextHolder() {
        return contextHolder;
    }

    public Lang getIssue() {
        return issue;
    }

    public void sendIssue(CommandSender sender) {
        this.issue.sendError(sender, this.contextHolder);
    }

    public Guild getGuild() {
        return guild;
    }

    public SimpleChunkLocation getChunk() {
        return chunk;
    }
}
