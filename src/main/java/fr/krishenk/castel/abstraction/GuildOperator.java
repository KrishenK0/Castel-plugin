package fr.krishenk.castel.abstraction;

import fr.krishenk.castel.constants.group.Group;
import fr.krishenk.castel.constants.group.Guild;
import org.jetbrains.annotations.Nullable;

public interface GuildOperator extends GroupOperator {
    public Guild getGuild();

    @Override
    @Nullable
    default Group getGroup() {
        return this.getGuild();
    }
}
