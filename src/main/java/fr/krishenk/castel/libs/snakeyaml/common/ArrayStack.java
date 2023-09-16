
package fr.krishenk.castel.libs.snakeyaml.common;

import java.util.ArrayList;

public final class ArrayStack<T> {
    private final ArrayList<T> stack;

    public ArrayStack(int initSize) {
        this.stack = new ArrayList(initSize);
    }

    public void push(T obj) {
        this.stack.add(obj);
    }

    public T pop() {
        return this.stack.remove(this.stack.size() - 1);
    }

    public boolean isEmpty() {
        return this.stack.isEmpty();
    }
}

