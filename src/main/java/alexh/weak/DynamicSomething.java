package alexh.weak;

import alexh.LiteJoiner;
import java.util.stream.Stream;

import static alexh.weak.DynamicChildLogic.using;

class DynamicSomething extends AbstractDynamic<Object> implements Dynamic, TypeDescriber {

    public DynamicSomething(Object inner) {
        super(inner);
    }

    @Override
    public Dynamic get(Object key) {
        return new ParentAbsence.Barren<>(this, key);
    }

    @Override
    public String describeType() {
        return inner.getClass().getSimpleName();
    }

    @Override
    public String toString() {
        return "root:" + describeType();
    }

    @Override
    public Stream<Dynamic> children() {
        return Stream.empty();
    }

    static class Child extends DynamicSomething implements DynamicChild {

        private final Dynamic parent;
        private final Object key;

        Child(Dynamic parent, Object key, Object inner) {
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
                describeType();
        }
    }
}
