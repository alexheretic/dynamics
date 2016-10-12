package alexh;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.CoreMatchers.anyOf;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import alexh.weak.Dynamic;
import alexh.weak.XmlDynamic;
import org.junit.Assert;
import org.junit.Test;

public class DynamicXmlNamespaceTest {

    static final String XML =
        "<xml xmlns:s=\"http://example.com/rootspace/something\" xmlns:an=\"http://another-example.com\">" +
            "<s:content>" +
                "<s:empty_element/>" +
                "<int_element>12345</int_element>" +
                "<s:large_decimal>12341234123412341234.334234</s:large_decimal>" +
                "<multi_empty_element/>" +
                "<multi_empty_element/>" +
                "<multi_empty_element/>" +
                "<s:just_attrs hello=\"world\" foo=\"bar\" />" +
                "<ns_attrs s:eses=\"ssss\" an:ans=\"anananan\" s:same-name=\"what?\" an:same-name=\"why?\"/>" +
            "</s:content>" +
            "<an:content>" +
                "<an:empty_element/>" +
                "<int_element>23456</int_element>" +
                "<s:multi_element>123</s:multi_element>" +
                "<multi_element>456</multi_element>" +
                "<an:multi_element>hello there</an:multi_element>" +
            "</an:content>" +
            "<three-and-two>" +
                "<s:hey>hey example.com!</s:hey>" +
                "<s:hey>hello example.com!</s:hey>" +
                "<an:hey>tag another-example.com!</an:hey>" +
                "<s:hey>salut example.com!</s:hey>" +
                "<an:hey>howdy another-example.com!</an:hey>" +
            "</three-and-two>" +
        "</xml>";

    Dynamic root = new XmlDynamic(XML);

    @Test
    public void hasElementNameWithNamespace_matchesNamespace() {
        assertTrue(XmlDynamic.hasElementName("http://another-example.com::empty_element")
            .test(root.get("xml|content[1]|empty_element")));
    }

    @Test
    public void hasElementNameWithNamespace_differentNamespace() {
        assertFalse(XmlDynamic.hasElementName("http://example.com/rootspace/something::empty_element")
            .test(root.get("xml|content[1]|empty_element")));
    }

    @Test
    public void hasElementNameWithNamespace_explicitlyNoNamespace() {
        assertFalse(XmlDynamic.hasElementName("none::three-and-two")
            .test(root.get("xml|content[1]|empty_element")));
    }

    @Test
    public void hasElementNameWithNoNamespace_matchesNoNamespace() {
        assertTrue(XmlDynamic.hasElementName("none::three-and-two")
            .test(root.get("xml|three-and-two")));
    }

    @Test
    public void hasElementNameWithNoNamespace_namespace() {
        assertFalse(XmlDynamic.hasElementName("http://another-example.com::three-and-two")
            .test(root.get("xml|three-and-two")));
    }

    @Test
    public void standardUsageIgnoresNamespaces() {
        Assert.assertThat(root.get("xml|content|large_decimal").asString(), is("12341234123412341234.334234"));
        Assert.assertThat(root.get("xml|content[1]|multi_element").asString(), is("123"));
    }

    @Test
    public void colonUsedToDeclareExplicitNamespaceSelection() {
        Assert.assertThat(root.get("xml|http://another-example.com::content|http://another-example.com::multi_element")
            .asString(), is("hello there"));
    }

    @Test
    public void mixedExplicitNamespaceAndNonExplicitSelection() {
        Assert.assertThat(root.get("xml|http://another-example.com::content|int_element").asString(), is("23456"));
    }

    @Test
    public void explicitNoNamespaceSelection() {
        Assert.assertThat(root.get("xml|content[1]|none::multi_element").asString(), is("456"));
        assertFalse(root.get("xml|content|none::large_decimal").isPresent());
    }

    @Test
    public void standardUsageIgnoresNamespacesOnAttributes() {
        Assert.assertThat(root.get("xml|content|ns_attrs|@eses").asString(), is("ssss"));
    }

    @Test
    public void colonUsedToDeclareExplicitNamespaceSelectionOnAttributes() {
        Assert.assertThat(root.get("xml|content|ns_attrs|http://another-example.com::@same-name").asString(), is("why?"));
        Assert.assertThat(root.get("xml|content|ns_attrs|http://example.com/rootspace/something::@same-name").asString(),
            is("what?"));
    }

    @Test
    public void standardUsageHandlesMultipleSameLocalNameAttributes() {
        String attr1 = root.get("xml|content|ns_attrs|@same-name").asString();
        String attr2 = root.get("xml|content|ns_attrs|@same-name[1]").asString();

        Assert.assertThat(attr1, anyOf(is("what?"), is("why?")));
        Assert.assertThat(attr2, anyOf(is("what?"), is("why?")));

        if (attr1.equals("what?")) Assert.assertThat(attr2, is("why?"));
        else Assert.assertThat(attr2, is("what?"));
    }

    @Test
    public void elementIndicesAdjustToNamespaceScope() {
        assertThat(root.get("xml|three-and-two|http://example.com/rootspace/something::hey[2]").asString())
            .isEqualTo("salut example.com!");
        assertThat(root.get("xml|three-and-two|http://another-example.com::hey").asString())
            .isEqualTo("tag another-example.com!");
        assertThat(root.get("xml|three-and-two|http://another-example.com::hey[2]").isPresent())
            .isEqualTo(false);
    }

    @Test
    public void searchUsingNamespaces() {
        int val = root.allChildrenBreadthFirst()
            .filter(XmlDynamic.hasElementName("http://another-example.com::content"))
            .findAny()
            .get()
            .get("int_element").convert().intoInteger();

        assertThat(val).isEqualTo(23456);
    }
}
