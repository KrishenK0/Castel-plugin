package fr.krishenk.castel.locale.compiler;

public class MessageTokenResult {
    public final int index;
    protected final MessagePiece piece;

    public MessageTokenResult(int index, MessagePiece piece) {
        this.index = index;
        this.piece = piece;
    }
}
