package alexh.weak;

import alexh.Fluent;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.function.Function;

import static java.util.Collections.unmodifiableMap;

public class Converter {

    private static final String VALUE_KEY = "value";

    private static final Map<Class<?>, Function<Object, ? extends Converter>> typeConverters =
        unmodifiableMap(new Fluent.LinkedHashMap<Class<?>, Function<Object, ? extends Converter>>()
                .append(Integer.class, IntConverter::new)
                .append(Long.class, LongConverter::new)
                .append(Double.class, DoubleConverter::new)
                .append(BigDecimal.class, DecimalConverter::new)
                .append(Dynamic.class, o -> convert(((Dynamic)o).asObject()))
                .append(Map.class, MapConverter::new)
                .append(Collection.class, CollectionConverter::new)
                .append(Optional.class, OptionalConverter::new)
                .append(java.util.Date.class, UtilDateInstantConverter::new)
                // fallback
                .append(Object.class, Converter::new)

        );

    public static Converter convert(Object o) {
        return typeConverters.getOrDefault(o.getClass(), typeConverters.entrySet().stream()
            .filter(entry -> entry.getKey().isInstance(o))
            .findFirst()
            .map(Map.Entry::getValue).get()).apply(o);
    }

    protected final Object o;

    Converter(Object o) {
        this.o = o;
    }

    public String intoString() {
        return o.toString();
    }

    public int intoInteger() {
        return intoDecimal().setScale(0, RoundingMode.HALF_UP).intValueExact();
    }

    public long intoLong() {
        return intoDecimal().setScale(0, RoundingMode.HALF_UP).longValueExact();
    }

    public double intoDouble() {
        return intoDecimal().doubleValue();
    }

    public BigDecimal intoDecimal() {
        return new BigDecimal(intoString());
    }

    public Object intoObject() {
        return o;
    }

    public Map intoMap() {
        return new Fluent.HashMap<>().append(VALUE_KEY, o);
    }

    public List intoList() {
        List list = new ArrayList();
        list.add(o);
        return list;
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

    static class TypeConverter<T> extends Converter {

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
            return value().map(o -> convert(o).intoString()).orElse(super.intoString());
        }

        @Override
        public Object intoObject() {
            return value().map(o -> convert(o).intoObject()).orElse(literal().size());
        }

        @Override
        public int intoInteger() {
            return value().map(o -> convert(o).intoInteger()).orElse(literal().size());
        }

        @Override
        public long intoLong() {
            return value().map(o -> convert(o).intoLong()).orElse((long) literal().size());
        }

        @Override
        public double intoDouble() {
            return value().map(o -> convert(o).intoDouble()).orElse((double) literal().size());
        }

        @Override
        public BigDecimal intoDecimal() {
            return value().map(o -> convert(o).intoDecimal()).orElse(new BigDecimal(literal().size()));
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

    static class CollectionConverter extends TypeConverter<Collection<?>> {

        CollectionConverter(Object o) {
            super(o);
        }

        private Optional<Object> onlyElement() {
            return Optional.ofNullable(literal().size() == 1 ? literal().iterator().next() : null);
        }

        @Override
        public String intoString() {
            return onlyElement().map(o -> convert(o).intoString()).orElse(super.intoString());
        }

        @Override
        public Object intoObject() {
            return onlyElement().map(o -> convert(o).intoObject()).orElse(literal().size());
        }

        @Override
        public int intoInteger() {
            return onlyElement().map(o -> convert(o).intoInteger()).orElse(literal().size());
        }

        @Override
        public long intoLong() {
            return onlyElement().map(o -> convert(o).intoLong()).orElse((long) literal().size());
        }

        @Override
        public double intoDouble() {
            return onlyElement().map(o -> convert(o).intoDouble()).orElse((double) literal().size());
        }

        @Override
        public BigDecimal intoDecimal() {
            return onlyElement().map(o -> convert(o).intoDecimal()).orElse(new BigDecimal(literal().size()));
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
            return new ArrayList<>(literal());
        }
    }

    static class OptionalConverter extends TypeConverter<Optional<?>> {

        OptionalConverter(Object o) {
            super(o);
        }

        @Override
        public String intoString() {
            return literal().map(o -> convert(o).intoString()).orElse(literal().toString());
        }

        @Override
        public Object intoObject() {
            if (literal().isPresent()) return literal().get();
            else return literal();
        }

        @Override
        public int intoInteger() {
            return literal().map(o -> convert(o).intoInteger()).orElse(0);
        }

        @Override
        public long intoLong() {
            return literal().map(o -> convert(o).intoLong()).orElse(0l);
        }

        @Override
        public double intoDouble() {
            return literal().map(o -> convert(o).intoDouble()).orElse(0d);
        }

        @Override
        public BigDecimal intoDecimal() {
            return literal().map(o -> convert(o).intoDecimal()).orElse(new BigDecimal(0));
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
