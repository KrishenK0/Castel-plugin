package fr.krishenk.castel.managers.inviterequests;

import fr.krishenk.castel.constants.land.SerializationContext;
import fr.krishenk.castel.constants.metadata.CastelMetadata;
import fr.krishenk.castel.constants.metadata.CastelObject;
import fr.krishenk.castel.data.dataproviders.DataSetter;
import fr.krishenk.castel.data.dataproviders.SectionCreatableDataSetter;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class PlayerJoinRequestsMeta implements CastelMetadata {
    private Set<UUID> requests;

    public PlayerJoinRequestsMeta(Set<UUID> requests) {
        this.requests = requests;
    }

    public Set<UUID> getRequests() {
        return requests;
    }

    public void setRequests(Set<UUID> requests) {
        this.requests = requests;
    }

    @Override
    public @NotNull Object getValue() {
        return this.requests;
    }

    @Override
    public void setValue(@NotNull Object obj) {
        Objects.requireNonNull(obj);
        this.requests = new HashSet<>((Set<UUID>) obj);
    }

    @Override
    public void serialize(@NotNull CastelObject<?> obj, @NotNull SerializationContext<SectionCreatableDataSetter> context) {
        context.getDataProvider().setCollection(this.requests, DataSetter::setUUID);
    }

    @Override
    public boolean shouldSave(@NotNull CastelObject<?> container) {
        return!((Collection<?>)this.requests).isEmpty();
    }
}
