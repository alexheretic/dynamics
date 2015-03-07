package alexh.weak;

import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.ls.DOMImplementationLS;
import org.w3c.dom.ls.LSSerializer;
import org.xml.sax.InputSource;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;
import java.io.Reader;
import java.io.StringReader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static alexh.Unchecker.uncheckedGet;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toList;

public class XmlDynamic extends AbstractDynamic<Node> implements TypeDescriber, AvailabilityDescriber {
    
    private static final XPathExpression ALL = uncheckedGet(() -> XPathFactory.newInstance().newXPath().compile("//*"));
    private static final String FALLBACK_TO_STRING = "Xml[unable to serialize]";
    private static final String NONE_NAMESPACE = "none";
    private static final String NS_INDICATOR = "::";
    
    private static Stream<Node> stream(NodeList nodes) {
        return IntStream.range(0, nodes.getLength()).mapToObj(nodes::item);
    }

    private static Stream<Node> stream(NamedNodeMap nodeMap) {
        return IntStream.range(0, nodeMap.getLength()).mapToObj(nodeMap::item);
    }

    public XmlDynamic(Node inner) {
        super(inner);
    }

    public XmlDynamic(InputSource xml) {
        this(uncheckedGet(() -> (Node) ALL.evaluate(xml, XPathConstants.NODE)));
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
        return Stream.concat(elements(), attributes());
    }

    @Override
    public String describeAvailability() {
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

        return keys.toString();
    }

    @Override
    public String describeType() {
        return "Xml";
    }

    Child childElement(Node inner, int index) {
        return new Child(inner, this, index == 0 ? inner.getLocalName() : inner.getLocalName() + '[' + index + ']');
    }

    @Override
    public int hashCode() {
        final String toString = toString();
        return FALLBACK_TO_STRING.equals(toString) ? super.hashCode() : toString.hashCode();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        final String otherAsString = o.toString();
        return !FALLBACK_TO_STRING.equals(otherAsString) && otherAsString.equals(this.toString());
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
        return toString();
    }

    @Override
    public String toString() {
        try { return serializer().writeToString(inner); }
        catch (RuntimeException ex) { return FALLBACK_TO_STRING; }
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
            return Optional.ofNullable(inner.getFirstChild())
                .map(Node::getNodeValue)
                .orElseGet(() -> elements().map(Object::toString)
                            .map(String::trim)
                            .reduce((s1, s2) -> s1 + s2)
                            .orElse(""));
        }

        @Override
        protected Stream<Child> attributesWith(Predicate<Node> predicate) {
            final Map<String, Integer> keyLastIndex = new HashMap<>();
            return stream(inner.getAttributes())
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
            return stream(inner.getChildNodes())
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
        public Object key() {
            return key;
        }

        Child childAttribute(Node inner, int index) {
            String name = "@" + inner.getLocalName();
            return new Child(inner, this, index == 0 ? name : name + '[' + index + ']');
        }
    }
}
