package fr.krishenk.castel.constants.group.model.logs.misc.ranks;

import fr.krishenk.castel.constants.group.model.logs.AuditLog;
import fr.krishenk.castel.constants.group.model.logs.AuditLogProvider;
import fr.krishenk.castel.constants.land.DeserializationContext;
import fr.krishenk.castel.constants.land.SerializationContext;
import fr.krishenk.castel.constants.namespace.Namespace;
import fr.krishenk.castel.constants.player.CastelPlayer;
import fr.krishenk.castel.constants.player.Rank;
import fr.krishenk.castel.data.dataproviders.SectionableDataGetter;
import fr.krishenk.castel.data.dataproviders.SectionableDataSetter;
import fr.krishenk.castel.libs.xseries.XMaterial;
import fr.krishenk.castel.locale.provider.MessageBuilder;

import java.sql.SQLException;

public class LogRankChangeMaterial extends LogRankChange {
    private XMaterial newMaterial;
    private XMaterial oldMaterial;
    public static final Namespace NS = Namespace.castel("RANK_CHANGE_MATERIAL");
    public static final AuditLogProvider PROVIDER = new AuditLogProvider() {
        @Override
        public AuditLog construct() {
            return new LogRankChangeMaterial();
        }

        @Override
        public Namespace getNamespace() {
            return NS;
        }
    };

    @Override
    public AuditLogProvider getProvider() {
        return PROVIDER;
    }

    protected LogRankChangeMaterial() {
    }

    public LogRankChangeMaterial(CastelPlayer player, Rank rank, XMaterial newMaterial) {
        super(player, rank);
        this.oldMaterial = rank.getMaterial();
        this.newMaterial = newMaterial;
    }

    public XMaterial getOldMaterial() {
        return oldMaterial;
    }

    @Override
    public void deserialize(DeserializationContext<SectionableDataGetter> context) throws SQLException {
        super.deserialize(context);
        SectionableDataGetter json = context.getDataProvider();
        this.oldMaterial = XMaterial.matchXMaterial(json.getString("oldMaterial")).get();
        this.newMaterial = XMaterial.matchXMaterial(json.getString("newMaterial")).get();
    }

    @Override
    public void serialize(SerializationContext<SectionableDataSetter> context) {
        super.serialize(context);
        SectionableDataSetter json = context.getDataProvider();
        json.setString("oldMaterial", this.oldMaterial.name());
        json.setString("newMaterial", this.newMaterial.name());
    }

    @Override
    public void addEdits(MessageBuilder builder) {
        super.addEdits(builder);
        builder.parse("old-material", this.oldMaterial);
        builder.parse("new-material", this.newMaterial);
    }
}
