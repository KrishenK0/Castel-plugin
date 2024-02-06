package fr.krishenk.castel.managers.structures;

import fr.krishenk.castel.constants.group.Group;
import fr.krishenk.castel.constants.player.*;
import fr.krishenk.castel.lang.Config;
import fr.krishenk.castel.libs.xseries.XMaterial;
import fr.krishenk.castel.libs.xseries.XTag;
import fr.krishenk.castel.locale.provider.MessageBuilder;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Consumer;

public class RankEditor {
    private final Player player;
    private final CastelPlayer cp;
    private final Rank rank;
    private final Group group;

    public RankEditor(Player player, Rank rank, Group group) {
        this.player = player;
        this.cp = CastelPlayer.getCastelPlayer(player);
        this.rank = rank;
        this.group = group;
    }

    public static MessageBuilder getRankEdits(Rank rank) {
        return new MessageBuilder().raws("rank_node", rank.getNode(), "rank_priority", rank.getPriority(), "rank_max_claims", rank.getMaxClaims(), "rank_material", rank.getMaterial(), "rank_symbol", rank.getSymbol(), "rank_name", rank.getName(), "rank_color", rank.getColor());
    }

    public static XMaterial randomMaterial() {
        XMaterial mat;
        Material material;
        ThreadLocalRandom random = ThreadLocalRandom.current();
        while ((material = (mat = XMaterial.VALUES[random.nextInt(0, XMaterial.VALUES.length)]).parseMaterial()) == null || XTag.INVENTORY_NOT_DISPLAYABLE.isTagged(XMaterial.matchXMaterial(material)));
        return mat;
    }

    public static Rank createNewRank(RankMap ranks) {
        String node = Config.Ranks.NEW_RANK_NODE.getManager().getString();
        int i = 0;
        while (ranks.getRanks().containsKey(node)) {
            node = "node (" + ++i + ')';
        }
        String name = Config.Ranks.NEW_RANK_NAME.getManager().getString();
        String color = Config.Ranks.NEW_RANK_COLOR.getManager().getString();
        String symbol = Config.Ranks.NEW_RANK_SYMBOL.getManager().getString();
        int maxClaims = Config.Ranks.NEW_RANK_MAX_CLAIMS.getManager().getInt();
        List<String> permissions = Config.Ranks.NEW_RANK_PERMISSIONS.getManager().getStringList();
        Set<GuildPermission> perms = !permissions.isEmpty() && permissions.get(0).equals("@MEMBER") ? ranks.getLowestRank().copyPermissions() : GuildPermissionRegistry.parse(permissions);
        String materialName = Config.Ranks.NEW_RANK_MATERIAL.getManager().getString();
        XMaterial material = materialName.equalsIgnoreCase("random") ? RankEditor.randomMaterial() : XMaterial.matchXMaterial(materialName).orElse(XMaterial.DIRT);
        return new Rank(node, name, color, symbol, material, ranks.size()-1, maxClaims, perms);
    }
}
