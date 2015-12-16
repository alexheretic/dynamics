package alexh;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static org.junit.Assert.assertEquals;
import java.util.List;
import org.junit.Test;

public class LiteJoinerTest {

    @Test
    public void some() {
        final String separator = ";";
        final List<?> parts = asList("hello", "you", new Object());
        assertEquals(parts.get(0) + separator + parts.get(1) + separator + parts.get(2), LiteJoiner.on(separator).join(parts));
    }

    @Test
    public void none() {
        assertEquals("", LiteJoiner.on("-").join(emptyList()));
    }
}
