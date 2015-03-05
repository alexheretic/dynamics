package alexh.weak;

import org.xml.sax.InputSource;
import java.io.Reader;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toCollection;

public interface Dynamic {

    String ROOT_KEY = "root";

    static Dynamic from(Object val) {
        if (val == null) return DynamicNothing.INSTANCE;
        else if (val instanceof Dynamic) return (Dynamic) val;
        else if (val instanceof Map) return new DynamicMap((Map) val);
        else if (val instanceof List) return new DynamicList((List) val);
        else return new DynamicSomething(val);
    }

    static Dynamic fromXml(InputSource xml) {
        return new XmlDynamic(xml);
    }

    static Dynamic fromXml(Reader xml) {
        return new XmlDynamic(xml);
    }

    static Dynamic fromXml(String xml) {
        return new XmlDynamic(xml);
    }

    Dynamic get(Object key);

    boolean isPresent();

    Object asObject();

    Stream<Dynamic> children();

    default Set<Dynamic> childSet() {
        return children().collect(toCollection(LinkedHashSet::new));
    }

    default Dynamic get(String key, String separator) {
        Dynamic result = this;
        for (String part : key.split(Pattern.quote(separator)))
            result = result.get(part);
        return result;
    }

    default Dynamic get(String key, char separator) {
        return get(key, String.valueOf(separator));
    }

    default Optional<Object> asOptional() {
        return isPresent() ? Optional.of(asObject()) : Optional.empty();
    }

    default <T> Optional<T> asOptional(Class<T> type) {
        return asOptional().map(type::cast);
    }

    default <T> T as(Class<T> type) {
        return type.cast(asObject());
    }

    default String asString() {
        return as(String.class);
    }

    default List<?> asList() {
        return as(List.class);
    }

    default Map<?, ?> asMap() {
        return as(Map.class);
    }

    default boolean is(Class<?> type) {
        return isPresent() && type.isInstance(asObject());
    }

    default boolean isMap() {
        return is(Map.class);
    }

    default boolean isString() {
        return is(String.class);
    }

    default boolean isList() {
        return is(List.class);
    }
}
