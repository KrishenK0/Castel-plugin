package fr.krishenk.castel.utils.string;

import java.util.Objects;

public class QuantumString implements Cloneable {
    private final String original;
    private final String quantumValue;

    public QuantumString(String original, boolean quantum) {
        Objects.requireNonNull(original, "Quantum original string cannot be null");
        this.original = quantum ? original : null;
        this.quantumValue = quantum ? original.toLowerCase() : original;
    }

    public QuantumString(String value, String quantumValue) {
        this.original = value;
        this.quantumValue = Objects.requireNonNull(quantumValue, "Quantum value of string cannot be null");
    }

    public int length() {
        return this.quantumValue.length();
    }

    public boolean isEmpty() {
        return this.quantumValue.isEmpty();
    }

    public static QuantumString empty() {
        return new QuantumString("", false);
    }

    public boolean isQuantum() {
        return this.original != null;
    }

    @Override
    public int hashCode() {
        return this.quantumValue.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof QuantumString && this.quantumValue.equals(((QuantumString)obj).quantumValue);
    }

    @Override
    public String toString() {
        return "QuantumString:[quantum="+this.isQuantum() + ", original=" +this.original+", quantumValue="+this.quantumValue+"]";
    }

    @Override
    protected Object clone() {
        try {
            return super.clone();
        } catch (CloneNotSupportedException e) {
            throw new AssertionError();
        }
    }

    public String getOriginal() {
        return original;
    }

    public String getQuantumValue() {
        return quantumValue;
    }

    public String getQuantum() {
        return this.isQuantum() ? this.original : this.quantumValue;
    }
}
