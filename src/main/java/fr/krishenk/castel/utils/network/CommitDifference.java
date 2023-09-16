package fr.krishenk.castel.utils.network;

import org.jetbrains.annotations.NotNull;

import java.util.List;

public class CommitDifference {
    @NotNull
    private final String baseSHA;
    @NotNull
    private final String headSHA;
    private final int totalCommitDifference;
    @NotNull
    private final String htmlURL;
    @NotNull
    private final List<String> files;

    public CommitDifference(@NotNull String baseSHA, @NotNull String headSHA, int totalCommitDifference, @NotNull String htmlURL, @NotNull List<String> files) {
        this.baseSHA = baseSHA;
        this.headSHA = headSHA;
        this.totalCommitDifference = totalCommitDifference;
        this.htmlURL = htmlURL;
        this.files = files;
    }

    @NotNull
    public final String getBaseSHA() {
        return this.baseSHA;
    }

    @NotNull
    public final String getHeadSHA() {
        return this.headSHA;
    }

    public final int getTotalCommitDifference() {
        return this.totalCommitDifference;
    }

    @NotNull
    public final String getHtmlURL() {
        return this.htmlURL;
    }

    @NotNull
    public final List<String> getFiles() {
        return this.files;
    }

    @NotNull
    public String toString() {
        return "CommitDifference{\n  " + this.baseSHA + " -> " + this.headSHA + "\n  total commits: " + this.totalCommitDifference + "\n  HTML URL: " + this.htmlURL + "\n  files:" + this.files + " \n}";
    }
}


