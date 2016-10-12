package alexh.weak;

import java.util.Iterator;
import java.util.stream.Stream;

class BreadthChildIterator implements Iterator<Dynamic> {
    private final Dynamic root;
    private int depth;
    private Iterator<Dynamic> current;

    BreadthChildIterator(Dynamic root) {
        this.root = root;
        depth = 1;
        current = root.children().iterator();
    }

    private Stream<Dynamic> nextDepth() {
        Stream<Dynamic> childrenAtNextDepth = root.children();
        int nextDepth = 1;
        while (nextDepth <= this.depth) {
            childrenAtNextDepth = childrenAtNextDepth.flatMap(Dynamic::children);
            nextDepth += 1;
        }
        return childrenAtNextDepth;
    }

    private boolean moveDepthIfAvailable() {
        Iterator<Dynamic> nextDepth = nextDepth().iterator();
        if (nextDepth.hasNext()) {
            current = nextDepth;
            depth += 1;
            return true;
        }
        return false;
    }

    @Override
    public boolean hasNext() {
        return current.hasNext() || moveDepthIfAvailable();
    }

    @Override
    public Dynamic next() {
        if (!current.hasNext()) moveDepthIfAvailable();
        return current.next();
    }
}