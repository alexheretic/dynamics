package alexh.weak;

import alexh.LiteJoiner;
import java.util.List;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static alexh.weak.DynamicChildLogic.using;
import static java.lang.String.format;

class DynamicList extends AbstractDynamic<List> implements Dynamic, TypeDescriber, AvailabilityDescriber {

    public DynamicList(List inner) {
        super(inner);
    }

    /** @return nullable */
    private Integer keyToIndex(Object key) {
        Integer index = null;

        if (key instanceof Integer) {
            index = (Integer) key;
        }
        else if (key instanceof Number) {
            index = ((Number) key).intValue();
        }
        else if (key instanceof String) {
            try { index = Integer.valueOf((String) key); }
            catch (NumberFormatException ex) {/* no need to handle directly */}
        }

        return index != null && index >= 0 && index < inner.size() ? index : null;
    }

    @Override
    public Dynamic get(Object key) {
        if (inner.isEmpty()) return new ParentAbsence.Empty<>(this, key);

        final Integer index = keyToIndex(key);
        if (index == null) return new ChildAbsence.Missing<>(this, key);

        final Object val = inner.get(index);
        return val != null ? DynamicChild.from(this, key, val) : new ChildAbsence.Null(this, key);
    }

    @Override
    public Stream<Dynamic> children() {
        return IntStream.range(0, inner.size()).mapToObj(this::get);
    }

    @Override
    public String describeType() {
        return "List";
    }

    @Override
    public String describeAvailability() {
        return inner.isEmpty() ? "[]" : format("[0..%d]", inner.size()-1);
    }

    @Override
    public String toString() {
        return "root:"+ describeType() + describeAvailability();
    }

    static class Child extends DynamicList implements DynamicChild {

        private final Dynamic parent;
        private final Object key;

        Child(Dynamic parent, Object key, List inner) {
            super(inner);
            this.parent = parent;
            this.key = key;
        }

        @Override
        public Dynamic parent() {
            return parent;
        }

        @Override
        public Object key() {
            return key;
        }

        @Override
        public String toString() {
            return LiteJoiner.on(ARROW).join(using(this).getAscendingKeyChainWithRoot()) + ":" +
                describeType() + describeAvailability();
        }
    }
}
