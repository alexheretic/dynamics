package alexh;

import alexh.weak.Dynamic;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.AllOf.allOf;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

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
        assertThat(Dynamic.from(null).toString(), allOf(containsString("root"), containsString("null")));
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
        assertThat(obj.toString().toLowerCase(), allOf(containsString("root"), containsString("123"), containsString("bar")));
        System.out.println("null-child dynamic toString: "+ obj);
    }
}
