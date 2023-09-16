package fr.krishenk.castel.utils.network;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import fr.krishenk.castel.CLogger;
import fr.krishenk.castel.CastelPlugin;
import fr.krishenk.castel.utils.cache.CachedValue;
import fr.krishenk.castel.utils.debugging.CastelDebug;
import fr.krishenk.castel.utils.debugging.DebugNS;
import org.bukkit.Bukkit;
import org.jetbrains.annotations.NotNull;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.ReadableByteChannel;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.nio.file.attribute.FileAttribute;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class JSONRequester {
    @NotNull
    public static final JSONRequester INSTANCE;
    @NotNull
    private static final URL API_URL;
    @NotNull
    private static final String USER_AGENT;
    @NotNull
    private static final CachedValue<String> MASTER_SHA;

    private JSONRequester() {
    }

    
    public static final <T> T get(@NotNull String urlString, @NotNull Class<T> rootType) throws IOException {
        return JSONRequester.get(new URL(urlString), rootType);
    }

    
    public static final <T> T get(@NotNull URL url, @NotNull Class<T> rootType) throws IOException {
        Object object;
        URLConnection connection = url.openConnection();
        connection.setConnectTimeout((int) Duration.ofSeconds(30L).toMillis());
        connection.setReadTimeout((int)Duration.ofSeconds(10L).toMillis());
        connection.setRequestProperty("User-Agent", USER_AGENT);
        BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        try {
            object = new Gson().fromJson(reader, rootType);
        }
        catch (Throwable ex) {
            StringBuilder stringBuilder = new StringBuilder().append("Failed to fetch JSON '");
            InputStream inputStream = connection.getInputStream();
            
            throw new IllegalStateException(stringBuilder.append(INSTANCE.inputStreamToString(inputStream)).toString(), ex);
        }
        return (T)object;
    }

    private final String inputStreamToString(InputStream input) {
        String string = new BufferedReader(new InputStreamReader(input)).lines().collect(Collectors.joining("\n"));
        
        return string;
    }

    
    private static /* synthetic */ void getAPI_URL$annotations() {
    }

    @NotNull
    public static final CachedValue<String> getMASTER_SHA() {
        return MASTER_SHA;
    }

    
    public static /* synthetic */ void getMASTER_SHA$annotations() {
    }

    
    @NotNull
    public static final CommitDifference getDifference(@NotNull String lastCommit, @NotNull String[] folderFilter) throws IOException {
        JsonObject json = JSONRequester.get("https://api.github.com/repos/CryptoMorin/KingdomsX/compare/" + lastCommit + "...master?per_page=1", JsonObject.class);
        String htmlURL = json.get("html_url").getAsString();
        int totalCommitDiff = json.get("total_commits").getAsInt();
        List files = new ArrayList();
        for (JsonElement file : json.get("files").getAsJsonArray()) {
            String filterStart;
            String string;
            String fileName;
            block2: {
                fileName = file.getAsJsonObject().get("filename").getAsString();
                String[] arrstring = folderFilter;
                int n = arrstring.length;
                for (int i = 0; i < n; ++i) {
                    String string2;
                    String x = string2 = arrstring[i];
                    boolean bl = false;
                    
                    if (!fileName.startsWith(x)) continue;
                    string = string2;
                    break block2;
                }
                string = null;
            }
            if ((filterStart = string) == null) continue;
            
            String string3 = fileName.substring(filterStart.length());
            
            files.add(string3);
        }
        
        return new CommitDifference(lastCommit, "master", totalCommitDiff, htmlURL, files);
    }

    
    public static final boolean downloadGitHubFile(@NotNull String path, @NotNull Path to) throws IOException {
        boolean bl;
        URL url = new URL("https://github.com/CryptoMorin/KingdomsX/blob/master/" + path + "?raw=true");
        try {
            INSTANCE.downloadFile(url, to);
            bl = true;
        }
        catch (FileNotFoundException ex) {
            bl = false;
        }
        return bl;
    }

    public final void downloadFile(@NotNull URL link, @NotNull Path to) throws IOException {
        CLogger.debug(CastelDebug.DOWNLOAD, "Downloading: " + link);
        ReadableByteChannel readableByteChannel = Channels.newChannel(link.openStream());
        
        ReadableByteChannel readChan = readableByteChannel;
        Files.createDirectories(to.getParent());
        OpenOption[] arropenOption = new OpenOption[]{StandardOpenOption.CREATE, StandardOpenOption.WRITE};
        FileChannel output = FileChannel.open(to, arropenOption);
        output.transferFrom(readChan, 0L, Long.MAX_VALUE);
        readChan.close();
        output.close();
    }

    
    @NotNull
    public static final String getMasterSHA() {
        String string;
        JsonObject res = null;
        try {
            res = JSONRequester.get(API_URL, JsonObject.class);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        string = res.get("sha").getAsString();

        return string;
    }

    static {
        URL uRL;
        INSTANCE = new JSONRequester();
        try {
            uRL = new URL("https://api.github.com/repos/CryptoMorin/KingdomsX/commits/master?per_page=1");
        }
        catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
        API_URL = uRL;
        USER_AGENT = "KingdomsX/" + CastelPlugin.getInstance().getDescription().getVersion() + " (" + System.getProperty("os.name") + "; " + System.getProperty("os.version") + "; " + System.getProperty("java.vendor") + "; " + System.getProperty("java.version") + ") " + Bukkit.getName() + '/' + Bukkit.getVersion() + " (" + Bukkit.getBukkitVersion() + ')';
        CLogger.debug(CastelDebug.DOWNLOAD, "User-Agent: " + USER_AGENT);
        MASTER_SHA = new CachedValue<>(JSONRequester::getMasterSHA, Duration.ofMinutes(1L));

    }
}

