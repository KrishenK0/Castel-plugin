package fr.krishenk.castel.commands;

public class CommandAdminMigrate extends CastelParentCommand{
    public CommandAdminMigrate(CastelParentCommand parent) {
        super("migrate", parent);
        new CommandAdminMigrateDatabase(this);
    }
}
