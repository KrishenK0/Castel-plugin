package fr.krishenk.castel.utils.compilers;

import fr.krishenk.castel.utils.MathUtils;

import java.util.*;
import java.util.function.Function;
import java.util.function.Supplier;

public class ConditionalCompiler {
    private final String expression;
    private final int end;
    private int offset;
    private final LinkedList<LogicalOperand> operands = new LinkedList();
    private final LinkedList<LogicalOperator> operators = new LinkedList();
    int lastOperandIndex = -1;
    int lastOpIndex = -1;
    int lastLastOpIndex = 0;

    public ConditionalCompiler(String expression, int offset, int end) {
        this.expression = offset != 0 ? expression : expression.replace('\n', ' ').replace('\r', ' ');
        this.offset = offset;
        this.end = end;
    }

    public static ConditionalCompiler compile(String expr) {
        return new ConditionalCompiler(expr, 0, expr.length());
    }

    private static OperandInformation operandProperties(String str) {
        int len = str.length();
        boolean variable = true;
        int startedSpaces = 0;
        if (str.charAt(0) == '\'') {
            for (int i = 1; i < len; ++i) {
                char ch = str.charAt(i);
                if (ch != '\'') continue;
                startedSpaces = i + 1;
                break;
            }
            return new OperandInformation(OperandType.STRING, startedSpaces);
        }
        for (int i = 0; i < len; ++i) {
            char ch = str.charAt(i);
            if (ch >= 'A' && ch <= 'Z' || ch >= 'a' && ch <= 'z' || ch == '_') {
                if (startedSpaces == 0) continue;
                variable = false;
                continue;
            }
            if (ch == ' ') {
                if (startedSpaces != 0) continue;
                startedSpaces = i;
                continue;
            }
            variable = false;
            startedSpaces = 0;
        }
        return new OperandInformation(variable ? OperandType.VARIABLE : OperandType.ARITHMETIC, startedSpaces);
    }

    private void handleOperand(boolean finalize) {
        LogicalOperand operand;
        if (this.lastOperandIndex == -1) {
            return;
        }
        String full = this.expression.substring(this.lastOperandIndex, this.offset);
        OperandInformation info = ConditionalCompiler.operandProperties(full);
        if (info.spacesFrom != 0 && info.type != OperandType.ARITHMETIC) {
            full = full.substring(0, Math.abs(info.spacesFrom));
        }
        block0 : switch (info.type) {
            case VARIABLE: {
                switch (full) {
                    case "true":
                    case "else": {
                        operand = ConstantLogicalOperand.TRUE;
                        break block0;
                    }
                    case "false": {
                        operand = ConstantLogicalOperand.FALSE;
                        break block0;
                    }
                    case "null":
                    case "nil": {
                        throw this.exception(this.lastOperandIndex, "Cannot use reserved logical boolean value '" + full + '\'', full);
                    }
                }
                operand = new LogicalVariableOperand(full);
                break;
            }
            case ARITHMETIC: {
                MathCompiler.Expression arithExpr = MathCompiler.compile(full);
                operand = new ArithmeticOperand(arithExpr);
                break;
            }
            case STRING: {
                operand = new StringOperand(full.substring(1, full.length() - 1));
                break;
            }
            default: {
                throw new AssertionError();
            }
        }
        String finalFull = full;
        this.finalizeOperand(operand, finalize, () -> finalFull);
    }

    private void finalizeOperand(LogicalOperand operand, boolean finalize, Supplier<String> expr) {
        LogicalOperator lastOperator = this.operators.peekLast();
        if (lastOperator != null && lastOperator.unary) {
            if (!lastOperator.acceptsOperandOfType(operand)) {
                throw this.exception(this.lastOperandIndex, "Right hand side of '" + lastOperator.symbol + "' unary operator must be " + lastOperator.acceptedOperands[0] + " expression", expr.get());
            }
            operand = new UnaryLogicalOperator(lastOperator, operand);
            this.operators.removeLast();
        }
        this.operands.addLast(operand);
        if (finalize || this.operators.size() == 2 && this.operands.size() == 3) {
            this.handleOperations(finalize, expr);
        }
    }

    private void checkBlanks() {
        if (this.operands.isEmpty()) {
            throw this.exception(0, "Blank expression");
        }
        if (!this.operators.isEmpty()) {
            LogicalOperator op = this.operators.getLast();
            throw this.exception(this.lastLastOpIndex, "Blank operand on right hand side of '" + op.symbol + "' binary operator", op.symbol);
        }
    }

    private BiLogicalOperator createOperator(LogicalOperand lhs, LogicalOperator op, LogicalOperand rhs) {
        if (!op.acceptsOperandOfType(lhs)) {
            throw this.exception(this.lastLastOpIndex, "Left hand side of '" + op.symbol + "' operator must be " + op.acceptedOperands[0] + " expression", op.symbol);
        }
        if (!op.acceptsOperandOfType(rhs)) {
            throw this.exception(this.lastOperandIndex, "Right hand side of '" + op.symbol + "' operator must be " + op.acceptedOperands[0] + " expression", op.symbol);
        }
        return new BiLogicalOperator(lhs, op, rhs);
    }

    public void handleOperations(boolean finalize, Supplier<String> subExpr) {
        if (this.operands.size() < 2) {
            return;
        }
        if (this.operators.size() == 2) {
            LogicalOperand firstOperand = this.operands.pollFirst();
            LogicalOperand secondOperand = this.operands.pollFirst();
            LogicalOperand thirdOperand = this.operands.pollFirst();
            LogicalOperator firstOp = this.operators.getFirst();
            LogicalOperator secondOp = this.operators.peekLast();
            if (thirdOperand == null) {
                throw this.exception(this.lastOperandIndex, "Right hand side empty");
            }
            if (firstOp.isComparator() && secondOp.isComparator()) {
                BiLogicalOperator firstOperation = this.createOperator(firstOperand, firstOp, secondOperand);
                BiLogicalOperator secondOperation = this.createOperator(secondOperand, firstOp, thirdOperand);
                BiLogicalOperator intermediateOperation = this.createOperator(firstOperation, LogicalOperator.AND, secondOperation);
                this.operands.add(intermediateOperation);
                this.operators.clear();
                return;
            }
            if (firstOp.hasPrecedenceOver(secondOp)) {
                BiLogicalOperator operation = this.createOperator(firstOperand, firstOp, secondOperand);
                this.operands.add(operation);
                this.operands.add(thirdOperand);
                this.operators.removeFirst();
            } else {
                BiLogicalOperator operation = this.createOperator(secondOperand, secondOp, thirdOperand);
                this.operands.add(firstOperand);
                this.operands.add(operation);
                this.operators.removeLast();
            }
            if (!finalize) {
                return;
            }
        }
        this.operands.add(this.createOperator(this.operands.pollFirst(), this.operators.pollLast(), this.operands.pollLast()));
    }

    public void handleOp() {
        if (this.lastOpIndex == -1) {
            return;
        }
        LogicalOperator currentOp = this.getOperator(this.lastOpIndex);
        LogicalOperator lastOp = this.operators.peekLast();
        if (this.operands.isEmpty() && !currentOp.unary) {
            throw this.exception(this.lastLastOpIndex, "Blank operand on left hand side of '" + currentOp.symbol + "' operator", currentOp.symbol);
        }
        if (!this.operators.isEmpty()) {
            if (!currentOp.unary && this.operands.size() < 2) {
                throw this.exception(this.lastLastOpIndex - lastOp.symbolSize(), "Blank operand on right side of '" + lastOp.symbol + "' binary operator.", lastOp.symbol);
            }
            if (lastOp.unary) {
                throw this.exception(this.lastOperandIndex, "Unary operator '" + lastOp.symbol + "' was followed by another operator " + currentOp.symbol, currentOp.symbol);
            }
        }
        this.operators.addLast(currentOp);
    }

    private static Collection<Integer> pointerToName(int from, String name) {
        ArrayList<Integer> pointers = new ArrayList<Integer>(name.length());
        for (int i = 1; i < name.length(); ++i) {
            pointers.add(from + i);
        }
        return pointers;
    }

    public LogicalOperand evaluate() {
        while (this.offset < this.end) {
            char ch = this.expression.charAt(this.offset);
            if (ConditionalCompiler.isLogicalOperator(ch)) {
                if (this.lastOperandIndex != -1) {
                    this.handleOperand(false);
                    this.lastOperandIndex = -1;
                }
                if (ch != ' ' && this.lastOpIndex == -1) {
                    this.lastOpIndex = this.lastLastOpIndex = this.offset;
                }
            } else {
                this.handleOp();
                if (ch != ' ' && this.lastOperandIndex == -1) {
                    if (ch == '(') {
                        int subExprStart = this.offset + 1;
                        int subExprEnd = this.getSubExpression(subExprStart);
                        this.lastOperandIndex = this.offset++;
                        LogicalOperand subExpr = new ConditionalCompiler(this.expression, subExprStart, subExprEnd).evaluate();
                        this.finalizeOperand(subExpr, false, () -> this.expression.substring(subExprStart - 1, subExprEnd + 1));
                        this.offset = subExprEnd;
                        this.lastOperandIndex = -1;
                    } else {
                        this.lastOperandIndex = this.offset;
                    }
                }
                this.lastOpIndex = -1;
            }
            ++this.offset;
        }
        if (this.lastOperandIndex != -1) {
            this.handleOperand(true);
        } else {
            this.handleOperations(true, () -> this.expression);
        }
        this.handleOp();
        this.checkBlanks();
        return this.operands.getLast();
    }

    private LogicalOperator getOperator(int lastLogical) {
        String hint;
        String op = this.expression.substring(lastLogical, this.offset);
        int len = op.length();
        for (LogicalOperator operator : LogicalOperator.values()) {
            String symbol = operator.symbol;
            if (len != operator.symbolSize() || !symbol.equals(op)) continue;
            return operator;
        }
        if (op.startsWith("!!")) {
            hint = " (hint: Redundant multiple negation operators are not allowed)";
        } else if (op.startsWith("=>")) {
            hint = " (hint: Did you mean '>=' operator?)";
        } else if (op.startsWith("=<")) {
            hint = " (hint: Did you mean '<=' operator?)";
        } else {
            Optional<LogicalOperator> merged = Arrays.stream(LogicalOperator.values()).filter(x -> op.contains(x.symbol)).findFirst();
            hint = merged.map(logicalOperator -> " (hint: You have to write '" + logicalOperator.symbol + "' operator separated with a space from other operators)").orElse("");
        }
        throw this.exception(lastLogical, "Unrecognized logical operator '" + op + '\'' + hint, op);
    }

    private int getSubExpression(int start) {
        int params = 1;
        int len = this.expression.length();
        for (int i = start; i < len; ++i) {
            char ch = this.expression.charAt(i);
            if (ch == '(') {
                ++params;
                continue;
            }
            if (ch != ')' || --params != 0) continue;
            return i;
        }
        throw this.exception(start - 1, "Unclosed subexpression");
    }

    private static String spaces(int times) {
        char[] spaces = new char[times];
        Arrays.fill(spaces, ' ');
        return new String(spaces);
    }

    private LogicalException exception(int at, String message) {
        return this.exception(at, message, new ArrayList<Integer>());
    }

    private LogicalException exception(int at, String message, String name) {
        return this.exception(at, message, ConditionalCompiler.pointerToName(at, name));
    }

    private LogicalException exception(int at, String message, Collection<Integer> pointers) {
        int max = 0;
        pointers.add(at);
        for (Integer pointer : pointers) {
            if (pointer <= max) continue;
            max = pointer;
        }
        StringBuilder pointerStr = new StringBuilder(ConditionalCompiler.spaces(max + 2));
        pointers.forEach(x -> pointerStr.setCharAt(x + 1, '^'));
        return new LogicalException(message + " at offset " + at + " in expression:\n\"" + this.expression + "\"\n" + pointerStr);
    }

    private static boolean isLogicalOperator(char letter) {
        return letter == '<' || letter == '>' || letter == '!' || letter == '=' || letter == '&' || letter == '|';
    }

    private static final class OperandInformation {
        private final OperandType type;
        private final int spacesFrom;

        private OperandInformation(OperandType type, int spacesFrom) {
            this.type = type;
            this.spacesFrom = spacesFrom;
        }
    }

    private enum OperandType {
        VARIABLE,
        ARITHMETIC,
        STRING;
    }

    public static final class ConstantLogicalOperand
            extends LogicalOperand {
        private final boolean constant;
        public static final ConstantLogicalOperand TRUE = new ConstantLogicalOperand(true);
        public static final ConstantLogicalOperand FALSE = new ConstantLogicalOperand(false);

        private ConstantLogicalOperand(boolean constant) {
            this.constant = constant;
        }

        @Override
        public Boolean eval(Function<String, Object> variables) {
            return this.constant;
        }

        public String toString() {
            return String.valueOf(this.constant);
        }
    }

    public static final class LogicalException
            extends RuntimeException {
        public LogicalException(String msg) {
            super(msg);
        }
    }

    public static final class LogicalVariableOperand
            extends LogicalOperand {
        private final String name;

        public LogicalVariableOperand(String name) {
            this.name = name;
        }

        @Override
        public Object eval(Function<String, Object> variables) {
            Object applied = variables.apply(this.name);
            if (applied == null) {
                throw new IllegalArgumentException("Unknown variable: " + this.name);
            }
            return this.parseVariable(this.name, applied);
        }

        public String toString() {
            return '{' + this.name + '}';
        }
    }

    public static final class ArithmeticOperand
            extends LogicalOperand {
        private final MathCompiler.Expression expression;

        public ArithmeticOperand(MathCompiler.Expression expression) {
            this.expression = expression;
        }

        @Override
        public Double eval(Function<String, Object> variables) {
            return this.expression.eval((String x) -> {
                Object result = variables.apply(x);
                if (result == null) {
                    throw new IllegalArgumentException("Unknown variable: " + x);
                }
                result = this.parseVariable(x, result);
                return MathUtils.expectDouble(x, result);
            });
        }

        public String toString() {
            return this.expression.toString();
        }
    }

    public static final class StringOperand
            extends LogicalOperand {
        private final String string;

        public StringOperand(String string) {
            this.string = string;
        }

        @Override
        public String eval(Function<String, Object> variables) {
            return this.string;
        }

        public String toString() {
            return "StringOperand[" + this.string + ']';
        }
    }

    public static abstract class LogicalOperand {
        public abstract Object eval(Function<String, Object> var1);

        public Object parseVariable(String name, Object applied) {
            if (applied instanceof Boolean) {
                return applied;
            }
            if (applied instanceof Number) {
                return ((Number)applied).doubleValue();
            }
            if (applied instanceof String) {
                if (applied.equals("true")) {
                    return true;
                }
                if (applied.equals("false")) {
                    return false;
                }
                try {
                    return Double.parseDouble((String)applied);
                }
                catch (NumberFormatException ex) {
                    return applied;
                }
            }
            throw new IllegalArgumentException("Unknown variable type: '" + applied.getClass() + "' for object '" + applied + "' for variable: '" + name + '\'');
        }
    }

    public static enum LogicalOperator  {
        NOT("!", 1, new AcceptedOperand[]{AcceptedOperand.LOGICAL}, true) {

            @Override
            boolean evaluate(Object left, Object right) {
                return !this.assertBool(right);
            }
        },
        AND("&&", 4, AcceptedOperand.LOGICAL){

            @Override
            boolean evaluate(Object left, Object right) {
                return this.assertBool(left) && this.assertBool(right);
            }
        },
        OR("||", 5, AcceptedOperand.LOGICAL){

            @Override
            boolean evaluate(Object left, Object right) {
                return this.assertBool(left) || this.assertBool(right);
            }
        },
        NOT_EQUALS("!=", 3, AcceptedOperand.ARITHMETIC, AcceptedOperand.LOGICAL){

            @Override
            boolean evaluate(Object left, Object right) {
                try {
                    return !left.equals(right);
                }
                catch (Exception ex) {
                    return left != right;
                }
            }
        },
        EQUALS("==", 3, AcceptedOperand.ARITHMETIC, AcceptedOperand.LOGICAL, AcceptedOperand.STRING){

            @Override
            boolean evaluate(Object left, Object right) {
                try {
                    return left.equals(right);
                }
                catch (Exception ex) {
                    return left == right;
                }
            }
        },
        LESS_THAN_OR_EQUAL("<=", 2, AcceptedOperand.ARITHMETIC, AcceptedOperand.COMPARATOR, AcceptedOperand.STRING){

            @Override
            boolean evaluate(Object left, Object right) {
                return this.assertNumber(left) <= this.assertNumber(right);
            }
        },
        LESS_THAN("<", 2, AcceptedOperand.ARITHMETIC, AcceptedOperand.COMPARATOR){

            @Override
            boolean evaluate(Object left, Object right) {
                return this.assertNumber(left) < this.assertNumber(right);
            }
        },
        GREATER_THAN_OR_EQUAL(">=", 2, AcceptedOperand.ARITHMETIC, AcceptedOperand.COMPARATOR){

            @Override
            boolean evaluate(Object left, Object right) {
                return this.assertNumber(left) >= this.assertNumber(right);
            }
        },
        GREATER_THAN(">", 2, AcceptedOperand.ARITHMETIC, AcceptedOperand.COMPARATOR){

            @Override
            boolean evaluate(Object left, Object right) {
                return this.assertNumber(left) > this.assertNumber(right);
            }
        };
        private final String symbol;
        private final boolean unary;
        private final byte priority;
        private final AcceptedOperand[] acceptedOperands;

        LogicalOperator(String symbol, int priority, AcceptedOperand ... acceptedOperands) {
            this(symbol, priority, acceptedOperands, false);
        }

        public boolean isComparator() {
            return Arrays.stream(this.acceptedOperands).anyMatch(x -> x == AcceptedOperand.COMPARATOR);
        }

        LogicalOperator(String symbol, int priority, AcceptedOperand[] acceptedOperands, boolean unary) {
            if (acceptedOperands.length == 0 || acceptedOperands.length > 3) {
                throw new AssertionError("Invalid list of accepted operands: " + Arrays.toString(acceptedOperands) + " for operator " + symbol);
            }
            this.symbol = symbol;
            this.priority = (byte)priority;
            this.acceptedOperands = acceptedOperands;
            this.unary = unary;
        }

        public boolean hasPrecedenceOver(LogicalOperator other) {
            return this.priority <= other.priority;
        }

        public boolean acceptsOperandOfType(LogicalOperand operand) {
            if (operand instanceof LogicalVariableOperand) {
                return true;
            }
            AcceptedOperand operandType = operand instanceof ArithmeticOperand ? AcceptedOperand.ARITHMETIC : AcceptedOperand.LOGICAL;
            return Arrays.stream(this.acceptedOperands).anyMatch(x -> x == operandType);
        }

        abstract boolean evaluate(Object var1, Object var2);

        public int symbolSize() {
            return this.symbol.length();
        }

        public Double assertNumber(Object obj) {
            if (obj instanceof Double) {
                return (Double)obj;
            }
            throw new IllegalArgumentException("Operands of '" + this.symbol + "' operator must be numbers instead got: '" + obj + "' (" + (obj == null ? null : obj.getClass().getName() + ')'));
        }

        public boolean assertBool(Object obj) {
            if (obj instanceof Boolean) {
                return (Boolean)obj;
            }
            throw new IllegalArgumentException("Operands of '" + this.symbol + "' operator must be booleans instead got: '" + obj + "' (" + (obj == null ? null : obj.getClass().getName() + ')'));
        }

        public enum AcceptedOperand {
            ARITHMETIC,
            LOGICAL,
            COMPARATOR,
            STRING;
        }
    }

    public static final class UnaryLogicalOperator
            extends LogicalOperand {
        private final LogicalOperator op;
        private final LogicalOperand operand;

        public UnaryLogicalOperator(LogicalOperator op, LogicalOperand operand) {
            this.op = op;
            this.operand = operand;
        }

        @Override
        public Boolean eval(Function<String, Object> variables) {
            return this.op.evaluate(null, this.operand.eval(variables));
        }

        public String toString() {
            return this.op.symbol + this.operand.toString();
        }
    }

    public static final class BiLogicalOperator
            extends LogicalOperand {
        private final LogicalOperator operator;
        private final LogicalOperand lhs;
        private final LogicalOperand rhs;

        public BiLogicalOperator(LogicalOperand lhs, LogicalOperator operator, LogicalOperand rhs) {
            this.operator = operator;
            this.lhs = lhs;
            this.rhs = rhs;
        }

        @Override
        public Boolean eval(Function<String, Object> variables) {
            return this.operator.evaluate(this.lhs.eval(variables), this.rhs.eval(variables));
        }

        public String toString() {
            return '(' + this.lhs.toString() + ' ' + this.operator.symbol + ' ' + this.rhs.toString() + ')';
        }
    }
}


