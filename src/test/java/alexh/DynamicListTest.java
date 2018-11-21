package alexh;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static java.util.stream.Collectors.toList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import alexh.weak.Dynamic;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class DynamicListTest {

    private Dynamic dy;

    @BeforeEach
    public void setupMap() {
        dy = Dynamic.from(asList(
            new Fluent.HashMap<>()
                .append("mk1", "foo")
                .append("mk2", "bar"),
            "hello",
            new Object(),
            asList(1, 2, 3, null)
        ));
        assertNotNull(dy);
    }

    @Test
    public void stringIndex() {
        assertEquals("hello", dy.get("1").asObject());
    }

    @Test
    public void intIndex() {
        assertEquals("hello", dy.get(1).asObject());
    }

    @Test
    public void numberIndex() {
        assertEquals("hello", dy.get(1l).asObject());
        assertEquals("hello", dy.get(new BigDecimal("1")).asObject());
    }

    @Test
    public void children() {
        List<Dynamic> children = dy.children().collect(toList());
        assertThat(children.get(1).asObject())
            .as(children.toString())
            .isEqualTo("hello");
        assertThat(children.get(3).asObject())
            .as(children.toString())
            .isEqualTo(asList(1, 2, 3, null));
    }

    @Test
    public void numberIndex_notInt() {
        assertFalse(dy.get(Long.MAX_VALUE).isPresent());
        assertFalse(dy.get(Integer.MAX_VALUE + "12341234").isPresent());
    }

    @Test
    public void equalsImplementation() {
        assertEquals(Dynamic.from(asList(1, 2, 3, null)), Dynamic.from(asList(1, 2, 3, null)));
    }

    @Test
    public void hashCodeImplementation() {
        assertEquals(Dynamic.from(asList(1, 2, 3)).hashCode(), Dynamic.from(asList(1, 2, 3)).hashCode());
    }

    @Test
    public void toStringImplementation() {
        assertThat(dy.toString())
            .contains("root")
            .contains("0..3");
        System.out.println("list dynamic toString: "+ dy);
    }

    @Test
    public void toStringImplementationSize0() {
        Dynamic dynamic = Dynamic.from(emptyList());
        assertThat(dynamic.toString()).contains("Empty-List");
        System.out.println("list dynamic toString: "+ dynamic);
    }

    @Test
    public void toStringImplementationSize1() {
        Dynamic dynamic = Dynamic.from(singletonList("foo"));
        assertThat(dynamic.toString()).contains("[0]");
        System.out.println("list dynamic toString: "+ dynamic);
    }

    @Test
    public void toStringImplementationSize2() {
        Dynamic dynamic = Dynamic.from(asList("foo", "bar"));
        assertThat(dynamic.toString()).contains("[0, 1]");
        System.out.println("list dynamic toString: "+ dynamic);
    }

    private final Dynamic dy2 = Dynamic.from(asList(
        new Fluent.HashMap<>()
            .append("mk1", "foo")
            .append("mk2", "bar"),
        "hello",
        new Object(),
        asList(1, 2, 3, null)
    ));

    @Test
    public void childEqualsImplementation() {
        assertEquals(dy.get(1).get("foo"), dy2.get(1).get("foo"));
        assertEquals(dy.get(3).get(1), dy2.get(3).get(1));

    }

    @Test
    public void convertedKeyEquals() {
        assertEquals(dy.get(3).get("0"), dy2.get(3).get(0), "non-null value index conversion");
        assertEquals(dy.get(3).get("3"), dy2.get(3).get(3), "null value index conversion");
        assertEquals(dy.get(3).get("999"), dy2.get(3).get(999), "out-of-bounds value index conversion");
    }

    @Test
    public void childHashCodeImplementation() {
        assertEquals(dy.get(1).get("foo").hashCode(), dy2.get(1).get("foo").hashCode());
        assertEquals(dy.get(3).get(1).hashCode(), dy2.get(3).get(1).hashCode());
    }

    @Test
    public void convertedKeyHashCode() {
        assertEquals(dy.get(3).get("0").hashCode(), dy2.get(3).get(0).hashCode(), "non-null value index conversion");
        assertEquals(dy.get(3).get("3").hashCode(), dy2.get(3).get(3).hashCode(), "null value index conversion");
        assertEquals(dy.get(3).get("999").hashCode(), dy2.get(3).get(999).hashCode(), "out-of-bounds value index conversion");
    }

    @Test
    public void convertedPresentKeyShouldBeInteger() {
        assertTrue(dy.get(3).key().is(Integer.class), "non-converted");
        assertTrue(dy.get("3").key().is(Integer.class), "converted");
    }

    @Test
    public void childToStringImplementation() {
        Dynamic presentChild = dy.get(3);
        Dynamic absentChild = dy.get(1).get("foo");

        assertTrue(presentChild.isPresent(), "oh dear");
        assertFalse(absentChild.isPresent(), "oh dear");

        assertThat(presentChild.toString())
            .containsIgnoringCase("root->3")
            .containsIgnoringCase("list");

        assertThat(absentChild.toString())
            .containsIgnoringCase("root")
            .containsIgnoringCase("1")
            .containsIgnoringCase("foo");

        System.out.println("list-child dynamic toString: "+ presentChild);
    }
}
