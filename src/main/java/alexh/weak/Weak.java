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

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Wrapper for a weakly typed object or absence, providing casting, presence gathering & conversion methods
 *
 * @author Alex Butler
 */
public interface Weak<Self extends Weak<Self>> {

    /**
     * Returns if this instance wraps a non-null value. True implies {@link Weak#asObject()} will not throw
     * @return if this instance wraps a non-null value
     */
    boolean isPresent();

    /**
     * Returns inner value implicitly asserting that the value is not absent, ie {@link Weak#isPresent()} is true
     * @return unwrapped inner value
     * @throws java.util.NoSuchElementException absent
     */
    Object asObject();

    /**
     * Converts this value/absence wrapper into a standard optional
     * @return optional with consistent {@link Optional#isPresent()} to {@link Weak#isPresent()}
     */
    default Optional<Object> asOptional() {
        return isPresent() ? Optional.of(asObject()) : Optional.empty();
    }

    /**
     * As {@link Weak#asObject()} casting to input type
     * @param type cast type
     * @param <T> cast type
     * @return unwrapped inner value cast to input type
     * @throws ClassCastException
     */
    default <T> T as(Class<T> type) {
        return type.cast(asObject());
    }

    /**
     * Shortcut for as(String.class)
     * @see Weak#as(Class)
     */
    default String asString() {
        return as(String.class);
    }

    /**
     * Shortcut for as(List.class), with malleable generic type
     * @see Weak#as(Class)
     */
    default <T> List<T> asList() {
        return as(List.class);
    }

    /**
     * Shortcut for as(Map.class), with malleable generic type
     * @see Weak#as(Class)
     */
    default <K, V> Map<K, V> asMap() {
        return as(Map.class);
    }

    /**
     * @param type type to test inner value with
     * @return value is present and an instance of input type
     */
    default boolean is(Class<?> type) {
        return isPresent() && type.isInstance(asObject());
    }

    /**
     * Shortcut for is(Map.class)
     * @see Weak#is(Class)
     */
    default boolean isMap() {
        return is(Map.class);
    }

    /**
     * Shortcut for is(String.class)
     * @see Weak#is(Class)
     */
    default boolean isString() {
        return is(String.class);
    }

    /**
     * Shortcut for is(List.class)
     * @see Weak#is(Class)
     */
    default boolean isList() {
        return is(List.class);
    }

    /**
     * Shortcut for wrapping {@link Weak#asObject()} in a {@link Converter} instance
     * @see Converter#convert(Object)
     * @return {@link Converter} instance for inner value
     * @throws java.util.NoSuchElementException absent
     */
    default Converter convert() {
        return Converter.convert(asObject());
    }

    /**
     * Returns an optional wrapping of this instance, with resultant
     * {@link Optional#isPresent} return consistent with {@link Weak#isPresent()}
     * @return optional wrapping of this, or empty if this is absent
     */
    default Optional<Self> maybe() {
        return isPresent() ? Optional.of((Self) this) : Optional.empty();
    }
}
