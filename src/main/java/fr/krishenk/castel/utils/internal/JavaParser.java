package fr.krishenk.castel.utils.internal;

import fr.krishenk.castel.managers.GeneralizedEventWatcher;
import fr.krishenk.castel.utils.string.StringUtils;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.StringJoiner;

public class JavaParser {
    @NotNull
    private Class<?> methodParent;
    @NotNull
    private final String javaCode;
    private int index;
    @NotNull
    private final StringJoiner methodChainDesc;
    @NotNull
    private final List<Method> methodChain;
    private int previousIndex;

    public JavaParser(@NotNull Class<?> methodParent, @NotNull String javaCode) {
        super();
        this.methodParent = methodParent;
        this.javaCode = javaCode;
        this.methodChainDesc = new StringJoiner(" -> ");
        this.methodChain = new ArrayList<>();
    }

    @NotNull
    public final Void error(@NotNull String str) {
        Objects.requireNonNull(str);
        throw new IllegalArgumentException(str + " at " + this.index + ":\n   " + this.javaCode + "\n   " + StringUtils.repeat(' ', this.index - 1) + '^');
    }

    public final GeneralizedEventWatcher.MethodChain parse() {
        while (true) {
            this.index = this.javaCode.indexOf('.', this.previousIndex);
            String code;
            if (this.index == -1) {
                code = this.javaCode.substring(this.previousIndex);
                this.parseMethod(code);
                return new GeneralizedEventWatcher.MethodChain(this.methodChain);
            }

            code = this.javaCode.substring(this.previousIndex, this.index);
            this.parseMethod(code);
        }
    }

    public final void parseMethod(String methodCall) {
        if (methodCall.length() == 0) {
            this.error("Expected a method call (dot with no method)");
            throw new RuntimeException();
        } else {
            char firstChar = methodCall.charAt(0);
            if (!Character.isJavaIdentifierStart(firstChar)) {
                this.error("Invalid method call character");
                throw new RuntimeException();
            } else {
                boolean startedParens = false;
                boolean endedMethod = false;
                String methodStr = methodCall.substring(1);
                char[] methodCharArray = methodStr.toCharArray();
                int n = 0;

                for (int i = methodCharArray.length; n < i; ++n) {
                    char idParts = methodCharArray[n];
                    if (startedParens) {
                        if (endedMethod) {
                            this.error("Unexpected character after method call");
                            throw new RuntimeException();
                        }

                        if (idParts != ')') {
                            this.error("Expected method closing ) instead");
                            throw new RuntimeException();
                        }

                        endedMethod = true;
                    } else if (!Character.isJavaIdentifierPart(idParts)) {
                        if (idParts != '(') {
                            this.error("Invalid method call character '" + idParts + '\'');
                            throw new RuntimeException();
                        }

                        startedParens = true;
                    }
                }

                if (startedParens && !endedMethod) {
                    this.error("No method closing ) found");
                    throw new RuntimeException();
                } else {
                    methodStr = methodCall.substring(0, methodCall.length() - 2);
                    String methodName = methodStr;
                    this.methodChainDesc.add(this.methodParent.getName() + ' ' + methodCall);

                    try {
                        Method method = this.methodParent.getMethod(methodName);
                        method.setAccessible(true);
                        this.methodChain.add(method);
                        this.methodParent = method.getReturnType();
                    } catch (NoSuchMethodException var9) {
                        Method sigMismatch = GeneralizedEventWatcher.findMethodWithName(this.methodParent, methodName);
                        String hint = sigMismatch == null ? "" : " (Did you mean to use '" + sigMismatch + "' instead? If yes, you can't because this method requires parameters to be invoked) ";
                        this.error("Unknown method '" + this.methodChainDesc + "' " + hint);
                        throw new RuntimeException();
                    }

                    this.previousIndex = this.index + 1;
                }
            }
        }
    }


    public static GeneralizedEventWatcher.MethodChain parse(@NotNull Class<?> clazz, @NotNull String javaCode) {
        Objects.requireNonNull(clazz);
        Objects.requireNonNull(javaCode);
        return (new JavaParser(clazz, javaCode)).parse();
    }
}
