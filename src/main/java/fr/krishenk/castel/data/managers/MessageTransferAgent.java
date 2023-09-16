package fr.krishenk.castel.data.managers;

import fr.krishenk.castel.constants.mails.Mail;
import fr.krishenk.castel.data.CastelDataCenter;
import fr.krishenk.castel.data.CastelDatabase;
import fr.krishenk.castel.data.DataManager;
import fr.krishenk.castel.data.handlers.DataHandlerMail;
import fr.krishenk.castel.lang.Config;

import java.util.UUID;

public class MessageTransferAgent extends DataManager<UUID, Mail> {

    public MessageTransferAgent(CastelDataCenter dataCenter) {
        super("mails", dataCenter.constructDatabase("Mails", "mails", new DataHandlerMail()), false, true);
    }
}
