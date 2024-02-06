package fr.krishenk.castel.constants.group.model.logs;

import fr.krishenk.castel.CastelPlugin;
import fr.krishenk.castel.constants.group.model.logs.lands.LogGuildClaim;
import fr.krishenk.castel.constants.group.model.logs.lands.LogGuildUnclaim;
import fr.krishenk.castel.constants.group.model.logs.misc.*;
import fr.krishenk.castel.constants.group.model.logs.misc.ranks.*;
import fr.krishenk.castel.constants.group.model.logs.misc.renames.LogGuildChangeLore;
import fr.krishenk.castel.constants.group.model.logs.misc.renames.LogGuildChangeTag;
import fr.krishenk.castel.constants.group.model.logs.misc.renames.LogGuildRename;
import fr.krishenk.castel.constants.group.model.logs.purchases.LogGuildUpgradeMisc;
import fr.krishenk.castel.constants.group.model.logs.purchases.LogGuildUpgradePowerup;
import fr.krishenk.castel.constants.group.model.logs.relations.LogGuildRelationshipChangeEvent;
import fr.krishenk.castel.constants.namespace.Namespace;

import java.util.Arrays;
import java.util.Map;

public class StandardAuditLogRegister {
    public static void registerAll() {
        Map<Namespace, AuditLogProvider> registry = CastelPlugin.getInstance().getAuditLogRegistry().getRawRegistry();
        Arrays.asList(LogGuildJoin.PROVIDER, LogGuildLeave.PROVIDER, LogGuildKick.PROVIDER, LogGuildResourcePointsConvert.PROVIDER, LogGuildUpgradePowerup.PROVIDER, LogGuildUpgradeMisc.PROVIDER, LogGuildClaim.PROVIDER, LogGuildUnclaim.PROVIDER,  LogGuildPacifismStateChange.PROVIDER, LogGuildRelationshipChangeEvent.PROVIDER, LogGuildLeaderChange.PROVIDER, LogPlayerankChange.PROVIDER, LogGuildInvite.PROVIDER, LogRankCreate.PROVIDER, LogRankDelete.PROVIDER, LogRankChangeName.PROVIDER, LogRankChangeSymbol.PROVIDER, LogRankChangeColor.PROVIDER, LogRankChangePriority.PROVIDER, LogRankChangeMaxClaims.PROVIDER, LogRankChangeMaterial.PROVIDER, LogGroupServerTaxPay.PROVIDER, LogGuildChangeLore.PROVIDER, LogGuildRename.PROVIDER, LogGuildChangeTag.PROVIDER).forEach(x -> registry.put(x.getNamespace(), x));
    }
}
