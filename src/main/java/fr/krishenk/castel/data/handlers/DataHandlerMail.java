package fr.krishenk.castel.data.handlers;

import fr.krishenk.castel.constants.mails.Mail;
import fr.krishenk.castel.constants.mails.MailRecipientType;
import fr.krishenk.castel.data.dataproviders.*;

import java.sql.SQLException;
import java.util.*;

public class DataHandlerMail extends DataHandler<UUID, Mail> {
    public DataHandlerMail() {
        super(StdIdDataType.UUID, new SQLDataHandlerProperties(new String[0]));
    }

    @Override
    public void save(SectionableDataSetter provider, Mail data) {
        provider.setUUID("fromGroup", data.getFromGroup());
        provider.setUUID("sender", data.getSender());
        provider.setUUID("inReplyTo", data.getInReplyTo());
        provider.setString("subject", data.getSubject());
        provider.setLong("sent", data.getTime());
        provider.get("recipients").setMap(data.getRecipients(), (key, keyProvider, value) -> {
            keyProvider.setUUID(key);
            keyProvider.getValueProvider().setString(value.name());
        });
        provider.get("message").setCollection(data.getMessage(), DataSetter::setString);
        DataHandlerMetadata.serializeMetadata(provider, data);
    }

    @Override
    public Mail load(SectionableDataGetter provider, UUID uuid) throws SQLException {
        UUID fromGroup = provider.get("fromGroup").asUUID();
        UUID sender = provider.get("sender").asUUID();
        UUID inReplyTo = provider.get("inReplyTo").asUUID();
        String subject = provider.getString("subject");
        long sent = provider.getLong("sent");
        Map<UUID, MailRecipientType> recipients = provider.get("recipients").asMap(new HashMap<>(), (map, key, value) -> {
            try {
                map.put(key.asUUID(), MailRecipientType.valueOf(value.asString()));
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        });
        List<String> message = provider.get("message").asCollection(new ArrayList<>(), (list, element) -> {
            try {
                list.add(element.asString());
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        });
        Mail mail = new Mail(uuid, fromGroup, sender, message, subject, inReplyTo, recipients);
        DataHandlerMetadata.deserializeMetadata(provider, mail);
        return mail;
    }
}
