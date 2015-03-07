package alexh;

import org.junit.Test;
import java.util.List;

import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;

public class LiteJoinerTest {

    @Test
    public void some() {
        final String separator = ";";
        final List<?> parts = asList("hello", "you", new Object());
        assertEquals(parts.get(0) + separator + parts.get(1) + separator + parts.get(2), LiteJoiner.on(separator).join(parts));
    }

    @Test
    public void none() {
        final String separator = "-";
        final Iterable<?> parts = asList();
        assertEquals("", LiteJoiner.on(separator).join(parts));
    }
}
