package fr.krishenk.castel.utils;

import fr.krishenk.castel.locale.compiler.MessageCompiler;
import fr.krishenk.castel.locale.compiler.MessageObject;
import fr.krishenk.castel.locale.compiler.MessagePiece;
import fr.krishenk.castel.locale.provider.MessageBuilder;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class ItemUtil {
    public ItemUtil() {}

    public static void translate(ItemMeta meta, MessageBuilder settings) {
        if (meta.hasDisplayName()) {
            MessageObject itemTile = MessageCompiler.compile(meta.getDisplayName());
            meta.setDisplayName(itemTile.buildPlain(settings));
        }

        if (meta.hasLore()) {
            List<String> lores = meta.getLore();
            List<String> newLores = new ArrayList<>();
            MessageObject lastColor = null;
            for (String lore : lores) {
                MessageObject loreLine = MessageCompiler.compile(lore);
                MessageObject newLastColors = loreLine.findLastColors();
                if (lastColor != null) loreLine = lastColor.merge(loreLine);
                if (newLastColors != null) lastColor = newLastColors;

                List<MessageObject> newLines = loreLine.splitBy(piece -> piece instanceof MessagePiece.NewLine);

                for (MessageObject line : newLines) {
                    newLores.add(line.buildPlain(settings));
                }
            }

            meta.setLore(newLores);
        }
    }
}
