package ca.ualberta.cs.smr.core.dependenceGraph;

import org.jgrapht.Graph;
import org.jgrapht.traverse.DepthFirstIterator;
import org.jgrapht.util.TypeUtil;

import java.util.ArrayDeque;
import java.util.Deque;

public class ModifiedDepthFirstIterator<V, E> extends DepthFirstIterator<V, E> {


    public ModifiedDepthFirstIterator(Graph<V, E> graph) {
        super(graph);
    }

    public static final Object SENTINEL = new Object();


    private Deque<Object> stack = new ArrayDeque<>();

    @Override
    protected boolean isConnectedComponentExhausted()
    {
        for (;;) {
            if (stack.isEmpty()) {
                return true;
            }
            if (stack.getLast() != SENTINEL) {
                // Found a non-sentinel.
                return false;
            }

            // Found a sentinel: pop it, record the finish time,
            // and then loop to check the rest of the stack.

            // Pop null we peeked at above.
            stack.removeLast();

            // This will pop corresponding vertex to be recorded as finished.
            recordFinish();
        }
    }

    @Override
    protected V provideNextVertex()
    {
        V v;
        for (;;) {
            Object o = stack.removeLast();
            if (o == SENTINEL) {
                // This is a finish-time sentinel we previously pushed.
                recordFinish();
                // Now carry on with another pop until we find a non-sentinel
            } else {
                // Got a real vertex to start working on
                v = TypeUtil.uncheckedCast(o);
                break;
            }
        }

        // Push a sentinel for v onto the stack so that we'll know
        // when we're done with it.
        stack.addLast(v);
        stack.addLast(SENTINEL);
        putSeenData(v, VisitColor.GRAY);
        return v;
    }

    private void recordFinish()
    {
        V v = TypeUtil.uncheckedCast(stack.removeLast());
        putSeenData(v, VisitColor.BLACK);
        finishVertex(v);
    }

    /**
     * Retrieves the LIFO stack of vertices which have been encountered but not yet visited (WHITE).
     * This stack also contains <em>sentinel</em> entries representing vertices which have been
     * visited but are still GRAY. A sentinel entry is a sequence (v, SENTINEL), whereas a
     * non-sentinel entry is just (v).
     *
     * @return stack
     */
    public Deque<Object> getStack()
    {
        return stack;
    }

    @Override
    protected void encounterVertex(V vertex, E edge)
    {
        Node node = (Node) vertex;
        if(node.hasManyEdges()) {
            // If there is more than one edge and this is our first time encountering the vertex, then we cannot add
            // it to the stack so we can maintain ordering dependence.
            node.decreaseEdgeCount();
        }
        else {
            // If this vertex has one edge or no edges, we can add it to the stack because ordering dependence is
            // preserved.
            putSeenData(vertex, VisitColor.WHITE);
            stack.addLast(vertex);
        }
    }

    @Override
    protected void encounterVertexAgain(V vertex, E edge)
    {
        VisitColor color = getSeenData(vertex);
        Node node = (Node) vertex;
        if (color != VisitColor.WHITE) {
            // We've already visited this vertex; no need to mess with the
            // stack (either it's BLACK and not there at all, or it's GRAY
            // and therefore just a sentinel).
            return;
        }
        if(node.hasManyEdges()) {
            // We've already visited this vertex but the vertex has more than one edge that it depends on. We cannot
            // add it until the only edge is the one we are traversing currently.
            node.decreaseEdgeCount();
            return;
        }
        // Since we've encountered it before, and it's still WHITE, it
        // *must* be on the stack. Use removeLastOccurrence on the
        // assumption that for typical topologies and traversals,
        // it's likely to be nearer the top of the stack than
        // the bottom of the stack.
        boolean found = stack.removeLastOccurrence(vertex);
        assert (found);
        stack.addLast(vertex);
    }

}
