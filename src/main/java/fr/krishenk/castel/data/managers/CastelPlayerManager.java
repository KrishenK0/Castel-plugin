package fr.krishenk.castel.data.managers;

import fr.krishenk.castel.CastelPlugin;
import fr.krishenk.castel.constants.player.CastelPlayer;
import fr.krishenk.castel.data.CastelDataCenter;
import fr.krishenk.castel.data.DataManager;
import fr.krishenk.castel.data.handlers.DataHandlerCastelPlayer;

import java.util.UUID;

public class CastelPlayerManager extends DataManager<UUID, CastelPlayer> {
    public CastelPlayerManager(CastelDataCenter dataCenter) {
        super("players", dataCenter.constructDatabase("Players", "players", new DataHandlerCastelPlayer()));
        this.autoSave(CastelPlugin.getInstance());
    }
}
