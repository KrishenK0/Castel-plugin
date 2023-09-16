package fr.krishenk.castel.locale;

import fr.krishenk.castel.CLogger;
import fr.krishenk.castel.CastelPlugin;
import fr.krishenk.castel.locale.compiler.MessageObject;
import fr.krishenk.castel.locale.compiler.placeholders.StandardCastelPlaceholder;
import fr.krishenk.castel.locale.messenger.DefinedMessenger;
import fr.krishenk.castel.locale.provider.MessageBuilder;
import fr.krishenk.castel.locale.provider.MessageProvider;
import fr.krishenk.castel.utils.internal.nonnull.NonNullMap;
import fr.krishenk.castel.utils.string.StringUtils;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.*;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public enum SupportedLanguage {
    EN(Locale.ENGLISH, "English", "US", "UK");
//    HU("Magyar", "Magyar nyelv", "Hungarian"),
//    RU("\u0420\u0443\u0441\u0441\u043a\u0438\u0439", "Russian", "P\u0443\u0441\u0441\u043a\u0438\u0439 \u044f\u0437\u044b\u043a", "Russia"),
//    ES("Espa\u00f1ol", "Spanish", "Espanol", "Castilian", "Castellano"),
//    DE(Locale.GERMAN, "Deutsch", "German", "Schwiizerdutsch", "\u00d6sterreichisches", "bar_AT", "gsw_CH", "deu_DE"),
//    PL("Polski", "Polish", "Poland"),
//    TR("T\u00fcrk\u00e7e", "T\u00fcrk dili", "Turkish", "Turkey"),
//    ZH(Locale.CHINESE, "\u6c49\u8bed", "\u6f22\u8a9e", "\u4e2d\u6587", "Zh\u014dngw\u00e9n", "Chinese", "China"),
//    CS("\u010ce\u0161tina", "Bohemian", "Czech");
    private Map<LanguageEntry, MessageProvider> messages;
    public static final String MACROS_ENTRY = "variables";
    private final String nativeName;
    private final Locale locale;
    private FileConfiguration adapter;
    private String installedCommitSHA;

    SupportedLanguage(String ... names) {
        this(null, names);
    }

    SupportedLanguage(Locale locale, String ... names) {
        this.locale = locale == null ? new Locale(this.name().toLowerCase(Locale.ENGLISH)) : locale;
        if (this.name().equals("EN")) {
            this.initialize();
        }
        this.nativeName = names[0];
        String lowerName = this.getLowerCaseName();
        NameContainer.NAMES.put(lowerName, this);
        Stream.of(names).forEach(x -> NameContainer.NAMES.put(x.toLowerCase(), this));
    }

    public void initialize() {
        if (this.name().equals("EN")) {
            this.installedCommitSHA = CastelPlugin.getInstance().getCompileCommitSHA();
        }
        if (this.messages == null) {
            this.messages = new NonNullMap<>(CastelLang.values().length);
        }
        if (this.adapter == null) {
            if (this.name().equals("EN")) {
                this.adapter = YamlConfiguration.loadConfiguration(new InputStreamReader(CastelPlugin.getInstance().getResource("lang.yml")));
                this.installedCommitSHA = CastelPlugin.getInstance().getCompileCommitSHA();
                CLogger.info(this.installedCommitSHA);
            } else {
                this.adapter = YamlConfiguration.loadConfiguration(new InputStreamReader(CastelPlugin.getInstance().getResource("lang.yml")));
            }
        }
    }

    private static InputStreamReader inputStreamOf(File file) {
        return new InputStreamReader(Objects.requireNonNull(CastelPlugin.getInstance().getResource(file.getName())));
    }

    public Locale getLocale() {
        return this.locale;
    }

    public static List<SupportedLanguage> getInstalled() {
        return Arrays.stream(values()).filter(SupportedLanguage::isInstalled).collect(Collectors.toList());
    }

//    public static Collection<String> getRegisteredGUINames() {
//        return EN.getGUIs().keySet();
//    }

    public Path getRepoPath() {
        return LanguageManager.LANGUAGES_REPO_FOLDER.resolve(this.getLowerCaseName());
    }

    protected void uninstall() {
        if (this == EN) {
            throw new IllegalStateException("Cannot uninstall default English translation");
        }
        this.installedCommitSHA = null;
        this.messages = null;
        this.adapter = null;
    }

    public boolean isInstalled() {
        return this.installedCommitSHA != null;
    }

    public Path getMainLanguageFile() {
        return CastelPlugin.getPath("languages").resolve(this.getLowerCaseName() + ".yml");
    }

    public void addMessage(DefinedMessenger messenger, MessageProvider provider) {
        this.messages.put(messenger.getLanguageEntry(), provider);
    }

    public Map<LanguageEntry, MessageProvider> getMessages() {
        return this.messages;
    }

    public void ensureInstalled() {
        if (!this.isInstalled()) {
            throw new IllegalStateException("This language isn't marked as installed: " + this.name());
        }
    }

    public String getInstalledCommitSHA() {
        return this.installedCommitSHA;
    }

    public void setInstalledCommitSHA(String installedCommitSHA) {
        this.installedCommitSHA = installedCommitSHA;
    }

    public String getLowerCaseName() {
        return StringUtils.toLatinLowerCase(this.name());
    }

    public FileConfiguration getAdapter() {
        this.ensureInstalled();
        return this.adapter;
    }

    public String getNativeName() {
        return this.nativeName;
    }

    public static SupportedLanguage fromName(String name) {
        int iso3 = name.indexOf(95);
        if (iso3 != -1) {
            name = name.substring(0, iso3);
        }
        return NameContainer.NAMES.get(name.toLowerCase());
    }

    public MessageProvider getMessage(LanguageEntry lang, boolean error) {
        MessageProvider current = this.messages == null ? null : this.messages.get(lang);
        if (this == EN && current == null) {
            if (error) {
                throw new IllegalStateException("Language entry " + lang + " is null for English");
            }
            return null;
        }
        return current == null ? EN.getMessage(lang, error) : current;
    }

    public MessageObject getMessage(String ... path) {
        LanguageEntry entry = new LanguageEntry(path);
        MessageProvider current = this.messages == null ? null : this.messages.get(entry);
        if (current == null) {
            current = SupportedLanguage.EN.messages.get(entry);
        }
        return current.getMessage();
    }

    public MessageObject getVariableRaw(String name) {
        LanguageEntry entry = new LanguageEntry(new String[]{MACROS_ENTRY, name});
        MessageProvider variable = this.messages.get(entry);
        return variable == null ? null : variable.getMessage();
    }

    public MessageObject getVariable(MessageBuilder settings, String name) {
        MessageProvider current;
        LanguageEntry entry = new LanguageEntry(new String[]{MACROS_ENTRY, name});
        current = this.messages == null ? null : this.messages.get(entry);
        if (current == null) {
            current = SupportedLanguage.EN.messages.get(entry);
        }
        if (current != null) {
            return current.getMessage();
        }
        return StandardCastelPlaceholder.getMacro(name, settings);
    }

    private static final class NameContainer {
        private static final Map<String, SupportedLanguage> NAMES = new HashMap<>(30);

        private NameContainer() {
        }
    }
}


