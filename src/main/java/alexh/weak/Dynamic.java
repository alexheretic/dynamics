package alexh.weak;

import org.xml.sax.InputSource;
import java.io.Reader;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toCollection;

public interface Dynamic extends Weak<Dynamic> {

    /** Default key value for top-level Dynamic instances */
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

    /**
     * Returns Weak instance wrapping the key for this node. To get the inner key call {@link Weak#asObject()}
     * Top-level Dynamic objects have the key value {@link Dynamic#ROOT_KEY}
     * @return key instance wrapper
     */
    Weak<?> key();
}
