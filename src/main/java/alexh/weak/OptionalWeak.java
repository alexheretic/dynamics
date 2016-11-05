package alexh.weak;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

/** Enhanced version of an Optional<Weak>, similar methods to Weak that return Optionals and don't throw */
public class OptionalWeak<W extends Weak<W>>  {

    private static final OptionalWeak EMPTY = new OptionalWeak(Optional.empty());

    public static <T extends Weak<T>> OptionalWeak<T> of(T val) {
        return new OptionalWeak<>(Optional.ofNullable(val));
    }

    @SuppressWarnings("unchecked")
    public static <T extends Weak<T>> OptionalWeak<T> empty() {
        return (OptionalWeak<T>) EMPTY;
    }

    private final Optional<W> inner;

    private OptionalWeak(Optional<W> inner) {
        this.inner = inner.filter(d -> d.isPresent());
    }

    public W get() {
        return inner.get();
    }

    public boolean isPresent() {
        return inner.isPresent();
    }

    public void ifPresent(Consumer<? super W> consumer) {
        inner.ifPresent(consumer);
    }

    public OptionalWeak<W> filter(Predicate<? super W> predicate) {
        Objects.requireNonNull(predicate);
        if (!isPresent()) return this;
        return predicate.test(get()) ? this : empty();
    }

    /** @see Optional#map(Function) */
    public <U> Optional<U> map(Function<? super W, ? extends U> mapper) {
        return inner.map(mapper);
    }

    /** @see Optional#flatMap(Function) */
    public <U> Optional<U> flatMap(Function<? super W, Optional<U>> mapper) {
        return inner.flatMap(mapper);
    }

    /** @see Optional#orElse(Object) */
    public W orElse(W other) {
        return inner.orElse(other);
    }

    /** @see Optional#orElseGet(Supplier) */
    public W orElseGet(Supplier<? extends W> other) {
        return inner.orElseGet(other);
    }

    /** @see Optional#orElseThrow(Supplier) */
    public <X extends Throwable> W orElseThrow(Supplier<? extends X> exceptionSupplier) throws X {
       return inner.orElseThrow(exceptionSupplier);
    }

    /**
     * @return inner value as an optional
     */
    public Optional<Object> asObject() {
        return inner.map(d -> d.asObject());
    }

    /**
     * Returns inner value as optional of input type, if the inner value is absent or not an instance of the input
     * type an empty optional is returned
     * @param type cast type
     * @param <T> cast type
     * @return inner value as optional of input type
     */
    public <T> Optional<T> as(Class<T> type) {
        return inner.filter(d -> d.is(type)).map(d -> d.as(type));
    }

    /**
     * Shortcut for as(String.class)
     * @see OptionalWeak#as(Class)
     */
    public Optional<String> asString() {
        return as(String.class);
    }

    /**
     * Shortcut for as(List.class), with malleable generic type
     * @see OptionalWeak#as(Class)
     */
    public <T> Optional<List<T>> asList() {
        return inner.filter(d -> d.isList()).map(d -> d.asList());
    }

    /**
     * Shortcut for as(Map.class), with malleable generic type
     * @see OptionalWeak#as(Class)
     */
    public <K, V> Optional<Map<K, V>> asMap() {
        return inner.filter(d -> d.isMap()).map(d -> d.asMap());
    }

    public ConverterMaybe convert() {
        return new ConverterMaybe(inner);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        OptionalWeak<?> that = (OptionalWeak<?>) o;
        return Objects.equals(inner, that.inner);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(inner);
    }

    @Override
    public String toString() {
        return inner.toString();
    }
}
