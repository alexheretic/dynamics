/*
 * Copyright 2015 Alex Butler
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package alexh.weak;

import static java.util.Arrays.asList;
import static java.util.Collections.unmodifiableMap;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toCollection;
import static java.util.stream.StreamSupport.stream;
import alexh.Fluent;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Logic container for converting weakly typed objects into core types,
 * ie
 * {@code convert(1234.5678d).intoDecimal()} returns a {@link BigDecimal} of 1234.5678
 *
 * {@code convert(1234.5678d).intoInteger()} returns a rounded int of 1235
 *
 * {@code convert("2015-03").intoLocalDateTime()} returns a {@link LocalDateTime} of 2015-03-01T00:00:00.000
 *
 * {@code convert(Optional.of("8484")).intoDouble()} returns a double of 8484.0
 *
 * Supports conversions from most basic types, in addition to java.util.Date & java.time instances,
 * object arrays, Iterables, Maps & Optionals + more from #toString conversions
 *
 * @author Alex Butler
 */
public class Converter {

    private static final String DEFAULT_MAP_KEY = "value";

    private static final Map<Class<?>, Function<Object, ? extends Converter>> typeConverters =
        unmodifiableMap(
            new Fluent.LinkedHashMap<Class<?>, Function<Object, ? extends Converter>>()
                .append(Integer.class, IntConverter::new)
                .append(Long.class, LongConverter::new)
                .append(Double.class, DoubleConverter::new)
                .append(BigDecimal.class, DecimalConverter::new)
                .append(Weak.class, o -> convert(((Weak) o).asObject()))
                .append(OptionalWeak.class, o -> new OptionalConverter(((OptionalWeak) o).asObject()))
                .append(Map.class, MapConverter::new)
                .append(Iterable.class, IterableConverter::new)
                .append(Optional.class, OptionalConverter::new)
                .append(java.util.Date.class, UtilDateInstantConverter::new)
                    // fallback
                .append(Object.class, Converter::new)
        );

    /**
     * @param value some object to convert
     * @return new Converter instance wrapper for the input value
     */
    public static Converter convert(Object value) {
        requireNonNull(value);

        if (value instanceof Object[])
            return convert(asList((Object[]) value));

        return typeConverters.getOrDefault(value.getClass(), typeConverters.entrySet().stream()
            .filter(entry -> entry.getKey().isInstance(value))
            .findFirst()
            .map(Map.Entry::getValue).get()).apply(value);
    }

    private static boolean doesNotThrow(Supplier<?> method) {
        try {
            method.get();
            return true;
        }
        catch (RuntimeException ex) { return false; }
    }

    /** wrapped inner value, the conversion target */
    protected final Object o;

    Converter(Object o) {
        this.o = o;
    }

    /**
     * Converts the inner value into a String, should always work
     * @return conversion
     */
    public String intoString() {
        return o instanceof String ? (String) o : o.toString();
    }

    /** @return {@link #intoString} will not throw an exception */
    public boolean intoStringWorks() {
        return doesNotThrow(this::intoString);
    }

    /**
     * @return conversion
     * @throws java.lang.RuntimeException cannot be converted
     */
    public int intoInteger() {
        return intoDecimal().setScale(0, RoundingMode.HALF_UP).intValueExact();
    }

    /** @return {@link #intoInteger} will not throw an exception */
    public boolean intoIntegerWorks() {
        return doesNotThrow(this::intoInteger);
    }

    /**
     * @return conversion
     * @throws java.lang.RuntimeException cannot be converted
     */
    public long intoLong() {
        return intoDecimal().setScale(0, RoundingMode.HALF_UP).longValueExact();
    }

    /** @return {@link #intoLong} will not throw an exception */
    public boolean intoLongWorks() {
        return doesNotThrow(this::intoLong);
    }

    /**
     * @return conversion
     * @throws java.lang.RuntimeException cannot be converted
     */
    public double intoDouble() {
        return intoDecimal().doubleValue();
    }

    /** @return {@link #intoDouble} will not throw an exception */
    public boolean intoDoubleWorks() {
        return doesNotThrow(this::intoDouble);
    }

    /**
     * @return conversion
     * @throws java.lang.RuntimeException cannot be converted
     */
    public BigDecimal intoDecimal() {
        return new BigDecimal(intoString());
    }

    /** @return {@link #intoDecimal} will not throw an exception */
    public boolean intoDecimalWorks() {
        return doesNotThrow(this::intoDecimal);
    }

    /**
     * Converts the inner value into a Map
     * will convert an Iterable into an index -> value map
     * will convert simple types into a size 1 map of {"value": ?}
     * as such this should always work
     * @return conversion
     */
    public Map intoMap() {
        return new Fluent.HashMap<>().append(DEFAULT_MAP_KEY, o);
    }

    /** @return {@link #intoMap} will not throw an exception */
    public boolean intoMapWorks() {
        return doesNotThrow(this::intoMap);
    }

    /**
     * Converts the inner value into a List
     * will convert a Map into a value list
     * will convert simple types into a size 1 list [?]
     * as such this should always work
     * @return conversion
     */
    public List intoList() {
        List list = new ArrayList();
        list.add(o);
        return list;
    }

    /** @return {@link #intoList} will not throw an exception */
    public boolean intoListWorks() {
        return doesNotThrow(this::intoList);
    }

    /**
     * Converts to string & permissively parses as a date, ignoring time zone
     * defaults day->1, month->1, hour->0, minute->0, second->0, nanoseconds->0
     * @throws java.time.format.DateTimeParseException converted string is in an invalid format
     */
    public LocalDateTime intoLocalDateTime() {
        return LocalDateTime.from(ConverterTimeFormats.parseWithDefaults(intoString()));
    }

    /** @return {@link #intoLocalDateTime} will not throw an exception */
    public boolean intoLocalDateTimeWorks() {
        return doesNotThrow(this::intoLocalDateTime);
    }

    /**
     * Converts to string & permissively parses as a date, requiring a time zone to be parsed
     * defaults day->1, month->1, hour->0, minute->0, second->0, nanoseconds->0
     * @throws java.time.format.DateTimeParseException converted string is in an invalid format
     */
    public ZonedDateTime intoZonedDateTime() {
        return ZonedDateTime.from(ConverterTimeFormats.parseWithDefaults(intoString()));
    }

    /** @return {@link #intoZonedDateTime} will not throw an exception */
    public boolean intoZonedDateTimeWorks() {
        return doesNotThrow(this::intoZonedDateTime);
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

    /** @return ConverterMaybe instance, for fluent handling of non-convertibles  */
    public ConverterMaybe maybe() {
        return new ConverterMaybe(o);
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
            return Optional.ofNullable(literal().get(DEFAULT_MAP_KEY));
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
