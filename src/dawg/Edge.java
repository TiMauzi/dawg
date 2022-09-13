package dawg;

/**
 * Representation of a DAWG's edge.
 *
 * @author Tim Sockel
 */
class Edge {

    private String label;
    private State endState;
    private EdgeType type;


    public Edge(String label, State endState) {
        this(label, endState, EdgeType.primary);
    }

    public Edge(String label, State endState, EdgeType type) {
        this.label = label;
        this.endState = endState;
        this.type = type;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public State getEndState() {
        return endState;
    }

    public void setEndState(State endState) {
        this.endState = endState;
    }

    public EdgeType getType() {
        return type;
    }

    public void setType(EdgeType type) {
        this.type = type;
    }

    public void setType(String typeName) {
        switch (typeName) {
            case "secondary" -> this.type = EdgeType.secondary;
            case "suffixPointer" -> this.type = EdgeType.suffixPointer;
            default -> this.type = EdgeType.primary;
        }
    }

}
