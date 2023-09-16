
package fr.krishenk.castel.libs.snakeyaml.exceptions;

import fr.krishenk.castel.libs.snakeyaml.common.CharConstants;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public final class Mark
implements Serializable {
    private final String name;
    private final int index;
    private final int line;
    private final int column;
    private final int pointer;
    private final int[] buffer;

    private static int[] toCodePoints(char[] str) {
        int[] codePoints = new int[Character.codePointCount(str, 0, str.length)];
        int i = 0;
        int c = 0;
        while (i < str.length) {
            int cp;
            codePoints[c] = cp = Character.codePointAt(str, i);
            i += Character.charCount(cp);
            ++c;
        }
        return codePoints;
    }

    public Mark(String name, int index, int line, int column, int[] buffer, int pointer) {
        this.name = name;
        this.index = index;
        this.line = line;
        this.column = column;
        this.buffer = buffer;
        this.pointer = pointer;
    }

    public Mark(String name, int index, int line, int column, char[] str, int pointer) {
        this(name, index, line, column, Mark.toCodePoints(str), pointer);
    }

    private static boolean isLineBreak(int c) {
        return CharConstants.NULL_OR_LINEBR.has(c);
    }

    public String createSnippet(int indent, int maxLength, String pointerPrefix, List<Integer> pointerIndices) {
        int i;
        float half = (float)maxLength / 2.0f - 1.0f;
        int start = this.pointer;
        String head = "";
        while (start > 0 && !Mark.isLineBreak(this.buffer[start - 1])) {
            if (!((float)(this.pointer - --start) > half)) continue;
            head = " ... ";
            start += 5;
            break;
        }
        String tail = "";
        int end = this.pointer;
        while (end < this.buffer.length && !Mark.isLineBreak(this.buffer[end])) {
            if (!((float)(++end - this.pointer) > half)) continue;
            tail = " ... ";
            end -= 5;
            break;
        }
        StringBuilder result = new StringBuilder();
        for (i = 0; i < indent; ++i) {
            result.append(' ');
        }
        result.append(head);
        for (i = start; i < end; ++i) {
            result.appendCodePoint(this.buffer[i]);
        }
        result.append(tail);
        if (pointerIndices != null) {
            pointerIndices.add(this.pointer);
            result.append('\n');
            int until = indent + this.pointer - start + head.length();
            for (int i2 = 0; i2 < until; ++i2) {
                result.append(' ');
            }
            result.append(pointerPrefix);
            for (Integer pointerIndex : pointerIndices) {
                result.append('^');
            }
        }
        return result.toString();
    }

    public String createSnippet() {
        return this.createSnippet("");
    }

    public String createSnippet(String pointerPrefix) {
        return this.createSnippet(4, 75, pointerPrefix, new ArrayList<Integer>());
    }

    public String toString() {
        return " in " + this.name + ", line " + (this.line + 1) + ", column " + (this.column + 1) + ":\n" + this.createSnippet();
    }

    public String getName() {
        return this.name;
    }

    public int getLine() {
        return this.line;
    }

    public int getColumn() {
        return this.column;
    }

    public int getIndex() {
        return this.index;
    }

    public int[] getBuffer() {
        return this.buffer;
    }

    public int getPointer() {
        return this.pointer;
    }
}

