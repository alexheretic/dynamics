package alexh;

import java.util.function.*;

public class Unchecker {

    public static <T> Supplier<T> uncheck(ThrowingSupplier<T> supplier) {
        return () -> {
            try { return supplier.get(); }
            catch (RuntimeException | Error e) { throw e; }
            catch (Throwable t) { throw new RuntimeException(t); }
        };
    }
    
    public static <T> T uncheckedGet(ThrowingSupplier<T> supplier) {
        return uncheck(supplier).get();
    }
    
    public static void unchecked(ThrowingRunnable runnable) {
        uncheck(runnable).run();
    }
    
    public static <In, Out> Function<In, Out> uncheck(ThrowingFunction<In, Out> function) {
        return (In in) -> uncheckedGet(() -> function.apply(in));
    }
    
    public static <In1, In2, Out> BiFunction<In1, In2, Out> uncheck(ThrowingBiFunction<In1, In2, Out> function) {
        return (In1 in1, In2 in2) -> uncheckedGet(() -> function.apply(in1, in2));
    }

    public static Runnable uncheck(ThrowingRunnable runnable) {
        return () -> uncheckedGet(() -> {
            runnable.run();
            return null;
        });
    }
    
    public static <T> Consumer<T> uncheck(ThrowingConsumer<T> consumer) {
        return (T t) -> unchecked(() -> consumer.accept(t));
    }

    public static <T, U> BiConsumer<T, U> uncheck(ThrowingBiConsumer<T, U> consumer) {
        return (T t, U u) -> unchecked(() -> consumer.accept(t, u));
    }

    public interface ThrowingSupplier<T> {
        T get() throws Throwable;
    }
    
    public interface ThrowingRunnable {
        void run() throws Throwable;
    }

    public interface ThrowingFunction<In, Out> {
        Out apply(In in) throws Throwable;
    }
    
    public interface ThrowingBiFunction<In1, In2, Out> {
        Out apply(In1 in1, In2 in2) throws Throwable;
    }
    
    public interface ThrowingConsumer<T> {
        void accept(T t) throws Throwable;
    }
    
    public interface ThrowingBiConsumer<T, U> {
        void accept(T t, U u) throws Throwable;
    }
}
