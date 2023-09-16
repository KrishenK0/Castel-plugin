package fr.krishenk.castel.commands.general.misc;

import fr.krishenk.castel.commands.CastelCommand;
import fr.krishenk.castel.commands.CommandContext;
import fr.krishenk.castel.libs.xseries.XSound;
import fr.krishenk.castel.locale.compiler.MessageCompiler;
import fr.krishenk.castel.locale.provider.MessageBuilder;
import fr.krishenk.castel.utils.internal.ProxyBytecodeManipulator;

public class CommandAbout extends CastelCommand {
    public CommandAbout() {
        super("about", true);
    }

    @Override
    public void execute(CommandContext context) {
        MessageCompiler.compile("\n&8-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-\n\n            &c♚ &eCastel &c♚\n&7| &2%description%\n&7| &2Developer&8: &9KrishenK\n&7| &2Fork of&8: hover:{&2KingdomsX;&6Click to open link;@https://www.spigotmc.org/resources/77670/}\n&7| &2Version&8: &9%version%\n\n&8-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-\n").getExtraProvider().withSound(XSound.parse("BLOCK_NOTE_BLOCK_PLING")).send(context.getSender(), (new MessageBuilder()).raws("description", plugin.getDescription().getDescription(), "version", plugin.getDescription().getVersion()));
    }

    static {
        ProxyBytecodeManipulator.$($.class, "RESOURCE", "NONCE", "USER");
    }

    private static final class $ {
        private static final CharSequence RESOURCE = "%__RESOURCE__%";
        private static final CharSequence NONCE = "%__NONCE__%";
        private static final CharSequence USER = "%__USER__%";
    }
}
