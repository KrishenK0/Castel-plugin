package fr.krishenk.castel.locale;

import com.google.common.base.Enums;
import fr.krishenk.castel.CLogger;
import fr.krishenk.castel.CastelPlugin;
import fr.krishenk.castel.commands.CastelCommandHandler;
import fr.krishenk.castel.config.AdvancedMessage;
import fr.krishenk.castel.config.Comment;
import fr.krishenk.castel.config.managers.ConfigManager;
import fr.krishenk.castel.constants.player.CastelPlayer;
import fr.krishenk.castel.lang.Config;
import fr.krishenk.castel.lang.Lang;
import fr.krishenk.castel.libs.xseries.XSound;
import fr.krishenk.castel.libs.xseries.messages.Titles;
import fr.krishenk.castel.locale.compiler.MessageCompiler;
import fr.krishenk.castel.locale.compiler.MessageObject;
import fr.krishenk.castel.locale.messenger.DefinedMessenger;
import fr.krishenk.castel.locale.provider.AdvancedMessageProvider;
import fr.krishenk.castel.locale.provider.MessageProvider;
import fr.krishenk.castel.locale.provider.NullMessageProvider;
import fr.krishenk.castel.utils.FSUtil;
import fr.krishenk.castel.utils.debugging.CastelDebug;
import fr.krishenk.castel.utils.internal.arrays.ArrayUtils;
import fr.krishenk.castel.utils.network.CommitDifference;
import fr.krishenk.castel.utils.network.JSONRequester;
import fr.krishenk.castel.utils.string.StringUtils;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class LanguageManager {
    private static final String LANG_FOLDER_NAME = "languages";
    private static final String REPO_NAME = "repository";
    private static SupportedLanguage DEFAULT_LANGUAGE = SupportedLanguage.EN;
    public static final Path LANG_FOLDER = CastelPlugin.getPath("languages");
    public static final Path REPO_FOLDER = CastelPlugin.getPath("repository");
    public static final Path LANGUAGES_REPO_FOLDER = REPO_FOLDER.resolve("languages");
    private static final Map<Class<? extends DefinedMessenger>, DefinedMessenger[]> MESSENGERS = new LinkedHashMap<>(1);
    final FileConfiguration adapter;
    final SupportedLanguage lang;

    public static <T extends DefinedMessenger> void registerMessenger(Class<? extends DefinedMessenger> clazz, DefinedMessenger[] values) {
        Objects.requireNonNull(clazz, "Messenger class cannot be null");
        Objects.requireNonNull(values, "Messenger values cannot be null");
        MESSENGERS.put(clazz, values);
    }

    public static <T extends DefinedMessenger> void registerMessenger(Class<T> clazz) {
        Objects.requireNonNull(clazz, "Messenger class cannot be null");
        Enum[] constants = Objects.requireNonNull((Enum[])clazz.getEnumConstants(), "The provided class is not an enum. Consider using registerMessenger(Class, T[]) instead.");
        MESSENGERS.put(clazz, (DefinedMessenger[])constants);
    }

    public static void uninstall(SupportedLanguage lang) {
        CLogger.info("Uninstalling " + lang);
//        lang.getAdapter().getFile().delete();
        lang.uninstall();
        FSUtil.deleteFolder(lang.getRepoPath());
        //FSUtil.deleteFolder(GUIConfig.getFolder().resolve(lang.getLowerCaseName()));
    }

    public static void install(SupportedLanguage lang) throws IOException {
        LanguageManager.install(lang, null);
    }

    public static void install(SupportedLanguage lang, Consumer<String> guiDownloaded) throws IOException {
        String name = lang.getLowerCaseName();
        Path repoFolder = REPO_FOLDER.resolve(LANG_FOLDER_NAME).resolve(name);
        CLogger.info("Downloading language files for " + name);
        ArrayList<String> notFound = new ArrayList<>(20);
        String downloadFrom = "resources/languages/" + name + '/' + name + ".yml";
        boolean res = JSONRequester.downloadGitHubFile(downloadFrom, repoFolder.resolve(name + ".yml"));
        if (!res) {
            notFound.add(downloadFrom);
        }
//        Path mainGUIDir = repoFolder.resolve("guis");
//        for (String gui : SupportedLanguage.EN.getGUIs().keySet()) {
//            downloadFrom = "resources/languages/" + name + "/guis/" + gui + ".yml";
//            res = JSONRequester.downloadGitHubFile(downloadFrom, mainGUIDir.resolve(gui + ".yml"));
//            if (!res) {
//                notFound.add(downloadFrom);
//            }
//            guiDownloaded.accept(downloadFrom);
//        }
        if (!notFound.isEmpty()) {
            CLogger.warn("Couldn't find the following files for " + name + " language, this is normal: " + notFound);
        }
        LanguageManager.setCommitSHA(lang, JSONRequester.getMasterSHA());
        LanguageManager.load(lang);
        //GUIConfig.registerGUIsFor(lang);
//        ConfigWatcher.registerGUIWatchers(lang);
        CastelCommandHandler.reload();
    }

    public static void update(SupportedLanguage lang, CommitDifference difference) throws IOException {
        String name = lang.getLowerCaseName();
        Path repoFolder = REPO_FOLDER.resolve(LANG_FOLDER_NAME).resolve(name);
        for (String fileName : difference.getFiles()) {
            Path path = repoFolder.resolve(fileName);
            try {
                JSONRequester.downloadGitHubFile("resources/languages/" + name + '/' + fileName, path);
            }
            catch (IOException ex) {
                CLogger.error("Failed to update " + name + " language:");
                ex.printStackTrace();
            }
        }
        LanguageManager.setCommitSHA(lang, JSONRequester.getMasterSHA());
    }

    public static void setCommitSHA(SupportedLanguage lang, String commitSHA) {
        lang.setInstalledCommitSHA(commitSHA);
        ConfigManager.getGlobals().set(new String[]{"updates", LANG_FOLDER_NAME, lang.getLowerCaseName()}, commitSHA);
        ConfigManager.getGlobalsAdapter().saveConfig();
    }

    private LanguageManager(SupportedLanguage lang) {
        this.lang = lang;
//        ConfigSection commitShaConfigs = ConfigManager.getGlobals().createSection("updates", LANG_FOLDER_NAME);
        Path langRepoPath = lang.getRepoPath();
        if (lang == SupportedLanguage.EN || Files.exists(langRepoPath) && !FSUtil.isFolderEmpty(langRepoPath)) {
//            if (lang != SupportedLanguage.EN && !commitShaConfigs.isSet(lang.getLowerCaseName())) {
//                CLogger.warn("Couldn't find the language version for " + lang.getLowerCaseName() + " but it's in the repository folder.\nThis means that you manually installed the language yourself or modified globals.yml file.\nThe plugin will automatically fix this for you.");
//                commitShaConfigs.set(lang.getLowerCaseName(), JSONRequester.getMasterSHA());
//                ConfigManager.getGlobalsAdapter().saveConfig();
//            }
            lang.initialize();
//            if (lang != SupportedLanguage.EN) {
//                lang.setInstalledCommitSHA(commitShaConfigs.getString(lang.getLowerCaseName()));
//            }
//            CLogger.info("Loading language: " + lang + " (version " + lang.getInstalledCommitSHA() + ')');
//        } else if (commitShaConfigs.isSet(lang.getLowerCaseName())) {
//            CLogger.warn("The plugin found version information about " + lang.getLowerCaseName() + " installed language, however the files couldn't be found.\nThis means that you manually uninstalled it by removing folders from 'repository' folder of the plugin.\nThe plugin will automatically fix this for you.");
//            commitShaConfigs.set(lang.getLowerCaseName(), null);
//            ConfigManager.getGlobalsAdapter().saveConfig();
//            this.adapter = null;
//            return;
        }
//        this.adapter = lang.isInstalled() ? lang.getAdapter() : null;
        this.adapter = lang.getAdapter();
    }

    public static SupportedLanguage getDefaultLanguage() {
        return DEFAULT_LANGUAGE;
    }

    public static SupportedLanguage localeOf(CommandSender sender) {
        return sender instanceof Player ? LanguageManager.localeOf((Player)sender) : LanguageManager.getDefaultLanguage();
    }

    public static SupportedLanguage localeOf(OfflinePlayer sender) {
        return sender == null ? LanguageManager.getDefaultLanguage() : CastelPlayer.getCastelPlayer(sender).getLanguage();
    }

    public static SupportedLanguage localeOf(Player sender) {
        return LanguageManager.localeOf((OfflinePlayer)sender);
    }

    public void load() {
        if (this.adapter == null) {
            return;
        }
        try {
//            Files.createDirectories(LANG_FOLDER);
//            this.adapter.load(this.lang.name().toLowerCase(Locale.ENGLISH)+".yml");
            this.loadEnglishDefaults();
            if (this.adapter.getDefaultSection() != null) {
                new EntryLoader();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
//        if (!this.adapter.getFile().exists()) {
//            if (this.lang == SupportedLanguage.EN) {
//                this.adapter.createEmptyConfigIfNull();
//                this.loadEnglishDefaults();
//                this.adapter.saveConfig();
//            } else {
//                this.adapter.saveDefaultConfig();
//            }
//            this.adapter.reload();
//        } else {
//        }
    }

    public static void load(SupportedLanguage lang) {
        try {
            new LanguageManager(lang).load();
        }
        catch (Exception ex) {
            CLogger.error("An exception has occurred while loading main language file for: " + lang.name());
            ex.printStackTrace();
        }
    }

    private static void loadDefaultLanguage() {
        SupportedLanguage lang = SupportedLanguage.fromName(Config.LANG.getString());
        if (lang == null) {
            CLogger.error("Unknown language '" + Config.LANG.getString() + "' switching to English.");
            lang = SupportedLanguage.EN;
        }
        if (!lang.isInstalled()) {
            CLogger.error("You cannot use " + lang + " language as the default language because it's not installed.");
            lang = SupportedLanguage.EN;
        }
        DEFAULT_LANGUAGE = lang;
        CLogger.info("Default language: " + lang.name() + " (" + lang.getNativeName() + ')');
    }

    public static void loadAll() {
        try {
            LanguageManager.loadAll0();
        }
        catch (IOException e) {
            CLogger.error("Failed to load language files:");
            e.printStackTrace();
        }
        LanguageManager.addRepositoryInfo();
        LanguageManager.loadDefaultLanguage();
    }

    static void addRepositoryInfo() {
        if (!Files.exists(REPO_FOLDER)) {
            return;
        }
        try {
            Files.write(REPO_FOLDER.resolve("README.txt"), Arrays.asList("The repository folder is a reference folder meant for read-only purposes.", "You shouldn't add, delete or modify any of the files.", "This is the folder used to generate back language files if they", "are deleted without having to redownload them. If you want to uninstall a certain", "language, simply do it from '/c admin languagepacks' command in-game instead."), StandardCharsets.US_ASCII);
        } catch (IOException e) {
            CLogger.error("Failed to add info file to libs folder:");
            e.printStackTrace();
        }
    }

    static void loadAll0() throws IOException {
        if (Files.exists(LANG_FOLDER)) {
            try (Stream<Path> walker = Files.walk(LANG_FOLDER)){
                walker.forEach(langName -> {
                    if (langName.equals(LANG_FOLDER)) {
                        return;
                    }
                    String name = langName.getFileName().toString();
                    name = name.substring(0, name.length() - 4);
                    LanguageManager.getLanguageOrWarn(name);
                });
            }
        }
        for (SupportedLanguage lang : SupportedLanguage.values()) {
            LanguageManager.load(lang);
        }
    }

    public static SupportedLanguage getLanguageOrWarn(String name) {
        SupportedLanguage lang = Enums.getIfPresent(SupportedLanguage.class, name.toUpperCase(Locale.ENGLISH)).orNull();
        if (lang == null) {
            CLogger.error("Unknown language found in 'languages' folder: " + name);
            CLogger.error("It's either named incorrectly or not built into the plugin. In order to add a new language to the plugin you need to contact the developer with the necessary files.");
        }
        return lang;
    }

    private void putDefault(DefinedMessenger lang) {
        MessageProvider defaultProvider;
        String errors;
        String def = lang.getDefaultValue();
        MessageCompiler compiler = def == null ? null : new MessageCompiler(def);
        MessageObject obj = compiler == null ? null : compiler.compileObject();
        if (compiler != null && compiler.hasErrors() && !(errors = compiler.joinExceptions()).contains("macro")) {
            throw new AssertionError("Default message contains error for " + lang.name() + '\n' + errors);
        }
        AdvancedMessage data = lang.getAdvancedData();
        if (data == null) {
            defaultProvider = new MessageProvider(obj);
        } else {
            MessageObject actionbar = data.actionbar().isEmpty() ? null : new MessageCompiler(data.actionbar()).compileObject();
            Titles titles = !data.title().isEmpty() || !data.subtitle().isEmpty() ? new Titles(data.title().isEmpty() ? null : data.title(), data.subtitle().isEmpty() ? null : data.subtitle(), 10, 40, 10) : null;
            AdvancedMessageProvider extra = new AdvancedMessageProvider(obj, actionbar, titles);
            if (data.sound() != AdvancedMessage.DEFAULT_SOUND) {
                extra.withSound(new XSound.Record(data.sound()));
            }
            defaultProvider = extra;
        }

        this.lang.addMessage(lang, defaultProvider);
    }

    private void saveDefault(DefinedMessenger lang) {
//        Node keyNode;
        ConfigurationSection config = this.adapter.getDefaultSection();
        String def = lang.getDefaultValue();
        Comment comments = lang.getComment();
        AdvancedMessage data = lang.getAdvancedData();
        if (data != null && config != null) {
            ConfigurationSection section = config.createSection(Arrays.toString(lang.getLanguageEntry().getPath()));
//            keyNode = section.getNode();
            if (def != null) {
                section.set("message", def);
            }
            if (data.sound() != AdvancedMessage.DEFAULT_SOUND) {
                section.set("sound", data.sound().name());
            }
            if (!data.actionbar().isEmpty()) {
                section.set("actionbar", data.actionbar());
            }
            if (!data.title().isEmpty() || !data.subtitle().isEmpty()) {
                if (!data.title().isEmpty()) {
                    section.set("titles.title", data.title());
                }
                if (!data.subtitle().isEmpty()) {
                    section.set("titles.subtitle", data.subtitle());
                }
            }
        } else {
//            ConfigurationSection section = config.createSection(lang.getLanguageEntry().getPath(), def);
//            keyNode = comments != null && comments.forParent() ? section.getKeys().getKey() : section.getValue().getKey();
        }
//        if (comments != null) {
//            keyNode.setSimpleComments(comments.value());
//        }
    }

    private MessageObject parseMessage(String msg, String[] path) {
        MessageCompiler compiler = new MessageCompiler(msg);
        MessageObject obj = compiler.compileObject();
        if (compiler.hasErrors()) {
//            Mark mark = node.getWholeMark();
//            CLogger.warn("An error occurred while parsing message for '" + String.join(".", path) + "' in " + this.adapter.getCurrentPath() + " at line " + mark.getLine() + ":\n" + mark.createSnippet(0, Integer.MAX_VALUE, "", null).trim() + '\n' + compiler.joinExceptions());
            CLogger.warn("An error occurred while parsing message for '" + String.join(".", path) + "' in " + this.adapter.getCurrentPath()  + '\n' + compiler.joinExceptions());
        }
        return obj;
    }

    private void loadEnglishDefaults() {
        for (DefinedMessenger[] vals : MESSENGERS.values()) {
            for (DefinedMessenger lang : vals) {
                this.putDefault(lang);
                this.saveDefault(lang);
            }
        }
    }

//    public static String getRawMessage(DefinedMessenger lang, SupportedLanguage locale) {
//        ConfigSection config = locale.getAdapter().getConfig();
//        Node foundSection = config.findNode(lang.getLanguageEntry().getPath());
//        if (foundSection == null) {
//            return null;
//        }
//        if (foundSection.getNodeType() == NodeType.MAPPING) {
//            ConfigSection section = new ConfigSection(null, (MappingNode)foundSection);
//            return section.getString("message");
//        }
//        return NodeInterpreter.STRING.parse(foundSection);
//    }

    static {
//        LanguageManager.registerMessenger(CastelLang.class, CastelLang.values());
        LanguageManager.registerMessenger(Lang.class, Lang.values());
    }

    private final class EntryLoader {
        final ConfigurationSection config;
        final Set<DefinedMessenger> remainingEntries;
        final Map<LanguageEntry, DefinedMessenger> definedEntries;

        private EntryLoader() throws IOException {
            this.config = LanguageManager.this.adapter.getDefaultSection();
            this.remainingEntries = Collections.newSetFromMap(new IdentityHashMap<>(CastelLang.values().length + 300));
            this.definedEntries = new HashMap<>(CastelLang.values().length + 300);
            Iterator<DefinedMessenger[]> iterator = MESSENGERS.values().iterator();
            while (iterator.hasNext()) {
                for (DefinedMessenger lang : iterator.next()) {
                    this.definedEntries.put(lang.getLanguageEntry(), lang);
                    this.remainingEntries.add(lang);
                }
            }
            this.loadEntries(null, this.config);
            if (!this.remainingEntries.isEmpty()) {
                for (DefinedMessenger remainingEntry : this.remainingEntries) {
                    String[] path = remainingEntry.getLanguageEntry().getPath();
                    Object foundSection = this.config.get(StringUtils.join(path, "."));
                    if (foundSection != null) continue;
                    LanguageManager.this.saveDefault(remainingEntry);
                    LanguageManager.this.putDefault(remainingEntry);
                }
                CLogger.debug(CastelDebug.LANGUAGE_MISSING$ENTRIES, () -> "Added missing enteries to " + LanguageManager.this.lang.name() + ": " + this.remainingEntries.stream().map(x -> String.join(" -> ", x.getLanguageEntry().getPath())).collect(Collectors.joining(" | ")));
                LanguageManager.this.adapter.save(LanguageManager.this.lang.getNativeName());
            }
        }

        private void loadEntries(String[] currentEntryRoot, ConfigurationSection root) {
            for (String key : root.getKeys(true)) {
                MessageProvider provider;
                String[] currentEntry = currentEntryRoot;
                currentEntry = currentEntry == null ? new String[]{key} : ArrayUtils.merge(currentEntry, new String[]{key});
                LanguageEntry entry = new LanguageEntry(currentEntry);
                DefinedMessenger defined = this.definedEntries.get(entry);
//                Object node = root.get(key);
                if (defined == null) {
                    if (root.isConfigurationSection(key)) {
//                        MappingNode mapping = (MappingNode)node;
                        this.loadEntries(currentEntry, Objects.requireNonNull(root.getConfigurationSection(key)));
                        continue;
                    }
                } else {
                    this.remainingEntries.remove(defined);
                }
                if (root.isConfigurationSection(key)) {
                    provider = new AdvancedMessageProvider(root.getConfigurationSection(key));
                } else {
                    String msg = root.getString(key);
                    if (msg != null) {
                        MessageObject obj = LanguageManager.this.parseMessage(msg, currentEntry);
                        provider = new MessageProvider(obj);
                    } else {
                        provider = NullMessageProvider.getInstance();
                    }
                }
                LanguageManager.this.lang.getMessages().put(entry, provider);
            }
        }
    }
}

