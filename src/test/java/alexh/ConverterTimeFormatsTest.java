package alexh;

import static org.assertj.core.api.Assertions.assertThat;
import alexh.weak.ConverterTimeFormats;
import java.time.ZonedDateTime;
import java.time.temporal.TemporalAccessor;
import org.junit.jupiter.api.Test;

/**
 * Note: most of ConverterTimeFormats functionality is tested as part of the java.time functionality
 * @see ConverterTest
 */
public class ConverterTimeFormatsTest {

    @Test
    public void parseSanity() {
        TemporalAccessor parsed = ConverterTimeFormats.parse("2015-12-03T12:23:34.456Z");
        assertThat(ZonedDateTime.from(parsed)).isEqualTo(ZonedDateTime.parse("2015-12-03T12:23:34.456Z"));
    }
}
