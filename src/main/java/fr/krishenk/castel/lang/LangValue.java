package fr.krishenk.castel.lang;

public enum LangValue {
    PLAYER("player"),
    GUILD("guild");
    private String name;

    LangValue(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return '{'+this.name+'}';
    }
}
