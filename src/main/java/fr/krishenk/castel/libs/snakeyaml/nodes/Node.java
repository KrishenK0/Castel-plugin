
package fr.krishenk.castel.libs.snakeyaml.nodes;

import fr.krishenk.castel.libs.snakeyaml.comments.CommentLine;
import fr.krishenk.castel.libs.snakeyaml.comments.CommentType;
import fr.krishenk.castel.libs.snakeyaml.common.Anchor;
import fr.krishenk.castel.libs.snakeyaml.exceptions.Mark;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public abstract class Node
implements Cloneable {
    private Tag tag;
    private final Mark startMark;
    protected Mark endMark;
    private Anchor anchor;
    private List<CommentLine> inLineComments;
    private List<CommentLine> blockComments;

    public Node(Tag tag, Mark startMark, Mark endMark) {
        this.setTag(tag);
        this.startMark = startMark;
        this.endMark = endMark;
    }

    public Tag getTag() {
        return this.tag;
    }

    public Mark getEndMark() {
        return this.endMark;
    }

    public Mark getWholeMark() {
        int[] firstBuffer = this.startMark.getBuffer();
        int[] secondBuffer = this.endMark.getBuffer();
        int[] combinedBuffer = new int[firstBuffer.length + secondBuffer.length];
        System.arraycopy(firstBuffer, 0, combinedBuffer, 0, firstBuffer.length);
        System.arraycopy(secondBuffer, 0, combinedBuffer, firstBuffer.length, secondBuffer.length);
        return new Mark(this.startMark.getName(), this.startMark.getIndex(), this.startMark.getLine(), this.startMark.getColumn(), combinedBuffer, this.startMark.getPointer());
    }

    public abstract NodeType getNodeType();

    public Mark getStartMark() {
        return this.startMark;
    }

    public void setTag(Tag tag) {
        this.tag = Objects.requireNonNull(tag, "Tag in a Node is required.");
    }

    public abstract void cacheConstructed(Object var1);

    public abstract Object getParsed();

    public final boolean equals(Object obj) {
        return super.equals(obj);
    }

    public final int hashCode() {
        return super.hashCode();
    }

    public Anchor getAnchor() {
        return this.anchor;
    }

    public void setAnchor(Anchor anchor) {
        this.anchor = anchor;
    }

    public List<CommentLine> getInLineComments() {
        return this.inLineComments;
    }

    public void setInLineComments(List<CommentLine> inLineComments) {
        this.inLineComments = inLineComments;
    }

    public List<CommentLine> getBlockComments() {
        return this.blockComments;
    }

    public void setBlockComments(List<CommentLine> blockComments) {
        this.blockComments = blockComments;
    }

    public void setSimpleComments(String[] comments) {
        if (comments == null || comments.length == 0) {
            return;
        }
        this.blockComments = Arrays.stream(comments).map(x -> new CommentLine(null, null, x.isEmpty() ? "" : ' ' + x, x.isEmpty() ? CommentType.BLANK_LINE : CommentType.BLOCK)).collect(Collectors.toList());
    }

    public abstract Node clone();

    protected Node copyPropertiesOf(Node other) {
        this.anchor = other.anchor;
        if (other.inLineComments != null) {
            this.inLineComments = new ArrayList<CommentLine>(other.inLineComments);
        }
        if (other.blockComments != null) {
            this.blockComments = new ArrayList<CommentLine>(other.blockComments);
        }
        return this;
    }
}

