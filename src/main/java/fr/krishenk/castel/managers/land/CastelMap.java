package fr.krishenk.castel.managers.land;

import com.google.common.base.Strings;
import fr.krishenk.castel.CastelPlugin;
import fr.krishenk.castel.CastelPluginPermission;
import fr.krishenk.castel.constants.group.Guild;
import fr.krishenk.castel.constants.group.model.relationships.GuildRelation;
import fr.krishenk.castel.constants.land.Land;
import fr.krishenk.castel.constants.land.location.SimpleChunkLocation;
import fr.krishenk.castel.constants.player.CastelPlayer;
import fr.krishenk.castel.lang.Config;
import fr.krishenk.castel.locale.compiler.MessageCompiler;
import fr.krishenk.castel.locale.provider.MessageBuilder;
import fr.krishenk.castel.managers.land.claiming.AbstractClaimProcessor;
import fr.krishenk.castel.managers.land.claiming.ClaimClipboard;
import fr.krishenk.castel.services.ServiceHandler;
import fr.krishenk.castel.utils.Compass;
import fr.krishenk.castel.utils.XScoreboard;
import fr.krishenk.castel.utils.config.ConfigPath;
import fr.krishenk.castel.utils.internal.nonnull.NonNullMap;
import fr.krishenk.castel.utils.string.StringUtils;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class CastelMap {
    public static final Map<UUID, XScoreboard> SCOREBOARDS = NonNullMap.of(new ConcurrentHashMap<>());
    private Player player;
    private CastelPlayer cp;
    private Guild cpGuild;
    private int height;
    private int width;
    private boolean clipboardMode;
    private ClaimClipboard clipboard;
    private final String beginMain;
    private final String distanceMain;
    private SimpleChunkLocation center;
    protected SimpleChunkLocation chunk;
    protected Land land;
    protected Guild guild;
    private boolean showHidden;
    private final MessageBuilder settings;
    private String failReason;

    public CastelMap() {
        this.beginMain = Config.Map.BEGIN.getManager().getString();
        this.distanceMain = Config.Map.DISTANCE.getManager().getString();
        this.settings = new MessageBuilder();
        this.failReason = "&cNone";
    }

    public CastelMap setSize(int height, int width) {
        this.height = height;
        this.width = width;
        return this;
    }

    public CastelMap clipboardMode() {
        this.clipboardMode = true;
        this.clipboard = ClaimClipboard.getClipboard().get(this.player.getUniqueId());
        this.clipboardMode = this.clipboard != null;
        return this;
    }

    public CastelMap forPlayer(Player player) {
        this.player = Objects.requireNonNull(player, "Cannot show map to null player");
        this.cp = CastelPlayer.getCastelPlayer(player);
        this.cpGuild = this.cp.getGuild();
        this.center = SimpleChunkLocation.of(player.getLocation());
        this.showHidden = CastelPluginPermission.SHOW_HIDDEN_GROUPS.hasPermission(player);
        this.settings.withContext(player);
        return this;
    }

    void currentChunk(SimpleChunkLocation chunk) {
        this.chunk = chunk;
        this.land = chunk.getLand();
        this.guild = this.land != null && this.land.isClaimed() ? this.land.getGuild() : null;
    }

    String getCurrentChunkOptionName() {
        if (this.chunk.equalsIgnoreWorld(this.center)) {
            return "you";
        } else if (ServiceHandler.isInRegion(this.chunk)) {
            return "protected";
        } else {
            GuildRelation relation;
            if (this.guild != null && (this.showHidden || !this.guild.isHidden() || this.guild.isMember(this.cp))) {

                    String property = "land";

                    relation = this.cpGuild == null ? GuildRelation.NEUTRAL : this.guild.getRelationWith(this.cpGuild);
                    return property + '.' + relation.name().toLowerCase(Locale.ENGLISH);

            } else {
//                NationZone nationZone = org.kingdoms.constants.land.Land.getNationZone(this.chunk);
//                if (nationZone == null || this.showHidden && nationZone.getNation().isHidden() && nationZone.getNation().isMember(this.cp)) {
                    if (this.clipboardMode) {
                        AbstractClaimProcessor result = this.clipboard.getClaims().get(this.chunk.worldlessWrapper());
                        if (result != null) {
                            if (!result.isSuccessful()) {
                                this.failReason = result.getIssue().getProvider(this.cp.getLanguage()).getMessage().buildPlain(result.getContextHolder());
                            }

                            return "clipboard." + (result.isSuccessful() ? "added" : "failed");
                        } else {
                            return "clipboard.wilderness";
                        }
                    } else {
                        return "wilderness";
                    }
//                } else {
//                    relation = this.cpGuild == null ? KingdomRelation.NEUTRAL : nationZone.getKingdom().getRelationWith(this.cpGuild);
//                    return "nation-zone." + relation.name().toLowerCase(Locale.ENGLISH);
//                }
            }
        }
    }
    
    public void displayAsync() {
        Bukkit.getScheduler().runTaskAsynchronously(CastelPlugin.getInstance(), this::display);
    }
    
    public void display() {
        this.displayHeader();
        this.process();
        this.displayFooter();
    }
    
    public static boolean isUsingScoreboard(Player player) {
        return SCOREBOARDS.containsKey(player.getUniqueId());
    }

    public void displayAsScoreboard() {
        XScoreboard scoreboard = SCOREBOARDS.get(this.player.getUniqueId());
        if (scoreboard == null) {
            scoreboard = new XScoreboard("lands", MessageCompiler.compile(Config.Map.HEADER.getManager().getString()), this.settings);
            scoreboard.setAlignRight(this.beginMain);
        } else {
            scoreboard.clearLines();
        }

        this.settings.raw("compass", Compass.translateCardinalDirection(this.player));
        int height = Math.min(this.height, 7);

        for(int y = -height; y <= height; ++y) {
            StringBuilder row = new StringBuilder();

            for(int x = -this.width; x <= this.width; ++x) {
                this.currentChunk(this.center.getRelative(x, y));
                String property = this.getCurrentChunkOptionName();
                ConfigurationSection section = Config.MAP.getConfigurationSection(StringUtils.join(ConfigPath.buildRaw("elements." + property), "."));
                if (section == null) {
                    throw new IllegalStateException("Cannot find map section for property: " + property);
                }

                this.settings.raw("reason", this.failReason).raw("X", this.chunk.getX()).raw("Z", this.chunk.getZ()).other(this.guild);
                String icon = section.getString("icon");
                if (icon == null) {
                    throw new IllegalStateException("Cannot find map icon section for property: " + property);
                }

                row.append(icon);
            }

            scoreboard.addLine(MessageCompiler.compile(row.toString()));
        }

        scoreboard.buildLines(this.settings);
        scoreboard.setForPlayer(this.player);
        SCOREBOARDS.put(this.player.getUniqueId(), scoreboard);
    }
    
    void displayHeader() {
        MessageCompiler.compile(Config.Map.HEADER.getManager().getString()).getSimpleProvider().send(this.player, this.settings);
    }
    
    void displayFooter() {
        MessageCompiler.compile(Config.Map.FOOTER.getManager().getString()).getSimpleProvider().send(this.player, this.settings);
    }
    
    public void process() {
        List<TextComponent> rows = new ArrayList();

        for(int y = -this.height; y <= this.height; ++y) {
            TextComponent rowComponent = new TextComponent();
            rowComponent.addExtra(this.beginMain);
            boolean first = true;

            for(int x = -this.width; x <= this.width; ++x) {
                this.currentChunk(this.center.getRelative(x, y));
                String property = this.getCurrentChunkOptionName();
                ConfigurationSection section = Config.MAP.getConfigurationSection(StringUtils.join(ConfigPath.buildRaw("elements." + property), "."));
                if (section == null) {
                    throw new IllegalStateException("Cannot find map section for property: " + property);
                }

                this.settings.raw("compass", Compass.translateCardinalDirection(this.player)).raw("reason", this.failReason).raw("X", this.chunk.getX()).raw("Z", this.chunk.getZ()).other(this.guild);
                TextComponent element = new TextComponent();
                TextComponent hover = new TextComponent();
                List<String> hovers = section.getStringList("hover");
                String hoverMsg;
                if (!hovers.isEmpty()) {
                    int i = 1;

                    for(Iterator<String> it = hovers.iterator(); it.hasNext(); hover.addExtra(hoverMsg)) {
                        hoverMsg = MessageCompiler.compile(it.next()).buildPlain(this.settings);
                        if (i++ != hovers.size()) {
                            hoverMsg = hoverMsg + '\n';
                        }
                    }

                    HoverEvent hoverEvent = MessageCompiler.constructHoverEvent(new TextComponent[]{hover});
                    element.setHoverEvent(hoverEvent);
                }

                String action = section.getString("action");
                if (!Strings.isNullOrEmpty(action)) {
                    ClickEvent.Action eventAction = ClickEvent.Action.RUN_COMMAND;
                    if (action.startsWith("url:")) {
                        eventAction = ClickEvent.Action.OPEN_FILE;
                        action = action.substring(4);
                    } else if (action.startsWith("|")) {
                        eventAction = ClickEvent.Action.SUGGEST_COMMAND;
                        action = action.substring(1);
                    }

                    action = MessageCompiler.compile(action).buildPlain(this.settings);
                    ClickEvent clickEvent = new ClickEvent(eventAction, action);
                    element.setClickEvent(clickEvent);
                }

                String icon = section.getString("icon");
                if (icon == null) {
                    throw new IllegalStateException("Cannot find map icon section for property: " + property);
                }

                icon = MessageCompiler.compile(icon).buildPlain(this.settings);
                hoverMsg = first ? "" : MessageCompiler.compile(this.distanceMain).buildPlain(this.settings);
                element.addExtra(hoverMsg + icon);
                rowComponent.addExtra(element);
                if (y == 0 && x == this.width) {
                    String comp = MessageCompiler.compile(Config.Map.COMPASS.getManager().getString()).buildPlain(this.settings);
                    rowComponent.addExtra(comp);
                }

                first = false;
            }

            rows.add(rowComponent);
        }

        rows.forEach((component) -> this.player.spigot().sendMessage(component));
    }
}
