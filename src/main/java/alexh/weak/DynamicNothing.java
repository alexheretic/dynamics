package alexh.weak;

import java.util.NoSuchElementException;
import java.util.stream.Stream;

enum DynamicNothing implements Dynamic, TypeDescriber {
    INSTANCE;

    @Override
    public Dynamic get(Object key) {
        return new ParentAbsence.Barren<>(this, key);
    }

    @Override
    public boolean isPresent() {
        return false;
    }

    @Override
    public Object asObject() {
        throw new NoSuchElementException("null 'root' premature end of path *root*");
    }

    @Override
    public Stream<Dynamic> children() {
        return Stream.empty();
    }

    @Override
    public Dynamic key() {
        return DynamicChild.key(this, ROOT_KEY);
    }

    @Override
    public String describeType() {
        return "null";
    }

    @Override
    public String toString() {
        return ROOT_KEY + ":" + describeType();
    }
}
