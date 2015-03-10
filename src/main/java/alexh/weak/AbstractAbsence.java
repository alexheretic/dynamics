package alexh.weak;

import alexh.LiteJoiner;
import java.util.Objects;
import java.util.stream.Stream;

abstract class AbstractAbsence<Parent extends Dynamic> implements DynamicChild {

    protected final Parent parent;
    protected final Object key;

    AbstractAbsence(Parent parent, Object key) {
        this.parent = parent;
        this.key = key;
    }

    @Override
    public Parent parent() {
        return parent;
    }

    @Override
    public Dynamic key() {
        return DynamicChild.key(this, key);
    }

    @Override
    public boolean isPresent() {
        return false;
    }

    @Override
    public Stream<Dynamic> children() {
        return Stream.empty();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AbstractAbsence other = (AbstractAbsence) o;
        return Objects.equals(parent, other.parent)
            && Objects.equals(key, other.key);
    }

    @Override
    public int hashCode() {
        return Objects.hash(parent, key);
    }

    @Override
    public String toString() {
        return LiteJoiner.on(ARROW).join(DynamicChildLogic.using(this).getAscendingKeyChainWithRoot()) + ":absent";
    }
}
