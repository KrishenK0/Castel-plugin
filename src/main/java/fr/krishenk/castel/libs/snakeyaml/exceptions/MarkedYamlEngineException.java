
package fr.krishenk.castel.libs.snakeyaml.exceptions;

import java.util.Objects;

public class MarkedYamlEngineException
extends YamlEngineException {
    private final String context;
    private final String problem;
    private final Mark contextMark;
    private final Mark problemMark;

    protected MarkedYamlEngineException(String context, Mark contextMark, String problem, Mark problemMark, Throwable cause) {
        super(context + "; " + problem + "; " + problemMark, cause);
        this.context = context;
        this.contextMark = contextMark;
        this.problem = problem;
        this.problemMark = Objects.requireNonNull(problemMark, "problemMark must be provided");
    }

    protected MarkedYamlEngineException(String context, Mark contextMark, String problem, Mark problemMark) {
        this(context, contextMark, problem, problemMark, null);
    }

    @Override
    public String getMessage() {
        return this.toString();
    }

    @Override
    public String toString() {
        StringBuilder lines = new StringBuilder();
        if (this.context != null) {
            lines.append(this.context);
            lines.append('\n');
        }
        if (this.contextMark != null && (this.problem == null || this.problemMark == null || this.contextMark.getName().equals(this.problemMark.getName()) || this.contextMark.getLine() != this.problemMark.getLine() || this.contextMark.getColumn() != this.problemMark.getColumn())) {
            lines.append(this.contextMark);
            lines.append('\n');
        }
        if (this.problem != null) {
            lines.append(this.problem);
            lines.append('\n');
        }
        if (this.problemMark != null) {
            lines.append(this.problemMark);
            lines.append('\n');
        }
        return lines.toString();
    }

    public String getContext() {
        return this.context;
    }

    public Mark getContextMark() {
        return this.contextMark;
    }

    public String getProblem() {
        return this.problem;
    }

    public Mark getProblemMark() {
        return this.problemMark;
    }
}

