package fr.krishenk.castel.locale.provider;

import fr.krishenk.castel.CastelPlugin;
import fr.krishenk.castel.lang.Config;
import fr.krishenk.castel.libs.xseries.XSound;
import fr.krishenk.castel.libs.xseries.messages.ActionBar;
import fr.krishenk.castel.libs.xseries.messages.Titles;
import fr.krishenk.castel.locale.compiler.MessageCompiler;
import fr.krishenk.castel.locale.compiler.MessageObject;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.util.Objects;

public final class AdvancedMessageProvider extends MessageProvider {
    private final MessageObject actionbar;
    private final Titles titles;
    private XSound.Record sound;

    public AdvancedMessageProvider(MessageObject obj, MessageObject actionbar, Titles titles) {
        super(obj);
        this.actionbar = actionbar;
        this.titles = titles;
    }

    public AdvancedMessageProvider(ConfigurationSection accessor) {
        super(AdvancedMessageProvider.compileMain(accessor));
        String actionbar = accessor.getString("actionbar");
        this.actionbar = actionbar == null ? null : MessageCompiler.compile(actionbar);
        String sound = accessor.getString("sound");
        this.sound = sound == null ? null : XSound.parse(sound);
        ConfigurationSection titleSection = accessor.getConfigurationSection("titles");
        this.titles = titleSection != null ? Titles.parseTitle(titleSection) : null;
    }

    static MessageObject compileMain(ConfigurationSection accessor) {
        Objects.requireNonNull(accessor, "Config accessor is null");
        String msg = accessor.getString("message");
        return msg == null ? null : MessageCompiler.compile(msg);
    }

    public AdvancedMessageProvider withSound(XSound.Record sound) {
        this.sound = sound;
        return this;
    }

    public AdvancedMessageProvider err() {
        this.sound = XSound.parse(Config.ERROR_SOUND.getString());
        return this;
    }

    @Override
    public void handleExtraServices(CommandSender receiver, MessageBuilder builder) {
        if (receiver instanceof Player) {
            Player player = (Player)receiver;
            if (this.sound != null) {
                this.sound.forPlayer(player).play();
            }
            if (this.actionbar != null) {
                ActionBar.sendActionBar((Plugin) CastelPlugin.getInstance(), player, this.actionbar.buildPlain(builder));
            }
            if (this.titles != null) {
                Titles titles = this.titles.clone();
                if (titles.getTitle() != null) {
                    titles.setTitle(MessageCompiler.compile(titles.getTitle()).buildPlain(builder));
                }
                if (titles.getSubtitle() != null) {
                    titles.setSubtitle(MessageCompiler.compile(titles.getSubtitle()).buildPlain(builder));
                }
                titles.send(player);
            }
        }
    }
}

