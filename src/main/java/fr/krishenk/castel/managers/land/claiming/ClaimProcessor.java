package fr.krishenk.castel.managers.land.claiming;

import fr.krishenk.castel.constants.group.Guild;
import fr.krishenk.castel.constants.land.Land;
import fr.krishenk.castel.constants.land.location.SimpleChunkLocation;
import fr.krishenk.castel.constants.player.CastelPlayer;
import fr.krishenk.castel.constants.player.StandardGuildPermission;
import fr.krishenk.castel.data.Pair;
import fr.krishenk.castel.lang.Config;
import fr.krishenk.castel.lang.Lang;
import fr.krishenk.castel.libs.xseries.XBiome;
import fr.krishenk.castel.locale.compiler.placeholders.PlaceholderContextBuilder;
import fr.krishenk.castel.services.ServiceHandler;
import fr.krishenk.castel.services.SoftService;
import fr.krishenk.castel.services.worldguard.ServiceWorldGuard;
import fr.krishenk.castel.utils.MathUtils;
import fr.krishenk.castel.utils.versionsupport.VersionSupport;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Biome;
import org.bukkit.configuration.ConfigurationSection;

import java.util.List;
import java.util.Locale;

public class ClaimProcessor extends AbstractClaimProcessor {
    public boolean dontCheckConnections;
    private boolean shouldOverclaim = false;

    public ClaimProcessor(SimpleChunkLocation chunk, CastelPlayer cp, Guild guild) {
        super(chunk, cp, guild);
    }

    public ClaimProcessor dontCheckConnections() {
        this.dontCheckConnections = true;
        return this;
    }

    @Override
    public AbstractClaimProcessor recompile() {
        ClaimProcessor processor = new ClaimProcessor(this.chunk, this.cp, this.guild);
        if (this.dontCheckConnections) processor.dontCheckConnections();
        return processor.process();
    }

    @Override
    public long getResourcePoints() {
        return -this.rp;
    }

    @Override
    public double getMoney() {
        return -this.money;
    }

    @Override
    public void finalizeRequest() {
        super.finalizeRequest();
        Long cd = Config.Claims.UNCLAIM_COOLDOWN.getManager().getTime(this.contextHolder);
        if (cd != null) UnclaimProcessor.getUnclaimCooldown().add(this.chunk, cd);
    }

    protected Lang handleMaxClaims() {
        int maxGuildClaim = this.guild.getMaxClaims(this.chunk.getWorld());
        if ((this.guild.getLandLocations().size() + 1) > maxGuildClaim) {
            this.var("limit", maxGuildClaim);
            return Lang.COMMAND_CLAIM_MAX_CLAIMS;
        }
        int maxClaims = this.cp.getRank().getMaxClaims();
        if (maxClaims >= 0 && this.cp.getClaims().size() + 1 > maxClaims) {
            this.var("limit", maxClaims);
            return maxClaims == 0 ? Lang.COMMAND_CLAIM_NO_CLAIMS_PLAYER : Lang.COMMAND_CLAIM_MAX_CLAIMS_PLAYER;
        }
        return null;
    }

    public boolean hasCosts() {
        return this.rp > 0L || this.money > 0.0;
    }

    public boolean shouldOverclaim() {
        return this.shouldOverclaim;
    }

    protected Lang checkProtectedRegion() {
        if (SoftService.WORLD_GUARD.isAvailable()) {
            int radius = Config.Claims.PROTECTED_REGION_RADIUS.getManager().getInt();
            if (radius <= 1) {
                if (ServiceHandler.isInRegion(this.chunk)) {
                    return Lang.COMMAND_CLAIM_IN_REGION;
                }
            } else {
                ServiceWorldGuard wg = (ServiceWorldGuard)SoftService.WORLD_GUARD.getService();
                World world = this.chunk.getBukkitWorld();
                int x = this.chunk.getX();
                int z = this.chunk.getZ();
                SimpleChunkLocation protectedRegion = this.chunk.findFromSurroundingChunks(radius, (current) -> wg.isChunkInRegion(world, x, z, radius) ? current : null);
                if (protectedRegion != null) {
                    return Lang.COMMAND_CLAIM_NEAR_REGION;
                }
            }

        }
        return null;
    }

    public static BiomeClaimResult processBiome(SimpleChunkLocation chunkLocation) {
        ConfigurationSection worldSettings = Config.Claims.BIOMES.getManager().withProperty(chunkLocation.getWorld()).getSection();
        if (worldSettings != null) {
            boolean whitelist = worldSettings.getBoolean("whitelist");
            List<String> biomes = worldSettings.getStringList("biomes");
            ConfigurationSection costFactorSection = worldSettings.getConfigurationSection("cost-factor");
            World world = chunkLocation.getBukkitWorld();
            Location center = chunkLocation.getCenterLocation();
            double lowestY = VersionSupport.getMinWorldHeight(world);
            double highestY = world.getMaxHeight();
            XBiome xBiome = null;

            long costFactor;
            for (costFactor = 0L; lowestY < highestY; lowestY += 50.0) {
                Location loc = center.clone();
                loc.setY(lowestY);
                Biome biome = loc.getBlock().getBiome();
                xBiome = XBiome.matchXBiome(biome);
                costFactor = costFactorSection.getLong(xBiome.name());
                boolean contains = biomes.contains(xBiome.name().toUpperCase(Locale.ENGLISH));
                if (contains != whitelist) {
                    return new BiomeClaimResult(xBiome, costFactor, true);
                }
            }

            return new BiomeClaimResult(xBiome, costFactor, false);
        }
        return new BiomeClaimResult(null, 0L, false);
    }

    public static Lang checkWorldAndPermission(String world, CastelPlayer cp) {
        if (Config.DISABLED_WORLDS.getStringList().contains(world)) {
            return Lang.DISABLED_WORLD;
        } else if (Config.Claims.DISABLED_WORLDS.getManager().getStringList().contains(world)) {
            return Lang.COMMAND_CLAIM_DISABLED_WORLD;
        } else {
            return !cp.hasPermission(StandardGuildPermission.CLAIM) ? StandardGuildPermission.CLAIM.getDeniedMessage() : null;
        }
    }

    protected Lang checkConstants() {
        return checkWorldAndPermission(this.chunk.getWorld(), this.cp);
    }

    public Lang processIssue() {
        Lang issue = this.checkConstants();
        if (issue != null) {
            return issue;
        } else {
            Land land = this.chunk.getLand();
            if (land != null && land.isClaimed()) {
                if (this.guild.isClaimed(this.chunk)) {
                    return Lang.COMMAND_CLAIM_ALREADY_OWNED;
                }

                if (!Config.Powers.POWER_ENABLED.getManager().getBoolean()) {
                    return Lang.COMMAND_CLAIM_OCCUPIED_LAND;
                }

                Guild other = land.getGuild();
                if (other != null && !other.canBeOverclaimed()) {
                    return Lang.COMMAND_CLAIM_CANT_OVERCLAIM;
                }

                this.shouldOverclaim = true;
            }

            if (!this.cp.isAdmin()) {
                Lang result = this.handleMaxClaims();
                if (result != null) {
                    return result;
                }

                result = this.checkProtectedRegion();
                if (result != null) {
                    return result;
                }

                BiomeClaimResult biome = processBiome(this.chunk);
                if (biome.isDisallowed()) {
                    this.var("biome", biome.getBiome().name().toLowerCase(Locale.ENGLISH));
                    return Lang.COMMAND_CLAIM_DISALLOWED_BIOME;
                }

                Pair<Long, Double> costs = calculateCosts(this.guild, 1, this.auto, biome.getCostFactor());
                if (costs != null) {
                    this.rp = costs.getKey();
                    this.money = costs.getValue();
                    this.var("rp", this.rp);
                    this.var("money", this.money);
                    this.var("biome_cost_factor", biome.getCostFactor());
                    if (!this.guild.hasResourcePoints(this.rp)) {
                        return Lang.COMMAND_CLAIM_NEED_RP;
                    }

                    if (!this.guild.hasMoney(this.money)) {
                        return Lang.COMMAND_CLAIM_NEED_MONEY;
                    }

                    this.rp = -this.rp;
                    this.money = -this.money;
                }

                if (!this.dontCheckConnections) {
                    if (!Land.isConnected(this.chunk, this.guild)) {
                        return Lang.COMMAND_CLAIM_NOT_CONNECTED;
                    }

                    if (Land.validateDistance(this.chunk, this.guild.getId()) != null) {
                        return Lang.COMMAND_CLAIM_NOT_DISTANCED;
                    }
                }

//                NationZone nationZone = Land.getNationZone(this.chunk);
//                if (nationZone != null) {
//                    Nation nation = nationZone.getNation();
//                    if (!nation.isMember(this.cp.getKingdomId())) {
//                        this.var("nation", nation.getName());
//                        return Lang.COMMAND_CLAIM_NATION_ZONE;
//                    }
//                }
            }

            return null;
        }
    }

    public ClaimProcessor process() {
        this.issue = this.processIssue();
        return this;
    }

    public static Pair<Long, Double> calculateCosts(Guild guild, int lands, boolean auto, long biomeFactor) {
        int starterPack = Config.Claims.STARTER_FREE.getManager().getInt();
        int currentLands = guild.getLandLocations().size();
        int freeLands = starterPack - (currentLands + lands);
        if (freeLands >= 0) {
            return null;
        } else {
            if (starterPack > currentLands) {
                lands -= starterPack - currentLands;
                if (lands <= 0) {
                    return null;
                }
            }

            PlaceholderContextBuilder ctx = (new PlaceholderContextBuilder()).withContext(guild);
            ctx.raw("biome_cost_factor", biomeFactor);
            Config.Claims rpOpt = auto ? Config.Claims.RESOURCE_POINTS_AUTO_CLAIMS : Config.Claims.RESOURCE_POINTS_CLAIMS;
            long rp = (long) MathUtils.eval(rpOpt.getManager().getMathExpression(), ctx);
            double money = 0.0;
            if (ServiceHandler.bankServiceAvailable()) {
                Config.Claims moneyOpt = auto ? Config.Claims.MONEY_AUTO_CLAIMS : Config.Claims.MONEY_CLAIMS;
                money = fr.krishenk.castel.utils.MathUtils.eval(moneyOpt.getManager().getMathExpression(), ctx);
            }

            return Pair.of(rp * (long)lands, money * (double)lands);
        }
    }
}
