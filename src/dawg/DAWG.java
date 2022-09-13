package dawg;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

/**
 * A class for indexing text in a DAWG structure.
 *
 * @author Tim Sockel
 * @see "Blumer, A. et al. (1985). <i>The smallest automation recognizing the subwords of a text.</i>
 * Theoretical Computer Science, 40, 31–55.
 * DOI: <a href="https://www.doi.org/10.1016/0304-3975(85)90157-4">10.1016/0304-3975(85)90157-4</a>"
 */
public class DAWG {

    private ArrayList<State> states = new ArrayList<>();
    private int curStateNum = 0;

    private State source;

    public DAWG(List<String> wList, boolean verbose) {

        long indexCounter = 0;
        long startTime = System.currentTimeMillis();

        this.source = new State(curStateNum++);
        this.states.add(this.source);
        State currentSink = this.source;

        boolean secureStop = false;
        for (String w: wList) {
            char[] wArray = w.toCharArray();
            for (char a: wArray) {
                currentSink = this.update(currentSink, a);
                if (verbose && indexCounter++ % 1000 == 999) {
                    long timeStamp = System.currentTimeMillis() - startTime;
                    System.out.println("Indexed " + indexCounter + " symbols ... (" + timeStamp + " ms; "+ Runtime.getRuntime().freeMemory() + " Bytes of memory left)");
                }
                if (Runtime.getRuntime().freeMemory() <= 2000000) {
                    secureStop = true;
                    System.out.println("It seems like this might be too much data to analyze at once. " +
                            "Therefore, indexing has been interrupted and stopped here.");
                    break;
                }
            }
            if (secureStop) break;
        }

        long completeTime = System.currentTimeMillis() - startTime;
        System.out.println("==============\nIndexed " + indexCounter + " symbols in total. " +
                "This took " + completeTime + " ms.");
    }

    public DAWG(List<String> wList) {
        this(wList, true);
    }

    /**
     * Adds another symbol to the DAWG.
     * @param currentSink The state, which is the current sink of the DAWG.
     * @param a The symbol to be added.
     * @return The new sink.
     */
    private State update(State currentSink, char a) {
        // 1. Create a state named newsink and a primary edge labeled a from currentsink to newsink.
        State newSink = new State(curStateNum++);
        this.states.add(newSink);
        Edge aEdge = new Edge(Character.toString(a), newSink, EdgeType.primary);
        currentSink.addEdge(aEdge);

        // 2. Let currentstate be currentsink and let suffixstate be undefined.
        State currentState = currentSink;
        State suffixState = null;

        // 3. While currentstate is not source and suffixstate is undefined do:
        while (!currentState.equals(this.source) && suffixState == null) {
            // (a) Let currentstate be the state pointed to by the suffix pointer of currentstate.
            currentState = currentState.getSuffixPointer().getEndState();
            /* (b) Check whether currentstate has an outgoing edge labeled a.
             *  (1) If currentstate does not have an outgoing edge labeled a, then create
             *      a secondary edge from currentstate to newsink labeled a.
             */
            if (!currentState.getEdgeLabels().contains(Character.toString(a))) {
                Edge secondaryEdge = new Edge(Character.toString(a), newSink, EdgeType.secondary);
                currentState.addEdge(secondaryEdge);
            }
            /*  (2) Else, if currentstate has a primary outgoing edge labeled a, then let
             *      suffixstate be the state to which this edge leads.
             */
            else {
                Edge outgoingEdge = currentState.getEdge(Character.toString(a));
                if (outgoingEdge.getType().equals(EdgeType.primary)) {
                    suffixState = outgoingEdge.getEndState();
                }
            /*  (3) Else (currentstate has a secondary outgoing edge labeled a):
             */
                else {
                    // (a) Let childstate be the state that the outgoing edge labeled a leads to.
                    State childState = outgoingEdge.getEndState();
                    // (b) Let suffixstate be split(currentstate, childstate).
                    suffixState = this.split(currentState, childState);
                }
            }
        }

        // 4. If suffixstate is still undefined, let suffixstate be source.
        if (suffixState == null) suffixState = source;

        // 5. Set the suffix pointer of newsink to point to suffixstate and return newsink.
        newSink.setSuffixPointer(suffixState);
        return newSink;
    }

    /**
     * The "split case" of the algorithm.
     * @param parentState The parent state.
     * @param childState The child state.
     * @return The suffix state.
     */
    private State split(State parentState, State childState) {
        // 1. Create a state called newchildstate.
        State newChildState = new State(curStateNum++);
        this.states.add(newChildState);

        // 2. Make the secondary edge from parentstate to childstate into a primary edge
        //    from parentstate to newchildstate (with the same label).
        for (Edge e: parentState.getEdges()) {
            if (e.getEndState().equals(childState)) {
                e.setType(EdgeType.primary);
                e.setEndState(newChildState);
            }
        }

        // 3. For every primary and secondary outgoing edge of childstate, create a secondary
        //    outgoing edge of newchildstate with the same label and leading to the same state.
        for (Edge e: childState.getEdges()) {
            if (!e.getType().equals(EdgeType.suffixPointer)) {
                Edge newEdge = new Edge(e.getLabel(), e.getEndState(), EdgeType.secondary);
                newChildState.addEdge(newEdge);
            }
        }

        // 4. Set the suffix pointer of newchildstate equal to that of childstate.
        newChildState.setSuffixPointer(childState.getSuffixPointer());

        // 5. Reset the suffix pointer of childstate to point to newchildstate.
        childState.setSuffixPointer(newChildState);

        // 6. Let currentstate be parentstate.
        State currentState = parentState;

        // 7. While currentstate is not source do:
        while (!currentState.equals(source)) {
            // (a) Let currentstate be the state pointed to by the suffix pointer of currentstate.
            currentState = currentState.getSuffixPointer().getEndState();
            // (b) If currentstate has a secondary edge to childstate,
            //     make it a secondary edge to newchildstate (with the same label).
            boolean noSecondaryEdges = true;
            for (Edge e: currentState.getEdges()) {
                if (e.getType().equals(EdgeType.secondary) && e.getEndState().equals(childState)) {
                    noSecondaryEdges = false;
                    e.setEndState(newChildState);
                }
            }
            // (c) Else, break out of the while loop.
            if (noSecondaryEdges) break;
        }

        // 8. Return newchildstate.
        return newChildState;
    }

    // Output methods:

    /**
     * Prints all primary and secondary edges of the DAWG. Suffix pointers are no longer considered here.
     */
    public void printEdges() {
        System.out.println("=== Edges  ===");
        for (State state: states) {
            HashSet<Edge> curEdges = state.getEdges();
            for (Edge e: curEdges) {
                System.out.println(state.getName() + "\t--\"" + e.getLabel() + "\"-[" + e.getType() + "]-->\t"
                        + e.getEndState().getName());
            }
        }
        System.out.println("==============");
    }

    /**
     * Saves all edges of the <tt>DAWG</tt> to a file.
     * @param inputFilePath The original text's file path.
     */
    public void saveEdges(String inputFilePath) {
        System.out.println("=== Saving ===");
        String path = inputFilePath + "_edges.txt";
        if (System.getProperty("os.name").startsWith("Windows")) {  // Windows uses backslashes, Unix doesn't.
            path = path.replaceAll("/", "\\\\");
        }
        File outputFile = new File(path);
        try {
            if (outputFile.createNewFile()) {
                System.out.println("New file \"" + path + "\" created.");
            } else {
                System.out.println("Use existing file \"" + path + "\".");
            }
            FileWriter edgeWriter = new FileWriter(path);
            for (State state: states) {
                HashSet<Edge> curEdges = state.getEdges();
                for (Edge e: curEdges) {
                    edgeWriter.write(state.getName() + "\t--\"" + e.getLabel() + "\"-[" + e.getType() + "]-->\t"
                            + e.getEndState().getName() + "\n");
                }
            }
            System.out.println("Wrote all edge information to this file.");
            edgeWriter.close();
        } catch (IOException e) {
            System.out.println("This didn't work ...");
        }
        System.out.println("==============");
    }

    /**
     * Counts all states in the DAWG and prints out the sum.
     */
    public void printNumberOfStates() {
        System.out.println("=== States ===");
        int numberOfStates = 0;
        for (State s: this.states) {
            numberOfStates++;
        }
        System.out.println("The DAWG contains " + numberOfStates + " states in total.\n==============");
    }

    /**
     * Checks whether or not a specific infix is contained in the DAWG.
     * @param query The infix string that should be searched for.
     * @return <tt>true</tt> iff the DAWG contains a path for the <tt>query</tt>.
     */
    public boolean search(String query) {
        char[] queryChars = query.toCharArray();
        boolean output = true;
        State curState = this.source;
        for (char c: queryChars) {
            if (curState.getEdge(c) != null) {
                curState = curState.getEdge(c).getEndState();
            } else {
                return false;
            }
        }
        return true;
    }
}
