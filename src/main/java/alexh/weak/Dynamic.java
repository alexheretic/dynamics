package alexh.weak;

import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.Stream;

public interface Dynamic extends Weak<Dynamic> {

    /** Default key value for top-level Dynamic instances */
    String ROOT_KEY = "root";

    /**
     * Wraps a value in a dynamic representation, properly handled nested types are Maps & Lists all other types
     * become dynamic end-points. In the general case you simply have to convert to one of these to be handled as a
     * nested dynamic object
     * See {@link Converter} to perform simple conversions to these types where applicable
     * See {@link XmlDynamic} for an XML handling dynamic representation
     * @param val some value
     * @return Dynamic representation of the input value
     */
    static Dynamic from(Object val) {
        if (val == null) return DynamicNothing.INSTANCE;
        else if (val instanceof Dynamic) return (Dynamic) val;
        else if (val instanceof Map) return new DynamicMap((Map) val);
        else if (val instanceof List) return new DynamicList((List) val);
        else return new DynamicSomething(val);
    }

    /**
     * Returns a dynamic wrapping the immediate child of this instance with the input key,
     * or a dynamic representing the lack of such a child
     * @param key child key
     * @return dynamic representing the child matching the input key
     */
    Dynamic get(Object key);

    /** @return stream of all children of this instance */
    Stream<Dynamic> children();

    /**
     * Performs multiple gets as described by an input key path
     * so {@code dynamic.get("one").get("two").get("three")}
     * is equivalent to {@code dynamic.get("one.two.three", ".")}
     * @param keyPath successive child keys separated by separator string
     * @param separator a string separator to split the input key input multiple keys
     * @return dynamic representing the nested child
     */
    default Dynamic get(String keyPath, String separator) {
        Dynamic result = this;
        for (String part : keyPath.split(Pattern.quote(separator)))
            result = result.get(part);
        return result;
    }

    /**
     * Returns Weak instance wrapping the key for this node. To get the inner key call {@link Weak#asObject()}
     * Top-level Dynamic objects have the key value {@link Dynamic#ROOT_KEY}
     * @return key instance wrapper
     */
    Weak<?> key();
}
