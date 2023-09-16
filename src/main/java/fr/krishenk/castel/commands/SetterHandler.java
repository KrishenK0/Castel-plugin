package fr.krishenk.castel.commands;

import com.google.common.base.Strings;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

public class SetterHandler {
    private static final List<String> SUGGESTIONS = Arrays.asList("add", "decrease", "set");

    public static SetterResult eval(String action, double data, String amountStr) {
        double amount;
        try {
            amount = Double.parseDouble(amountStr);
        } catch (NumberFormatException e) {
            return SetterResult.NOT_NUMBER;
        }

        return eval(action, data, amount);
    }

    public static SetterResult eval(String action, double data, double amount) {
        switch (action.toLowerCase(Locale.ENGLISH)) {
            case "add":
            case "increase":
                return SetterResult.ADDITION.withValue(data + amount);
            case "remove":
            case "decrease":
                return SetterResult.SUBSTRACTION.withValue(data - amount);
            case "set":
            case "setraw":
                return SetterResult.SET.withValue(amount);
            default:
                return SetterResult.UNKNOWN;
        }
    }

    public static List<String> tabComplete(String starts) {
        return Strings.isNullOrEmpty(starts) ? SUGGESTIONS : SUGGESTIONS.stream().filter(x -> x.startsWith(starts.toLowerCase())).collect(Collectors.toList());
    }

    public enum SetterResult {
        ADDITION,
        SUBSTRACTION,
        SET,
        UNKNOWN,
        NOT_NUMBER;
        private double value;

        public SetterResult withValue(double value) {
            this.value = value;
            return this;
        }

        public double getValue() {
            return value;
        }
    }
}
