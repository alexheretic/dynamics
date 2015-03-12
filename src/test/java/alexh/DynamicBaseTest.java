package alexh;

import alexh.weak.Dynamic;
import org.junit.Before;
import org.junit.Test;
import java.time.LocalDate;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.*;
import java.util.stream.Stream;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.emptyMap;
import static java.util.stream.Collectors.toSet;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.*;

public class DynamicBaseTest {

    private Dynamic dy;

    @Before
    public void setupMap() {
        dy = Dynamic.from(new Fluent.HashMap<>()
            .append("key1", new Fluent.HashMap<>()
                .append("key2", "hello")
                .append("key3", new Fluent.HashMap<>()
                    .append("key4", 123)
                    .append("key6", null)
                    .append("key7", emptyMap())
                )
            )
            .append("key5", asList(1, 2, 3, 4))
            .append("key8", emptyList())
            .append("key9", asList("hello", null, null, null))
        );

        assertNotNull(dy);
    }
    
    @Test
    public void isPresent() {
        assertTrue(dy.isPresent());
    }

    @Test
    public void innerPresenceKey1() {
        assertTrue(dy.get("key1").isPresent());
    }

    @Test
    public void nestedInnerPresenceKey1() {
        assertTrue(dy.get("key1").get("key3").get("key4").isPresent());
    }

    @Test
    public void innerAbsence() {
        assertFalse(dy.get("foo").isPresent());
    }

    @Test
    public void nestedInnerAbsence() {
        assertFalse(dy.get("foo").get("bar").get("sooooo").isPresent());
        assertFalse(dy.get("key1").get("key2").get("key4").isPresent());
        assertFalse(dy.get("key1").get("key3").get("foo").isPresent());
    }

    @Test
    public void getAsList() {
        assertEquals(asList(1, 2, 3, 4), dy.get("key5").asList());
    }

    @Test(expected = ClassCastException.class)
    public void getAsList_notList() {
        dy.get("key1").asList();
    }

    @Test(expected = NoSuchElementException.class)
    public void getAsList_absent() {
        dy.get("key44").asList();
    }

    @Test
    public void getAs() {
        String key1Key2 = dy.get("key1").get("key2").as(String.class);
        assertEquals("hello", key1Key2);

        Integer key1Key3Key4 = dy.get("key1").get("key3").get("key4").as(Integer.class);
        assertEquals(Integer.valueOf(123), key1Key3Key4);
    }

    @Test(expected = ClassCastException.class)
    public void getAs_notClass() {
        dy.get("key1").get("key2").as(LocalDate.class).atStartOfDay();
    }

    @Test(expected = NoSuchElementException.class)
    public void getAs_absent() {
        dy.get("key44").as(LocalDate.class);
    }

    @Test
    public void getAsString() {
        String key1Key2 = dy.get("key1").get("key2").asString();
        assertEquals("hello", key1Key2);
    }

    @Test(expected = ClassCastException.class)
    public void getAsString_notClass() {
        dy.get("key1").get("key3").asString();
    }

    @Test(expected = NoSuchElementException.class)
    public void getAsString_absent() {
        dy.get("key44").asString();
    }

    @Test
    public void getAsMap() {
        Map key1Key3 = dy.get("key1").get("key3").asMap();
        assertEquals(new Fluent.HashMap<>()
            .append("key4", 123)
            .append("key6", null)
            .append("key7", emptyMap()), key1Key3);
    }

    @Test(expected = ClassCastException.class)
    public void getAsMap_notClass() {
        dy.get("key1").get("key2").asMap();
    }

    @Test(expected = NoSuchElementException.class)
    public void getAsMap_absent() {
        dy.get("key44").asMap();
    }

    @Test
    public void getListElement_present() {
        assertEquals(Integer.valueOf(3), dy.get("key5").get(2).as(Integer.class));
    }

    @Test
    public void getListElement_absent() {
        assertFalse(dy.get("key5").get(4).isPresent());
    }

    @Test
    public void asOptional() {
        assertEquals(dy.asObject(), dy.asOptional().get());

        assertTrue(dy.get("key1").asOptional().isPresent());
        assertEquals(Optional.of("hello"), dy.get("key1").get("key2").asOptional());

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
        assertTrue(dy.get("key1").get("key2").is(String.class));
    }

    @Test
    public void is_isnt() {
        assertFalse(dy.is(String.class)); // wrong type
        assertFalse(dy.get("key1").get("key2").is(LocalDate.class)); // wrong type
        assertFalse(dy.get("foo").get("bar").is(Map.class)); // absent
    }

    @Test
    public void isString_is() {
        assertTrue(dy.get("key1").get("key2").isString());
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
        assertFalse(dy.get("key1").get("key2").isMap());
    }

    @Test
    public void isList_is() {
        assertTrue(dy.get("key5").isList());
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
    public void getAllAvailableChildren() {
        Set<Dynamic> expected = Stream.of("key1", "key5", "key8", "key9").map(dy::get).collect(toSet());

        assertEquals(expected, dy.children().collect(toSet()));
        assertEquals(expected, dy.childSet());
    }

    @Test
    public void providesConverterInstanceMethod() {
        assertThat(dy.get("key1.key3.key4", ".").convert().intoString(), is("123"));
    }
}
