package fr.krishenk.castel.locale.compiler;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

public class MessageCompilerException extends RuntimeException {
    private final String target;
    private final String problem;
    private final int index;

    public MessageCompilerException(String target, String problem, int index, String message) {
        super(message);
        this.target = target;
        this.problem = problem;
        this.index = index;
    }

    public String getTarget() {
        return this.target;
    }

    public int getIndex() {
        return this.index;
    }

    public String getProblem() {
        return this.problem;
    }

    protected static String spaces(int times) {
        char[] spaces = new char[times];
        Arrays.fill(spaces, ' ');
        return new String(spaces);
    }

    protected static Collection<Integer> pointerToName(int from, String name) {
        if (name == null) {
            return new ArrayList<Integer>();
        }
        ArrayList<Integer> pointers = new ArrayList<Integer>(name.length());
        for (int i = 1; i < name.length(); ++i) {
            pointers.add(from + i);
        }
        return pointers;
    }
}

