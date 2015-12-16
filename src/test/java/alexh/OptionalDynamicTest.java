package alexh;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonMap;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import alexh.weak.Dynamic;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import org.junit.Test;

public class OptionalDynamicTest {

    private final Dynamic example = Dynamic.from(new Fluent.HashMap<>()
        .append("hello", "world")
        .append("number?", 123)
        .append("someList", asList(45555l, 345l))
        .append("aMap", singletonMap("value", 33)));

    @Test
    public void fluentMaybe() {
        Optional<Object> optional = example.get("number?").maybe().asObject();
        assertThat(optional, is(Optional.of(123)));
    }

    @Test
    public void fluentOptionalCasting() {
        Optional<String> optionalMappedString = example.get("hello").maybe().as(String.class);
        assertThat(optionalMappedString, is(Optional.of("world")));

        Optional<Integer> optionalMappedInt = example.get("number?").maybe().as(Integer.class);
        assertThat(optionalMappedInt, is(Optional.of(123)));
    }

    @Test
    public void castMaybesDoNotThrow() {
        Optional<LocalDate> optionalMappedLocalDate = example.get("hello").maybe().as(LocalDate.class);
        assertThat(optionalMappedLocalDate, is(Optional.empty()));
    }

    @Test
    public void asListMalleableGenericShortcut() {
        Optional<List<Long>> someList = example.get("someList").maybe().asList();
        assertThat(someList, is(Optional.of(asList(45555l, 345l))));

        assertThat(example.get("not-here").maybe().asList(), is(Optional.empty()));
    }

    @Test
    public void asMapMalleableGenericShortcut() {
        Optional<Map<String, Integer>> map = example.get("aMap").maybe().asMap();
        assertThat(map, is(Optional.of(singletonMap("value", 33))));

        assertThat(example.get("not-here").maybe().asMap(), is(Optional.empty()));
    }

    @Test
    public void asStringShortcut() {
        assertThat(example.get("hello").maybe().asString(), is(Optional.of("world")));

        assertThat(example.get("not-here").maybe().asString(), is(Optional.empty()));
    }

    @Test
    public void converterMaybeUsage() {
        assertThat(example.get("number?").maybe().convert().intoDecimal(), is(Optional.of(new BigDecimal(123))));

        assertThat(example.get("hello").maybe().convert().intoDecimal(), is(Optional.empty()));
        assertThat(example.get("not-here").maybe().convert().intoDecimal(), is(Optional.empty()));
    }

    @Test
    public void equalsWorks() {
        assertThat(example.get("number?").maybe(), is(Dynamic.from(new Fluent.HashMap<>()
            .append("hello", "world")
            .append("number?", 123)
            .append("someList", asList(45555l, 345l))
            .append("aMap", singletonMap("value", 33))).get("number?").maybe()));
    }

    @Test
    public void hashCodeWorks() {
        assertThat(example.get("number?").maybe().hashCode(), is(Dynamic.from(new Fluent.HashMap<>()
            .append("hello", "world")
            .append("number?", 123)
            .append("someList", asList(45555l, 345l))
            .append("aMap", singletonMap("value", 33))).get("number?").maybe().hashCode()));
    }

    @Test
    public void toStringWorks() {
        assertThat(example.get("number?").maybe().toString(), is("Optional[root->number?:Integer]"));
        assertThat(example.get("not-here").maybe().toString(), is("Optional.empty"));
    }

    @Test
    public void coreOptionalMethods() {
        assertThat(example.get("hello").maybe().map(d -> d.asString()), is(Optional.of("world")));
        assertThat(example.get("hello").maybe().flatMap(d -> Optional.of(d.asString())), is(Optional.of("world")));

        AtomicBoolean wasPresent = new AtomicBoolean(false);
        example.get("hello").maybe().ifPresent(d -> wasPresent.set(true));
        assertTrue("#ifPresent didn't work", wasPresent.get());

        assertThat(example.get("hello").maybe().filter(d -> d.asString().length() >= 50).asString(),
            is(Optional.empty()));
        assertThat(example.get("hello").maybe().filter(d -> d.asString().length() < 50).asString(),
            is(Optional.of("world")));

        assertThat(example.get("hello").maybe().orElse(example.get("another-thing")).asString(),
            is("world"));

        assertThat(example.get("hello").maybe().orElseGet(() -> example.get("another-thing")).asString(),
            is("world"));

        assertThat(example.get("not-here").maybe().orElse(example.get("hello")).asString(),
            is("world"));
        assertThat(example.get("not-here").maybe().orElseGet(() -> example.get("hello")).asString(),
            is("world"));

        assertThat(example.get("hello").maybe().orElseThrow(() -> new RuntimeException("won't happen")).asString(),
            is("world"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void coreOptionalMethodOrElseThrow() {
        example.get("not-here").maybe().orElseThrow(() -> new IllegalArgumentException("this is happening"));
    }
}
