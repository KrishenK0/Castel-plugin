package fr.krishenk.castel.locale.compiler;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ColorAccessor {
    private final MessagePiece[] pieces;
    private final List<MessagePiece> colors = new ArrayList<MessagePiece>(3);
    private final List<MessagePiece> tempColors = new ArrayList<MessagePiece>(3);
    private int colorIndex;
    private final boolean backwards;
    private MessagePiece piece;

    ColorAccessor(MessagePiece[] pieces, int colorIndex, boolean backwards) {
        this.pieces = pieces;
        this.colorIndex = colorIndex;
        this.backwards = backwards;
    }

    void compile() {
        int i;
        int n = i = this.backwards ? this.pieces.length - 1 : 0;
        while (this.backwards ? i >= 0 : i < this.pieces.length) {
            this.piece = this.pieces[i];
            if (this.piece instanceof MessagePiece.Color) {
                if (this.piece instanceof MessagePiece.HexColor) {
                    if (this.backwards ? this.backwardsSolidColorHandler() : this.forwardsSolidColorHandler()) {
                        break;
                    }
                } else if (this.piece instanceof MessagePiece.SimpleColor) {
                    MessagePiece.SimpleColor simpleColor = (MessagePiece.SimpleColor)this.piece;
                    if (simpleColor.getColor().isColor()) {
                        if (this.backwards ? this.backwardsSolidColorHandler() : this.forwardsSolidColorHandler()) {
                            break;
                        }
                    } else {
                        this.tempColors.add(this.piece);
                    }
                }
            }
            i += this.backwards ? -1 : 1;
        }
        if (!this.backwards) {
            this.colors.addAll(this.tempColors);
        }
        if (this.backwards) {
            Collections.reverse(this.colors);
        }
    }

    boolean forwardsSolidColorHandler() {
        if (--this.colorIndex < 0) {
            return true;
        }
        this.colors.clear();
        this.tempColors.clear();
        this.colors.add(this.piece);
        return false;
    }

    boolean backwardsSolidColorHandler() {
        this.colors.clear();
        if (!this.tempColors.isEmpty()) {
            this.colors.addAll(this.tempColors);
            this.tempColors.clear();
        }
        this.colors.add(this.piece);
        return --this.colorIndex <= 0;
    }

    public static List<MessagePiece> of(MessagePiece[] pieces, int colorIndex, boolean backwards) {
        ColorAccessor accessor = new ColorAccessor(pieces, colorIndex, backwards);
        accessor.compile();
        return accessor.colors;
    }
}


