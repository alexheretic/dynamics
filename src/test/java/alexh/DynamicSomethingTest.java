package alexh;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import alexh.weak.Dynamic;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class DynamicSomethingTest {

    private Dynamic dy;

    @BeforeEach
    public void setupMap() {
        dy = Dynamic.from("something");
        assertNotNull(dy);
    }

    @Test
    public void isItself() {
        assertEquals("something", dy.asObject());
    }

    @Test
    public void noChildren() {
        assertFalse(dy.get("anything").isPresent());
        assertFalse(dy.get(1).isPresent());
        assertThat(dy.children()).isEmpty();
    }

    @Test
    public void equalsImplementation() {
        assertEquals(Dynamic.from("a string"), Dynamic.from("a string"));
    }

    @Test
    public void hashCodeImplementation() {
        assertEquals(Dynamic.from("a string").hashCode(), Dynamic.from("a string").hashCode());
    }

    @Test
    public void toStringImplementation() {
        Dynamic obj = Dynamic.from(new Object());
        assertThat(obj.toString())
            .containsIgnoringCase("root")
            .containsIgnoringCase("object");
        System.out.println("object dynamic toString: "+ obj);
    }

    @Test
    public void childEqualsImplementation() {
        assertEquals(Dynamic.from("a string").get(123), Dynamic.from("a string").get(123));
    }

    @Test
    public void childHashCodeImplementation() {
        assertEquals(Dynamic.from("a string").get(123).hashCode(), Dynamic.from("a string").get(123).hashCode());
    }

    @Test
    public void childToStringImplementation() {
        Dynamic obj = Dynamic.from(new Object()).get(123);
        assertThat(obj.toString())
            .containsIgnoringCase("root")
            .contains("123");
        System.out.println("object-child dynamic toString: "+ obj);
    }
}
