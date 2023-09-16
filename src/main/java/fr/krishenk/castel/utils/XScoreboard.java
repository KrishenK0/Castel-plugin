package fr.krishenk.castel.utils;

import fr.krishenk.castel.libs.xseries.ReflectionUtils;
import fr.krishenk.castel.libs.xseries.XMaterial;
import fr.krishenk.castel.locale.compiler.MessageObject;
import fr.krishenk.castel.locale.provider.MessageBuilder;
import net.minecraft.server.v1_16_R3.IScoreboardCriteria;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Score;
import org.bukkit.scoreboard.Scoreboard;

import java.util.ArrayList;
import java.util.Collection;

public class XScoreboard {
    private static final char[] COLORS = new char[]{'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'c', 'd', 'f', 'r'};
    private static final boolean SUPPORTS_INFINITE_LENGTH = ReflectionUtils.supports(9);
    private final MessageObject title;
    private final Objective mainObjective;
    private final Collection<MessageObject> lines = new ArrayList<>(10);
    private String alignRight;

    public XScoreboard(String id, MessageObject title, MessageBuilder settings) {
        this.title = title;
        Scoreboard scoreboard = Bukkit.getScoreboardManager().getNewScoreboard();
        String titleStr = title.buildPlain(settings);
        this.mainObjective = ReflectionUtils.supports(17) ? scoreboard.registerNewObjective(id, IScoreboardCriteria.DUMMY.toString(), titleStr) : scoreboard.registerNewObjective(id, titleStr);
        this.mainObjective.setDisplaySlot(DisplaySlot.SIDEBAR);
    }

    public void update(MessageBuilder settings) {
        this.mainObjective.setDisplayName(this.title.buildPlain(settings));
    }

    public void setAlignRight(String alignRight) {
        this.alignRight = alignRight;
    }

    public Scoreboard getScoreboard() {
        return this.mainObjective.getScoreboard();
    }

    public void addLine(MessageObject obj) {
        if (this.lines.size() >= 15) {
            throw new IllegalStateException("Scoreboards cannot have more than 15 lines");
        }
        this.lines.add(obj);
    }

    public Collection<MessageObject> getLines() {
        return this.lines;
    }

    public void setForPlayer(Player player) {
        player.setScoreboard(this.getScoreboard());
    }

    public void clearLines() {
        this.lines.clear();
        Scoreboard scoreboard = this.mainObjective.getScoreboard();
        for (String entry : scoreboard.getEntries()) {
            scoreboard.resetScores(entry);
        }
    }

    public XScoreboard buildLines(MessageBuilder settings) {
        int linePosition = this.lines.size();
        for (MessageObject line : this.lines) {
            String translatedLine = this.alignRight + line.buildPlain(settings);
            Score score = this.mainObjective.getScore(XScoreboard.lengthCheckedLine(translatedLine));
            int duplicateResolverIndex = 0;
            while (score.isScoreSet()) {
                String sanitizedLine = "\u00a7" + COLORS[duplicateResolverIndex++] + translatedLine;
                score = this.mainObjective.getScore(XScoreboard.lengthCheckedLine(sanitizedLine));
            }
            score.setScore(linePosition--);
        }
        return this;
    }

    private static String lengthCheckedLine(String str) {
        return !XMaterial.supports(13) && str.length() > 40 ? str.substring(0, 40) : str;
    }

    public String toString() {
        return "XScoreboard[" + this.mainObjective.getName() + ", " + this.mainObjective.getDisplayName() + ", lines=" + this.lines.size() + ']';
    }
}

