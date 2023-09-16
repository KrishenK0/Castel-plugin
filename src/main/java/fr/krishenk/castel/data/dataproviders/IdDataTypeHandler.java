package fr.krishenk.castel.data.dataproviders;

import fr.krishenk.castel.data.statements.getters.SimpleResultSetQuery;
import fr.krishenk.castel.data.statements.setters.SimplePreparedStatement;
import org.jetbrains.annotations.NotNull;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.Objects;
import java.util.function.Function;

public abstract class IdDataTypeHandler<T> {
    @NotNull
    private final String prefix;
    @NotNull
    private final Class<T> klass;
    @NotNull
    private final String[] columns;
    @NotNull
    private final String columnsTuple;
    @NotNull
    private final String whereClause;
    @NotNull
    private final String inClause;

    public IdDataTypeHandler(@NotNull String prefix, @NotNull Class<T> klass, @NotNull String[] columnsArg) {
        Objects.requireNonNull(prefix, "prefix");
        Objects.requireNonNull(klass, "klass");
        Objects.requireNonNull(columnsArg, "columnsArg");
//        this.prefix = prefix;
//        this.klass = klass;
//
//        if (columnsArg.length == 0) {
//            this.columns = new String[]{prefix};
//        } else {
//            this.columns = Arrays.stream(columnsArg).map(item -> prefix + '_' + item).toArray(String[]::new);
//        }
//
//        this.columnsTuple = Arrays.stream(columns).map(column -> "`" + column + "`").collect(Collectors.joining(", "));
//        this.whereClause = Arrays.stream(columns).map(column -> "`" + column + "`=?").collect(Collectors.joining(" AND "));
//        this.inClause = Arrays.stream(columns).map(column -> "?").collect(Collectors.joining(", "));

        this.prefix = prefix;
        this.klass = klass;

        if (columnsArg.length == 0) {
            this.columns = new String[]{this.prefix};
        } else {
            this.columns = Arrays.stream(columnsArg)
                    .map(item -> this.prefix + '_' + item)
                    .toArray(String[]::new);
        }

        final Function<String, CharSequence> namelessClass_1 = x -> '`' + x + '`';
        this.columnsTuple = Arrays.stream(this.columns)
                .collect(StringBuilder::new, (sb, s) -> {
                    if (sb.length() > 0) sb.append(", ");
                    sb.append(namelessClass_1.apply(s));
                }, StringBuilder::append)
                .toString();

        final Function<String, CharSequence> namelessClass_2 = x -> '`' + x + "`=?";
        this.whereClause = Arrays.stream(this.columns)
                .collect(StringBuilder::new, (sb, s) -> {
                    if (sb.length() > 0) sb.append(" AND ");
                    sb.append(namelessClass_2.apply(s));
                }, StringBuilder::append)
                .toString();

        final Function<String, CharSequence> namelessClass_3 = s -> "?";
        this.inClause = Arrays.stream(this.columns)
                .collect(StringBuilder::new, (sb, s) -> {
                    if (sb.length() > 0) sb.append(", ");
                    sb.append(namelessClass_3.apply(s));
                }, StringBuilder::append)
                .toString();
    }

    @NotNull
    public final String getPrefix() {
        return this.prefix;
    }

    @NotNull
    public final Class<T> getKlass() {
        return this.klass;
    }

    @NotNull
    public final String[] getColumns() {
        return this.columns;
    }

    @NotNull
    public final String getColumnsTuple() {
        return this.columnsTuple;
    }

    @NotNull
    public final String getWhereClause() {
        return this.whereClause;
    }

    @NotNull
    public final String getInClause() {
        return this.inClause;
    }

    public abstract void setSQL(@NotNull SimplePreparedStatement statement, T id);

    public abstract T fromSQL(@NotNull SimpleResultSetQuery result) throws SQLException;

    public abstract T fromString(@NotNull String string);

    @NotNull
    public abstract String toString(T id);
}
