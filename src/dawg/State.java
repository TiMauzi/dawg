package dawg;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * Representation of a state within a DAWG.
 *
 * @author Tim Sockel
 */
class State {

    private int name;

    private HashSet<Edge> edges;

    private Edge suffixPointer;

    public State(int name) {
        this.name = name;
        this.edges = new HashSet<>();
    }

    public void addEdge(Edge edge) {
        edges.add(edge);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (obj.getClass() != this.getClass()) {
            return false;
        }
        return ((State) obj).name == this.name;
    }

    public int getName() {
        return name;
    }

    public void setName(int name) {
        this.name = name;
    }

    public HashSet<Edge> getEdges() {
        return edges;
    }

    public Edge getEdge(String label) {
        for (Edge e: this.edges) {
            if (e.getLabel().equals(label)) {
                return e;
            }
        }
        return null;
    }

    public Edge getEdge(char label) {
        return getEdge(String.valueOf(label));
    }

    public Edge getEdge(State endState) {
        for (Edge e: this.edges) {
            if (e.getEndState().equals(endState)) {
                return e;
            }
        }
        return null;
    }

    public void setEdges(HashSet<Edge> edges) {
        this.edges = edges;
    }

    public HashSet<String> getEdgeLabels() {
        HashSet<String> labels = new HashSet<>();
        for (Edge e: this.edges) {
            labels.add(e.getLabel());
        }
        return labels;
    }

    public Edge getSuffixPointer() {
        return suffixPointer;
    }

    public void setSuffixPointer(Edge suffixPointer) {
        this.suffixPointer = suffixPointer;
    }

    public void setSuffixPointer(State endState) {
        Edge suffixPointerEdge = new Edge("", endState, EdgeType.suffixPointer);
        this.setSuffixPointer(suffixPointerEdge);
    }


}
