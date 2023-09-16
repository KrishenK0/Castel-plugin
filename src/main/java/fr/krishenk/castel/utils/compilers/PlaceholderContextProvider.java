package fr.krishenk.castel.utils.compilers;

public interface PlaceholderContextProvider {
    PlaceholderContextProvider EMPTY = placeholder -> null;

    Object processPlaceholder(String placeholder);
}
