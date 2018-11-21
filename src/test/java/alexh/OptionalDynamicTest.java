package alexh;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonMap;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import alexh.weak.Dynamic;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import org.junit.jupiter.api.Test;

public class OptionalDynamicTest {

    private final Dynamic example = Dynamic.from(new Fluent.HashMap<>()
        .append("hello", "world")
        .append("number?", 123)
        .append("someList", asList(45555l, 345l))
        .append("aMap", singletonMap("value", 33)));

    @Test
    public void fluentMaybe() {
        Optional<Object> optional = example.get("number?").maybe().asObject();
        assertThat(optional).isEqualTo(Optional.of(123));
    }

    @Test
    public void fluentOptionalCasting() {
        Optional<String> optionalMappedString = example.get("hello").maybe().as(String.class);
        assertThat(optionalMappedString).isEqualTo(Optional.of("world"));

        Optional<Integer> optionalMappedInt = example.get("number?").maybe().as(Integer.class);
        assertThat(optionalMappedInt).isEqualTo(Optional.of(123));
    }

    @Test
    public void castMaybesDoNotThrow() {
        Optional<LocalDate> optionalMappedLocalDate = example.get("hello").maybe().as(LocalDate.class);
        assertThat(optionalMappedLocalDate).isEqualTo(Optional.empty());
    }

    @Test
    public void asListMalleableGenericShortcut() {
        Optional<List<Long>> someList = example.get("someList").maybe().asList();
        assertThat(someList).isEqualTo(Optional.of(asList(45555l, 345l)));

        assertThat(example.get("not-here").maybe().asList()).isEqualTo(Optional.empty());
    }

    @Test
    public void asMapMalleableGenericShortcut() {
        Optional<Map<String, Integer>> map = example.get("aMap").maybe().asMap();
        assertThat(map).isEqualTo(Optional.of(singletonMap("value", 33)));

        assertThat(example.get("not-here").maybe().asMap()).isEqualTo(Optional.empty());
    }

    @Test
    public void asStringShortcut() {
        assertThat(example.get("hello").maybe().asString()).isEqualTo(Optional.of("world"));

        assertThat(example.get("not-here").maybe().asString()).isEqualTo(Optional.empty());
    }

    @Test
    public void converterMaybeUsage() {
        assertThat(example.get("number?").maybe().convert().intoDecimal()).isEqualTo(Optional.of(new BigDecimal(123)));

        assertThat(example.get("hello").maybe().convert().intoDecimal()).isEqualTo(Optional.empty());
        assertThat(example.get("not-here").maybe().convert().intoDecimal()).isEqualTo(Optional.empty());
    }

    @Test
    public void equalsWorks() {
        assertThat(example.get("number?").maybe()).isEqualTo(Dynamic.from(new Fluent.HashMap<>()
            .append("hello", "world")
            .append("number?", 123)
            .append("someList", asList(45555l, 345l))
            .append("aMap", singletonMap("value", 33))).get("number?").maybe());
    }

    @Test
    public void hashCodeWorks() {
        assertThat(example.get("number?").maybe().hashCode()).isEqualTo(Dynamic.from(new Fluent.HashMap<>()
            .append("hello", "world")
            .append("number?", 123)
            .append("someList", asList(45555l, 345l))
            .append("aMap", singletonMap("value", 33))).get("number?").maybe().hashCode());
    }

    @Test
    public void toStringWorks() {
        assertThat(example.get("number?").maybe().toString()).isEqualTo("Optional[root->number?:Integer]");
        assertThat(example.get("not-here").maybe().toString()).isEqualTo("Optional.empty");
    }

    @Test
    public void coreOptionalMethods() {
        assertThat(example.get("hello").maybe().map(d -> d.asString())).isEqualTo(Optional.of("world"));
        assertThat(example.get("hello").maybe().flatMap(d -> Optional.of(d.asString()))).isEqualTo(Optional.of("world"));

        AtomicBoolean wasPresent = new AtomicBoolean(false);
        example.get("hello").maybe().ifPresent(d -> wasPresent.set(true));
        assertTrue(wasPresent.get(), "#ifPresent didn't work");

        assertThat(example.get("hello").maybe().filter(d -> d.asString().length() >= 50).asString()).isEqualTo(Optional.empty());
        assertThat(example.get("hello").maybe().filter(d -> d.asString().length() < 50).asString()).isEqualTo(Optional.of("world"));

        assertThat(example.get("hello").maybe().orElse(example.get("another-thing")).asString()).isEqualTo("world");

        assertThat(example.get("hello").maybe().orElseGet(() -> example.get("another-thing")).asString()).isEqualTo("world");

        assertThat(example.get("not-here").maybe().orElse(example.get("hello")).asString()).isEqualTo("world");
        assertThat(example.get("not-here").maybe().orElseGet(() -> example.get("hello")).asString()).isEqualTo("world");

        assertThat(example.get("hello").maybe().orElseThrow(() -> new RuntimeException("won't happen")).asString()).isEqualTo("world");
    }

    @Test
    public void coreOptionalMethodOrElseThrow() {
        assertThrows(IllegalArgumentException.class,
            () -> example.get("not-here").maybe().orElseThrow(() -> new IllegalArgumentException("this is happening")));
    }
}
