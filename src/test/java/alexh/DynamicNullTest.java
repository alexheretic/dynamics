package alexh;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import alexh.weak.Dynamic;
import org.junit.jupiter.api.Test;

public class DynamicNullTest {

    @Test
    public void dynamicFromNull() {
        Dynamic nullDynamic = Dynamic.from(null);
        assertFalse(nullDynamic.isPresent());
        assertFalse(nullDynamic.isList());
        assertFalse(nullDynamic.isMap());
        assertFalse(nullDynamic.isString());
        assertFalse(nullDynamic.is(Object.class));
        assertFalse(nullDynamic.get("foo").get("bar").isPresent());
    }

    @Test
    public void equalsImplementation() {
        assertEquals(Dynamic.from(null), Dynamic.from(null));
    }

    @Test
    public void hashCodeImplementation() {
        assertEquals(Dynamic.from(null).hashCode(), Dynamic.from(null).hashCode());
    }

    @Test
    public void toStringImplementation() {
        assertThat(Dynamic.from(null).toString()).contains("root").contains("null");
        System.out.println("null dynamic toString: " + Dynamic.from(null));
    }

    @Test
    public void noChildren() {
        assertFalse(Dynamic.from(null).children().findAny().isPresent());
    }

    @Test
    public void childEqualsImplementation() {
        assertEquals(Dynamic.from(null).get(123).get("bar"), Dynamic.from(null).get(123).get("bar"));
    }

    @Test
    public void childHashCodeImplementation() {
        assertEquals(Dynamic.from(null).get(123).get("bar").hashCode(), Dynamic.from(null).get(123).get("bar").hashCode());
    }

    @Test
    public void childToStringImplementation() {
        Dynamic obj = Dynamic.from(new Object()).get(123).get("bar");
        assertThat(obj.toString())
            .containsIgnoringCase("root")
            .containsIgnoringCase("123")
            .containsIgnoringCase("bar");
        System.out.println("null-child dynamic toString: "+ obj);
    }
}
