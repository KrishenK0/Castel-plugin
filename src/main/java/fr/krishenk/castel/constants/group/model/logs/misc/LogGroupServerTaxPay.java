package fr.krishenk.castel.constants.group.model.logs.misc;

import fr.krishenk.castel.constants.group.model.logs.AuditLog;
import fr.krishenk.castel.constants.group.model.logs.AuditLogProvider;
import fr.krishenk.castel.constants.land.DeserializationContext;
import fr.krishenk.castel.constants.land.SerializationContext;
import fr.krishenk.castel.constants.namespace.Namespace;
import fr.krishenk.castel.data.dataproviders.SectionableDataGetter;
import fr.krishenk.castel.data.dataproviders.SectionableDataSetter;
import fr.krishenk.castel.locale.provider.MessageBuilder;

import java.sql.SQLException;

public class LogGroupServerTaxPay extends AuditLog {
    private double amount;
    private static final Namespace NS = Namespace.castel("GROUP_TAX_SERVER_PAY");
    public static final AuditLogProvider PROVIDER = new AuditLogProvider(){
        @Override
        public AuditLog construct() {
            return new LogGroupServerTaxPay();
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

    protected LogGroupServerTaxPay() {
    }

    public LogGroupServerTaxPay(double amount) {
        this.amount = amount;
    }

    @Override
    public void deserialize(DeserializationContext<SectionableDataGetter> context) throws SQLException {
        super.deserialize(context);
        this.amount = context.getDataProvider().getDouble("amount");
    }

    @Override
    public void serialize(SerializationContext<SectionableDataSetter> context) {
        super.serialize(context);
        context.getDataProvider().setDouble("amount", this.amount);
    }

    @Override
    public void addEdits(MessageBuilder builder) {
        super.addEdits(builder);
        builder.parse("amount", this.amount);
    }
}
