package alexh.weak;

import static alexh.weak.DynamicChildLogic.using;
import alexh.LiteJoiner;
import java.util.Collection;
import java.util.Set;
import java.util.stream.Stream;

/**
 * Dynamic wrapper for Collection classes, much weaker functionality compared with List/Map as #get(Object) won't work
 * but provides error messaging and #children() to the same standard
 */
class DynamicCollection extends AbstractDynamic<Collection> implements Dynamic, Describer {

    static final String NO_KEY = "?";

    DynamicCollection(Collection inner) {
        super(inner);
    }

    @Override
    protected Object keyLiteral() {
        return ROOT_KEY;
    }

    @Override
    public Dynamic get(Object childKey) {
        if (inner.isEmpty()) return new ParentAbsence.Empty<>(this, childKey);
        return new ChildAbsence.Missing<>(this, childKey);
    }

    @Override
    public Stream<Dynamic> children() {
        return ((Collection<?>) inner).stream()
            .map(val -> val == null ? new ChildAbsence.Null(this, NO_KEY) : DynamicChild.from(this, NO_KEY, val));
    }

    @Override
    public String describe() {
        final String type = inner instanceof Set ? "Set" : "Collection";
        if (inner.isEmpty()) return "Empty-" + type;
        return type + "[size:" + inner.size() + "]";
    }

    @Override
    public String toString() {
        return keyLiteral() + ":" + describe();
    }

    static class Child extends DynamicCollection implements DynamicChild {

        private final Dynamic parent;
        private final Object key;

        Child(Dynamic parent, Object key, Collection inner) {
            super(inner);
            this.parent = parent;
            this.key = key;
        }

        @Override
        public Dynamic parent() {
            return parent;
        }

        @Override
        public Object keyLiteral() {
            return key;
        }

        @Override
        public String toString() {
            return LiteJoiner.on(ARROW).join(using(this).getAscendingKeyChainWithRoot()) + ":" +
                describe();
        }
    }
}
