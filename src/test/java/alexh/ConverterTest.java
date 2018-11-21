package alexh;

import static alexh.ConverterTest.Tester.test;
import static alexh.weak.Converter.convert;
import static java.util.Arrays.asList;
import static java.util.Collections.*;
import static java.util.stream.Collectors.toCollection;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import alexh.weak.Converter;
import alexh.weak.ConverterMaybe;
import java.math.BigDecimal;
import java.time.*;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;

public class ConverterTest {
    
    private static final String EXPECTED_DEFAULT_MAP_KEY = "value";

    @Test
    public void mostlyWorkingWorksMethod() {
        test("12341234")
            .expect(Converter::intoStringWorks, true)
            .expect(Converter::intoIntegerWorks, true)
            .expect(Converter::intoLongWorks, true)
            .expect(Converter::intoDoubleWorks, true)
            .expect(Converter::intoDecimalWorks, true)
            .expect(Converter::intoListWorks, true)
            .expect(Converter::intoMapWorks, true)
            .expect(Converter::intoLocalDateTimeWorks, true)
            .expect(Converter::intoZonedDateTimeWorks, false);
    }

    @Test
    public void mostlyNotWorkingWorksMethod() {
        test(new Object())
            .expect(Converter::intoStringWorks, true)
            .expect(Converter::intoIntegerWorks, false)
            .expect(Converter::intoLongWorks, false)
            .expect(Converter::intoDoubleWorks, false)
            .expect(Converter::intoDecimalWorks, false)
            .expect(Converter::intoListWorks, true)
            .expect(Converter::intoMapWorks, true)
            .expect(Converter::intoLocalDateTimeWorks, false)
            .expect(Converter::intoZonedDateTimeWorks, false);
    }

    @Test
    public void aString() {
        test("hello world")
            .expect(Converter::intoString, "hello world")
            .expect(Converter::intoMap, singletonMap(EXPECTED_DEFAULT_MAP_KEY, "hello world"))
            .expect(Converter::intoList, asList("hello world"))
            .throwsWhen(Converter::intoInteger)
            .throwsWhen(Converter::intoLong)
            .throwsWhen(Converter::intoDouble)
            .throwsWhen(Converter::intoDecimal)
            .throwsWhen(Converter::intoLocalDateTime)
            .throwsWhen(Converter::intoZonedDateTime);
    }

    @Test
    public void aStringMaybe() {
        test("hello world")
            .expect(ConverterMaybe::intoString, Optional.of("hello world"))
            .expect(ConverterMaybe::intoMap, Optional.of(singletonMap(EXPECTED_DEFAULT_MAP_KEY, "hello world")))
            .expect(ConverterMaybe::intoList, Optional.of(asList("hello world")))
            .emptyWhen(ConverterMaybe::intoInteger)
            .emptyWhen(ConverterMaybe::intoLong)
            .emptyWhen(ConverterMaybe::intoDouble)
            .emptyWhen(ConverterMaybe::intoDecimal)
            .emptyWhen(ConverterMaybe::intoLocalDateTime)
            .emptyWhen(ConverterMaybe::intoZonedDateTime)
            .emptyWhen(c -> c.intoZonedDateTimeOrUse(ZoneId.systemDefault()));
    }

    @Test
    public void stringInteger() {
        test("1234")
            .expect(Converter::intoInteger, 1234)
            .expect(Converter::intoLong, 1234l)
            .expect(Converter::intoDouble, 1234d)
            .expect(Converter::intoDecimal, new BigDecimal(1234));
    }

    @Test
    public void stringLong() {
        test("1425688985487")
            .throwsWhen(Converter::intoInteger)
            .expect(Converter::intoLong, 1425688985487l)
            .expect(Converter::intoDouble, 1425688985487d)
            .expect(Converter::intoDecimal, new BigDecimal(1425688985487l))
            .expect(Converter::intoLocalDateTime, LocalDateTime
                .ofInstant(Instant.ofEpochMilli(1425688985487l), ZoneId.of("Europe/London")))
            .throwsWhen(Converter::intoZonedDateTime);
    }

    @Test
    public void stringDouble() {
        test("12345.6789")
            .expect(Converter::intoInteger, 12346)
            .expect(Converter::intoLong, 12346l)
            .expect(Converter::intoDouble, 12345.6789d)
            .expect(Converter::intoDecimal, new BigDecimal("12345.6789"));
    }

    @Test
    public void stringBigDecimal() {
        test("12345112341234123109548109238758132764189723644123123123.6713212312123476123984761293874312389")
            .throwsWhen(Converter::intoInteger)
            .throwsWhen(Converter::intoLong)
            .expect(Converter::intoDouble, 12345112341234123109548109238758132764189723644123123123.6713212312123476123984761293874312389d)
            .expect(Converter::intoDecimal, new BigDecimal("12345112341234123109548109238758132764189723644123123123.6713212312123476123984761293874312389"));
    }

    @Test
    public void stringLongableBigDecimal() {
        test("123451123412341231.6713212312123476123984761293874312389")
            .throwsWhen(Converter::intoInteger)
            .expect(Converter::intoLong, 123451123412341232l)
            .expect(Converter::intoDouble, 123451123412341231.67132123121234761239847612938743123899d)
            .expect(Converter::intoDecimal, new BigDecimal("123451123412341231.6713212312123476123984761293874312389"))
            .expect(Converter::intoLocalDateTime, LocalDateTime
                .ofInstant(Instant.ofEpochMilli(123451123412341232l), ZoneId.systemDefault()))
            .throwsWhen(Converter::intoZonedDateTime);
    }

    @Test
    public void isoDateString() {
        test("2015-03-07T00:37:41.946642144Z[Europe/London]")
            .converts(c -> c.intoZonedDateTimeOrUse(ZoneId.of("Pacific/Tahiti")), ZonedDateTime.parse("2015-03-07T00:37:41.946642144Z[Europe/London]"))
            .converts(Converter::intoZonedDateTime, ZonedDateTime.parse("2015-03-07T00:37:41.946642144Z[Europe/London]"))
            .expect(Converter::intoLocalDateTime, ZonedDateTime.parse("2015-03-07T00:37:41.946642144Z[Europe/London]").toLocalDateTime());

        test("2015-03-07T00:37:41.946")
            .converts(c -> c.intoZonedDateTimeOrUse(ZoneId.of("Europe/London")), ZonedDateTime.parse("2015-03-07T00:37:41.946Z[Europe/London]"))
            .throwsWhen(Converter::intoZonedDateTime)
            .expect(Converter::intoLocalDateTime, LocalDateTime.parse("2015-03-07T00:37:41.946"));

        test("2015-03-07T00:3")
            .converts(c -> c.intoZonedDateTimeOrUse(ZoneId.of("Europe/London")), ZonedDateTime.parse("2015-03-07T00:03:00.000Z[Europe/London]"))
            .throwsWhen(Converter::intoZonedDateTime)
            .expect(Converter::intoLocalDateTime, LocalDateTime.parse("2015-03-07T00:03:00.000"));

        test("2015-03-07")
            .converts(c -> c.intoZonedDateTimeOrUse(ZoneId.of("Europe/London")), ZonedDateTime.parse("2015-03-07T00:00:00.000Z[Europe/London]"))
            .throwsWhen(Converter::intoZonedDateTime)
            .expect(Converter::intoLocalDateTime, LocalDateTime.parse("2015-03-07T00:00:00.000"));

        test("2015-03")
            .converts(c -> c.intoZonedDateTimeOrUse(ZoneId.of("Europe/London")), ZonedDateTime.parse("2015-03-01T00:00:00.000Z[Europe/London]"))
            .throwsWhen(Converter::intoZonedDateTime)
            .expect(Converter::intoLocalDateTime, LocalDateTime.parse("2015-03-01T00:00:00.000"));

        test("2015")
            .converts(c -> c.intoZonedDateTimeOrUse(ZoneId.of("Europe/London")), ZonedDateTime.parse("2015-01-01T00:00:00.000Z[Europe/London]"))
            .throwsWhen(Converter::intoZonedDateTime)
            .expect(Converter::intoLocalDateTime, LocalDateTime.parse("2015-01-01T00:00:00.000"));
    }

    @Test
    public void timeZoneNameEnd() {
        test("2015-03-07T00:37:41.946642144Z[Europe/London]")
            .converts(c -> c.intoZonedDateTimeOrUse(ZoneId.of("Pacific/Tahiti")), ZonedDateTime.parse("2015-03-07T00:37:41.946642144Z[Europe/London]"))
            .converts(Converter::intoZonedDateTime, ZonedDateTime.parse("2015-03-07T00:37:41.946642144Z[Europe/London]"))
            .expect(Converter::intoLocalDateTime, ZonedDateTime.parse("2015-03-07T00:37:41.946642144Z[Europe/London]").toLocalDateTime());

        test("2015-03-07T00:37:41.946Z[Europe/London]")
            .converts(c -> c.intoZonedDateTimeOrUse(ZoneId.of("CET")), ZonedDateTime.parse("2015-03-07T00:37:41.946Z[Europe/London]"))
            .converts(Converter::intoZonedDateTime, ZonedDateTime.parse("2015-03-07T00:37:41.946Z[Europe/London]"))
            .expect(Converter::intoLocalDateTime, LocalDateTime.parse("2015-03-07T00:37:41.946"));

        test("2015-03-07T00:37:41Z[Europe/London]")
            .converts(c -> c.intoZonedDateTimeOrUse(ZoneId.of("Europe/London")), ZonedDateTime.parse("2015-03-07T00:37:41.000Z[Europe/London]"))
            .converts(Converter::intoZonedDateTime, ZonedDateTime.parse("2015-03-07T00:37:41.000Z[Europe/London]"))
            .expect(Converter::intoLocalDateTime, LocalDateTime.parse("2015-03-07T00:37:41.000"));

        test("2015-03-07T00:37Z[Europe/London]")
            .converts(c -> c.intoZonedDateTimeOrUse(ZoneId.of("Europe/London")), ZonedDateTime.parse("2015-03-07T00:37:00.000Z[Europe/London]"))
            .converts(Converter::intoZonedDateTime, ZonedDateTime.parse("2015-03-07T00:37:00.000Z[Europe/London]"))
            .expect(Converter::intoLocalDateTime, LocalDateTime.parse("2015-03-07T00:37:00.000"));

        test("2015-03-07T12Z[Europe/London]")
            .converts(c -> c.intoZonedDateTimeOrUse(ZoneId.of("Europe/London")), ZonedDateTime.parse("2015-03-07T12:00:00.000Z[Europe/London]"))
            .converts(Converter::intoZonedDateTime, ZonedDateTime.parse("2015-03-07T12:00:00.000Z[Europe/London]"))
            .expect(Converter::intoLocalDateTime, LocalDateTime.parse("2015-03-07T12:00:00.000"));
    }

    @Test
    public void offsetEnd() {
        test("2015-03-07T00:37:41.946642144+00:00")
            .converts(c -> c.intoZonedDateTimeOrUse(ZoneId.of("Asia/Novosibirsk")), ZonedDateTime.parse("2015-03-07T00:37:41.946642144+00:00"))
            .converts(Converter::intoZonedDateTime, ZonedDateTime.parse("2015-03-07T00:37:41.946642144+00:00"))
            .expect(Converter::intoLocalDateTime, LocalDateTime.parse("2015-03-07T00:37:41.946642144"));

        test("2015-03-07T00:37:41.946+00:00")
            .converts(c -> c.intoZonedDateTimeOrUse(ZoneId.of("CET")), ZonedDateTime.parse("2015-03-07T00:37:41.946+00:00"))
            .converts(Converter::intoZonedDateTime, ZonedDateTime.parse("2015-03-07T00:37:41.946+00:00"))
            .expect(Converter::intoLocalDateTime, LocalDateTime.parse("2015-03-07T00:37:41.946"));

        test("2015-03-07T00:37:41+00:00")
            .converts(c -> c.intoZonedDateTimeOrUse(ZoneId.of("Europe/London")), ZonedDateTime.parse("2015-03-07T00:37:41.000Z[Europe/London]"))
            .converts(Converter::intoZonedDateTime, ZonedDateTime.parse("2015-03-07T00:37:41.000Z"))
            .expect(Converter::intoLocalDateTime, LocalDateTime.parse("2015-03-07T00:37:41.000"));

        test("2015-03-07T00:37+00:00")
            .converts(c -> c.intoZonedDateTimeOrUse(ZoneId.of("Europe/London")), ZonedDateTime.parse("2015-03-07T00:37:00.000+00:00"))
            .converts(Converter::intoZonedDateTime, ZonedDateTime.parse("2015-03-07T00:37:00.000+00:00"))
            .expect(Converter::intoLocalDateTime, LocalDateTime.parse("2015-03-07T00:37:00.000"));

        test("2015-03-07T12+00:00")
            .converts(c -> c.intoZonedDateTimeOrUse(ZoneId.of("Europe/London")), ZonedDateTime.parse("2015-03-07T12:00:00.000+00:00"))
            .converts(Converter::intoZonedDateTime, ZonedDateTime.parse("2015-03-07T12:00:00.000+00:00"))
            .expect(Converter::intoLocalDateTime, LocalDateTime.parse("2015-03-07T12:00:00.000"));
    }

    @Test
    public void zEnd() {
        test("2015-03-07T00:37:41.946642144Z")
            .converts(c -> c.intoZonedDateTimeOrUse(ZoneId.of("Asia/Novosibirsk")), ZonedDateTime.parse("2015-03-07T00:37:41.946642144Z[Z]"))
            .converts(Converter::intoZonedDateTime, ZonedDateTime.parse("2015-03-07T00:37:41.946642144Z[Z]"))
            .expect(Converter::intoLocalDateTime, LocalDateTime.parse("2015-03-07T00:37:41.946642144"));

        test("2015-03-07T00:37:41.946Z")
            .converts(c -> c.intoZonedDateTimeOrUse(ZoneId.of("Europe/London")), ZonedDateTime.parse("2015-03-07T00:37:41.946Z[Europe/London]"))
            .converts(Converter::intoZonedDateTime, ZonedDateTime.parse("2015-03-07T00:37:41.946Z"))
            .expect(Converter::intoLocalDateTime, LocalDateTime.parse("2015-03-07T00:37:41.946"));

        test("2015-03-07T00:37:41Z")
            .converts(c -> c.intoZonedDateTimeOrUse(ZoneId.of("Europe/London")), ZonedDateTime.parse("2015-03-07T00:37:41.000Z[Europe/London]"))
            .converts(Converter::intoZonedDateTime, ZonedDateTime.parse("2015-03-07T00:37:41.000Z"))
            .expect(Converter::intoLocalDateTime, LocalDateTime.parse("2015-03-07T00:37:41.000"));

        test("2015-03-07T00:37Z")
            .converts(c -> c.intoZonedDateTimeOrUse(ZoneId.of("Europe/London")), ZonedDateTime.parse("2015-03-07T00:37:00.000Z[Europe/London]"))
            .converts(Converter::intoZonedDateTime, ZonedDateTime.parse("2015-03-07T00:37:00.000Z"))
            .expect(Converter::intoLocalDateTime, LocalDateTime.parse("2015-03-07T00:37:00.000"));

        test("2015-03-07T12Z")
            .converts(c -> c.intoZonedDateTimeOrUse(ZoneId.of("Europe/London")), ZonedDateTime.parse("2015-03-07T12:00:00.000Z[Europe/London]"))
            .converts(Converter::intoZonedDateTime, ZonedDateTime.parse("2015-03-07T12:00:00.000Z"))
            .expect(Converter::intoLocalDateTime, LocalDateTime.parse("2015-03-07T12:00:00.000"));
    }

    @Test
    public void noColonTimezone() {
        test("2015-03-07T00:37:41.946+0100")
            .converts(c -> c.intoZonedDateTimeOrUse(ZoneId.of("CET")), ZonedDateTime.parse("2015-03-06T23:37:41.946Z"))
            .converts(Converter::intoZonedDateTime, ZonedDateTime.parse("2015-03-07T00:37:41.946+01:00"))
            .expect(Converter::intoLocalDateTime, LocalDateTime.parse("2015-03-07T00:37:41.946"));

        test("2015-03-07T00:37:41.946-0530")
            .converts(c -> c.intoZonedDateTimeOrUse(ZoneId.of("CET")), ZonedDateTime.parse("2015-03-07T06:07:41.946Z"))
            .converts(Converter::intoZonedDateTime, ZonedDateTime.parse("2015-03-07T06:07:41.946Z"))
            .expect(Converter::intoLocalDateTime, LocalDateTime.parse("2015-03-07T00:37:41.946"));

        test("2015-03-07T00:37:41.946+0000")
            .converts(c -> c.intoZonedDateTimeOrUse(ZoneId.of("CET")), ZonedDateTime.parse("2015-03-07T00:37:41.946Z"))
            .converts(Converter::intoZonedDateTime, ZonedDateTime.parse("2015-03-07T00:37:41.946Z"))
            .expect(Converter::intoLocalDateTime, LocalDateTime.parse("2015-03-07T00:37:41.946"));

        test("2015-03-07T00:37:41.946+00")
            .converts(c -> c.intoZonedDateTimeOrUse(ZoneId.of("CET")), ZonedDateTime.parse("2015-03-07T00:37:41.946Z"))
            .converts(Converter::intoZonedDateTime, ZonedDateTime.parse("2015-03-07T00:37:41.946Z"))
            .expect(Converter::intoLocalDateTime, LocalDateTime.parse("2015-03-07T00:37:41.946"));

        test("2015-03-07T00:37:41.946-10")
            .converts(c -> c.intoZonedDateTimeOrUse(ZoneId.of("CET")), ZonedDateTime.parse("2015-03-07T10:37:41.946Z"))
            .converts(Converter::intoZonedDateTime, ZonedDateTime.parse("2015-03-07T10:37:41.946Z"))
            .expect(Converter::intoLocalDateTime, LocalDateTime.parse("2015-03-07T00:37:41.946"));
    }

    @Test
    public void noTIsoDateString() {
        test("2016-03-19 22:26:58.820234345")
            .converts(c -> c.intoZonedDateTimeOrUse(ZoneOffset.UTC), ZonedDateTime.parse("2016-03-19T22:26:58.820234345Z"))
            .expect(Converter::intoLocalDateTime, LocalDateTime.parse("2016-03-19T22:26:58.820234345"))
            .throwsWhen(Converter::intoZonedDateTime);
    }

    @Test
    public void dayMonthYearString() {
        test("7-March-2015 00:37:41.946123234-02:00")
            .converts(c -> c.intoZonedDateTimeOrUse(ZoneId.of("Europe/London")), ZonedDateTime.parse("2015-03-07T02:37:41.946123234Z"))
            .converts(Converter::intoZonedDateTime, ZonedDateTime.parse("2015-03-07T02:37:41.946123234Z"))
            .expect(Converter::intoLocalDateTime, LocalDateTime.parse("2015-03-07T00:37:41.946123234"));

        test("7-March-2015 00:37:41.946")
            .converts(c -> c.intoZonedDateTimeOrUse(ZoneId.of("+00:00")), ZonedDateTime.parse("2015-03-07T00:37:41.946Z"))
            .throwsWhen(Converter::intoZonedDateTime)
            .expect(Converter::intoLocalDateTime, LocalDateTime.parse("2015-03-07T00:37:41.946"));

        test("7-March-2015")
            .alsoTest("7-Mar-2015")
            .alsoTest("7/Mar/2015")
            .alsoTest("7/March/2015")
            .converts(c -> c.intoZonedDateTimeOrUse(ZoneId.of("+00:00")), ZonedDateTime.parse("2015-03-07T00:00:00.000Z"))
            .throwsWhen(Converter::intoZonedDateTime)
            .expect(Converter::intoLocalDateTime, LocalDateTime.parse("2015-03-07T00:00:00.000"));

        test("7-March") // don't default year
            .throwsWhen(c -> c.intoZonedDateTimeOrUse(ZoneId.of("+00:00")))
            .throwsWhen(Converter::intoZonedDateTime)
            .throwsWhen(Converter::intoZonedDateTime);
    }

    @Test
    public void utilDateToStringStyleParsing() {
        test("Sat Mar 07 00:54:06 UTC 2015")
            .converts(c -> c.intoZonedDateTimeOrUse(ZoneId.of("Europe/London")), ZonedDateTime.parse("2015-03-07T00:54:06Z"))
            .converts(Converter::intoZonedDateTime, ZonedDateTime.parse("2015-03-07T00:54:06Z"))
            .expect(Converter::intoLocalDateTime, LocalDateTime.parse("2015-03-07T00:54:06"));

        // with millis
        test("Sat Mar 07 00:54:06.345 UTC 2015")
            .converts(c -> c.intoZonedDateTimeOrUse(ZoneId.of("Europe/London")), ZonedDateTime.parse("2015-03-07T00:54:06.345Z"))
            .converts(Converter::intoZonedDateTime, ZonedDateTime.parse("2015-03-07T00:54:06.345Z"))
            .expect(Converter::intoLocalDateTime, LocalDateTime.parse("2015-03-07T00:54:06.345"));

        // local
        test("Sun Mar 08 07:44:02 2015")
            .converts(c -> c.intoZonedDateTimeOrUse(ZoneId.of("+00:00")), ZonedDateTime.parse("2015-03-08T07:44:02Z"))
            .throwsWhen(Converter::intoZonedDateTime)
            .expect(Converter::intoLocalDateTime, LocalDateTime.parse("2015-03-08T07:44:02"));

        // relaxed leading 0s
        test("Sun Mar 8 7:44:02 2015")
            .converts(c -> c.intoZonedDateTimeOrUse(ZoneId.of("+00:00")), ZonedDateTime.parse("2015-03-08T07:44:02Z"))
            .throwsWhen(Converter::intoZonedDateTime)
            .expect(Converter::intoLocalDateTime, LocalDateTime.parse("2015-03-08T07:44:02"));

        // no seconds
        test("Sun Mar 8 7:44 2015")
            .converts(c -> c.intoZonedDateTimeOrUse(ZoneId.of("+00:00")), ZonedDateTime.parse("2015-03-08T07:44:00Z"))
            .throwsWhen(Converter::intoZonedDateTime)
            .expect(Converter::intoLocalDateTime, LocalDateTime.parse("2015-03-08T07:44:00"));

        // verbose day/month
        test("Sunday March 8 7:44 2015")
            .converts(c -> c.intoZonedDateTimeOrUse(ZoneId.of("+00:00")), ZonedDateTime.parse("2015-03-08T07:44:00Z"))
            .throwsWhen(Converter::intoZonedDateTime)
            .expect(Converter::intoLocalDateTime, LocalDateTime.parse("2015-03-08T07:44:00"));
    }

    @Test
    public void epochMillisLongParsing() {
        test("1425688985487")
            .alsoTest(1425688985487l)
            .converts(c -> c.intoZonedDateTimeOrUse(ZoneId.of("+00:00")), ZonedDateTime.parse("2015-03-07T00:43:05.487Z"))
            .throwsWhen(Converter::intoZonedDateTime)
            .expect(Converter::intoLocalDateTime, LocalDateTime.parse("2015-03-07T00:43:05.487"));
    }

    @Test
    public void epochMillisInstantParsing() {
        test(Instant.ofEpochMilli(1425688985487l))
            .converts(c -> c.intoZonedDateTimeOrUse(ZoneId.of("+06:00")), ZonedDateTime.parse("2015-03-07T00:43:05.487Z"))
            .converts(Converter::intoZonedDateTime, ZonedDateTime.parse("2015-03-07T00:43:05.487Z"))
            .expect(Converter::intoLocalDateTime, LocalDateTime.parse("2015-03-07T00:43:05.487"));
    }

    @Test
    public void utilDateSupport() {
        test(new java.util.Date(1425688985487l))
            .expect(Converter::intoLong, 1425688985487l)
            .expect(Converter::intoDecimal, new BigDecimal(1425688985487l))
            .expect(Converter::intoDouble, 1425688985487d)
            .expect(Converter::intoString, "1425688985487")
            .converts(c -> c.intoZonedDateTimeOrUse(ZoneId.of("+00:00")), ZonedDateTime.parse("2015-03-07T00:43:05.487Z"))
            .throwsWhen(Converter::intoZonedDateTime)
            .expect(Converter::intoLocalDateTime, LocalDateTime.parse("2015-03-07T00:43:05.487"));
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

        test(now).expect(Converter::intoLocalDateTime, now)
            .throwsWhen(Converter::intoZonedDateTime)
            .converts(c -> c.intoZonedDateTimeOrUse(ZoneId.of("-03:00")), now.atZone(ZoneId.of("-03:00")));

        test(zNow).expect(Converter::intoLocalDateTime, zNow.toLocalDateTime())
            .converts(Converter::intoZonedDateTime, zNow)
            .converts(c -> c.intoZonedDateTimeOrUse(ZoneId.of("-03:00")), zNow);
    }

    @Test
    public void intConversions() {
        test(59839)
            .expect(Converter::intoInteger, 59839)
            .expect(Converter::intoLong, 59839l)
            .expect(Converter::intoDouble, 59839d)
            .expect(Converter::intoDecimal, new BigDecimal(59839))
            .expect(Converter::intoString, "59839")
            .expect(Converter::intoList, singletonList(59839))
            .expect(Converter::intoMap, singletonMap(EXPECTED_DEFAULT_MAP_KEY, 59839))
            .expect(Converter::intoLocalDateTime, LocalDateTime.ofInstant(Instant.ofEpochMilli(59839), ZoneId.systemDefault()))
            .throwsWhen(Converter::intoZonedDateTime);
    }

    @Test
    public void longConversions() {
        test(123412344444l)
            .throwsWhen(Converter::intoInteger)
            .expect(Converter::intoLong, 123412344444l)
            .expect(Converter::intoDouble, 123412344444d)
            .expect(Converter::intoDecimal, new BigDecimal(123412344444l))
            .expect(Converter::intoString, "123412344444")
            .expect(Converter::intoList, singletonList(123412344444l))
            .expect(Converter::intoMap, singletonMap(EXPECTED_DEFAULT_MAP_KEY, 123412344444l))
            .expect(Converter::intoLocalDateTime, LocalDateTime.ofInstant(Instant.ofEpochMilli(123412344444l), ZoneId.systemDefault()))
            .throwsWhen(Converter::intoZonedDateTime);

        test(-123412344444l).throwsWhen(Converter::intoInteger);
        test(1234l).expect(Converter::intoInteger, 1234);
    }

    @Test
    public void doubleConversions_large() {
        final double bigDouble = 12345000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000d;
        test(bigDouble)
            .throwsWhen(Converter::intoInteger)
            .throwsWhen(Converter::intoLong)
            .expect(Converter::intoDouble, bigDouble)
            .expect(Converter::intoDecimal, new BigDecimal(bigDouble))
            .expect(Converter::intoString, String.valueOf(bigDouble))
            .expect(Converter::intoList, singletonList(bigDouble))
            .expect(Converter::intoMap, singletonMap(EXPECTED_DEFAULT_MAP_KEY, bigDouble))
            .throwsWhen(Converter::intoLocalDateTime)
            .throwsWhen(Converter::intoZonedDateTime);
    }

    @Test
    public void doubleConversions_normal() {
        final double someDouble = 12341234.67896789d;
        test(someDouble)
            .expect(Converter::intoInteger, 12341235)
            .expect(Converter::intoLong, 12341235l)
            .expect(Converter::intoDouble, someDouble)
            .expect(Converter::intoDecimal, new BigDecimal(someDouble))
            .expect(Converter::intoString, String.valueOf(someDouble))
            .expect(Converter::intoList, singletonList(someDouble))
            .expect(Converter::intoMap, singletonMap(EXPECTED_DEFAULT_MAP_KEY, someDouble))
            .expect(Converter::intoLocalDateTime, LocalDateTime.ofInstant(Instant.ofEpochMilli(12341235), ZoneId.systemDefault()))
            .throwsWhen(Converter::intoZonedDateTime);
    }

    @Test
    public void doubleConversions_small() {
        final double smallDouble = 0.00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000123123123d;
        test(smallDouble)
            .expect(Converter::intoInteger, 0)
            .expect(Converter::intoLong, 0l)
            .expect(Converter::intoDouble, smallDouble)
            .expect(Converter::intoDecimal, new BigDecimal(smallDouble))
            .expect(Converter::intoString, String.valueOf(smallDouble))
            .expect(Converter::intoList, singletonList(smallDouble))
            .expect(Converter::intoMap, singletonMap(EXPECTED_DEFAULT_MAP_KEY, smallDouble))
            .throwsWhen(Converter::intoLocalDateTime)
            .throwsWhen(Converter::intoZonedDateTime);
    }

    @Test
    public void decimalConversions_large() {
        final BigDecimal bigDecimal = new BigDecimal("12345000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000");
        test(bigDecimal)
            .throwsWhen(Converter::intoInteger)
            .throwsWhen(Converter::intoLong)
            .expect(Converter::intoDouble, bigDecimal.doubleValue())
            .expect(Converter::intoDecimal, bigDecimal)
            .expect(Converter::intoString, String.valueOf(bigDecimal))
            .expect(Converter::intoList, singletonList(bigDecimal))
            .expect(Converter::intoMap, singletonMap(EXPECTED_DEFAULT_MAP_KEY, bigDecimal))
            .throwsWhen(Converter::intoLocalDateTime)
            .throwsWhen(Converter::intoZonedDateTime);
    }

    @Test
    public void decimalConversions_normal() {
        final BigDecimal someDecimal = new BigDecimal("12341234.67896789");
        test(someDecimal)
            .expect(Converter::intoInteger, 12341235)
            .expect(Converter::intoLong, 12341235l)
            .expect(Converter::intoDouble, someDecimal.doubleValue())
            .expect(Converter::intoDecimal, someDecimal)
            .expect(Converter::intoString, String.valueOf(someDecimal))
            .expect(Converter::intoList, singletonList(someDecimal))
            .expect(Converter::intoMap, singletonMap(EXPECTED_DEFAULT_MAP_KEY, someDecimal))
            .expect(Converter::intoLocalDateTime, LocalDateTime.ofInstant(Instant.ofEpochMilli(12341235), ZoneId.systemDefault()))
            .throwsWhen(Converter::intoZonedDateTime);
    }

    @Test
    public void decimalConversions_small() {
        final BigDecimal smallDecimal = new BigDecimal("0.00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000123123123");
        test(smallDecimal)
            .expect(Converter::intoInteger, 0)
            .expect(Converter::intoLong, 0l)
            .expect(Converter::intoDouble, smallDecimal.doubleValue())
            .expect(Converter::intoDecimal, smallDecimal)
            .expect(Converter::intoString, String.valueOf(smallDecimal))
            .expect(Converter::intoList, singletonList(smallDecimal))
            .expect(Converter::intoMap, singletonMap(EXPECTED_DEFAULT_MAP_KEY, smallDecimal))
            .throwsWhen(Converter::intoLocalDateTime)
            .throwsWhen(Converter::intoZonedDateTime);
    }

    @Test
    public void singletonStringListUnpacking() {
        test(singletonList("hello"))
            .throwsWhen(Converter::intoInteger)
            .throwsWhen(Converter::intoLong)
            .throwsWhen(Converter::intoDouble)
            .throwsWhen(Converter::intoDecimal)
            .expect(Converter::intoString, "hello")
            .expect(Converter::intoList, singletonList("hello"))
            .expect(Converter::intoMap, singletonMap(0, "hello"))
            .throwsWhen(Converter::intoLocalDateTime)
            .throwsWhen(Converter::intoZonedDateTime);
    }

    @Test
    public void singletonIntListUnpacking() {
        test(singletonList(59839))
            .expect(Converter::intoInteger, 59839)
            .expect(Converter::intoLong, 59839l)
            .expect(Converter::intoDouble, 59839d)
            .expect(Converter::intoDecimal, new BigDecimal(59839))
            .expect(Converter::intoString, "59839")
            .expect(Converter::intoList, singletonList(59839))
            .expect(Converter::intoMap, singletonMap(0, 59839))
            .expect(Converter::intoLocalDateTime, LocalDateTime.ofInstant(Instant.ofEpochMilli(59839), ZoneId.systemDefault()))
            .throwsWhen(Converter::intoZonedDateTime);
    }

    @Test
    public void normalStringList() {
        test(asList("hello", "world"))
            .throwsWhen(Converter::intoInteger)
            .throwsWhen(Converter::intoLong)
            .throwsWhen(Converter::intoDouble)
            .throwsWhen(Converter::intoDecimal)
            .expect(Converter::intoString, asList("hello", "world").toString())
            .expect(Converter::intoList, asList("hello", "world"))
            .expect(Converter::intoMap, new Fluent.HashMap<Integer, String>()
                .append(0, "hello")
                .append(1, "world"))
            .throwsWhen(Converter::intoLocalDateTime)
            .throwsWhen(Converter::intoZonedDateTime);
    }

    @Test
    public void set() {
        Set<String> set = Stream.of("hello", "world").collect(toCollection(LinkedHashSet::new));
        test(set)
            .throwsWhen(Converter::intoInteger)
            .throwsWhen(Converter::intoLong)
            .throwsWhen(Converter::intoDouble)
            .throwsWhen(Converter::intoDecimal)
            .expect(Converter::intoString, asList("hello", "world").toString())
            .expect(Converter::intoList, asList("hello", "world"))
            .expect(Converter::intoMap, new Fluent.HashMap<Integer, String>()
                .append(0, "hello")
                .append(1, "world"))
            .throwsWhen(Converter::intoLocalDateTime)
            .throwsWhen(Converter::intoZonedDateTime);
    }

    @Test
    public void iterable() {
        final Iterable<Character> iterable = new StringAsIterable("12345hello");
        test(iterable)
            .throwsWhen(Converter::intoInteger)
            .throwsWhen(Converter::intoLong)
            .throwsWhen(Converter::intoDouble)
            .throwsWhen(Converter::intoDecimal)
            .expect(Converter::intoString, iterable.toString())
            .expect(Converter::intoList, asList('1', '2', '3', '4', '5', 'h', 'e', 'l', 'l', 'o'))
            .expect(Converter::intoMap, new Fluent.HashMap<Integer, Character>()
                .append(0, '1')
                .append(1, '2')
                .append(2, '3')
                .append(3, '4')
                .append(4, '5')
                .append(5, 'h')
                .append(6, 'e')
                .append(7, 'l')
                .append(8, 'l')
                .append(9, 'o'))
            .throwsWhen(Converter::intoLocalDateTime)
            .throwsWhen(Converter::intoZonedDateTime);
    }

    @Test
    public void objectArray() {
        final String[] array = new String[] { "hello", "world", "12345" };
        test(array)
            .throwsWhen(Converter::intoInteger)
            .throwsWhen(Converter::intoLong)
            .throwsWhen(Converter::intoDouble)
            .throwsWhen(Converter::intoDecimal)
            .expect(Converter::intoString, Arrays.toString(array))
            .expect(Converter::intoList, asList("hello", "world", "12345"))
            .expect(Converter::intoMap, new Fluent.HashMap<Integer, String>()
                .append(0, "hello")
                .append(1, "world")
                .append(2, "12345"))
            .throwsWhen(Converter::intoLocalDateTime)
            .throwsWhen(Converter::intoZonedDateTime);
    }

    @Test
    public void valueMapUnpacking() {
        Map valueMap = singletonMap(EXPECTED_DEFAULT_MAP_KEY, 12341234);
        test(valueMap)
            .expect(Converter::intoInteger, 12341234)
            .expect(Converter::intoLong, 12341234l)
            .expect(Converter::intoDouble, 12341234d)
            .expect(Converter::intoDecimal, new BigDecimal(12341234))
            .expect(Converter::intoString, "12341234")
            .expect(Converter::intoList, singletonList(12341234))
            .expect(Converter::intoMap, singletonMap(EXPECTED_DEFAULT_MAP_KEY, 12341234))
            .expect(Converter::intoLocalDateTime, LocalDateTime.ofInstant(Instant.ofEpochMilli(12341234), ZoneId.systemDefault()))
            .throwsWhen(Converter::intoZonedDateTime);
    }

    @Test
    public void nonValueSingletonMapUnpacking() {
        Map valueMap = singletonMap("anything-else", 12341234);
        test(valueMap)
            .throwsWhen(Converter::intoInteger)
            .throwsWhen(Converter::intoLong)
            .throwsWhen(Converter::intoDouble)
            .throwsWhen(Converter::intoDecimal)
            .expect(Converter::intoString, singletonMap("anything-else", 12341234).toString())
            .expect(Converter::intoList, singletonList(12341234))
            .expect(Converter::intoMap, singletonMap("anything-else", 12341234))
            .throwsWhen(Converter::intoLocalDateTime)
            .throwsWhen(Converter::intoZonedDateTime);
    }

    @Test
    public void map() {
        final Object someObj = new Object();
        final Map someMap = new Fluent.LinkedHashMap<>()
            .append("something", 2341234)
            .append(12341234, someObj)
            .append(null, 333);

        test(someMap)
            .throwsWhen(Converter::intoInteger)
            .throwsWhen(Converter::intoLong)
            .throwsWhen(Converter::intoDouble)
            .throwsWhen(Converter::intoDecimal)
            .expect(Converter::intoString, someMap.toString())
            .expect(Converter::intoList, asList(2341234, someObj, 333))
            .expect(Converter::intoMap, someMap)
            .throwsWhen(Converter::intoLocalDateTime)
            .throwsWhen(Converter::intoZonedDateTime);
    }

    @Test
    public void nonEmptyOptionalUnpacking() {
        final Optional<Long> optional = Optional.of(1234123412341234l);
        test(optional)
            .throwsWhen(Converter::intoInteger)
            .expect(Converter::intoLong, 1234123412341234l)
            .expect(Converter::intoDouble, 1234123412341234d)
            .expect(Converter::intoDecimal, new BigDecimal(1234123412341234l))
            .expect(Converter::intoString, "1234123412341234")
            .expect(Converter::intoList, singletonList(1234123412341234l))
            .expect(Converter::intoMap, singletonMap(EXPECTED_DEFAULT_MAP_KEY, 1234123412341234l))
            .expect(Converter::intoLocalDateTime, LocalDateTime.ofInstant(Instant.ofEpochMilli(1234123412341234l), ZoneId.systemDefault()))
            .throwsWhen(Converter::intoZonedDateTime);

        test(Optional.of("123"))
            .expect(Converter::intoInteger, 123)
            .expect(Converter::intoLong, 123l)
            .expect(Converter::intoDouble, 123d)
            .expect(Converter::intoDecimal, new BigDecimal(123l))
            .expect(Converter::intoString, "123")
            .expect(Converter::intoList, singletonList("123"))
            .expect(Converter::intoMap, singletonMap(EXPECTED_DEFAULT_MAP_KEY, "123"))
            .expect(Converter::intoLocalDateTime, LocalDateTime.parse("0123-01-01T00:00"))
            .throwsWhen(Converter::intoZonedDateTime);
    }

    @Test
    public void emptyOptional() {
        final Optional<Long> optional = Optional.empty();
        test(optional)
            .throwsWhen(Converter::intoInteger)
            .throwsWhen(Converter::intoLong)
            .throwsWhen(Converter::intoDouble)
            .throwsWhen(Converter::intoDecimal)
            .expect(Converter::intoString, optional.toString())
            .expect(Converter::intoList, emptyList())
            .expect(Converter::intoMap, emptyMap())
            .throwsWhen(Converter::intoLocalDateTime)
            .throwsWhen(Converter::intoZonedDateTime);
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

        <T> Tester expect(Function<Converter, T> method, T expected) {
            testables.forEach(o -> assertThat(method.apply(convert(o))).isEqualTo(expected));
            return this;
        }

        <T> Tester expect(Function<ConverterMaybe, Optional<T>> method, Optional<T> expected) {
            testables.forEach(o -> assertThat(method.apply(convert(o).maybe())).isEqualTo(expected));
            return this;
        }

        <T> Tester emptyWhen(Function<ConverterMaybe, Optional<T>> method) {
            testables.forEach(o -> assertThat(method.apply(convert(o).maybe())).isEqualTo(Optional.empty()));
            return this;
        }

        Tester converts(Function<Converter, ZonedDateTime> method, ZonedDateTime expected) {
            testables.forEach(o -> {
                ZonedDateTime actual = method.apply(convert(o));
                assertTrue(actual.isEqual(expected), actual + " --not-expected--> " + expected);
            });
            return this;
        }
    }

    static class StringAsIterable implements Iterable<Character> {
        private final String string;

        public StringAsIterable(String string) {
            this.string = string;
        }

        @Override
        public Iterator<Character> iterator() {
            List<Character> chars = new ArrayList<>();
            for (char c : string.toCharArray())
                chars.add(c);
            return chars.iterator();
        }
    }
}
