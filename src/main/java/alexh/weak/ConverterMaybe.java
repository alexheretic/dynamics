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

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

/**
 * Logic container for converting weakly typed objects into core types wrapped in optionals,
 * @see Converter
 * ie
 * {@code convert("8484").maybe().intoDouble()} returns an Optional[8484.0]
 * {@code convert(new Object()).maybe().intoDouble()} returns an Optional.empty
 *
 * As optionals are returned ConverterMaybe methods never throw
 *
 * @author Alex Butler
 */
public class ConverterMaybe {

    /** wrapped inner value, the conversion target */
    protected final Object o;

    ConverterMaybe(Object o) {
        this.o = o;
    }

    private <T> Optional<T> optional(Function<Converter, T> fn) {
        try { return Optional.ofNullable(fn.apply(Converter.convert(o))); }
        catch (RuntimeException ex) { return Optional.empty(); }
    }

    /**
     * @see Converter#intoString()
     * never throws
     */
    public Optional<String> intoString() {
        return optional(Converter::intoString);
    }

    /**
     * @see Converter#intoInteger()
     * never throws
     */
    public Optional<Integer> intoInteger() {
        return optional(Converter::intoInteger);
    }

    /**
     * @see Converter#intoLong()
     * never throws
     */
    public Optional<Long> intoLong() {
        return optional(Converter::intoLong);
    }

    /**
     * @see Converter#intoDouble()
     * never throws
     */
    public Optional<Double> intoDouble() {
        return optional(Converter::intoDouble);
    }

    /**
     * @see Converter#intoDecimal()
     * never throws
     */
    public Optional<BigDecimal> intoDecimal() {
        return optional(Converter::intoDecimal);
    }

    /**
     * @see Converter#intoMap()
     * never throws
     */
    public Optional<Map> intoMap() {
        return optional(Converter::intoMap);
    }

    /**
     * @see Converter#intoList()
     * never throws
     */
    public Optional<List> intoList() {
        return optional(Converter::intoList);
    }

    /**
     * @see Converter#intoLocalDateTime()
     * never throws
     */
    public Optional<LocalDateTime> intoLocalDateTime() {
        return optional(Converter::intoLocalDateTime);
    }

    /**
     * @see Converter#intoZonedDateTime()
     * never throws
     */
    public Optional<ZonedDateTime> intoZonedDateTime() {
        return optional(Converter::intoZonedDateTime);
    }

    /**
     * @see Converter#intoZonedDateTimeOrUse(ZoneId)
     * never throws
     */
    public Optional<ZonedDateTime> intoZonedDateTimeOrUse(ZoneId fallback) {
        return optional(c -> c.intoZonedDateTimeOrUse(fallback));
    }
}
