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

import static java.util.Spliterators.spliteratorUnknownSize;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.*;

/**
 * Wrapper allowing weakly-typed nested structure selection that's null-safe
 *
 * As an example consider a Map with the following structure called 'message'
 * <pre>{@code
 *    {
 *      "product": {
 *        "investment": {
 *          "info": {
 *            "current": {
 *              "name": "some name"
 *            }
 *          }
 *        }
 *      }
 *    }
 * }</pre>
 * We can select the nested 'name' field with
 * {@code Dynamic.from(message).get("product.investment.info.current.name", ".").asString()}
 *
 * @author Alex Butler
 */
public interface Dynamic extends Weak<Dynamic> {

    /** Default key value for top-level Dynamic instances */
    String ROOT_KEY = "root";

    /**
     * Wraps a value in a dynamic representation, properly handled nested types are Maps & Collections all other types
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
        else if (val instanceof Collection) return new DynamicCollection((Collection) val);
        else return new DynamicSomething(val);
    }

    /**
     * Returns a dynamic wrapping the immediate child of this instance with the input key,
     * or a dynamic representing the lack of such a child. This is never null.
     * @param key child key
     * @return dynamic representing the child matching the input key, or a dynamic representing the key's absence
     */
    Dynamic get(Object key);

    /** @return stream of all children of this instance */
    Stream<Dynamic> children();

    /**
     * Performs multiple String gets as described by an input key path
     * so {@code dynamic.get("one.two.three", ".")}
     * is equivalent to {@code dynamic.get("one").get("two").get("three")}
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
     * Shortcut for {@code dynamic.get("some.path.to.somewhere", ".")} avoiding the need for the second argument
     * Warning this should be considered beta, I'm not sure this is the best API approach
     * @param dotSeparatedPath successive child keys separated by "." character
     * @return dynamic representing the nested child
     */
    default Dynamic dget(String dotSeparatedPath) {
        return get(dotSeparatedPath, ".");
    }

    /**
     * Returns Weak instance wrapping the key for this node. To get the inner key call {@link Weak#asObject()}
     * Top-level Dynamic objects have the key value {@link Dynamic#ROOT_KEY}
     * @return key instance wrapper
     */
    Weak<?> key();

    default Stream<Dynamic> allChildren() {
        return allChildrenDepthFirst();
    }

    default Stream<Dynamic> allChildrenDepthFirst() {
        return children().flatMap(child -> Stream.concat(Stream.of(child), child.allChildrenDepthFirst()));
    }

    default Stream<Dynamic> allChildrenBreadthFirst() {
        return StreamSupport.stream(spliteratorUnknownSize(new BreadthChildIterator(this), Spliterator.ORDERED), false);
    }
}
