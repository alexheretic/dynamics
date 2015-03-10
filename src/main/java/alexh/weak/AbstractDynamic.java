package alexh.weak;

public abstract class AbstractDynamic<T> implements Dynamic {

    protected final T inner;

    public AbstractDynamic(T inner) {
        this.inner = inner;
    }

    @Override
    public boolean isPresent() {
        return true;
    }

    @Override
    public Object asObject() {
        return inner;
    }

    @Override
    public int hashCode() {
        return inner.hashCode();
    }

    protected abstract Object keyLiteral();

    @Override
    public Dynamic key() {
        return DynamicChild.key(this, keyLiteral());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AbstractDynamic other = (AbstractDynamic) o;
        return inner.equals(other.inner);
    }
}
