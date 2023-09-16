package fr.krishenk.castel.commands.admin.item;

import fr.krishenk.castel.commands.CastelParentCommand;

public class CommandAdminItem extends CastelParentCommand {
    public CommandAdminItem(CastelParentCommand parent) {
        super("item", parent);
        if (!this.isDisabled()) {
            new CommandAdminItemResourcePoints(this);
            new CommandAdminItemInject(this);
            //new CommandAdminItemTurret(this);
            //new CommandAdminItemStructure(this);
            new CommandAdminItemEditor(this);
        }
    }

//    TODO : Custom item ? (turret, structures, ...)
//    protected static List<String> getValidStyles(Map<String, CastelItemStyle<?, ?, ?>> styles) {
//        return styles.values().stream().filter(CastelItemStyle::hasItem)
//    }
}
