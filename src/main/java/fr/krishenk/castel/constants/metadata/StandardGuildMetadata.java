package fr.krishenk.castel.constants.metadata;

import fr.krishenk.castel.constants.land.SerializationContext;
import fr.krishenk.castel.data.CastelGson;
import fr.krishenk.castel.data.dataproviders.SectionCreatableDataSetter;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Objects;

public class StandardGuildMetadata implements CastelMetadata {
    private Object value;

    public StandardGuildMetadata(Object value) {
        this.value = value;
    }

    @NotNull
    @Override
    public Object getValue() {
        return value;
    }

    @Override
    public void setValue(Object value) {
        this.value = value;
    }

    public final String getString() {
        return this.getValue().toString();
    }

    public final int getInt() {
        Number number = this.asNumber();
        return number.intValue();
    }

    public final float getFloat() {
        Number number = this.asNumber();
        return number.floatValue();
    }

    public final double getDouble() {
        Number number = this.asNumber();
        return number.doubleValue();
    }

    public final long getLong() {
        Number number = this.asNumber();
        return number.longValue();
    }

    public final boolean getBoolean() {
        Object object = this.getValue();
        return (Boolean)object;
    }

    private Number asNumber() {
        return (Number)this.getValue();
    }

    public int hashCode() {
        return this.getValue().hashCode();
    }

    public String toString() {
        return "GuildMetadata {" + this.getValue() + " }";
    }

    public boolean equals(Object other) {
        return other instanceof StandardGuildMetadata && Objects.equals(this.getValue(), ((StandardGuildMetadata) other).getValue());
    }

    public final List<String> getStringList() {
        return (List) this.getValue();
    }

    @Override
    public void serialize(@NotNull CastelObject<?> obj, @NotNull SerializationContext<SectionCreatableDataSetter> context) {
        context.getDataProvider().setString(CastelGson.toJson(this.getValue()));
    }

    @Override
    public boolean shouldSave(@NotNull CastelObject<?> container) {
        return true;
    }

    public static StandardGuildMetadata of(Object value) {
        return new StandardGuildMetadata(value);
    }
}
