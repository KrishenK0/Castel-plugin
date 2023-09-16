package fr.krishenk.castel.managers.inviterequests;

import fr.krishenk.castel.constants.land.SerializationContext;
import fr.krishenk.castel.constants.metadata.CastelMetadata;
import fr.krishenk.castel.constants.metadata.CastelObject;
import fr.krishenk.castel.data.dataproviders.SectionCreatableDataSetter;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.UUID;

public class GuildJoinRequestsMeta implements CastelMetadata {
    private Map<UUID, Long> requests;

    public GuildJoinRequestsMeta(Map<UUID, Long> requests) {
        this.requests = requests;
    }

    public Map<UUID, Long> getRequests() {
        return requests;
    }

    public void setRequests(Map<UUID, Long> requests) {
        this.requests = requests;
    }

    @Override
    public @NotNull Object getValue() {
        return this.requests;
    }

    @Override
    public void setValue(@NotNull Object obj) {
        this.requests = (Map<UUID, Long>) obj;
    }

    @Override
    public void serialize(@NotNull CastelObject<?> obj, @NotNull SerializationContext<SectionCreatableDataSetter> context) {
        context.getDataProvider().setMap(this.requests, (k, kp, v) -> {
            kp.setUUID(k);
            kp.getValueProvider().setLong(v);
        });
    }
}
