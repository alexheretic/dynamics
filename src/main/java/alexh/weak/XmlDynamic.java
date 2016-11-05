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

import static alexh.Unchecker.uncheckedGet;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toList;
import java.io.Reader;
import java.io.StringReader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.ls.DOMImplementationLS;
import org.w3c.dom.ls.LSSerializer;
import org.xml.sax.InputSource;

/**
 * Dynamic implementation for XML documents
 * All keys & values in an XmlDynamic unwrap to Strings, try {@link Dynamic#convert()}
 * to implement basic string->type conversions
 * <p>
 * As an example consider the following XML structure stored in a String 'xmlMessage'
 * <pre>{@code
 *     <product>
 *         <investment>
 *             <info>
 *                 <current>
 *                     <name>some name</name>
 *                 </current>
 *             </info>
 *         </investment>
 *     </product>
 * }</pre>
 * We can select the nested 'name' element value with
 * <br/>{@code new XmlDynamic(xmlMessage).get("product.investment.info.current.name", ".").asString()}
 * <p>
 * Also since XML has certain key name restrictions the pipe character '|' can be used as a splitter without declaration
 * <br/>ie {@code new XmlDynamic(xmlMessage).get("product|investment|info|current|name").asString()}
 * @see XmlDynamic#get(Object)
 *
 * @author Alex Butler
 */
public class XmlDynamic extends AbstractDynamic<Node> implements Describer {

    private static final String FALLBACK_TO_STRING = "Xml[unable to serialize]";
    private static final String NONE_NAMESPACE = "none";
    private static final String NS_INDICATOR = "::";

    /** Needs to be thread-safe, childNodes NodeList is not! */
    private static Stream<Node> streamChildNodes(Node node) {
        int children = node.getChildNodes().getLength();
        Node firstChild = node.getFirstChild();
        if (firstChild == null) return Stream.empty();
        return Stream.iterate(firstChild, prev -> prev != null ? prev.getNextSibling() : null)
            .limit(children)
            .filter(n -> n != null);
    }

    /** Needs to be thread-safe */
    private static Stream<Node> streamAttributes(Node node) {
        final NamedNodeMap attributes = node.getAttributes();
        if (attributes == null) return Stream.empty();
        return IntStream.range(0, attributes.getLength()).mapToObj(attributes::item);
    }

    private static Node inputSourceToNode(InputSource xml) {
        final XPathExpression all = uncheckedGet(() -> XPathFactory.newInstance().newXPath().compile("//*"));
        synchronized (XPathExpression.class) { // evaluate not thread-safe
            return uncheckedGet(() -> (Node) all.evaluate(xml, XPathConstants.NODE));
        }
    }

    /**
     * Predicate for an xml element's name as it appears in the xml itself (case insensitive)
     * Inside the xml dynamic multiple elements with the same names have unique suffices ie
     * <root>
     *     <child>1</child>
     *     <child>2</child>
     * </root>
     * the 'child' elements have internal names 'child' and 'child[1]' a predicate
     * {@code dynamic -> dynamic.key().asString().equals("child")} would only match the first
     *
     * {@code hasElementName("child")} matches both
     *
     * @param elementName xml element name
     * @return predicate to match XmlDynamic instances of elements with input name
     */
    public static Predicate<? super Dynamic> hasElementName(String elementName) {
        final String[] nsKey = elementName.split(NS_INDICATOR);

        return element -> {
            if (!(element instanceof XmlDynamic)) return false;

            String simpleName = elementName;
            if (nsKey.length == 2) {
                String namespace = nsKey[0];
                String elNamespace = ((XmlDynamic) element).inner.getNamespaceURI();
                if (NONE_NAMESPACE.equals(namespace)) {
                    if (elNamespace != null) return false;
                }
                else if (!namespace.equals(elNamespace)) return false;
                simpleName = nsKey[1];
            }

            String key = element.key().asString();
            if (key.endsWith("]"))
                key = key.substring(0, key.lastIndexOf("["));

            return key.equalsIgnoreCase(simpleName);
        };
    }

    public XmlDynamic(Node inner) {
        super(inner);
    }

    public XmlDynamic(InputSource xml) {
        this(inputSourceToNode(xml));
    }

    public XmlDynamic(Reader xml) {
        this(new InputSource(xml));
    }

    public XmlDynamic(String xml) {
        this(new StringReader(xml));
    }

    /** Dynamic Xml values are always {@link String}s */
    @Override
    public boolean is(Class<?> type) {
        return String.class.equals(type);
    }

    /**
     * {@inheritDoc}
     * <p>
     * The pipe character '|' can be used as a splitter without declaration
     * ie {@code xmlDynamic.get("product|investment|info|current|name").asString()}.
     *
     * Multiple child elements with the same local-name effectively have [i] appended to them where i is their
     * index counting from top to bottom.
     * For example:
     * <pre>{@code
     *     <product id="1234">
     *         <string>hello</string>
     *         <string>hey</string>
     *         <string>hi</string>
     *         <string>howdy</string>
     *     </product>
     * }</pre>
     * <br/>{@code xmlDynamic.get("product|string")} returns "hello"
     * <br/>{@code xmlDynamic.get("product|string[0]")} also returns "hello"
     * <br/>{@code xmlDynamic.get("product|string[1]")} returns "hey"
     * <br/>{@code xmlDynamic.get("product|string[2]")} returns "hi"
     * <br/>{@code xmlDynamic.get("product|string[3]")} returns "howdy"
     * <p>
     * Attributes can be accessed in exactly the same way as elements, or explicitly
     * <br/>{@code xmlDynamic.get("product|id").asString()} return "1234"
     * <br/>{@code xmlDynamic.get("product|@id").asString()} also returns "1234"
     * <p>
     * Namespaces are ignored by default, but can be used explicitly using the "::" separator
     * For example:
     * <pre>{@code
     *     <ex:product xmlns:ex="http://example.com/example">
     *         <message>hello</message>
     *     </ex:product>
     * }</pre>
     * <br/>{@code xmlDynamic.get("product|message")} returns "hello"
     * <br/>{@code xmlDynamic.get("http://example.com/example::product|none::message")} also returns "hello"
     */
    @Override
    public Dynamic get(Object keyObject) {
        final String keyToString = keyObject.toString();
        if (keyToString.contains("|")) return get(keyToString, "|");

        if (children().allMatch(o -> false)) {
            if (asString().isEmpty()) return new ParentAbsence.Empty<>(this, keyObject);
            return new ParentAbsence.Barren<>(this, keyObject);
        }

        final String key = keyToString.endsWith("[0]") ? keyToString.substring(0, keyToString.length() - 3) : keyToString;

        final String[] nsKey = key.split(NS_INDICATOR);
        if (nsKey.length == 2) return getWithNamespace(nsKey[0], nsKey[1]);

        final Optional<? extends Dynamic> match;

        if (key.isEmpty()) match = Optional.empty();
        else if (key.startsWith("@")) match = attributes().filter(attr -> attr.key.equals(key)).findAny();
        else if (key.endsWith("]")) match = elements().filter(el -> el.key.equals(key)).findAny();
        else {
            match = Stream.concat(elements().filter(el -> el.key.equals(key)),
                attributes().filter(attr -> attr.key.equals("@"+ key))).findFirst();
        }

        return match.map(Dynamic.class::cast).orElse(new ChildAbsence.Missing<>(this, keyObject));
    }

    protected Dynamic getWithNamespace(String namespace, String key) {
        final Optional<? extends Dynamic> match;

        final Predicate<Node> nodeInNamespace;
        if (NONE_NAMESPACE.equals(namespace)) nodeInNamespace = node -> node.getNamespaceURI() == null;
        else nodeInNamespace = node -> namespace.equals(node.getNamespaceURI());

        if (key.isEmpty()) match = Optional.empty();
        else if (key.startsWith("@")) {
            match = attributesWith(nodeInNamespace).filter(attr -> attr.key.equals(key)).findAny();
        }
        else if (key.endsWith("]")) {
            match = elementsWith(nodeInNamespace).filter(el -> el.key.equals(key)).findAny();
        }
        else {
            match = Stream.concat(elementsWith(nodeInNamespace).filter(el -> el.key.equals(key)),
                attributesWith(nodeInNamespace).filter(attr -> attr.key.equals("@"+ key))).findFirst();
        }

        return match.map(Dynamic.class::cast).orElse(new ChildAbsence.Missing<>(this, namespace + NS_INDICATOR + key));
    }

    protected Stream<Child> attributes() {
        return attributesWith(n -> true);
    }

    protected Stream<Child> attributesWith(Predicate<Node> predicate) {
        return Stream.empty();
    }

    protected Stream<Child> elements() {
        return elementsWith(n -> true);
    }

    protected Stream<Child> elementsWith(Predicate<Node> predicate) {
        return Stream.of(inner).filter(predicate).map(n -> childElement(n, 0));
    }

    @Override
    public Stream<Dynamic> children() {
        synchronized (inner.getOwnerDocument()) {
            return Stream.concat(elements(), attributes());
        }
    }

    @Override
    protected Object keyLiteral() {
        return ROOT_KEY;
    }

    @Override
    public String describe() {
        if (isString() && asString().isEmpty())
            return "Empty-Xml";

        List<String> keys = Stream.concat(elements(), attributes())
            .map(child -> child.key)
            .collect(toList());

        Map<String, Integer> keyLastIndex = new HashMap<>();
        keys.forEach(key -> {
            if (key.endsWith("]")) {
                StringBuilder index = new StringBuilder();
                for (int i = key.length()-2; i != -1; --i) {
                    char c = key.charAt(i);
                    if (c == '[') break;
                    else index.append(c);
                }
                if (index.length() != 0) {
                    keyLastIndex.put(key.substring(0, key.indexOf("[")), Integer.valueOf(index.toString()));
                }
            }
        });

        keyLastIndex.forEach((multiKey, maxIndex) -> {
            keys.removeIf(key ->
                key.equals(multiKey) || key.endsWith("]") && key.substring(0, key.indexOf("[")).equals(multiKey));
            keys.add(multiKey + "[0.." + maxIndex + "]");
        });

        return keys.isEmpty() ? "Xml" : "Xml" + keys.toString();
    }

    Child childElement(Node inner, int index) {
        return new Child(inner, this, index == 0 ? inner.getLocalName() : inner.getLocalName() + '[' + index + ']');
    }

    @Override
    public int hashCode() {
        final String toString = fullXml();
        return FALLBACK_TO_STRING.equals(toString) ? super.hashCode() : toString.hashCode();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        final String otherAsString = ((XmlDynamic)o).fullXml();
        return !FALLBACK_TO_STRING.equals(otherAsString) && otherAsString.equals(this.fullXml());
    }

    protected LSSerializer serializer() {
        LSSerializer serializer = ((DOMImplementationLS) inner.getOwnerDocument()
            .getImplementation()
            .getFeature("LS", "3.0"))
            .createLSSerializer();

        serializer.getDomConfig().setParameter("xml-declaration", false);
        return serializer;
    }

    @Override
    public String asObject() {
        synchronized (inner.getOwnerDocument()) {
            return serializer().writeToString(inner);
        }
    }

    /** @return this dynamic key->value entry as an XML string */
    public String fullXml() {
        try {
            synchronized (inner.getOwnerDocument()) {
                return serializer().writeToString(inner);
            }
        }
        catch (RuntimeException ex) { return FALLBACK_TO_STRING; }
    }

    @Override
    public String toString() {
        return keyLiteral() + ":"+ describe();
    }

    static class Child extends XmlDynamic implements DynamicChild {

        private final Dynamic parent;
        private final String key;

        Child(Node inner, Dynamic parent, String key) {
            super(inner);
            this.parent = parent;
            this.key = requireNonNull(key);
        }

        @Override
        public String asObject() {
            synchronized (inner.getOwnerDocument()) {
                return Optional.ofNullable(inner.getFirstChild())
                    .map(Node::getNodeValue)
                    .orElseGet(() -> elements().map(Child::fullXml)
                        .map(String::trim)
                        .reduce((s1, s2) -> s1 + s2)
                        .orElse(""));
            }
        }

        @Override
        protected Stream<Child> attributesWith(Predicate<Node> predicate) {
            final Map<String, Integer> keyLastIndex = new HashMap<>();
            return streamAttributes(inner)
                .filter(predicate)
                .map(attr -> {
                    final Integer index = Optional.ofNullable(keyLastIndex.get(attr.getLocalName())).map(i -> i + 1).orElse(0);
                    keyLastIndex.put(attr.getLocalName(), index);
                    return childAttribute(attr, index);
                });
        }

        @Override
        protected Stream<Child> elementsWith(Predicate<Node> predicate) {
            final Map<String, Integer> keyLastIndex = new HashMap<>();
            return streamChildNodes(inner)
                .filter(node -> node.getLocalName() != null)
                .filter(predicate)
                .map(node -> {
                    final Integer index = Optional.ofNullable(keyLastIndex.get(node.getLocalName())).map(i -> i + 1).orElse(0);
                    keyLastIndex.put(node.getLocalName(), index);
                    return childElement(node, index);
                });
        }

        @Override
        public Dynamic parent() {
            return parent;
        }

        @Override
        public Object keyLiteral() {
            return key;
        }

        Child childAttribute(Node inner, int index) {
            String name = "@" + inner.getLocalName();
            return new Child(inner, this, index == 0 ? name : name + '[' + index + ']');
        }
    }
}
