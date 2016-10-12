package alexh;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.emptyMap;
import static java.util.Collections.singletonMap;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.*;
import alexh.weak.Dynamic;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import org.junit.Before;
import org.junit.Test;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Stream;

public class DynamicBaseTest {

    private Dynamic dy;

    @Before
    public void setupMap() {
        dy = Dynamic.from(new Fluent.LinkedHashMap<>()
            .append("a", new Fluent.LinkedHashMap<>()
                .append("one", new Fluent.LinkedHashMap<>()
                    .append("blah", "blah")
                    .append("yeah", "yeah"))
                .append("two", new Fluent.LinkedHashMap<>()
                    .append("ecks", 123)
                    .append("why", null)
                    .append("zed", emptyMap())))
            .append("b", asList(1, 2, 3, 4))
            .append("c", emptyList())
            .append("d", asList("hello", null, null, null)));

        assertNotNull(dy);
    }

    @Test
    public void allChildren_fromTheTop() {
        Set<Dynamic> expected = ImmutableSet.of(
            dy.get("a"),
            dy.dget("a.one"),
            dy.dget("a.one.blah"),
            dy.dget("a.one.yeah"),
            dy.dget("a.two"),
            dy.dget("a.two.ecks"),
            dy.dget("a.two.why"),
            dy.dget("a.two.zed"),
            dy.get("b"),
            dy.dget("b.0"),
            dy.dget("b.1"),
            dy.dget("b.2"),
            dy.dget("b.3"),
            dy.get("c"),
            dy.get("d"),
            dy.dget("d.0"),
            dy.dget("d.1"),
            dy.dget("d.2"),
            dy.dget("d.3"));

        Set<Dynamic> actual = dy.allChildren().collect(toSet());

        Set<Dynamic> unexpectedInActual = Sets.difference(actual, expected);
        Set<Dynamic> missingFromActual = Sets.difference(expected, actual);

        if (!unexpectedInActual.isEmpty())
            System.err.println("Unexpected in actual: " + unexpectedInActual);
        if (!missingFromActual.isEmpty())
            System.err.println("Missing from actual: " + missingFromActual);

        assertTrue(unexpectedInActual.isEmpty() && missingFromActual.isEmpty());
    }

    @Test
    public void allChildrenDepthFirst_fromTheTop() {
        List<Dynamic> expected = asList(
            dy.get("a"),
            dy.dget("a.one"),
            dy.dget("a.one.blah"),
            dy.dget("a.one.yeah"),
            dy.dget("a.two"),
            dy.dget("a.two.ecks"),
            dy.dget("a.two.why"),
            dy.dget("a.two.zed"),
            dy.get("b"),
            dy.dget("b.0"),
            dy.dget("b.1"),
            dy.dget("b.2"),
            dy.dget("b.3"),
            dy.get("c"),
            dy.get("d"),
            dy.dget("d.0"),
            dy.dget("d.1"),
            dy.dget("d.2"),
            dy.dget("d.3"));

        assertThat(dy.allChildrenDepthFirst().collect(toList()))
            .isEqualTo(expected);
    }

    @Test
    public void allChildrenDepthFirst_fromChild() {
        List<Dynamic> expected = asList(
            dy.dget("a.one"),
            dy.dget("a.one.blah"),
            dy.dget("a.one.yeah"),
            dy.dget("a.two"),
            dy.dget("a.two.ecks"),
            dy.dget("a.two.why"),
            dy.dget("a.two.zed"));

        assertThat(dy.get("a").allChildrenDepthFirst().collect(toList()))
            .isEqualTo(expected);
    }

    @Test
    public void allChildrenBreadthFirst_fromChild() {
        List<Dynamic> expected = asList(
            dy.dget("a.one"),
            dy.dget("a.two"),
            dy.dget("a.one.blah"),
            dy.dget("a.one.yeah"),
            dy.dget("a.two.ecks"),
            dy.dget("a.two.why"),
            dy.dget("a.two.zed"));

        assertThat(dy.get("a").allChildrenBreadthFirst().collect(toList()))
            .isEqualTo(expected);
    }

    @Test
    public void allChildrenDepthFirst_noUnneededFetches() {
        Dynamic poisoned = Dynamic.from(new Fluent.LinkedHashMap<>()
            .append("one", new Fluent.LinkedHashMap<>()
                .append("one-a", singletonMap("key123", "123"))
                .append("one-b", singletonMap("hello", 123))
                .append("one-c", "456"))
            .append("two", new PoisonousMap()));

        String val = poisoned.allChildrenDepthFirst()
            .filter(el -> el.maybe().asString().orElse("").equals("123"))
            .findFirst()
            .orElseThrow(() -> new NoSuchElementException("no child found with value \"123\""))
            .key().asString(); // should not throw

        assertThat(val).isEqualTo("key123");
    }

    @Test
    public void allChildrenBreadthFirst_fromTheTop() {
        List<Dynamic> expected = asList(
            dy.get("a"),
            dy.get("b"),
            dy.get("c"),
            dy.get("d"),
            dy.dget("a.one"),
            dy.dget("a.two"),
            dy.dget("b.0"),
            dy.dget("b.1"),
            dy.dget("b.2"),
            dy.dget("b.3"),
            dy.dget("d.0"),
            dy.dget("d.1"),
            dy.dget("d.2"),
            dy.dget("d.3"),
            dy.dget("a.one.blah"),
            dy.dget("a.one.yeah"),
            dy.dget("a.two.ecks"),
            dy.dget("a.two.why"),
            dy.dget("a.two.zed"));

        assertThat(dy.allChildrenBreadthFirst().collect(toList()))
            .isEqualTo(expected);
    }

    @Test
    public void allChildrenBreadthFirst_noUnneededFetches() {
        Dynamic poisoned = Dynamic.from(new Fluent.LinkedHashMap<>()
            .append("one", new Fluent.LinkedHashMap<>()
                .append("one-a", new PoisonousMap())
                .append("one-b", new PoisonousMap())
                .append("one-c", "456"))
            .append("two", "123"));

        String val = poisoned.allChildrenBreadthFirst()
            .filter(el -> el.maybe().asString().orElse("").equals("123"))
            .findFirst()
            .orElseThrow(() -> new NoSuchElementException("no child found with value \"123\""))
            .key().asString(); // should not throw

        assertThat(val).isEqualTo("two");

        val = poisoned.allChildrenBreadthFirst()
            .filter(el -> el.maybe().asString().orElse("").equals("456"))
            .findFirst()
            .orElseThrow(() -> new NoSuchElementException("no child found with value \"456\""))
            .key().asString(); // should not throw

        assertThat(val).isEqualTo("one-c");
    }

    @Test
    public void isPresent() {
        assertTrue(dy.isPresent());
    }

    @Test
    public void innerPresenceKey1() {
        assertTrue(dy.get("a").isPresent());
    }

    @Test
    public void nestedInnerPresenceKey1() {
        assertTrue(dy.get("a").get("two").get("ecks").isPresent());
    }

    @Test
    public void innerAbsence() {
        assertFalse(dy.get("foo").isPresent());
    }

    @Test
    public void nestedInnerAbsence() {
        assertFalse(dy.get("foo").get("bar").get("sooooo").isPresent());
        assertFalse(dy.get("a").get("one").get("ecks").isPresent());
        assertFalse(dy.get("a").get("two").get("foo").isPresent());
    }

    @Test
    public void getAsList() {
        assertEquals(asList(1, 2, 3, 4), dy.get("b").asList());
    }

    @Test(expected = ClassCastException.class)
    public void getAsList_notList() {
        dy.get("a").asList();
    }

    @Test(expected = NoSuchElementException.class)
    public void getAsList_absent() {
        dy.get("key44").asList();
    }

    @Test
    public void getAs() {
        String key1Key2 = dy.get("a").get("one").get("blah").as(String.class);
        assertEquals("blah", key1Key2);

        Integer key1Key3Key4 = dy.get("a").get("two").get("ecks").as(Integer.class);
        assertEquals(Integer.valueOf(123), key1Key3Key4);
    }

    @Test(expected = ClassCastException.class)
    public void getAs_notClass() {
        dy.get("a").get("one").as(LocalDate.class).atStartOfDay();
    }

    @Test(expected = NoSuchElementException.class)
    public void getAs_absent() {
        dy.get("key44").as(LocalDate.class);
    }

    @Test
    public void getAsString() {
        String key1Key2 = dy.get("a").get("one").get("yeah").asString();
        assertEquals("yeah", key1Key2);
    }

    @Test(expected = ClassCastException.class)
    public void getAsString_notClass() {
        dy.get("a").get("two").asString();
    }

    @Test(expected = NoSuchElementException.class)
    public void getAsString_absent() {
        dy.get("key44").asString();
    }

    @Test
    public void getAsMap() {
        Map key1Key3 = dy.get("a").get("two").asMap();
        assertEquals(new Fluent.HashMap<>()
            .append("ecks", 123)
            .append("why", null)
            .append("zed", emptyMap()), key1Key3);
    }

    @Test(expected = ClassCastException.class)
    public void getAsMap_notClass() {
        dy.get("a").get("one").get("blah").asMap();
    }

    @Test(expected = NoSuchElementException.class)
    public void getAsMap_absent() {
        dy.get("key44").asMap();
    }

    @Test
    public void getListElement_present() {
        assertEquals(Integer.valueOf(3), dy.get("b").get(2).as(Integer.class));
    }

    @Test
    public void getListElement_absent() {
        assertFalse(dy.get("b").get(4).isPresent());
    }

    @Test
    public void asOptional() {
        assertEquals(dy.asObject(), dy.asOptional().get());

        assertTrue(dy.get("a").asOptional().isPresent());
        assertEquals(Optional.of("blah"), dy.dget("a.one.blah").asOptional());

        assertFalse(dy.get("key44").get("key24").asOptional().isPresent());
    }

    @Test
    public void maybe() {
        assertEquals(dy.asObject(), dy.maybe().map(d -> d.asObject()).orElse(null));

        assertFalse(dy.get("key44").get("key24").maybe().isPresent());
    }

    @Test
    public void is_is() {
        assertTrue(dy.is(Map.class));
        assertTrue(dy.get("a").get("one").get("yeah").is(String.class));
    }

    @Test
    public void is_isnt() {
        assertFalse(dy.is(String.class)); // wrong type
        assertFalse(dy.get("a").get("one").is(LocalDate.class)); // wrong type
        assertFalse(dy.get("foo").get("bar").is(Map.class)); // absent
    }

    @Test
    public void isString_is() {
        assertTrue(dy.get("a").get("one").get("blah").isString());
    }

    @Test
    public void isString_isnt() {
        assertFalse(dy.isString());
    }

    @Test
    public void isMap_is() {
        assertTrue(dy.isMap());
    }

    @Test
    public void isMap_isnt() {
        assertFalse(dy.get("a").get("one").get("yeah").isMap());
    }

    @Test
    public void isList_is() {
        assertTrue(dy.get("b").isList());
    }

    @Test
    public void isList_isnt() {
        assertFalse(dy.isList());
    }

    @Test
    public void dynamicFromADynamicIsItself() {
        Dynamic dy = Dynamic.from("a string");
        assertEquals(dy, Dynamic.from(dy));
    }

    @Test
    public void getChildren() {
        Set<Dynamic> expected = Stream.of("a", "b", "c", "d").map(dy::get).collect(toSet());

        assertEquals(expected, dy.children().collect(toSet()));
    }

    @Test
    public void absenceHasNoChildren() {
        assertThat(dy.get("element that doesn't exist").children().count()).isEqualTo(0l);
    }

    @Test
    public void providesConverterInstanceMethod() {
        assertThat(dy.dget("a.two.ecks").convert().intoString()).isEqualTo("123");
    }

    static class PoisonousMap implements Map<String, String> {
        static class PoisonException extends RuntimeException {}

        @Override
        public int size() {
            return 1;
        }

        @Override
        public boolean isEmpty() {
            return false;
        }

        @Override
        public boolean containsKey(Object key) {
            return keySet().contains(key);
        }

        @Override
        public boolean containsValue(Object value) {
            throw new PoisonException();
        }

        @Override
        public String get(Object key) {
            throw new PoisonException();
        }

        @Override
        public String put(String key, String value) {
            throw new PoisonException();
        }

        @Override
        public String remove(Object key) {
            throw new PoisonException();
        }

        @Override
        public void putAll(Map<? extends String, ? extends String> m) {
            throw new PoisonException();
        }

        @Override
        public void clear() {
            throw new PoisonException();
        }

        @Override
        public Set<String> keySet() {
            return ImmutableSet.of("poison");
        }

        @Override
        public Collection<String> values() {
            throw new PoisonException();
        }

        @Override
        public Set<Entry<String, String>> entrySet() {
            throw new PoisonException();
        }

        @Override
        public boolean equals(Object o) {
            return false;
        }

        @Override
        public int hashCode() {
            return 0;
        }
    }
}
