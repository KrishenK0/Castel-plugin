package fr.krishenk.castel.utils.compilers;


import fr.krishenk.castel.utils.time.TimeUtils;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

public final class OldMathCompiler {
    private static final Map<String, Double> CONSTANTS = new HashMap<String, Double>(8);
    private static final Map<String, Function> FUNCTIONS = new HashMap<String, Function>(44);
    private static final Operator[] OPERATORS = new Operator[127];
    private static final Operator OPERAND = new Operator('\u0000', 0, 0, Side.NONE, null);
    private static final Operator OPERATOR_EQUAL = new Operator('=', 127, 127, Side.RIGHT, (a, b) -> b);
    private static final Sentence DEFAULT_VALUE = new ConstantExpr(0.0);
    private final String expression;
    private int offset;
    private final Stack<Stack<Integer>> nonlexicalEnvironmentSubExpr = new Stack<Stack<Integer>>(){
        {
            this.push(new Stack());
        }
    };

    private OldMathCompiler(String expression) {
        this.expression = expression;
    }

    private static void op(Operator opr) {
        if (opr.symbol >= OPERATORS.length) {
            throw new IllegalArgumentException("Operator handler cannot handle char '" + opr.symbol + "' with char code: " + opr.symbol);
        }
        OldMathCompiler.OPERATORS[opr.symbol] = opr;
    }

    public static Map<String, Double> getConstants() {
        return CONSTANTS;
    }

    public static Map<String, Function> getFunctions() {
        return FUNCTIONS;
    }

    public static Operator[] getOperators() {
        return OPERATORS;
    }

    private static void registerConstants() {
        CONSTANTS.put("E", Math.E);
        CONSTANTS.put("Euler", 0.5772156649015329);
        CONSTANTS.put("LN2", 0.693147180559945);
        CONSTANTS.put("LN10", 2.302585092994046);
        CONSTANTS.put("LOG2E", 1.442695040888963);
        CONSTANTS.put("LOG10E", 0.434294481903252);
        CONSTANTS.put("PHI", 1.618033988749895);
        CONSTANTS.put("PI", Math.PI);
    }

    private static void registerOperators() {
        OldMathCompiler.op(OPERATOR_EQUAL);
        OldMathCompiler.op(new Operator('^', 12, 13, Side.NONE, Math::pow));
        OldMathCompiler.op(new Operator('*', 10, (a, b) -> a * b));
        OldMathCompiler.op(new Operator('(', 10, (a, b) -> a * b));
        OldMathCompiler.op(new Operator('/', 10, (a, b) -> a / b));
        OldMathCompiler.op(new Operator('%', 10, (a, b) -> a % b));
        OldMathCompiler.op(new Operator('+', 9, Double::sum));
        OldMathCompiler.op(new Operator('-', 9, (a, b) -> a - b));
        OldMathCompiler.op(new Operator('~', 10, (a, b) -> (long)b ^ 0xFFFFFFFFFFFFFFFFL));
        OldMathCompiler.op(new Operator('@', 8, (a, b) -> Long.rotateLeft((long)a, (int)b)));
        OldMathCompiler.op(new Operator('#', 8, (a, b) -> Long.rotateRight((long)a, (int)b)));
        OldMathCompiler.op(new Operator('>', 8, (a, b) -> (long)a >> (int)b));
        OldMathCompiler.op(new Operator('<', 8, (a, b) -> (long)a << (int)b));
        OldMathCompiler.op(new Operator('$', 8, (a, b) -> (long)a >>> (int)b));
        OldMathCompiler.op(new Operator('&', 7, (a, b) -> (long)a & (long)b));
        OldMathCompiler.op(new Operator('!', 6, (a, b) -> (long)a ^ (long)b));
        OldMathCompiler.op(new Operator('|', 5, (a, b) -> (long)a | (long)b));
        for (int i = 33; i < 126; ++i) {
            char ch = (char)i;
            if (ch >= '0' && ch <= '9' || ch >= 'a' && ch <= 'z' || ch >= 'A' && ch <= 'Z' || ch == '_' || ch == ',' || ch == ';' || ch == '(' || ch == ')' || ch == '{' || ch == '}' || ch == '[' || ch == ']' || OldMathCompiler.getOperator(ch) != OPERAND) continue;
            OldMathCompiler.op(new Operator(ch, 1, null));
        }
    }

    private static void registerFunctions() {
        OldMathCompiler.fn("abs", p -> Math.abs(p.next()));
        OldMathCompiler.fn("acos", p -> Math.acos(p.next()));
        OldMathCompiler.fn("asin", p -> Math.asin(p.next()));
        OldMathCompiler.fn("atan", p -> Math.atan(p.next()));
        OldMathCompiler.fn("cbrt", p -> Math.cbrt(p.next()));
        OldMathCompiler.fn("ceil", p -> Math.ceil(p.next()));
        OldMathCompiler.fn("cos", p -> Math.cos(p.next()));
        OldMathCompiler.fn("cosh", p -> Math.cosh(p.next()));
        OldMathCompiler.fn("exp", p -> Math.exp(p.next()));
        OldMathCompiler.fn("expm1", p -> Math.expm1(p.next()));
        OldMathCompiler.fn("floor", p -> Math.floor(p.next()));
        OldMathCompiler.fn("getExponent", p -> Math.getExponent(p.next()));
        OldMathCompiler.fn("log", p -> Math.log(p.next()));
        OldMathCompiler.fn("log10", p -> Math.log10(p.next()));
        OldMathCompiler.fn("log1p", p -> Math.log1p(p.next()));
        OldMathCompiler.fn("max", p -> {
            double b;
            double a = p.next();
            return a >= (b = p.next()) ? a : b;
        }, 2);
        OldMathCompiler.fn("min", p -> {
            double b;
            double a = p.next();
            return a <= (b = p.next()) ? a : b;
        }, 2);
        OldMathCompiler.fn("nextUp", p -> Math.nextUp(p.next()));
        OldMathCompiler.fn("nextDown", p -> Math.nextDown(p.next()));
        OldMathCompiler.fn("nextAfter", p -> Math.nextAfter(p.next(), p.next()), 2);
        OldMathCompiler.fn("random", p -> ThreadLocalRandom.current().nextDouble(p.next(), p.next() + 1.0), 2);
        OldMathCompiler.fn("randInt", p -> ThreadLocalRandom.current().nextInt((int)p.next(), (int)p.next() + 1), 2);
        OldMathCompiler.fn("round", p -> Math.round(p.next()));
        OldMathCompiler.fn("rint", p -> Math.rint(p.next()));
        OldMathCompiler.fn("signum", p -> Math.signum(p.next()));
        OldMathCompiler.fn("whatPercentOf", p -> p.next() / p.next() * 100.0, 2);
        OldMathCompiler.fn("percentOf", p -> p.next() / 100.0 * p.next(), 2);
        OldMathCompiler.fn("sin", p -> Math.sin(p.next()));
        OldMathCompiler.fn("sinh", p -> Math.sinh(p.next()));
        OldMathCompiler.fn("bits", p -> Double.doubleToRawLongBits(p.next()));
        OldMathCompiler.fn("hash", p -> Double.hashCode(p.next()));
        OldMathCompiler.fn("identityHash", p -> System.identityHashCode(p.next()));
        OldMathCompiler.fn("time", p -> System.currentTimeMillis(), 0);
        OldMathCompiler.fn("sqrt", p -> Math.sqrt(p.next()));
        OldMathCompiler.fn("tan", p -> Math.tan(p.next()));
        OldMathCompiler.fn("tanh", p -> Math.tanh(p.next()));
        OldMathCompiler.fn("toDegrees", p -> Math.toDegrees(p.next()));
        OldMathCompiler.fn("toRadians", p -> Math.toRadians(p.next()));
        OldMathCompiler.fn("ulp", p -> Math.ulp(p.next()));
        OldMathCompiler.fn("scalb", p -> Math.scalb(p.next(), (int)p.next()), 2);
        OldMathCompiler.fn("hypot", p -> Math.hypot(p.next(), p.next()), 2);
        OldMathCompiler.fn("copySign", p -> Math.copySign(p.next(), p.next()), 2);
        OldMathCompiler.fn("IEEEremainder", p -> Math.IEEEremainder(p.next(), p.next()), 2);
        OldMathCompiler.fn("naturalSum", p -> {
            int n = (int)p.next();
            return (double)(n * (n + 1)) / 2.0;
        });
        OldMathCompiler.fn("reverse", p -> Long.reverse((long)p.next()));
        OldMathCompiler.fn("reverseBytes", p -> Long.reverseBytes((long)p.next()));
        OldMathCompiler.fn("gt", p -> p.next() > p.next() ? p.next() : p.next(2), 4);
        OldMathCompiler.fn("lt", p -> p.next() < p.next() ? p.next() : p.next(2), 4);
        OldMathCompiler.fn("ge", p -> p.next() >= p.next() ? p.next() : p.next(2), 4);
        OldMathCompiler.fn("le", p -> p.next() <= p.next() ? p.next() : p.next(2), 4);
    }

    private static void fn(String name, QuantumFunction handler) {
        OldMathCompiler.fn(name, handler, 1);
    }

    private static void fn(String name, QuantumFunction handler, int argCount) {
        FUNCTIONS.put(name, new Function(handler, argCount));
    }

    public static Sentence compile(String expression) throws NumberFormatException, ArithmeticException {
        if (expression == null || expression.isEmpty()) {
            return DEFAULT_VALUE;
        }
        return new OldMathCompiler(expression).compile(0, expression.length() - 1, null, OPERAND, OPERATOR_EQUAL, true);
    }

    private static Operator getOperator(char chr) {
        if (chr >= OPERATORS.length) {
            return OPERAND;
        }
        Operator opr = OPERATORS[chr];
        return opr == null ? OPERAND : opr;
    }

    private int skipWhitespace(int ofs, int end) {
        while (ofs <= end && this.expression.charAt(ofs) == ' ') {
            ++ofs;
        }
        return ofs;
    }

    private int skipUntilNonVar(int ofs, boolean varInterpolation) {
        char ch;
        int len = this.expression.length();
        while (ofs < len && (ch = this.charAt(ofs)) != '}' && (varInterpolation || ch != ' ' && ch != '(' && ch != ')' && ch != '[' && ch != ']' && ch != ',' && ch != ';' && OldMathCompiler.getOperator(ch) == OPERAND)) {
            ++ofs;
        }
        return ofs;
    }

    private Sentence compile(int beg, int end) throws NumberFormatException, ArithmeticException {
        return this.compile(beg, end, null, OPERAND, OPERATOR_EQUAL, false);
    }

    private boolean isScientificNotation(int index) {
        char notation = this.charAt(index);
        return notation == 'E' || notation == 'e';
    }

    private boolean closingParens(int closingParenIndex) {
        Stack<Integer> env = this.nonlexicalEnvironmentSubExpr.peek();
        if (env.isEmpty()) {
            if (this.nonlexicalEnvironmentSubExpr.size() > 1) {
                this.nonlexicalEnvironmentSubExpr.pop();
                return true;
            }
            throw this.exception(closingParenIndex, "Cannot find matching opening parentheses");
        }
        env.pop();
        return false;
    }

    private char charAt(int index) {
        return this.expression.charAt(index);
    }

    private void fnArgSep(int ofs, char chr) {
        if (this.nonlexicalEnvironmentSubExpr.size() > 1) {
            Stack<Integer> at = this.nonlexicalEnvironmentSubExpr.peek();
            if (!at.isEmpty()) {
                throw this.exception(at.peek(), "Unclosed subexpression (hint: Argument separators cannot be in subexpressions)");
            }
        } else {
            throw this.exception(ofs, "Terminator character '" + chr + "' used outside of function arguments");
        }
        this.nonlexicalEnvironmentSubExpr.pop();
    }

    /*
     * Enabled aggressive block sorting
     * Enabled unnecessary exception pruning
     * Enabled aggressive exception aggregation
     */
    private Sentence compile(int beg, int end, Sentence lft, Operator pnd, Operator cur, boolean ensureSubExpressionClosings) throws NumberFormatException, ArithmeticException {
        Operator nxt = OPERAND;
        int ofs = beg;
        block2: while ((ofs = this.skipWhitespace(ofs, end)) <= end) {
            int signOffset = 0;
            boolean sign = true;
            boolean signCheck = true;
            beg = ofs;
            while (true) {
                block48: {
                    int beginAt;
                    block47: {
                        block45: {
                            Sentence rgt;
                            block43: {
                                char chr;
                                block52: {
                                    int newOfs;
                                    char parens;
                                    block51: {
                                        block49: {
                                            block50: {
                                                boolean isSign;
                                                block46: {
                                                    block44: {
                                                        if (ofs > end) break block43;
                                                        chr = this.expression.charAt(ofs);
                                                        if (chr != '{') break block44;
                                                        beginAt = ofs++;
                                                        break block45;
                                                    }
                                                    if (chr != '[') break block46;
                                                    beginAt = ofs++;
                                                    break block47;
                                                }
                                                boolean bl = isSign = chr == '-' || chr == '+';
                                                if (isSign && ofs != 0 && this.isScientificNotation(ofs - 1)) break block48;
                                                if (!signCheck) break block49;
                                                if (!isSign) break block50;
                                                sign = sign ? chr == '+' : chr == '-';
                                                ++signOffset;
                                                break block48;
                                            }
                                            signCheck = false;
                                        }
                                        if (chr == '.') break block48;
                                        nxt = OldMathCompiler.getOperator(chr);
                                        if (nxt == OPERAND) break block51;
                                        if (nxt.function == null) throw this.exception(ofs, "No functionality associated with reserved character '" + chr + "' (" + chr + ") outside of variable/placeholder interpolation");
                                        if (nxt == OPERATOR_EQUAL) {
                                            throw this.exception(ofs, "No functionality associated with reserved character '" + chr + "' (" + chr + ") outside of variable/placeholder interpolation");
                                        }
                                        break block43;
                                    }
                                    if (chr != ')') break block52;
                                    int index = ofs;
                                    while ((parens = this.charAt(newOfs = this.skipWhitespace(index, end))) == ')') {
                                        this.closingParens(newOfs);
                                        index = newOfs + 1;
                                        if (index <= end) continue;
                                        if (!this.nonlexicalEnvironmentSubExpr.peek().isEmpty()) {
                                            Stack<Integer> first = this.nonlexicalEnvironmentSubExpr.peek();
                                            throw this.exception(first.peek(), "Unclosed parentheses", first);
                                        }
                                        break block43;
                                    }
                                    if (parens == ',' || parens == ';') {
                                        this.fnArgSep(newOfs, parens);
                                    }
                                    break block43;
                                }
                                if (chr != ',' && chr != ';') break block48;
                                this.fnArgSep(ofs, chr);
                            }
                            char ch = this.expression.charAt(beg);
                            if (beg == ofs && (cur.side == Side.LEFT || nxt.side == Side.RIGHT)) {
                                rgt = null;
                            } else if (ch == '(') {
                                this.nonlexicalEnvironmentSubExpr.peek().push(beg);
                                rgt = this.compile(beg + 1, end);
                                ofs = this.skipWhitespace(this.offset + 1, end);
                                nxt = ofs <= end ? OldMathCompiler.getOperator(this.expression.charAt(ofs)) : OPERAND;
                            } else if (ch == '[') {
                                String timeStr;
                                Long time;
                                int timeEnd = this.expression.indexOf(93, beg + 1);
                                if (timeEnd == -1) {
                                    throw this.exception(beg, "Cannot find time literal closing bracket.");
                                }
                                if ((time = TimeUtils.parseTime(timeStr = this.expression.substring(++beg, timeEnd))) == null) {
                                    throw this.exception(beg, "Invalid time literal '" + timeStr + '\'', OldMathCompiler.pointerToName(beg, timeStr));
                                }
                                rgt = new ConstantExpr(time.doubleValue());
                            } else if (ch == '-' || ch >= '0' && ch <= '9') {
                                String evaluated = null;
                                try {
                                    int offset = beg + signOffset;
                                    evaluated = this.expression.substring(offset, ofs);
                                    rgt = new ConstantExpr((double)(sign ? 1 : -1) * Double.parseDouble(evaluated));
                                }
                                catch (NumberFormatException thr) {
                                    throw this.exception(beg, "Invalid numeric value \"" + evaluated + '\"', OldMathCompiler.pointerToName(beg, evaluated));
                                }
                            } else if (nxt.symbol == '(') {
                                rgt = this.parseFunction(beg, end);
                                ofs = this.skipWhitespace(this.offset, end);
                                nxt = ofs <= end ? OldMathCompiler.getOperator(this.expression.charAt(ofs)) : OPERAND;
                            } else {
                                int endVar;
                                if (ch < '!') throw this.exception(beg, "Unrecognized character '" + ch + "' (" + ch + ") outside of variable/placeholder interpolation");
                                if (ch > '~') {
                                    throw this.exception(beg, "Unrecognized character '" + ch + "' (" + ch + ") outside of variable/placeholder interpolation");
                                }
                                boolean varInterpolation = false;
                                int from = beg;
                                if (ch == '{') {
                                    ++from;
                                    varInterpolation = true;
                                }
                                if (this.isEmptyIgnoreSpace(from, endVar = this.skipUntilNonVar(from, varInterpolation))) {
                                    String msg;
                                    if (ch != ',' && ch != ';') {
                                        msg = "Blank operand '" + this.charAt(endVar) + '\'';
                                        if (ch != '%') throw this.exception(endVar, msg);
                                        msg = msg + " (hint: Write placeholders without '%' around them, or use {} if you must)";
                                        throw this.exception(endVar, msg);
                                    }
                                    msg = "Blank function argument";
                                    throw this.exception(endVar, msg);
                                }
                                String varName = this.expression.substring(from, endVar);
                                if (varName.equals("_")) {
                                    throw this.exception(from, "Reserved single underscore identifier");
                                }
                                Double val = CONSTANTS.get(varName);
                                rgt = val != null ? new ConstantExpr(val) : new Variable(varName);
                            }
                            if (cur.opPrecedence(Side.LEFT) < nxt.opPrecedence(Side.RIGHT)) {
                                rgt = this.compile(ofs + 1, end, rgt, cur, nxt, false);
                                ofs = this.offset;
                                Operator operator = nxt = ofs <= end ? OldMathCompiler.getOperator(this.expression.charAt(ofs)) : OPERAND;
                            }
                            if (lft != null) {
                                BiOperation op = new BiOperation(lft, cur, rgt);
                                lft = lft instanceof ConstantExpr && rgt instanceof ConstantExpr ? new ConstantExpr(op.eval(null)) : op;
                            } else {
                                lft = rgt;
                            }
                            cur = nxt;
                            if (pnd.opPrecedence(Side.LEFT) < cur.opPrecedence(Side.RIGHT)) {
                                if (cur.symbol == '(') {
                                    --ofs;
                                }
                                ++ofs;
                                continue block2;
                            }
                            if (ofs > end && cur != OPERAND) {
                                if (cur.side != Side.LEFT) throw this.exception(ofs, "Expression ends with a blank operand after operator '" + nxt.symbol + '\'');
                            }
                            if (ensureSubExpressionClosings) {
                                if (this.nonlexicalEnvironmentSubExpr.size() > 1) {
                                    throw new AssertionError((Object)("More than one non-lexical environment subexpressions: " + this.nonlexicalEnvironmentSubExpr));
                                }
                                Stack<Integer> first = this.nonlexicalEnvironmentSubExpr.peek();
                                if (!first.isEmpty()) {
                                    throw this.exception(first.peek(), "Unclosed parentheses", (Collection<Integer>)this.nonlexicalEnvironmentSubExpr.peek());
                                }
                            }
                            this.offset = ofs;
                            return lft;
                        }
                        while (this.charAt(ofs) != '}') {
                            if (ofs == end) {
                                throw this.exception(beginAt, "Variable interpolation not closed");
                            }
                            ++ofs;
                        }
                        --ofs;
                        break block48;
                    }
                    while (this.charAt(ofs) != ']') {
                        if (this.charAt(ofs) == '[') {
                            throw this.exception(ofs, "Nested time literal");
                        }
                        if (ofs == end) {
                            throw this.exception(beginAt, "Unclosed time literal");
                        }
                        ++ofs;
                    }
                    if ((ofs = this.skipWhitespace(ofs + 1, end)) <= end && this.charAt(ofs) == '[') {
                        throw this.exception(ofs, "Time literal immediately followed by another time literal");
                    }
                    ofs -= 2;
                }
                ++ofs;
            }
        }
        return lft;
    }

    private static Collection<Integer> pointerToName(int from, String name) {
        ArrayList<Integer> pointers = new ArrayList<Integer>(name.length());
        for (int i = 1; i < name.length(); ++i) {
            pointers.add(from + i);
        }
        return pointers;
    }

    private Sentence parseFunction(int beg, int end) {
        int index = this.expression.indexOf(40, beg);
        String func = this.expression.substring(beg, index).trim();
        Function function = FUNCTIONS.get(func);
        if (function == null) {
            String suggestion = OldMathCompiler.findFunction(func);
            suggestion = suggestion == null ? "" : "; Did you mean '" + suggestion + "' function?";
            throw this.exception(beg, "Function \"" + func + "\" not recognized" + suggestion, OldMathCompiler.pointerToName(beg, func));
        }
        Sentence[] args = this.parseArgs(index + 1, end);
        if (args.length < function.argCount) {
            throw this.exception(beg, "Too few arguments for function '" + func + "', expected: " + function.argCount + ", got: " + args.length, OldMathCompiler.pointerToName(beg, func));
        }
        if (args.length > function.argCount) {
            throw this.exception(beg, "Too many arguments for function '" + func + "', expected: " + function.argCount + ", got: " + args.length, OldMathCompiler.pointerToName(beg, func));
        }
        FunctionExpr fn = new FunctionExpr(func, function.function, args);
        if (Arrays.stream(args).allMatch(x -> x instanceof ConstantExpr)) {
            return new ConstantExpr(fn.eval(null));
        }
        return fn;
    }

    private static String spaces(int times) {
        char[] spaces = new char[times];
        Arrays.fill(spaces, ' ');
        return new String(spaces);
    }

    private ArithmeticException exception(int ofs, String txt) {
        return this.exception(ofs, txt, new ArrayList<Integer>());
    }

    private ArithmeticException exception(int ofs, String txt, Collection<Integer> pointers) {
        String errMsg = '\n' + txt + " at offset " + ofs + " in expression: \n\"" + this.expression + '\"';
        int max = 0;
        pointers.add(ofs);
        for (Integer pointer : pointers) {
            if (pointer <= max) continue;
            max = pointer;
        }
        StringBuilder pointerStr = new StringBuilder(OldMathCompiler.spaces(max + 2));
        pointers.forEach(x -> pointerStr.setCharAt(x + 1, '^'));
        return new ArithmeticException(errMsg + '\n' + pointerStr);
    }

    private static String findFunction(String name) {
        String lowerCaseName = name.toLowerCase(Locale.ENGLISH);
        Optional<String> exact = FUNCTIONS.keySet().stream().filter(x -> lowerCaseName.equals(x.toLowerCase(Locale.ENGLISH))).findFirst();
        return exact.orElseGet(() -> FUNCTIONS.keySet().stream().filter(x -> {
            String fn = x.toLowerCase(Locale.ENGLISH);
            return Math.abs(fn.length() - x.length()) < 10 && (lowerCaseName.contains(fn) || fn.contains(lowerCaseName));
        }).findFirst().orElse(null));
    }

    private boolean isEmptyIgnoreSpace(int begin, int end) {
        if (begin == end) {
            return true;
        }
        while (begin < end && this.expression.charAt(begin) == ' ') {
            ++begin;
        }
        return begin == end;
    }

    private Sentence[] parseArgs(int index, int end) {
        int closing = this.skipWhitespace(index, end);
        if (closing <= end && this.charAt(closing) == ')') {
            this.offset = closing + 1;
            return new Sentence[0];
        }
        ArrayList<Sentence> args = new ArrayList<Sentence>();
        int curIndex = index;
        do {
            this.nonlexicalEnvironmentSubExpr.push(new Stack());
            args.add(this.compile(curIndex, end));
            curIndex = this.offset++;
            if (curIndex > end) {
                throw this.exception(index - 1, "Cannot find closing pair for function");
            }
            curIndex = this.offset;
        } while (this.expression.charAt(this.offset - 1) != ')');
        return args.toArray(new Sentence[0]);
    }

    static {
        OldMathCompiler.registerOperators();
        OldMathCompiler.registerFunctions();
        OldMathCompiler.registerConstants();
    }

    public static final class Operator {
        public final char symbol;
        private final byte precedenceLeft;
        private final byte precedenceRight;
        private final Side side;
        private final TriDoubleFn function;

        protected Operator(char sym, int precedence, TriDoubleFn function) {
            this(sym, precedence, precedence, Side.NONE, function);
        }

        Operator(char sym, int precedenceL, int precedenceR, Side side, TriDoubleFn function) {
            this.symbol = sym;
            this.precedenceLeft = (byte)precedenceL;
            this.precedenceRight = (byte)precedenceR;
            this.side = side;
            this.function = function;
        }

        public String toString() {
            return "MathOperator['" + this.symbol + "']";
        }

        private byte opPrecedence(Side side) {
            if (this.side == Side.NONE || this.side != side) {
                return side == Side.LEFT ? this.precedenceLeft : this.precedenceRight;
            }
            return 127;
        }
    }

    private static enum Side  {
        RIGHT,
        LEFT,
        NONE;
    }

    @FunctionalInterface
    private static interface TriDoubleFn {
        public double apply(double var1, double var3);
    }

    @FunctionalInterface
    private static interface QuantumFunction {
        public double apply(FnArgs var1);
    }

    public static final class Function {
        private final QuantumFunction function;
        public final int argCount;

        private Function(QuantumFunction function, int argCount) {
            this.function = function;
            this.argCount = argCount;
        }
    }

    public static abstract class Sentence {
        public abstract double eval(java.util.function.Function<String, Double> var1);
    }

    private static class ConstantExpr
            extends Sentence {
        private final double value;

        public ConstantExpr(double value) {
            this.value = value;
        }

        @Override
        public double eval(java.util.function.Function<String, Double> variables) {
            return this.value;
        }

        public String toString() {
            return String.valueOf(this.value);
        }
    }

    private static class Variable
            extends Sentence {
        private final String name;

        public Variable(String name) {
            this.name = name;
        }

        @Override
        public double eval(java.util.function.Function<String, Double> variables) {
            Double val = variables.apply(this.name);
            if (val == null) {
                String guessFn = OldMathCompiler.findFunction(this.name);
                String suggestion = "";
                if (guessFn != null) {
                    suggestion = "; Did you mean to invoke '" + guessFn + "' function? If so, put parentheses after the name like '" + guessFn + "(args)'";
                }
                throw new RuntimeException("Unknown variable: '" + this.name + '\'' + suggestion);
            }
            return val;
        }

        public String toString() {
            return '{' + this.name + '}';
        }
    }

    private static final class BiOperation
            extends Sentence {
        private final Sentence left;
        private final Sentence right;
        private final Operator op;

        public BiOperation(Sentence left, Operator op, Sentence right) {
            this.left = left;
            this.right = right;
            this.op = op;
        }

        @Override
        public double eval(java.util.function.Function<String, Double> variables) {
            return this.op.function.apply(this.left.eval(variables), this.right == null ? Double.NaN : this.right.eval(variables));
        }

        public String toString() {
            return '(' + this.left.toString() + ' ' + this.op.symbol + (this.op.symbol == '(' ? "" : Character.valueOf(' ')) + this.right.toString() + ')';
        }
    }

    private static class FunctionExpr
            extends Sentence {
        private final String name;
        private final QuantumFunction handler;
        private final Sentence[] args;

        public FunctionExpr(String name, QuantumFunction handler, Sentence[] args) {
            this.name = name;
            this.handler = handler;
            this.args = args;
        }

        @Override
        public double eval(java.util.function.Function<String, Double> variables) {
            return this.handler.apply(new FnArgs(this, variables));
        }

        public String toString() {
            return this.name + '(' + (this.args.length == 0 ? "" : String.join((CharSequence)", ", (CharSequence[])Arrays.stream(this.args).map(Object::toString).toArray(String[]::new))) + ')';
        }
    }

    private static final class FnArgs {
        private final FunctionExpr func;
        private final java.util.function.Function<String, Double> variables;
        private int index = 0;

        private FnArgs(FunctionExpr func, java.util.function.Function<String, Double> variables) {
            this.func = func;
            this.variables = variables;
        }

        public double next() {
            return this.func.args[this.index++].eval(this.variables);
        }

        public double next(int i) {
            this.index = i;
            return this.func.args[this.index].eval(this.variables);
        }
    }
}

