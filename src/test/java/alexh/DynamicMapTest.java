package alexh;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import alexh.weak.Dynamic;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;

public class DynamicMapTest {

    private final Dynamic dynamicMap = Dynamic.from(new Fluent.HashMap<>()
        .append("hello", "world")
        .append("whats", "up?"));

    private final Dynamic dynamicMap2 = Dynamic.from(new Fluent.HashMap<>()
        .append(1, "something")
        .append("dictionary", new Fluent.HashMap<>()
            .append("hello", 123)));

    @Test
    public void key() {
        assertThat(dynamicMap.key().asObject()).isEqualTo(Dynamic.ROOT_KEY);
        assertThat(dynamicMap.get("hello").key().asObject()).isEqualTo("hello");
    }

    @Test
    public void stringGetsMatchToStringsIfOtherwiseWouldBeAbsent() {
        final Object someObject = new Object();

        Dynamic dy = Dynamic.from(new Fluent.HashMap<>()
            .append(1, new Fluent.HashMap<>()
                .append(2222222222222222l, 123))
            .append(someObject, "hello")
            .append(22, "hello")
            .append("22", "olleh"));

        assertThat(dy.get("1").get("2222222222222222").asObject()).isEqualTo(123);
        assertThat(dy.get(someObject.toString()).asObject()).isEqualTo("hello");

        assertThat(dy.get("22").asObject())
            .as("Should choose exact match over toString")
            .isEqualTo("olleh");
    }

    @Test
    public void nonStringGetsMatchStringsIfOtherwiseWouldBeAbsent() {
        final Object someObject = new Object();

        Dynamic dy = Dynamic.from(new Fluent.HashMap<>()
            .append("1", new Fluent.HashMap<>()
                .append("2222222222222222", 123))
            .append(someObject.toString(), "hello")
            .append(22, "hello")
            .append("22", "olleh"));

        assertThat(dy.get(1).get(new BigDecimal(2222222222222222L)).asObject()).isEqualTo(123);
        assertThat(dy.get(someObject).asObject()).isEqualTo("hello");

        assertThat(dy.get(22).asObject())
            .as("Should choose exact match over toString")
            .isEqualTo("hello");
    }

    @Test
    public void equalsImplementation() {
        assertEquals(dynamicMap, Dynamic.from(new Fluent.HashMap<>()
            .append("hello", "world")
            .append("whats", "up?")));
    }

    @Test
    public void hashCodeImplementation() {
        assertEquals(dynamicMap.hashCode(), Dynamic.from(new Fluent.HashMap<>()
            .append("hello", "world")
            .append("whats", "up?")).hashCode());
    }

    @Test
    public void toStringImplementation() {
        assertThat(dynamicMap.toString())
            .contains("root")
            .contains("hello")
            .contains("whats");
        System.out.println("map dynamic toString: "+ dynamicMap);
    }

    @Test
    public void childEqualsImplementation() {
        Dynamic dy = Dynamic.from(new Fluent.HashMap<>()
            .append(1, "something")
            .append("dictionary", new Fluent.HashMap<>()
                    .append("hello", 123)));

        assertEquals(dy.get(1).get("foo"), dynamicMap2.get(1).get("foo"));
        assertEquals(dy.get("dictionary").get("hello"), dynamicMap2.get("dictionary").get("hello"));
    }

    @Test
    public void childHashCodeImplementation() {
        Dynamic dy = Dynamic.from(new Fluent.HashMap<>()
            .append(1, "something")
            .append("dictionary", new Fluent.HashMap<>()
                .append("hello", 123)));

        assertEquals(dy.get(1).get("foo").hashCode(), dynamicMap2.get(1).get("foo").hashCode());
        assertEquals(dy.get("dictionary").get("hello").hashCode(), dynamicMap2.get("dictionary").get("hello").hashCode());
    }

    @Test
    public void childToStringImplementation() {
        Dynamic presentChild = dynamicMap2.get("dictionary");
        Dynamic absentChild = dynamicMap2.get(1).get("foo").get("bar");

        assertThat(presentChild.toString())
            .containsIgnoringCase("root->dictionary")
            .containsIgnoringCase("map");

        assertThat(absentChild.toString())
            .containsIgnoringCase("root")
            .containsIgnoringCase("1")
            .containsIgnoringCase("foo")
            .containsIgnoringCase("bar");

        System.out.println("map-child dynamic toString: "+ presentChild);
    }
}
