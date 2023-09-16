package fr.krishenk.castel.constants.player;

import fr.krishenk.castel.config.CastelConfig;
import fr.krishenk.castel.config.KeyedConfigAccessor;
import fr.krishenk.castel.config.implementation.KeyedYamlConfigAccessor;
import fr.krishenk.castel.config.implementation.YamlConfigAccessor;
import fr.krishenk.castel.data.Pair;
import fr.krishenk.castel.locale.MessageObjectBuilder;
import fr.krishenk.castel.locale.compiler.MessageCompiler;
import fr.krishenk.castel.locale.compiler.MessageObject;
import fr.krishenk.castel.locale.compiler.builders.LanguageEntryWithContext;
import fr.krishenk.castel.locale.compiler.container.MessageContainer;
import fr.krishenk.castel.locale.compiler.placeholders.PlaceholderContextBuilder;
import fr.krishenk.castel.locale.messenger.LanguageEntryMessenger;
import fr.krishenk.castel.locale.messenger.Messenger;
import fr.krishenk.castel.locale.provider.MessageBuilder;
import fr.krishenk.castel.utils.ConditionProcessor;
import fr.krishenk.castel.utils.compilers.ConditionalCompiler;
import fr.krishenk.castel.utils.config.ConfigSection;
import org.bukkit.entity.Player;

import java.util.*;

public class CastelChatChannel {
    private static final Map<String, CastelChatChannel> CHANNELS = new HashMap<String, CastelChatChannel>(5);
    private final String id;
    private final String dataId;
    private final MessageObject color;
    private final ConditionalCompiler.LogicalOperand recipientCondition;
    private final Collection<Pair<ConditionalCompiler.LogicalOperand, Messenger>> useConditions;
    private final MessageContainer formats;
    private final List<Pair<ConditionalCompiler.LogicalOperand, String>> adminFormats;
    public static final String GLOBAL = "global";
    public static final String GLOBAL_DATA_ID = "GLOBAL";
    public static final String NATION = "nation";

    public static void registerChannels() {
        CHANNELS.clear();
        ConfigSection section = CastelConfig.Chat.CHANNELS.getManager().getSection().getSection();
        for (Map.Entry<String, ConfigSection> channel : section.getSections().entrySet()) {
            String id = channel.getKey();
            KeyedConfigAccessor channelSection = CastelConfig.Chat.CHANNELS.getManager().withProperty(id).applyProperties();
            MessageObject color = MessageCompiler.compile(CastelConfig.Chat.CHANNELS_COLOR.getManager().withOption("channel", id).getString());
            String recCond = CastelConfig.Chat.CHANNELS_RECIPIENTS_CONDITION.getManager().withOption("channel", id).getString();
            ConditionalCompiler.LogicalOperand recipientCondition = recCond == null ? null : ConditionalCompiler.compile(recCond).evaluate();
            KeyedYamlConfigAccessor useConditionsOption = CastelConfig.Chat.CHANNELS_USE_CONDITIONS.getManager().withOption("channel", id);
            YamlConfigAccessor sectionCond = useConditionsOption.getSection();
            Collection<Pair<ConditionalCompiler.LogicalOperand, Messenger>> useConditions = null;
            if (sectionCond != null) {
                useConditions = ConditionProcessor.translatableConditions(sectionCond.getSection());
            } else {
                String singleCondition = useConditionsOption.getString();
                if (singleCondition != null) {
                    useConditions = Collections.singletonList(Pair.of(ConditionalCompiler.compile(singleCondition).evaluate(), new LanguageEntryMessenger("channels", "default-permission-fail")));
                }
            }
//            CastelChatChannel chan = new CastelChatChannel(id, color, recipientCondition, useConditions, MessageContainer.parse(channelSection.withProperty("formats")), MessageContainer.parseRaw(channelSection.clearExtras().withProperty("admin-formats")));
//            CHANNELS.put(chan.id, chan);
//            CHANNELS.put(chan.dataId, chan);
        }
    }

    public static CastelChatChannel getChannelUserFriendly(String name, MessageBuilder settings) {
        name = name.toLowerCase();
        for (CastelChatChannel chan : CHANNELS.values()) {
            if (name.equals(chan.id.toLowerCase())) {
                return chan;
            }
            if (name.equals(chan.getName().buildPlain(settings).toLowerCase())) {
                return chan;
            }
            if (!name.equals(chan.getShortName().buildPlain(settings).toLowerCase())) continue;
            return chan;
        }
        return null;
    }

    public static Map<String, CastelChatChannel> getChannels() {
        return CHANNELS;
    }

    public CastelChatChannel(String id, MessageObject color, ConditionalCompiler.LogicalOperand recipientCondition, Collection<Pair<ConditionalCompiler.LogicalOperand, Messenger>> useConditions, MessageContainer formats, List<Pair<ConditionalCompiler.LogicalOperand, String>> adminFormats) {
        this.id = id;
        this.dataId = id.toUpperCase(Locale.ENGLISH);
        this.color = color;
        this.recipientCondition = recipientCondition;
        this.useConditions = useConditions;
        this.formats = formats;
        this.adminFormats = adminFormats;
    }

    public static CastelChatChannel fromId(String id) {
        return CHANNELS.get(id);
    }

    public static CastelChatChannel getGlobalChannel() {
        return CastelChatChannel.fromId(GLOBAL);
    }

    public List<Pair<ConditionalCompiler.LogicalOperand, String>> getAdminFormat() {
        return this.adminFormats;
    }

    private MessageObjectBuilder getMessageObject(String entry) {
        return new LanguageEntryWithContext("channels", this.id, entry);
    }

    public MessageObjectBuilder getName() {
        return this.getMessageObject("name");
    }

    public MessageObjectBuilder getShortName() {
        return this.getMessageObject("short-name");
    }

    public MessageContainer getFormats() {
        return this.formats;
    }

    public MessageObject getColor() {
        return this.color;
    }

    public String getBypassPrefix() {
        return this.getOption(CastelConfig.Chat.CHANNELS_RANGED_BYPASS_PREFIX).getString();
    }

    public ConditionalCompiler.LogicalOperand getRecipientCondition() {
        return this.recipientCondition;
    }

    public Collection<Pair<ConditionalCompiler.LogicalOperand, Messenger>> getUseConditions() {
        return this.useConditions;
    }

    public Messenger canUse(Player player) {
        if (this.useConditions == null) {
            return null;
        }
        PlaceholderContextBuilder settings = new PlaceholderContextBuilder().withContext(player);
        for (Pair<ConditionalCompiler.LogicalOperand, Messenger> useCondition : this.useConditions) {
            if (ConditionProcessor.process(useCondition.getKey(), settings)) continue;
            return useCondition.getValue();
        }
        return null;
    }

    public boolean isGlobal() {
        return this.id.equals(GLOBAL);
    }

    private KeyedYamlConfigAccessor getOption(CastelConfig.Chat cfg) {
        return cfg.getManager().withOption("channel", this.id);
    }

    public String getId() {
        return this.id;
    }

    public String getDataId() {
        return this.dataId;
    }

    public String toString() {
        return "CastelChatChannel{" + this.id + '}';
    }
}

