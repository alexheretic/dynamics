package alexh;

import static java.util.Collections.singleton;
import static java.util.stream.Collectors.toList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.*;
import alexh.weak.Dynamic;
import org.junit.Before;
import org.junit.Test;
import java.math.BigDecimal;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.stream.Stream;

public class DynamicCollectionTest {

    @SafeVarargs
    static <T> Set<T> hashSet(T val, T... vals) {
        Set<T> set = new HashSet<>();
        set.add(val);
        Stream.of(vals).forEach(set::add);
        return set;
    }

    private Dynamic dy;

    @Before
    public void setupMap() {
        Collection<Object> collection = new LinkedBlockingDeque<>();

        collection.add(new Fluent.HashMap<>()
            .append("mk1", "foo")
            .append("mk2", "bar"));
        collection.add("hello");
        collection.add(new Object());
        collection.add(hashSet(1, 2, 3));

        dy = Dynamic.from(collection);
        assertNotNull(dy);
    }

    @Test
    public void indexShouldNotWork() {
        assertThat(dy.get("1").isPresent()).as("Collections should not be accessible via index").isFalse();
        assertThat(dy.get(1).isPresent()).as("Collections should not be accessible via index").isFalse();
        assertThat(dy.get(1l).isPresent()).as("Collections should not be accessible via index").isFalse();
        assertThat(dy.get(new BigDecimal("1")).isPresent()).as("Collections should not be accessible via index").isFalse();
    }

    @Test
    public void children() {
        List<Dynamic> children = dy.children().collect(toList());

        Dynamic child1 = children.get(1);
        assertThat(child1.asObject()).as(child1.toString()).isEqualTo("hello");

        Dynamic child3 = children.get(3);
        assertThat(child3.asObject()).as(child3.toString()).isEqualTo(hashSet(1, 2, 3));
    }

    @Test
    public void equalsImplementation() {
        assertEquals(Dynamic.from(hashSet(1, 2, 3)), Dynamic.from(hashSet(1, 2, 3)));
    }

    @Test
    public void hashCodeImplementation() {
        assertEquals(Dynamic.from(hashSet(1, 2, 3)).hashCode(), Dynamic.from(hashSet(1, 2, 3)).hashCode());
    }

    @Test
    public void toStringImplementation() {
        assertThat(dy.toString())
            .containsIgnoringCase("root")
            .contains("Collection")
            .containsIgnoringCase("[size:4]");
        System.out.println("collection dynamic toString: "+ dy);
    }

    @Test
    public void toStringImplementationSize0() {
        Dynamic dynamic = Dynamic.from(new HashSet<>());
        assertThat(dynamic.toString()).contains("Empty-Set");
        System.out.println("collection dynamic toString: "+ dynamic);
    }

    @Test
    public void toStringImplementationSize1() {
        Dynamic dynamic = Dynamic.from(singleton("foo"));
        assertThat(dynamic.toString()).contains("[size:1]");
        System.out.println("collection dynamic toString: "+ dynamic);
    }

    @Test
    public void toStringImplementationSize2() {
        Dynamic dynamic = Dynamic.from(hashSet("foo", "bar"));
        assertThat(dynamic.toString()).contains("[size:2]");
        System.out.println("collection dynamic toString: "+ dynamic);
    }

    @Test
    public void childEqualsHashcodeImplementation() {
        Collection<Object> collection = new LinkedBlockingDeque<>();

        collection.add(new Fluent.HashMap<>()
            .append("mk1", "foo")
            .append("mk2", "bar"));
        collection.add("hello");
        collection.add(new Object());
        collection.add(hashSet(1, 2, 3));

        Dynamic dy2 = Dynamic.from(collection);

        Dynamic setChild = dy.children().filter(c -> c.is(Collection.class)).findAny().get();

        Dynamic setChild2 = dy2.children().filter(c -> c.is(Collection.class)).findAny().get();

        assertThat(setChild2).isEqualTo(setChild);
        assertThat(setChild2.hashCode()).isEqualTo(setChild.hashCode());

        // absent child
        assertThat(dy2.get(123).get(234)).isEqualTo(dy2.get(123).get(234));
        assertThat(dy2.get(123).get(234).hashCode()).isEqualTo(dy2.get(123).get(234).hashCode());
    }

    @Test
    public void childToStringImplementation() {
        Dynamic presentChild = dy.children().filter(c -> c.is(Collection.class)).findAny().get();
        Dynamic absentChild = dy.get(1).get("foo");

        assertTrue("oh dear", presentChild.isPresent());
        assertFalse("oh dear", absentChild.isPresent());

        assertThat(presentChild.toString()).as("present child #toString()")
            .containsIgnoringCase("root->?")
            .contains("Set");

        assertThat(absentChild.toString()).as("absent child #toString()")
            .containsIgnoringCase("root")
            .containsIgnoringCase("1")
            .containsIgnoringCase("foo");

        System.out.println("set-child dynamic toString: "+ presentChild);
    }
}
