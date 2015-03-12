package alexh.weak;

import alexh.Fluent;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.function.Function;
import java.util.function.Supplier;

import static java.util.Arrays.asList;
import static java.util.Collections.unmodifiableMap;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toCollection;
import static java.util.stream.StreamSupport.stream;

public class Converter {

    private static final String VALUE_KEY = "value";

    private static final Map<Class<?>, Function<Object, ? extends Converter>> typeConverters =
        unmodifiableMap(
            new Fluent.LinkedHashMap<Class<?>, Function<Object, ? extends Converter>>()
                .append(Integer.class, IntConverter::new)
                .append(Long.class, LongConverter::new)
                .append(Double.class, DoubleConverter::new)
                .append(BigDecimal.class, DecimalConverter::new)
                .append(Dynamic.class, o -> convert(((Dynamic) o).asObject()))
                .append(Map.class, MapConverter::new)
                .append(Iterable.class, IterableConverter::new)
                .append(Optional.class, OptionalConverter::new)
                .append(java.util.Date.class, UtilDateInstantConverter::new)
                // fallback
                .append(Object.class, Converter::new)
        );

    public static Converter convert(Object o) {
        requireNonNull(o);

        if (o instanceof Object[])
            return convert(asList((Object[]) o));

        return typeConverters.getOrDefault(o.getClass(), typeConverters.entrySet().stream()
            .filter(entry -> entry.getKey().isInstance(o))
            .findFirst()
            .map(Map.Entry::getValue).get()).apply(o);
    }

    private static boolean doesNotThrow(Supplier<?> method) {
        try {
            method.get();
            return true;
        }
        catch (RuntimeException ex) { return false; }
    }

    protected final Object o;

    Converter(Object o) {
        this.o = o;
    }

    public String intoString() {
        return o.toString();
    }

    public boolean intoStringWorks() {
        return doesNotThrow(this::intoString);
    }

    public int intoInteger() {
        return intoDecimal().setScale(0, RoundingMode.HALF_UP).intValueExact();
    }

    public boolean intoIntegerWorks() {
        return doesNotThrow(this::intoInteger);
    }

    public long intoLong() {
        return intoDecimal().setScale(0, RoundingMode.HALF_UP).longValueExact();
    }

    public boolean intoLongWorks() {
        return doesNotThrow(this::intoLong);
    }

    public double intoDouble() {
        return intoDecimal().doubleValue();
    }

    public boolean intoDoubleWorks() {
        return doesNotThrow(this::intoDouble);
    }

    public BigDecimal intoDecimal() {
        return new BigDecimal(intoString());
    }

    public boolean intoDecimalWorks() {
        return doesNotThrow(this::intoDecimal);
    }

    public Map intoMap() {
        return new Fluent.HashMap<>().append(VALUE_KEY, o);
    }

    public boolean intoMapWorks() {
        return doesNotThrow(this::intoMap);
    }

    public List intoList() {
        List list = new ArrayList();
        list.add(o);
        return list;
    }

    public boolean intoListWorks() {
        return doesNotThrow(this::intoList);
    }

    /**
     * Converts to string & permissively parses as a date, ignoring time zone
     * defaults day->1, month->1, hour->0, minute->0, second->0, nanoseconds->0
     * @throws java.time.format.DateTimeParseException converted string is in an invalid format
     */
    public LocalDateTime intoLocalDateTime() {
        return LocalDateTime.from(DynamicTimeFormats.parseWithDefaults(intoString()));
    }

    /**
     * Converts to string & permissively parses as a date, requiring a time zone to be parsed
     * defaults day->1, month->1, hour->0, minute->0, second->0, nanoseconds->0
     * @throws java.time.format.DateTimeParseException converted string is in an invalid format
     */
    public ZonedDateTime intoZonedDateTime() {
        return ZonedDateTime.from(DynamicTimeFormats.parseWithDefaults(intoString()));
    }

    /**
     * Converts to string & permissively parses as a date, uses parsed time zone or falls back on input
     * defaults day->1, month->1, hour->0, minute->0, second->0, nanoseconds->0, time-zone->input
     * @param fallback time zone to use if value has none
     * @throws java.time.format.DateTimeParseException converted string is in an invalid format
     */
    public ZonedDateTime intoZonedDateTimeOrUse(ZoneId fallback){
        try { return intoZonedDateTime(); }
        catch (RuntimeException ex) { return intoLocalDateTime().atZone(fallback); }
    }

    static abstract class TypeConverter<T> extends Converter {

        TypeConverter(Object o) {
            super(o);
        }

        protected T literal() {
            return (T) o;
        }
    }

    static class IntConverter extends TypeConverter<Integer> {

        IntConverter(Object o) {
            super(o);
        }

        @Override
        public int intoInteger() {
            return literal();
        }

        @Override
        public long intoLong() {
            return (long) literal();
        }

        @Override
        public double intoDouble() {
            return (double) literal();
        }

        @Override
        public BigDecimal intoDecimal() {
            return new BigDecimal(literal());
        }
    }

    static class LongConverter extends TypeConverter<Long> {

        LongConverter(Object o) {
            super(o);
        }

        @Override
        public int intoInteger() {
            if (literal() < Integer.MIN_VALUE || literal() > Integer.MAX_VALUE)
                throw new IllegalArgumentException(literal() + " too large/small to be cast to int");
            return literal().intValue();
        }

        @Override
        public long intoLong() {
            return literal();
        }

        @Override
        public double intoDouble() {
            return (double) literal();
        }

        @Override
        public BigDecimal intoDecimal() {
            return new BigDecimal(literal());
        }
    }

    static class DoubleConverter extends TypeConverter<Double> {

        DoubleConverter(Object o) {
            super(o);
        }

        @Override
        public double intoDouble() {
            return literal();
        }

        @Override
        public BigDecimal intoDecimal() {
            return new BigDecimal(literal());
        }
    }

    static class DecimalConverter extends TypeConverter<BigDecimal> {

        DecimalConverter(Object o) {
            super(o);
        }

        @Override
        public BigDecimal intoDecimal() {
            return literal();
        }
    }

    static class MapConverter extends TypeConverter<Map<?, ?>> {

        MapConverter(Object o) {
            super(o);
        }

        private Optional<Object> value() {
            return Optional.ofNullable(literal().get(VALUE_KEY));
        }

        @Override
        public String intoString() {
            return value().map(o -> convert(o).intoString()).orElseGet(super::intoString);
        }

        @Override
        public int intoInteger() {
            return value().map(o -> convert(o).intoInteger()).orElseGet(super::intoInteger);
        }

        @Override
        public long intoLong() {
            return value().map(o -> convert(o).intoLong()).orElseGet(super::intoLong);
        }

        @Override
        public double intoDouble() {
            return value().map(o -> convert(o).intoDouble()).orElseGet(super::intoDouble);
        }

        @Override
        public BigDecimal intoDecimal() {
            return value().map(o -> convert(o).intoDecimal()).orElseGet(super::intoDecimal);
        }

        @Override
        public Map intoMap() {
            return new LinkedHashMap<>(literal());
        }

        @Override
        public List intoList() {
            return new ArrayList<>(literal().values());
        }
    }

    static class IterableConverter extends TypeConverter<Iterable<?>> {

        IterableConverter(Object o) {
            super(o);
        }

        private Optional<Object> onlyElement() {
            Iterator<?> iterator = literal().iterator();
            return Optional.ofNullable(iterator.hasNext() ? iterator.next() : null)
                .filter(o -> !iterator.hasNext());
        }

        @Override
        public String intoString() {
            return onlyElement().map(o -> convert(o).intoString()).orElseGet(super::intoString);
        }

        @Override
        public int intoInteger() {
            return onlyElement().map(o -> convert(o).intoInteger()).orElseGet(super::intoInteger);
        }

        @Override
        public long intoLong() {
            return onlyElement().map(o -> convert(o).intoLong()).orElseGet(super::intoLong);
        }

        @Override
        public double intoDouble() {
            return onlyElement().map(o -> convert(o).intoDouble()).orElseGet(super::intoDouble);
        }

        @Override
        public BigDecimal intoDecimal() {
            return onlyElement().map(o -> convert(o).intoDecimal()).orElseGet(super::intoDecimal);
        }

        @Override
        public Map intoMap() {
            Map<Integer, Object> map = new LinkedHashMap<>();
            Iterator<?> iterator = literal().iterator();
            for (int i = 0; iterator.hasNext(); ++i)
                map.put(i, iterator.next());
            return map;
        }

        @Override
        public List intoList() {
            return stream(literal().spliterator(), false).collect(toCollection(ArrayList::new));
        }
    }

    static class OptionalConverter extends TypeConverter<Optional<?>> {

        OptionalConverter(Object o) {
            super(o);
        }

        @Override
        public String intoString() {
            return literal().map(o -> convert(o).intoString()).orElseGet(super::intoString);
        }

        @Override
        public int intoInteger() {
            return literal().map(o -> convert(o).intoInteger()).orElseGet(super::intoInteger);
        }

        @Override
        public long intoLong() {
            return literal().map(o -> convert(o).intoLong()).orElseGet(super::intoLong);
        }

        @Override
        public double intoDouble() {
            return literal().map(o -> convert(o).intoDouble()).orElseGet(super::intoDouble);
        }

        @Override
        public BigDecimal intoDecimal() {
            return literal().map(o -> convert(o).intoDecimal()).orElseGet(super::intoDecimal);
        }

        @Override
        public Map intoMap() {
            return literal().map(o -> convert(o).intoMap()).orElseGet(LinkedHashMap::new);
        }

        @Override
        public List intoList() {
            return literal().map(o -> convert(o).intoList()).orElseGet(ArrayList::new);
        }
    }

    static class UtilDateInstantConverter extends TypeConverter<java.util.Date> {

        public UtilDateInstantConverter(Object o) {
            super(o);
        }

        @Override
        public BigDecimal intoDecimal() {
            return new BigDecimal(intoLong());
        }

        @Override
        public double intoDouble() {
            return intoLong();
        }

        @Override
        public long intoLong() {
            return literal().getTime();
        }

        @Override
        public String intoString() {
            return String.valueOf(literal().getTime());
        }
    }
}
