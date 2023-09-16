
package fr.krishenk.castel.libs.snakeyaml.validation;

import fr.krishenk.castel.libs.snakeyaml.exceptions.Mark;
import fr.krishenk.castel.libs.snakeyaml.nodes.Node;

public class ValidationFailure {
    private final Node node;
    private Mark marker;
    private Severity severity;
    private String message;

    public ValidationFailure(Severity severity, Node node, Mark marker, String message) {
        this.severity = severity;
        this.node = node;
        this.marker = marker == null ? node.getStartMark() : marker;
        this.message = message;
    }

    public Mark getMarker() {
        return this.marker;
    }

    public ValidationFailure withMarker(Mark marker) {
        this.marker = marker;
        return this;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Severity getSeverity() {
        return this.severity;
    }

    public Node getNode() {
        return this.node;
    }

    public String getMessage() {
        return this.message;
    }

    public enum Severity {
        ERROR,
        WARNING;
    }
}

