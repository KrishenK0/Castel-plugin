package fr.krishenk.castel.constants.group.model.logs.misc.bank;

import fr.krishenk.castel.constants.group.model.logs.AuditLog;
import fr.krishenk.castel.constants.group.model.logs.AuditLogProvider;
import fr.krishenk.castel.constants.group.model.logs.misc.LogPlayerOperator;
import fr.krishenk.castel.constants.land.DeserializationContext;
import fr.krishenk.castel.constants.land.SerializationContext;
import fr.krishenk.castel.constants.namespace.Namespace;
import fr.krishenk.castel.data.dataproviders.SectionableDataGetter;
import fr.krishenk.castel.data.dataproviders.SectionableDataSetter;
import fr.krishenk.castel.locale.provider.MessageBuilder;

import java.sql.SQLException;
import java.util.UUID;

public class LogGuildBankChange extends LogPlayerOperator {
    private double oldValue;
    private double newValue;
    private static final Namespace NS = Namespace.castel("BANK_CHANGE");
    public static final AuditLogProvider PROVIDER = new AuditLogProvider() {
        @Override
        public AuditLog construct() {
            return new LogGuildBankChange();
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

    protected LogGuildBankChange() {
    }

    public LogGuildBankChange(UUID player, double oldValue, double newValue) {
        super(player);
        this.oldValue = oldValue;
        this.newValue = newValue;
    }

    public double getAmount() {
        return newValue-oldValue;
    }

    @Override
    public void deserialize(DeserializationContext<SectionableDataGetter> context) throws SQLException {
        super.deserialize(context);
        SectionableDataGetter json = context.getDataProvider();
        this.oldValue = json.getDouble("oldValue");
        this.newValue = json.getDouble("newValue");
    }

    @Override
    public void serialize(SerializationContext<SectionableDataSetter> context) {
        super.serialize(context);
        SectionableDataSetter json = context.getDataProvider();
        json.setDouble("oldValue", this.oldValue);
        json.setDouble("newValue", this.newValue);
    }

    @Override
    public void addEdits(MessageBuilder builder) {
        super.addEdits(builder);
        builder.raw("old-value", this.oldValue);
        builder.raw("new-value", this.newValue);
    }
}
