package alexh;

import alexh.weak.Converter;
import org.junit.Test;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import static alexh.ConverterTest.Tester.test;
import static alexh.weak.Converter.convert;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonMap;
import static junit.framework.TestCase.assertTrue;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

public class ConverterTest {

    @Test
    public void aString() {
        test("hello world")
            .converts(Converter::intoString, "hello world")
            .converts(Converter::intoObject, "hello world")
            .converts(Converter::intoMap, singletonMap("value", "hello world"))
            .converts(Converter::intoList, asList("hello world"))
            .throwsWhen(Converter::intoInteger)
            .throwsWhen(Converter::intoLong)
            .throwsWhen(Converter::intoDouble)
            .throwsWhen(Converter::intoDecimal)
            .throwsWhen(Converter::intoLocalDateTime)
            .throwsWhen(Converter::intoZonedDateTime);
    }

    @Test
    public void stringInteger() {
        test("1234")
            .converts(Converter::intoInteger, 1234)
            .converts(Converter::intoLong, 1234l)
            .converts(Converter::intoDouble, 1234d)
            .converts(Converter::intoDecimal, new BigDecimal(1234));
    }

    @Test
    public void stringLong() {
        test("1425688985487")
            .throwsWhen(Converter::intoInteger)
            .converts(Converter::intoLong, 1425688985487l)
            .converts(Converter::intoDouble, 1425688985487d)
            .converts(Converter::intoDecimal, new BigDecimal(1425688985487l))
            .converts(Converter::intoLocalDateTime, LocalDateTime
                .ofInstant(Instant.ofEpochMilli(1425688985487l), ZoneId.systemDefault()))
            .throwsWhen(Converter::intoZonedDateTime);
    }

    @Test
    public void stringDouble() {
        test("12345.6789")
            .converts(Converter::intoInteger, 12346)
            .converts(Converter::intoLong, 12346l)
            .converts(Converter::intoDouble, 12345.6789d)
            .converts(Converter::intoDecimal, new BigDecimal("12345.6789"));
    }

    @Test
    public void stringBigDecimal() {
        test("12345112341234123109548109238758132764189723644123123123.6713212312123476123984761293874312389")
            .throwsWhen(Converter::intoInteger)
            .throwsWhen(Converter::intoLong)
            .converts(Converter::intoDouble, 12345112341234123109548109238758132764189723644123123123.6713212312123476123984761293874312389d)
            .converts(Converter::intoDecimal, new BigDecimal("12345112341234123109548109238758132764189723644123123123.6713212312123476123984761293874312389"));
    }

    @Test
    public void stringLongableBigDecimal() {
        test("123451123412341231.6713212312123476123984761293874312389")
            .throwsWhen(Converter::intoInteger)
            .converts(Converter::intoLong, 123451123412341232l)
            .converts(Converter::intoDouble, 123451123412341231.67132123121234761239847612938743123899d)
            .converts(Converter::intoDecimal, new BigDecimal("123451123412341231.6713212312123476123984761293874312389"))
            .converts(Converter::intoLocalDateTime, LocalDateTime
                .ofInstant(Instant.ofEpochMilli(123451123412341232l), ZoneId.systemDefault()))
            .throwsWhen(Converter::intoZonedDateTime);
    }

    @Test
    public void isoDateString() {
        test("2015-03-07T00:37:41.946642144Z[Europe/London]")
            .converts(c -> c.intoZonedDateTimeOrUse(ZoneId.of("Pacific/Tahiti")), ZonedDateTime.parse("2015-03-07T00:37:41.946642144Z[Europe/London]"))
            .converts(Converter::intoZonedDateTime, ZonedDateTime.parse("2015-03-07T00:37:41.946642144Z[Europe/London]"))
            .converts(Converter::intoLocalDateTime, ZonedDateTime.parse("2015-03-07T00:37:41.946642144Z[Europe/London]").toLocalDateTime());

        test("2015-03-07T00:37:41.946642144Z")
            .converts(c -> c.intoZonedDateTimeOrUse(ZoneId.of("Asia/Novosibirsk")), ZonedDateTime.parse("2015-03-07T00:37:41.946642144Z[Z]"))
            .converts(Converter::intoZonedDateTime, ZonedDateTime.parse("2015-03-07T00:37:41.946642144Z[Z]"))
            .converts(Converter::intoLocalDateTime, LocalDateTime.parse("2015-03-07T00:37:41.946642144"));

        test("2015-03-07T00:37:41.946+00:00")
            .converts(c -> c.intoZonedDateTimeOrUse(ZoneId.of("CET")), ZonedDateTime.parse("2015-03-07T00:37:41.946Z[Europe/London]"))
            .converts(Converter::intoZonedDateTime, ZonedDateTime.parse("2015-03-07T00:37:41.946+00:00[Europe/London]"))
            .converts(Converter::intoLocalDateTime, LocalDateTime.parse("2015-03-07T00:37:41.946"));

        test("2015-03-07T00:37:41.946")
            .converts(c -> c.intoZonedDateTimeOrUse(ZoneId.systemDefault()), ZonedDateTime.parse("2015-03-07T00:37:41.946Z[Europe/London]"))
            .throwsWhen(Converter::intoZonedDateTime)
            .converts(Converter::intoLocalDateTime, LocalDateTime.parse("2015-03-07T00:37:41.946"));

        test("2015-03-07T00:3")
            .converts(c -> c.intoZonedDateTimeOrUse(ZoneId.systemDefault()), ZonedDateTime.parse("2015-03-07T00:03:00.000Z[Europe/London]"))
            .throwsWhen(Converter::intoZonedDateTime)
            .converts(Converter::intoLocalDateTime, LocalDateTime.parse("2015-03-07T00:03:00.000"));

        test("2015-03-07")
            .converts(c -> c.intoZonedDateTimeOrUse(ZoneId.systemDefault()), ZonedDateTime.parse("2015-03-07T00:00:00.000Z[Europe/London]"))
            .throwsWhen(Converter::intoZonedDateTime)
            .converts(Converter::intoLocalDateTime, LocalDateTime.parse("2015-03-07T00:00:00.000"));

        test("2015-03")
            .converts(c -> c.intoZonedDateTimeOrUse(ZoneId.of("Europe/London")), ZonedDateTime.parse("2015-03-01T00:00:00.000Z[Europe/London]"))
            .throwsWhen(Converter::intoZonedDateTime)
            .converts(Converter::intoLocalDateTime, LocalDateTime.parse("2015-03-01T00:00:00.000"));

        test("2015")
            .converts(c -> c.intoZonedDateTimeOrUse(ZoneId.of("Europe/London")), ZonedDateTime.parse("2015-01-01T00:00:00.000Z[Europe/London]"))
            .throwsWhen(Converter::intoZonedDateTime)
            .converts(Converter::intoLocalDateTime, LocalDateTime.parse("2015-01-01T00:00:00.000"));
    }

    @Test
    public void dayMonthYearString() {
        test("7-March-2015 00:37:41.946-02:00")
            .converts(c -> c.intoZonedDateTimeOrUse(ZoneId.of("Europe/London")), ZonedDateTime.parse("2015-03-07T02:37:41.946Z"))
            .converts(Converter::intoZonedDateTime, ZonedDateTime.parse("2015-03-07T02:37:41.946Z"))
            .converts(Converter::intoLocalDateTime, LocalDateTime.parse("2015-03-07T00:37:41.946"));

        test("7-March-2015 00:37:41.946")
            .converts(c -> c.intoZonedDateTimeOrUse(ZoneId.of("+00:00")), ZonedDateTime.parse("2015-03-07T00:37:41.946Z"))
            .throwsWhen(Converter::intoZonedDateTime)
            .converts(Converter::intoLocalDateTime, LocalDateTime.parse("2015-03-07T00:37:41.946"));

        test("7-March-2015")
            .alsoTest("7-Mar-2015")
            .alsoTest("7/Mar/2015")
            .alsoTest("7/March/2015")
            .converts(c -> c.intoZonedDateTimeOrUse(ZoneId.of("+00:00")), ZonedDateTime.parse("2015-03-07T00:00:00.000Z"))
            .throwsWhen(Converter::intoZonedDateTime)
            .converts(Converter::intoLocalDateTime, LocalDateTime.parse("2015-03-07T00:00:00.000"));

        test("7-March") // don't default year
            .throwsWhen(c -> c.intoZonedDateTimeOrUse(ZoneId.of("+00:00")))
            .throwsWhen(Converter::intoZonedDateTime)
            .throwsWhen(Converter::intoZonedDateTime);
    }

    @Test
    public void utilDateToStringParsing() {
        test("Sat Mar 07 00:54:06 UTC 2015")
            .converts(c -> c.intoZonedDateTimeOrUse(ZoneId.of("Europe/London")), ZonedDateTime.parse("2015-03-07T00:54:06Z"))
            .converts(Converter::intoZonedDateTime, ZonedDateTime.parse("2015-03-07T00:54:06Z"))
            .converts(Converter::intoLocalDateTime, LocalDateTime.parse("2015-03-07T00:54:06"));
    }

    @Test
    public void epochMillisLongParsing() {
        test("1425688985487")
            .alsoTest(1425688985487l)
            .converts(c -> c.intoZonedDateTimeOrUse(ZoneId.of("+00:00")), ZonedDateTime.parse("2015-03-07T00:43:05.487Z"))
            .throwsWhen(Converter::intoZonedDateTime)
            .converts(Converter::intoLocalDateTime, LocalDateTime.parse("2015-03-07T00:43:05.487"));
    }

    @Test
    public void epochMillisInstantParsing() {
        test(Instant.ofEpochMilli(1425688985487l))
            .converts(c -> c.intoZonedDateTimeOrUse(ZoneId.of("+06:00")), ZonedDateTime.parse("2015-03-07T00:43:05.487Z"))
            .converts(Converter::intoZonedDateTime, ZonedDateTime.parse("2015-03-07T00:43:05.487Z"))
            .converts(Converter::intoLocalDateTime, LocalDateTime.parse("2015-03-07T00:43:05.487"));
    }

    @Test
    public void utilDateSupport() {
        test(new java.util.Date(1425688985487l))
            .converts(Converter::intoLong, 1425688985487l)
            .converts(Converter::intoDecimal, new BigDecimal(1425688985487l))
            .converts(Converter::intoDouble, 1425688985487d)
            .converts(Converter::intoString, "1425688985487")
            .converts(c -> c.intoZonedDateTimeOrUse(ZoneId.of("+00:00")), ZonedDateTime.parse("2015-03-07T00:43:05.487Z"))
            .throwsWhen(Converter::intoZonedDateTime)
            .converts(Converter::intoLocalDateTime, LocalDateTime.parse("2015-03-07T00:43:05.487"));
    }

    @Test
    public void rubbishDateStrings() {
        test("ylhuastyhaarstda").alsoTest("1234hello").alsoTest(new Object())
            .throwsWhen(c -> c.intoZonedDateTimeOrUse(ZoneId.of("+00:00")))
            .throwsWhen(Converter::intoZonedDateTime)
            .throwsWhen(Converter::intoLocalDateTime);
    }

    @Test
    public void timeIdentityConversions() {
        LocalDateTime now = LocalDateTime.now();
        ZonedDateTime zNow = now.atZone(ZoneId.of("+05:00"));

        test(now).converts(Converter::intoLocalDateTime, now)
            .throwsWhen(Converter::intoZonedDateTime)
            .converts(c -> c.intoZonedDateTimeOrUse(ZoneId.of("-03:00")), now.atZone(ZoneId.of("-03:00")));

        test(zNow).converts(Converter::intoLocalDateTime, zNow.toLocalDateTime())
            .converts(Converter::intoZonedDateTime, zNow)
            .converts(c -> c.intoZonedDateTimeOrUse(ZoneId.of("-03:00")), zNow);
    }

    static class Tester {
        static Tester test(Object o) {
            return new Tester(o);
        }

        final List<Object> testables;

        Tester(Object testable) {
            this.testables = new ArrayList<>();
            this.testables.add(testable);
        }

        Tester alsoTest(Object testable) {
            testables.add(testable);
            return this;
        }

        Tester throwsWhen(Function<Converter, ?> method) {
            testables.forEach(o -> {
                try {
                    final Object val = method.apply(convert(o));
                    fail("Method did not throw as expected, instead returned: " + val);
                }
                catch (RuntimeException e) {/* expected */}
            });
            return this;
        }

        <T> Tester converts(Function<Converter, T> method, T expected) {
            testables.forEach(o -> assertThat(method.apply(convert(o)), is(expected)));
            return this;
        }

        Tester converts(Function<Converter, ZonedDateTime> method, ZonedDateTime expected) {
            testables.forEach(o -> {
                ZonedDateTime actual = method.apply(convert(o));
                assertTrue(actual + " --not-expected--> " + expected, actual.isEqual(expected));
            });
            return this;
        }
    }
}
