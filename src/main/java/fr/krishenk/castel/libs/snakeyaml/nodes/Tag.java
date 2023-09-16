
package fr.krishenk.castel.libs.snakeyaml.nodes;

import java.util.Objects;

public final class Tag {
    public static final Tag INT = new Tag("int");
    public static final Tag FLOAT = new Tag("float");
    public static final Tag ALIAS = new Tag("alias");
    public static final Tag BOOL = new Tag("bool");
    public static final Tag NULL = new Tag("null");
    public static final Tag STR = new Tag("str");
    public static final Tag SEQ = new Tag("seq");
    public static final Tag MAP = new Tag("map");
    public static final Tag MERGE = new Tag("merge");
    public static final Tag COMMENT = new Tag("comment");
    private final String value;

    public Tag(String tag) {
        Objects.requireNonNull(tag, "Tag must be provided.");
        if (tag.isEmpty()) {
            throw new IllegalArgumentException("Tag must not be empty.");
        }
        if (tag.trim().length() != tag.length()) {
            throw new IllegalArgumentException("Tag must not contain leading or trailing spaces.");
        }
        this.value = tag;
    }

    public Tag(Class<?> clazz) {
        Objects.requireNonNull(clazz, "Class for tag must be provided.");
        this.value = clazz.getName();
    }

    public String getValue() {
        return this.value;
    }

    public String toString() {
        return this.value;
    }

    public boolean equals(Object obj) {
        if (obj instanceof Tag) {
            return this.value.equals(((Tag)obj).value);
        }
        return false;
    }

    public int hashCode() {
        return this.value.hashCode();
    }
}

