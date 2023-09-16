package fr.krishenk.castel.utils.compilers;

import fr.krishenk.castel.utils.time.TimeUtils;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Predicate;

public class MathCompiler {
    @NotNull
    public static final MathCompiler.Companion Companion = new MathCompiler.Companion();
    @NotNull
    private final String expression;
    private int offset;
    private final int end;
    private final boolean sub;
    private final LinkedList<MathCompiler.LexicalEnvironment> lexicalEnv;
    @NotNull
    private final LinkedList<Object> syntaxTree;
    @NotNull
    private static final Map<String, Double> CONSTANTS = new HashMap<>(8);
    @NotNull
    private static final Map<String, MathCompiler.Function> FUNCTIONS = new HashMap<>(44);
    @NotNull
    private static final MathCompiler.Operator[] OPERATORS = new MathCompiler.Operator[127];
    @NotNull
    private static final java.util.function.Function<String, Double> EMPTY_VARS = (String it) -> {throw new IllegalAccessError(); };
    @NotNull
    public static final MathCompiler.Expression DEFAULT_VALUE;

    private MathCompiler(@NotNull String expression, int offset, int end, boolean sub, LinkedList<MathCompiler.LexicalEnvironment> lexicalEnv) {
        this.expression = expression;
        this.offset = offset;
        this.end = end;
        this.sub = sub;
        this.lexicalEnv = lexicalEnv;
        this.syntaxTree = new LinkedList<>();
    }

    private int skipWhitespace(int offset, int end) {
        while (offset < end && this.expression.charAt(offset) == ' ') {
            ++offset;
        }
        return offset;
    }

    private int skipUntilNonVar(int firstOfs, boolean varInterpolation) {
        int ofs = firstOfs;
        char ch = this.expression.charAt(firstOfs);
        if (!varInterpolation) {
            while(('a' <= ch && ch < '{') || ('A' <= ch && ch < '[') || ('0' <= ch && ch < ':') || ch == '_') {
                ++ofs;
                if (ofs == this.end) {
                    return ofs;
                }

                ch = this.expression.charAt(ofs);
            }
        } else {
            while(true) {
                if (ch == '}') {
                    ++ofs;
                    break;
                }

                ++ofs;
                if (ofs == this.end) {
                    throw exception(firstOfs - 1, "Unclosed variable interpolation", new ArrayList<>());
                }

                ch = this.expression.charAt(ofs);
            }
        }

        return ofs;
    }

    private MathCompiler delegate() {
        return new MathCompiler(this.expression, this.offset, this.end, true, this.lexicalEnv);
    }

    private char charAt(int index) {
        return this.expression.charAt(index);
    }

    private boolean isInsideFunction() {
        return this.lexicalEnv.stream().anyMatch(x -> x.getFunction() != null);
    }

    private MathCompiler.Expression compile() throws NumberFormatException, ArithmeticException {
        if (this.offset == this.end) return this.build();
        do {
            int i = this.offset;
            char ch = this.expression.charAt(i);
            if (ch != ' ') {
                MathCompiler.Expression sentence;
                int from;
                MathCompiler.Expression expression;
                if ('0' <= ch && ch < ':') {
                    from = this.offset;
                    this.untilNonDouble();
                    int var6 = this.offset;
                    this.offset = var6 + -1;
                    MathCompiler.ConstantExpr constantExpr;
                    try {
                        constantExpr = new MathCompiler.ConstantExpr(Double.parseDouble(this.expression.substring(from, var6)), MathCompiler.ConstantExprType.NUMBER);
                    } catch (NumberFormatException var9) {
                        throw this.exception(from, "Invalid numeric value \"" + this.expression.substring(from, var6) + '"', Companion.pointerToName(from, this.expression.substring(from, var6)));
                    }

                    expression = constantExpr;
                } else if ('a' <= ch && ch < '{' || ('A' <= ch && ch < '[')) {
                    expression = this.parseVariable(false);
                } else if (ch == '{') {
                    this.offset++;
                    expression = this.parseVariable(true);
                } else {
                    if (ch == '"') {
                        this.offset++;
                        StringBuilder str = new StringBuilder();

                        char chr;
                        do {
                            int index = this.offset++;
                            chr = this.expression.charAt(index);
                            str.append(chr);
                        } while (chr != '"');
                        return new StringConstant(str.toString());
                    }

                    MathCompiler.LexicalEnvironment last;
                    MathCompiler.LexicalEnvironment environment;
                    if (ch == ',' || ch == ';') {
                        environment = this.lexicalEnv.peekLast();
                        if (environment == null) {
                            throw exception(this.offset, "Function argument separator outside of functions", new ArrayList<>());
                        }

                        last = environment;
                        if (last.getFunction() == null) {
                            if (isInsideFunction()) {
                                throw exception(last.getIndex(), "Unclosed parentheses", new ArrayList<>());
                            }

                            throw exception(this.offset, "Function argument separator outside of functions", new ArrayList<>());
                        }

                        return this.build();
                    }

                    if (ch == '[') {
                        expression = this.parsePeriod(this.offset);
                    } else if (ch == '(') {
                        from = this.offset++;
                        this.lexicalEnv.add(new MathCompiler.LexicalEnvironment(from, null));
                        MathCompiler subExpr = new MathCompiler(this.expression, this.offset, this.end, true, this.lexicalEnv);
                        sentence = subExpr.compile();
                        this.offset = subExpr.offset;
                        expression = sentence;
                    } else {
                        if (ch == ')') {
                            environment = this.lexicalEnv.pollLast();
                            if (environment == null) {
                                throw exception(this.offset, "No opening parentheses found for closing parenthes", new ArrayList<>());
                            }

                            last = environment;
                            MathCompiler.Function function = last.getFunction();
                            if (!(function != null && function.getArgCount() == 0) && this.syntaxTree.isEmpty()) {
                                throw exception(this.offset, "Empty subexpression", new ArrayList<>());
                            }

                            return this.build();
                        }

                        expression = null;
                    }
                }
                MathCompiler.Expression operand = expression;
                if (operand == null) {
                    MathCompiler.Operator op = OPERATORS[ch];
                    if (op == null) {
                        throw exception(this.offset, "Unrecognized character '" + ch + "' (" + ch + ") outside of variable/placeholder interpolation", new ArrayList<>());
                    }

                    this.handleOperator(op);
                } else {
                    if (this.syntaxTree.isEmpty()) {
                        this.syntaxTree.add(operand);
                    } else {
                        sentence = operand;
                        Object last = this.syntaxTree.getLast();
                        if (!(last instanceof MathCompiler.Operator)) {
                            throw exception(this.offset, "Expected an operator before operand", new ArrayList<>());
                        }

                        operatorBlock:
                        while (last instanceof Operator) {
                            switch (WhenMappings.SwitchMapping[((Operator) last).getArity$core().ordinal()]) {
                                case 1:
                                    break operatorBlock;
                                case 2:
                                    sentence = new BiOperation(DEFAULT_VALUE, (Operator) last, sentence);
                                    this.syntaxTree.removeLast();
                                    break;
                                case 3:
                                    this.syntaxTree.removeLast();
                                    Object peekLast = this.syntaxTree.peekLast();
                                    if (peekLast != null && !(peekLast instanceof Operator)) {
                                        this.syntaxTree.add(last);
                                        break operatorBlock;
                                    }

                                    sentence = new BiOperation(DEFAULT_VALUE, (Operator) last, sentence);
                            }
                            last = this.syntaxTree.peekLast();
                        }

                        this.syntaxTree.addLast(sentence);
                    }
                }
            }
            this.offset++;
        } while (this.offset < this.end);
        return this.build();
    }

    private MathCompiler.Expression build() {
        if ((this.offset >= this.end || !this.sub) && !this.lexicalEnv.isEmpty()) {
            ArrayList<Integer> list = new ArrayList<>();
            AtomicBoolean includesFunction = new AtomicBoolean();
            this.lexicalEnv.forEach((x) -> {
                list.add(x.getIndex());
                if (!(includesFunction.get())) includesFunction.set(x.getFunction() != null);
            });
            throw this.exception(this.lexicalEnv.getLast().getIndex(), "Unclosed parentheses" + (includesFunction.get() ? " and functions" : ""), list);
        } else if (this.syntaxTree.isEmpty()) {
            return DEFAULT_VALUE;
        } else {
            if (this.syntaxTree.size() == 1) {
                return (Expression) this.syntaxTree.getLast();
            } else {
                Object last = this.syntaxTree.getLast();
                if (last instanceof MathCompiler.Operator) {
                    String suggestIfPlaceholder = ((MathCompiler.Operator)last).getSymbol() == '%' ? " (Hint: Write placeholders without % around them." : "";
                    throw exception(this.end - 1, "Blank operand on right hand side of " + ((MathCompiler.Operator)last).getSymbol() + suggestIfPlaceholder, new ArrayList<>());
                } else {
                    MathCompiler.BiOperation operation = null;

                    MathCompiler.BiOperation biOperation;

                    ListIterator<Object> it = this.syntaxTree.listIterator();
                    while (it.hasNext()) {
                        Expression lhs = (Expression)it.next();
                        Operator op = (Operator)it.next();
                        Expression rhs = (Expression)it.next();

                        if (!it.hasNext()) {
                            biOperation = new BiOperation(lhs, op, rhs);
                        } else {
                            Operator operator = (Operator)it.next();
                            Expression op2 = (Expression)it.next();
                            BiOperation biOperation1;
                            if (op.hasPrecedenceOver$core(operator)) {
                                biOperation1 = new BiOperation(lhs, op, rhs);
                                it.previous();
                                it.previous();
                                it.previous();
                                it.remove();
                                it.previous();
                                it.remove();
                                it.previous();
                                it.set(biOperation1);
                            } else {
                                biOperation1 = new BiOperation(rhs, operator, op2);
                                it.remove();
                                it.previous();
                                it.remove();
                                it.previous();
                                it.set(biOperation1);
                                it.previous();
                                it.previous();
                            }
                            biOperation = biOperation1;
                        }
                        operation = biOperation;
                    }
                    return operation;
                }
            }
        }
    }

    private final MathCompiler.BiOperation grouped(ListIterator<Object> it) {
        MathCompiler.Expression lhs = (MathCompiler.Expression)it.next();
        MathCompiler.Operator op = (MathCompiler.Operator)it.next();
        MathCompiler.Expression rhs = (MathCompiler.Expression)it.next();

        if (!it.hasNext()) {
            return new MathCompiler.BiOperation(lhs, op, rhs);
        } else {
            MathCompiler.Operator op2 = (MathCompiler.Operator)it.next();
            MathCompiler.Expression op2Rhs = (MathCompiler.Expression)it.next();
            MathCompiler.BiOperation operation;
            MathCompiler.BiOperation biOperation;
            if (op.hasPrecedenceOver$core(op2)) {
                operation = new BiOperation(lhs, op, rhs);
                it.previous();
                it.previous();
                it.previous();
                it.remove();
                it.previous();
                it.remove();
                it.previous();
                it.set(operation);
            } else {
                operation = new BiOperation(rhs, op2, op2Rhs);
                it.remove();
                it.previous();
                it.remove();
                it.previous();
                it.set(operation);
                it.previous();
                it.previous();
            }
            biOperation = operation;

            return biOperation;
        }
    }

    private final void handleOperator(MathCompiler.Operator op) {
        Object last = this.syntaxTree.peekLast();
        if (!op.getArity$core().isUnary() && last instanceof MathCompiler.Operator) {
            throw exception(this.offset, "Blank operand on the left hand side of binary operator", new ArrayList<>());
        } else {
            this.syntaxTree.addLast(op);
        }
    }

    private final void handleOperand(MathCompiler.Expression expression) {
        if (this.syntaxTree.isEmpty()) {
            this.syntaxTree.add(expression);
        } else {
            MathCompiler.Expression sentence = expression;
            Object last = this.syntaxTree.getLast();
            if (!(last instanceof MathCompiler.Operator)) {
                throw exception(this.offset, "Expected an operator before operand", new ArrayList<>());
            } else {
                label36:
                while (last instanceof Operator) {
                    switch (WhenMappings.SwitchMapping[((Operator)last).getArity$core().ordinal()]) {
                        case 1:
                            break label36;
                        case 2:
                            sentence = new BiOperation(DEFAULT_VALUE, (Operator)last, sentence);
                            this.syntaxTree.removeLast();
                            break;
                        case 3:
                            this.syntaxTree.removeLast();
                            Object lastLast = this.syntaxTree.peekLast();
                            if (lastLast != null && !(lastLast instanceof Operator)) {
                                this.syntaxTree.add(last);
                                break label36;
                            }

                            sentence = new BiOperation(DEFAULT_VALUE, (Operator)last, sentence);
                    }
                    last = this.syntaxTree.peekLast();
                }

                this.syntaxTree.addLast(sentence);
            }
        }
    }

    private final void untilNonDouble() {
        int index$iv = this.offset;

        for(char ch = this.expression.charAt(index$iv); ('0' <= ch && ch < ':') || ch == 'x' || ch == 'e' || ch == 'E' || ch == '-' || ch == '.'; ch = this.expression.charAt(index$iv)) {
            if (ch == '-') {
                int i = this.offset - 1;
                char previous = this.expression.charAt(i);
                if (previous != 'e' && previous != 'E') {
                    return;
                }
            }

            ++this.offset;
            if (this.offset == this.end) {
                return;
            }

            index$iv = this.offset;
        }

    }

    private MathCompiler.Expression parseVariable(boolean interpolation) {
        int endVar = this.skipUntilNonVar(this.offset, interpolation);
        if (Objects.equals(this.expression.substring(this.offset, interpolation ? endVar - 1 : endVar), "_")) {
            throw exception(this.offset, "Reserved single underscore identifier", new ArrayList<>());
        } else {
            int ofs;
            ofs = endVar;
            while (ofs < this.end && this.expression.charAt(ofs) == ' ') {
                ++ofs;
            }
            if (ofs < this.end) {
                int i = ofs;
                if (this.expression.charAt(i) == '(') {
                    return this.parseFunction(this.expression.substring(ofs, interpolation ? endVar - 1 : endVar));
                }
            }

            int from = this.offset;
            this.offset = endVar - 1;
            Double it = CONSTANTS.get(this.expression.substring(from, interpolation ? endVar - 1 : endVar));
            return it == null ? new Variable(this.expression.substring(from, interpolation ? endVar - 1 : endVar)) : new ConstantExpr(it, ConstantExprType.CONSTANT_VARIABLE);
        }
    }

    private final MathCompiler.FunctionExpr parseFunction(String name) {
        String suggestion = Companion.findFunction(name);
        suggestion = suggestion == null ? "" : "; Did you mean '" + suggestion + "' function?";
        MathCompiler.Function var10000 = FUNCTIONS.get(name);
        if (var10000 == null) {
            throw exception(this.offset, "Unknown function: " + name + suggestion, new ArrayList<>());
        } else {
            this.offset++;
            ArrayList<Expression> args = new ArrayList<>();
            MathCompiler.LexicalEnvironment instance = new MathCompiler.LexicalEnvironment(this.offset, var10000);
            this.lexicalEnv.add(instance);
            int beg = this.offset;

            do {
                MathCompiler compiler = new MathCompiler(this.expression, this.offset, this.end, true, this.lexicalEnv);
                MathCompiler.Expression obj = compiler.compile();
                if (!Objects.equals(obj, DEFAULT_VALUE)) {
                    args.add(obj);
                }

                this.offset = compiler.offset + 1;
            } while(Objects.equals(this.lexicalEnv.peekLast(), instance));
            this.offset--;
            if (args.size() < var10000.getArgCount()) {
                throw this.exception(beg, "Too few arguments for function '" + name + "', expected: " + var10000.getArgCount() + ", got: " + args.size(), Companion.pointerToName(beg, name));
            } else if (args.size() > var10000.getArgCount()) {
                throw this.exception(beg, "Too many arguments for function '" + name + "', expected: " + var10000.getArgCount() + ", got: " + args.size(), Companion.pointerToName(beg, name));
            } else {
                return new MathCompiler.FunctionExpr(name, var10000, args.toArray(new MathCompiler.Expression[0]));
            }
        }
    }

    private final MathCompiler.ConstantExpr parsePeriod(int beg) {
        int timeEnd = this.expression.indexOf(']', beg + 1);
        if (timeEnd == -1) {
            throw exception(beg, "Cannot find time literal closing bracket.", new ArrayList<>());
        } else {
            this.offset = timeEnd;
            return new MathCompiler.ConstantExpr((double) TimeUtils.parseTime(this.expression.substring(++beg, timeEnd)), MathCompiler.ConstantExprType.TIME);
        }
    }

    private ArithmeticException exception(int ofs, String txt, Collection<Integer> pointers) {
        String errMsg = '\n' + txt + " at offset " + ofs + " in expression: \n\"" + this.expression + '"';
        int max = 0;
        pointers.add(ofs);

        for (Integer integer : pointers) {
            int pointer = ((Number) integer).intValue();
            if (pointer > max) {
                max = pointer;
            }
        }

        int times$iv = max + 2;
        char[] spaces$iv = new char[times$iv];
        Arrays.fill(spaces$iv, ' ');
        StringBuilder pointerStr = new StringBuilder(new String(spaces$iv));
        pointers.forEach(x -> pointerStr.setCharAt(x + 1, '^'));
        return new ArithmeticException(errMsg + '\n' + pointerStr);
    }

    @NotNull
    public static Map<String, Double> getConstants() {
        return Companion.getConstants();
    }
    
    @NotNull
    public static Map<String, MathCompiler.Function> getFunctions() {
        return Companion.getFunctions();
    }
    
    @NotNull
    public static MathCompiler.Expression compile(@Nullable String expression) throws NumberFormatException, ArithmeticException {
        return Companion.compile(expression);
    }

    static {
        DEFAULT_VALUE = new ConstantExpr(0.0, ConstantExprType.NUMBER);
        Companion.registerOperators();
        Companion.registerFunctions();
        Companion.registerConstants();
    }
    
    public static final class Function {
        
        private final MathCompiler.QuantumFunction function;
        private final boolean optimizable;
        private final int argCount;

        public Function(MathCompiler.QuantumFunction function, boolean optimizable, int argCount) {
            
            super();
            this.function = function;
            this.optimizable = optimizable;
            this.argCount = argCount;
        }

        @NotNull
        public MathCompiler.QuantumFunction getFunction() {
            return this.function;
        }

        public boolean getOptimizable$core() {
            return this.optimizable;
        }

        public int getArgCount() {
            return this.argCount;
        }
    }


    public static final class LexicalEnvironment {
        private final int index;
     
        private final MathCompiler.Function function;

        public LexicalEnvironment(int index, MathCompiler.Function function) {
            this.index = index;
            this.function = function;
        }

        public int getIndex() {
            return this.index;
        }
        
        
        public MathCompiler.Function getFunction() {
            return this.function;
        }

        public boolean isFunction() {
            return this.getFunction() != null;
        }

        @NotNull
        public String toString() {
            return "LexicalEnvironment{index=" + this.index + ", function=" +
                    (this.getFunction() != null) + '}';
        }
    }
    
    public enum Side {
        RIGHT,
        LEFT,
        NONE;

        Side() {
        }
    }

    public enum Arity {
        UNARY,
        BINARY,
        UNARY_AND_BINARY;

        Arity() {
        }

        public final boolean isUnary() {
            return this == UNARY || this == UNARY_AND_BINARY;
        }
    }

    public interface QuantumFunction {
        double apply(@NotNull MathCompiler.FnArgs var1);
    }

    public interface TriDoubleFn {
        double apply(double var1, double var3);
    }

    public static final class Operator {
        private final char symbol;
        private final byte precedenceLeft;
        private final byte precedenceRight;
        @NotNull
        private final MathCompiler.Side side;
        @NotNull
        private final MathCompiler.Arity arity;
        @NotNull
        private final MathCompiler.TriDoubleFn function;

        public Operator(char symbol, int precedenceL, int precedenceR, @NotNull MathCompiler.Side side, @NotNull MathCompiler.TriDoubleFn function) {
            
            
            super();
            this.symbol = symbol;
            this.precedenceLeft = (byte)precedenceL;
            this.precedenceRight = (byte)precedenceR;
            this.side = side;
            this.function = function;
            char var6 = this.symbol;
            this.arity = var6 == '-' ? MathCompiler.Arity.UNARY_AND_BINARY : (var6 == '~' ? MathCompiler.Arity.UNARY : MathCompiler.Arity.BINARY);
        }

        public char getSymbol() {
            return this.symbol;
        }

        @NotNull
        public MathCompiler.Arity getArity$core() {
            return this.arity;
        }

        @NotNull
        public MathCompiler.TriDoubleFn getFunction$core() {
            return this.function;
        }

        public Operator(char sym, int precedence, @NotNull MathCompiler.TriDoubleFn function) {
            
            this(sym, precedence, precedence, MathCompiler.Side.NONE, function);
        }

        @NotNull
        public String toString() {
            return "MathOperator['" + this.symbol + "']";
        }

        public boolean hasPrecedenceOver$core(@NotNull MathCompiler.Operator op) {
            
            return this.precedenceLeft >= op.precedenceLeft;
        }
    }

    public abstract static class Expression {
        public Expression() {
        }

        public abstract double eval(@NotNull java.util.function.Function<String, Double> var1);

        public final boolean isDefault() {
            return this == MathCompiler.DEFAULT_VALUE;
        }
        
        public final MathCompiler.Expression nullIfDefault() {
            return this.isDefault() ? null : this;
        }

        public final <T extends MathCompiler.Expression> boolean contains(@NotNull Class<T> ofType, @NotNull Predicate<T> predicate) {
            
            
            if (ofType.isInstance(this)) {
                
                if (predicate.test((T) this)) {
                    return true;
                }
            }

            boolean isContaining;
            if (this instanceof MathCompiler.BiOperation) {
                isContaining = ((MathCompiler.BiOperation)this).getLeft$core().contains(ofType, predicate) || ((MathCompiler.BiOperation)this).getRight$core().contains(ofType, predicate);
            } else if (this instanceof MathCompiler.FunctionExpr) {
                Expression[] args = ((MathCompiler.FunctionExpr)this).getArgs();
                int i = 0;
                while(true) {
                    if (i >= args.length) {
                        isContaining = false;
                        break;
                    }
                    if (args[i].contains(ofType, predicate)) {
                        isContaining = true;
                        break;
                    }
                    ++i;
                }
            } else {
                isContaining = false;
            }

            return isContaining;
        }
    }

    public static final class StringConstant extends MathCompiler.ConstantExpr {
        @NotNull
        private final String string;

        public StringConstant(@NotNull String string) {
            
            super(string.hashCode(), MathCompiler.ConstantExprType.STRING);
            this.string = string;
        }

        @NotNull
        public String getString() {
            return this.string;
        }

        public double eval(@NotNull java.util.function.Function<String, Double> variables) {
            
            return super.getValue();
        }

        @NotNull
        public String toString() {
            return '"' + this.string + '"';
        }
    }

    public static final class BiOperation extends MathCompiler.Expression {
        @NotNull
        private final MathCompiler.Expression left;
        @NotNull
        private final MathCompiler.Operator op;
        @NotNull
        private final MathCompiler.Expression right;

        public BiOperation(@NotNull MathCompiler.Expression left, @NotNull MathCompiler.Operator op, @NotNull MathCompiler.Expression right) {
            super();
            this.left = left;
            this.op = op;
            this.right = right;
        }

        @NotNull
        public MathCompiler.Expression getLeft$core() {
            return this.left;
        }

        @NotNull
        public MathCompiler.Operator getOp$core() {
            return this.op;
        }

        @NotNull
        public MathCompiler.Expression getRight$core() {
            return this.right;
        }

        public double eval(@NotNull java.util.function.Function<String, Double> variables) {
            
            return this.op.getFunction$core().apply(this.left.eval(variables), this.right.eval(variables));
        }

        @NotNull
        public String toString() {
            return String.valueOf('(') + this.left + ' ' + this.op.getSymbol() + (this.op.getSymbol() == '(' ? "" : ' ') + this.right + ')';
        }
    }

    public static final class FunctionExpr extends MathCompiler.Expression {
        @NotNull
        private final String name;
        @NotNull
        private final MathCompiler.Function handler;
        @NotNull
        private final MathCompiler.Expression[] args;

        public FunctionExpr(@NotNull String name, @NotNull MathCompiler.Function handler, @NotNull MathCompiler.Expression[] args) {
            super();
            this.name = name;
            this.handler = handler;
            this.args = args;
        }

        @NotNull
        public String getName() {
            return this.name;
        }

        @NotNull
        public MathCompiler.Function getHandler() {
            return this.handler;
        }

        @NotNull
        public MathCompiler.Expression[] getArgs() {
            return this.args;
        }

        public double eval(@NotNull java.util.function.Function<String, Double> variables) {
            return this.handler.getFunction().apply(new FnArgs(this, variables));
        }

        @NotNull
        public String toString() {
            String[] argsStr = Arrays.stream(this.args).map(Object::toString).toArray(String[]::new);
            return this.name + '(' + (this.args.length == 0 ? "" : String.join(", ", argsStr)) + ')';
        }
    }

    public enum ConstantExprType {
        OPTIMIZED,
        NUMBER,
        STRING,
        CONSTANT_VARIABLE,
        TIME;

        ConstantExprType() {
        }
    }

    public static class ConstantExpr extends MathCompiler.Expression {
        private final double value;
        @NotNull
        private final MathCompiler.ConstantExprType type;

        public ConstantExpr(double value, @NotNull MathCompiler.ConstantExprType type) {
            
            super();
            this.value = value;
            this.type = type;
        }

        public final double getValue() {
            return this.value;
        }

        @NotNull
        public final MathCompiler.ConstantExprType getType() {
            return this.type;
        }

        public double eval(@NotNull java.util.function.Function<String, Double> variables) {
            
            return this.value;
        }

        @NotNull
        public String toString() {
            return String.valueOf(this.value);
        }
    }

    private static final class Variable extends MathCompiler.Expression {
        @NotNull
        private final String name;

        public Variable(@NotNull String name) {
            
            super();
            this.name = name;
        }

        public double eval(@NotNull java.util.function.Function<String, Double> variables) {
            
            Double value = variables.apply(this.name);
            if (value == null) {
                String guessFn = MathCompiler.Companion.findFunction(this.name);
                String suggestion = "";
                if (guessFn != null) {
                    suggestion = "; Did you mean to invoke '" + guessFn + "' function? If so, put parentheses after the name like '" + guessFn + "(args)'";
                }

                throw new RuntimeException("Unknown variable: '" + this.name + '\'' + suggestion);
            } else {
                return value;
            }
        }

        @NotNull
        public String toString() {
            return '{' + this.name + '}';
        }
    }

    public static final class FnArgs {
        @NotNull
        private final MathCompiler.FunctionExpr func;
        @NotNull
        private final java.util.function.Function<String, Double> variables;
        private int index;

        public FnArgs(@NotNull MathCompiler.FunctionExpr func, @NotNull java.util.function.Function<String, Double> variables) {
            this.func = func;
            this.variables = variables;
        }

        public double next() {
            MathCompiler.Expression[] var10000 = this.func.getArgs();
            int var1 = this.index++;
            return var10000[var1].eval(this.variables);
        }

        public double next(int i) {
            this.index = i;
            return this.func.getArgs()[i].eval(this.variables);
        }
    }

    public static final class Companion {
        private Companion() {
        }

        
        @NotNull
        public Map<String, Double> getConstants() {
            return MathCompiler.CONSTANTS;
        }

        
        @NotNull
        public Map<String, MathCompiler.Function> getFunctions() {
            return MathCompiler.FUNCTIONS;
        }

        private void op(MathCompiler.Operator opr) {
            if (opr.getSymbol() >= MathCompiler.OPERATORS.length) {
                String str = "Operator handler cannot handle char '" + opr.getSymbol() + "' with char code: " + opr.getSymbol();
                throw new IllegalArgumentException(str);
            } else {
                MathCompiler.OPERATORS[opr.getSymbol()] = opr;
            }
        }

        private void registerConstants() {
            MathCompiler.CONSTANTS.put("E", Math.E);
            MathCompiler.CONSTANTS.put("PI", Math.PI);
            MathCompiler.CONSTANTS.put("Euler", 0.5772156649015329);
            MathCompiler.CONSTANTS.put("LN2", 0.693147180559945);
            MathCompiler.CONSTANTS.put("LN10", 2.302585092994046);
            MathCompiler.CONSTANTS.put("LOG2E", 1.442695040888963);
            MathCompiler.CONSTANTS.put("LOG10E", 0.434294481903252);
            MathCompiler.CONSTANTS.put("PHI", 1.618033988749895);
        }

        private void registerOperators() {
            this.op(new MathCompiler.Operator('^', 12, 13, MathCompiler.Side.NONE, Math::pow));
            this.op(new MathCompiler.Operator('*', 10, (a, b) -> a * b));
            this.op(new MathCompiler.Operator('(', 10, (a, b) -> a * b));
            this.op(new MathCompiler.Operator('/', 10, (a, b) -> a / b));
            this.op(new MathCompiler.Operator('%', 10, (a, b) -> a % b));
            this.op(new MathCompiler.Operator('+', 9, Double::sum));
            this.op(new MathCompiler.Operator('-', 9, (a, b) -> a - b));
            this.op(new MathCompiler.Operator('~', 10, (a, b) -> ~((long) b)));
            this.op(new MathCompiler.Operator('@', 8, (a, b) -> Long.rotateLeft((long)a, (int)b)));
            this.op(new MathCompiler.Operator('#', 8, (a, b) -> Long.rotateRight((long)a, (int)b)));
            this.op(new MathCompiler.Operator('>', 8, (a, b) -> (long)a >> (int)b));
            this.op(new MathCompiler.Operator('<', 8, (a, b) -> (long)a << (int)b));
            this.op(new MathCompiler.Operator('$', 8, (a, b) -> (long)a >>> (int)b));
            this.op(new MathCompiler.Operator('&', 7, (a, b) -> (long)a & (long)b));
            this.op(new MathCompiler.Operator('!', 6, (a, b) -> (long)a ^ (long)b));
            this.op(new MathCompiler.Operator('|', 5, (a, b) -> (long)a | (long)b));
        }

        private void registerFunctions() {
            this.fn(this, "abs", ( MathCompiler.FnArgs p) -> Math.abs(p.next()));
            this.fn(this, "acos", ( MathCompiler.FnArgs p) -> Math.acos(p.next()));
            this.fn(this, "asin", ( MathCompiler.FnArgs p) -> Math.asin(p.next()));
            this.fn(this, "atan", ( MathCompiler.FnArgs p) -> Math.atan(p.next()));
            this.fn(this, "cbrt", ( MathCompiler.FnArgs p) -> Math.cbrt(p.next()));
            this.fn(this, "ceil", ( MathCompiler.FnArgs p) -> Math.ceil(p.next()));
            this.fn(this, "cos", ( MathCompiler.FnArgs p) -> Math.cos(p.next()));
            this.fn(this, "cosh", ( MathCompiler.FnArgs p) -> Math.cosh(p.next()));
            this.fn(this, "exp", ( MathCompiler.FnArgs p) -> Math.exp(p.next()));
            this.fn(this, "expm1", ( MathCompiler.FnArgs p) -> Math.expm1(p.next()));
            this.fn(this, "floor", ( MathCompiler.FnArgs p) -> Math.floor(p.next()));
            this.fn(this, "getExponent", ( MathCompiler.FnArgs p) -> Math.getExponent(p.next()));
            this.fn(this, "log", ( MathCompiler.FnArgs p) -> Math.log(p.next()));
            this.fn(this, "log10", ( MathCompiler.FnArgs p) -> Math.log10(p.next()));
            this.fn(this, "log1p", ( MathCompiler.FnArgs p) -> Math.log1p(p.next()));
            this.fn("max", (MathCompiler.FnArgs p) -> {double a = p.next(); double b = p.next(); return Math.max(a, b);}, 2);
            this.fn("min", (FnArgs p) -> {double a = p.next(); double b = p.next(); return Math.min(a, b);}, 2);
            this.fn(this, "nextUp", (FnArgs p) -> Math.nextUp(p.next()));
            this.fn(this, "nextDown", (FnArgs p) -> Math.nextDown(p.next()));
            this.fn("nextAfter", (FnArgs p) -> Math.nextAfter(p.next(), p.next()), 2);
            this.fn("random", false, (FnArgs p) -> ThreadLocalRandom.current().nextDouble(p.next(), p.next() + 1.0), 2);
            this.fn("randInt", false, (FnArgs p) -> ThreadLocalRandom.current().nextInt((int)p.next(), (int)p.next() + 1), 2);
            this.fn(this, "round", (FnArgs p) -> Math.round(p.next()));
            this.fn(this, "rint", (FnArgs p) -> Math.rint(p.next()));
            this.fn(this, "signum", (FnArgs p) -> Math.signum(p.next()));
            this.fn("whatPercentOf", (FnArgs p) -> (p.next() / p.next() * (double)100), 2);
            this.fn("percentOf", (FnArgs p) -> (p.next() / (double)100 * p.next()), 2);
            this.fn(this, "sin", (FnArgs p) -> Math.sin(p.next()));
            this.fn(this, "sinh", (FnArgs p) -> Math.sinh(p.next()));
            this.fn(this, "bits", (FnArgs p) -> Double.doubleToRawLongBits(p.next()));
            this.fn(this, "hash", (FnArgs p) -> Double.hashCode(p.next()));
            this.fn(this, "identityHash", (FnArgs p) -> System.identityHashCode(p.next()));
            this.fn("time", false, (FnArgs p) -> System.currentTimeMillis(), 0);
            this.fn(this, "sqrt", (FnArgs p) -> Math.sqrt(p.next()));
            this.fn(this, "tan", (FnArgs p) -> Math.tan(p.next()));
            this.fn(this, "tanh", (FnArgs p) -> Math.tanh(p.next()));
            this.fn(this, "toDegrees", (FnArgs p) -> Math.toDegrees(p.next()));
            this.fn(this, "toRadians", (FnArgs p) -> Math.toRadians(p.next()));
            this.fn(this, "ulp", (FnArgs p) -> Math.ulp(p.next()));
            this.fn("scalb", (FnArgs p) -> Math.scalb(p.next(), (int)p.next()), 2);
            this.fn("hypot", (FnArgs p) -> Math.hypot(p.next(), p.next()), 2);
            this.fn("copySign", (FnArgs p) -> Math.copySign(p.next(), p.next()), 2);
            this.fn("IEEEremainder", (FnArgs p) -> Math.IEEEremainder(p.next(), p.next()), 2);
            this.fn(this, "naturalSum", (FnArgs p) -> { int n = (int)p.next(); return (double)(n * (n + 1)) / 2.0;});
            this.fn(this, "reverse", (FnArgs p) -> Long.reverse((long)p.next()));
            this.fn(this, "reverseBytes", (FnArgs p) -> Long.reverseBytes((long)p.next()));
            this.fn("eq", (FnArgs p) -> p.next() == p.next() ? p.next() : p.next(3), 4);
            this.fn("ne  ", (FnArgs p) -> !(p.next() == p.next()) ? p.next() : p.next(3), 4);
            this.fn("gt", (FnArgs p) -> p.next() > p.next() ? p.next() : p.next(3), 4);
            this.fn("lt", (FnArgs p) -> p.next() < p.next() ? p.next() : p.next(3), 4);
            this.fn("ge", (FnArgs p) -> p.next() >= p.next() ? p.next() : p.next(3), 4);
            this.fn("le", (FnArgs p) -> p.next() <= p.next() ? p.next() : p.next(3), 4);
        }

        private void fn(String name, QuantumFunction handler, int argCount) {
            this.fn(name, true, handler, argCount);
        }

        private void fn(Companion companion, String string, QuantumFunction quantumFunction) {
            int n = 1;
            companion.fn(string, quantumFunction, n);
        }

        private void fn(String name, boolean optimizable, QuantumFunction handler, int argCount) {
            FUNCTIONS.put(name, new Function(handler, optimizable, argCount));
        }

        private static void fn(Companion companion, String string, boolean bl, QuantumFunction quantumFunction, int n, int n2) {
            if ((n2 & 8) != 0) {
                n = 1;
            }
            companion.fn(string, bl, quantumFunction, n);
        }



        @NotNull
        public Expression compile(@Nullable String expression) throws NumberFormatException, ArithmeticException {
            return expression == null || ((CharSequence) expression).length() == 0 ? MathCompiler.DEFAULT_VALUE : this.optimize((new MathCompiler(expression, 0, expression.length(), false, new LinkedList<>())).compile());
        }

        private Expression optimize(Expression expression) {
            if (expression instanceof BiOperation) {
                Expression lhs = this.optimize(((BiOperation)expression).getLeft$core());
                Expression rhs = this.optimize(((BiOperation)expression).getRight$core());
                if (lhs instanceof ConstantExpr && rhs instanceof ConstantExpr) {
                    return new ConstantExpr(((BiOperation)expression).getOp$core().getFunction$core().apply(((ConstantExpr)lhs).getValue(), ((ConstantExpr)rhs).getValue()), MathCompiler.ConstantExprType.OPTIMIZED);
                }
            } else if (expression instanceof FunctionExpr) {
                if (!((FunctionExpr)expression).getHandler().getOptimizable$core()) {
                    return expression;
                }

                boolean allOptimized = true;
                ArrayList<Expression> newParameters = new ArrayList<>(((FunctionExpr)expression).getArgs().length);
                Expression[] args = ((FunctionExpr)expression).getArgs();
                int index = 0;
                for(int i = args.length; index < i; ++index) {
                    Expression arg = args[index];
                    Expression optimized = this.optimize(arg);
                    newParameters.add(optimized);
                    if (allOptimized) {
                        allOptimized = optimized instanceof ConstantExpr;
                    }
                }

                Expression expression1;
                if (allOptimized) {
                    expression1 = new ConstantExpr(((FunctionExpr)expression).getHandler().getFunction().apply(new FnArgs((FunctionExpr)expression, MathCompiler.EMPTY_VARS)), MathCompiler.ConstantExprType.OPTIMIZED);
                } else {
                    expression1 = new FunctionExpr(((FunctionExpr) expression).getName(), ((FunctionExpr) expression).getHandler(), newParameters.toArray(new Expression[0]));
                }

                return expression1;
            }

            return expression;
        }

        private Operator getOperator(char chr) {
            return MathCompiler.OPERATORS[chr];
        }

        private Collection<Integer> pointerToName(int from, String name) {
            List<Integer> pointers = new ArrayList<>(name.length());
            int i = 1;
            for(int var5 = name.length(); i < var5; ++i) {
                pointers.add(from + i);
            }

            return pointers;
        }

        private String spaces(int times) {
            char[] spaces = new char[times];
            Arrays.fill(spaces, ' ');
            return new String(spaces);
        }

        private String findFunction(String name) {
            String lowerCaseName = name.toLowerCase(Locale.ROOT);
            Optional<String> exact = MathCompiler.FUNCTIONS.keySet().stream().filter(x -> Objects.equals(lowerCaseName, x.toLowerCase(Locale.ROOT))).findFirst();
            return exact.orElseGet(() -> MathCompiler.FUNCTIONS.keySet().stream().filter(y -> y.toLowerCase(Locale.ROOT).contains(lowerCaseName)).findFirst().orElse(null));
        }
    }

    public static class WhenMappings {
        public static int[] SwitchMapping;

        static {
            int[] arrn = new int[MathCompiler.Arity.values().length];
            arrn[MathCompiler.Arity.BINARY.ordinal()] = 1;
            arrn[MathCompiler.Arity.UNARY.ordinal()] = 2;
            arrn[MathCompiler.Arity.UNARY_AND_BINARY.ordinal()] = 3;
            SwitchMapping = arrn;
        }
    }
}
