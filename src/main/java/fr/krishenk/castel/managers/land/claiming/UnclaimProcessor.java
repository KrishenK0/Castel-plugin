package fr.krishenk.castel.managers.land.claiming;

import fr.krishenk.castel.constants.group.Guild;
import fr.krishenk.castel.constants.land.Land;
import fr.krishenk.castel.constants.land.location.SimpleChunkLocation;
import fr.krishenk.castel.constants.player.CastelPlayer;
import fr.krishenk.castel.constants.player.StandardGuildPermission;
import fr.krishenk.castel.data.Pair;
import fr.krishenk.castel.lang.Config;
import fr.krishenk.castel.lang.Lang;
import fr.krishenk.castel.locale.compiler.placeholders.PlaceholderContextBuilder;
import fr.krishenk.castel.services.ServiceHandler;
import fr.krishenk.castel.utils.MathUtils;
import fr.krishenk.castel.utils.cooldown.Cooldown;
import fr.krishenk.castel.utils.time.TimeFormatter;
import org.bukkit.Bukkit;

public class UnclaimProcessor extends AbstractClaimProcessor {
    private static final Cooldown<SimpleChunkLocation> COOLDOWN = new Cooldown<>();
    private final boolean confirmed;

    public UnclaimProcessor(SimpleChunkLocation chunk, CastelPlayer cp, Guild guild, boolean confirmed) {
        super(chunk, cp, guild);
        this.confirmed = confirmed;
    }

    @Override
    public AbstractClaimProcessor recompile() {
        return (new UnclaimProcessor(this.chunk, this.cp, this.guild, this.confirmed)).process();
    }

    @Override
    protected Lang checkConstants() {
        return null;
    }

    public UnclaimProcessor process() {
        this.issue = this.processIssue();
        return this;
    }

    @Override
    protected Lang processIssue() {
        boolean canUnclaimAny = this.cp.hasPermission(StandardGuildPermission.UNCLAIM);
        if (!canUnclaimAny && !this.cp.hasPermission(StandardGuildPermission.UNCLAIM_OWNED)) {
            return StandardGuildPermission.UNCLAIM_OWNED.getDeniedMessage();
        }
        Guild guild = this.cp.getGuild();
        long unclaimCd = COOLDOWN.getTimeLeft(this.chunk);
        if (unclaimCd > 0L) {
            this.var("time", TimeFormatter.of(unclaimCd));
            return Lang.COMMAND_UNCLAIM_COOLDOWN;
        }
        Land land = this.chunk.getLand();
        if (land != null && land.isClaimed()) {
            if (!guild.isClaimed(this.chunk)) return Lang.COMMAND_UNCLAIM_OCCUPIED_LAND;
            if (!this.cp.isAdmin()) {
                Pair<Long, Double> costs = calculateRefund(guild, 1, this.auto);
                if (costs != null) {
                    this.rp = costs.getKey();
                    this.money = costs.getValue();
                    this.var("rp", this.rp);
                    this.var("money", this.money);
                }
            }

            if (!canUnclaimAny && !this.cp.getUUID().equals(land.getClaimedBy())) {
                this.var("claimer", Bukkit.getOfflinePlayer(land.getClaimedBy()).getName());
                return Lang.PERMISSIONS_UNCLAIM_OWNED;
            }

            if (!this.confirmed) {
                if (Config.Claims.UNCLAIM_CONFIRMATION_HOME.getManager().getBoolean() && land.isHomeLand())
                    return Lang.COMMAND_UNCLAIM_CONFIRMATION_HOME;
            }

            return Land.disconnectsLandsAfterUnclaim(this.chunk, guild) ? Lang.COMMAND_UNCLAIM_DISCONNECTION : null;
        }
        return Lang.COMMAND_UNCLAIM_NOT_CLAIMED;
    }

    public static Pair<Long, Double> calculateRefund(Guild guild, int lands, boolean auto) {
        int starterPack = Config.Claims.STARTER_FREE.getManager().getInt();
        int currentLands = guild.getLandLocations().size();
        if (currentLands - lands <= starterPack) {
            int remainingLands = currentLands - lands;
            int starterPackLeft = starterPack - remainingLands;
            lands -= starterPackLeft;
            if (lands <= 0) return null;
        }

        PlaceholderContextBuilder ctx = (new PlaceholderContextBuilder()).withContext(guild);
        Config.Claims rpOpt = auto ? Config.Claims.RESOURCE_POINTS_REFUND_AUTO_UNCLAIM : Config.Claims.RESOURCE_POINTS_REFUND_UNCLAIM;
        long rp = (long) MathUtils.eval(rpOpt.getManager().getMathExpression(), ctx);
        double money = 0.0;
        if (ServiceHandler.bankServiceAvailable()) {
            Config.Claims moneyOpt = auto ? Config.Claims.MONEY_REFUND_AUTO_UNCLAIM : Config.Claims.MONEY_REFUND_UNCLAIM;
        }

        return Pair.of(rp * lands, money * lands);
    }

    public static Cooldown<SimpleChunkLocation> getUnclaimCooldown() {
        return COOLDOWN;
    }
}
