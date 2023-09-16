package fr.krishenk.castel.constants.group;

import java.util.UUID;

@FunctionalInterface
public interface GroupResolver {
    GuildsResolver GUILDS_RESOLVER = new GuildsResolver();

    Group getGroup(UUID id);

    class GuildsResolver implements GroupResolver{
        @Override
        public Group getGroup(UUID id) {
            return Guild.getGuild(id);
        }
    }
}
