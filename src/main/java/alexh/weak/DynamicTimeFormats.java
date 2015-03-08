package alexh.weak;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.SignStyle;
import java.time.temporal.TemporalAccessor;
import java.util.function.Function;

import static alexh.weak.Converter.convert;
import static java.time.temporal.ChronoField.*;

public class DynamicTimeFormats {

    /** Parses ISO date-strings as permissively as possible */
    public static DateTimeFormatter ISO_PERMISSIVE = new DateTimeFormatterBuilder()
        .parseLenient()
        .parseCaseInsensitive()
        .appendValue(YEAR, 4, 10, SignStyle.EXCEEDS_PAD)
        .optionalStart()
        .appendLiteral('-').appendValue(MONTH_OF_YEAR, 1, 2, SignStyle.NOT_NEGATIVE)
        .optionalStart()
        .appendLiteral('-').appendValue(DAY_OF_MONTH, 1, 2, SignStyle.NOT_NEGATIVE)
        .optionalStart()
        .appendLiteral('T').appendValue(HOUR_OF_DAY, 1, 2, SignStyle.NOT_NEGATIVE)
        .optionalStart()
        .appendLiteral(':').appendValue(MINUTE_OF_HOUR, 1, 2, SignStyle.NOT_NEGATIVE)
        .optionalStart()
        .appendLiteral(':').appendValue(SECOND_OF_MINUTE, 1, 2, SignStyle.NOT_NEGATIVE)
        .optionalStart()
        .appendFraction(NANO_OF_SECOND, 0, 9, true).parseDefaulting(NANO_OF_SECOND, 0)
        .optionalStart()
        .appendOffsetId()
        .optionalStart()
        .appendLiteral('[').parseCaseSensitive().appendZoneRegionId().appendLiteral(']')

        .toFormatter();

    /**
     * Parses ISO date-strings as permissively as possible,
     * defaults day->1, month->1, hour->0, minute->0, second->0, nanoseconds->0
     */
    public static DateTimeFormatter ISO_PERMISSIVE_WITH_DEFAULTS = new DateTimeFormatterBuilder()
        .append(ISO_PERMISSIVE)
        .parseDefaulting(MONTH_OF_YEAR, 1)
        .parseDefaulting(DAY_OF_MONTH, 1)
        .parseDefaulting(HOUR_OF_DAY, 0)
        .parseDefaulting(MINUTE_OF_HOUR, 0)
        .parseDefaulting(SECOND_OF_MINUTE, 0)
        .parseDefaulting(NANO_OF_SECOND, 0)
        .toFormatter();

    /** Parses date-strings like "12-Mar-2015 12:34:54.654+03:00" */
    public static DateTimeFormatter DAY_MONTH_YEAR_PERMISSIVE = new DateTimeFormatterBuilder()
        .parseCaseInsensitive()
        .parseLenient()
        .appendValue(DAY_OF_MONTH, 1, 2, SignStyle.NOT_NEGATIVE)
        .optionalStart().appendLiteral('-').optionalEnd()
        .optionalStart().appendLiteral('/').optionalEnd()
        .appendPattern("MMM")
        .optionalStart().appendLiteral('-').optionalEnd()
        .optionalStart().appendLiteral('/').optionalEnd()
        .appendValue(YEAR, 4, 10, SignStyle.EXCEEDS_PAD)
        .optionalStart()
        .appendLiteral(' ').appendValue(HOUR_OF_DAY, 1, 2, SignStyle.NOT_NEGATIVE)
        .optionalStart()
        .appendLiteral(':').appendValue(MINUTE_OF_HOUR, 1, 2, SignStyle.NOT_NEGATIVE)
        .optionalStart()
        .appendLiteral(':').appendValue(SECOND_OF_MINUTE, 1, 2, SignStyle.NOT_NEGATIVE)
        .optionalStart()
        .appendFraction(NANO_OF_SECOND, 0, 9, true)
        .optionalStart()
        .appendOffsetId()
        .toFormatter();

    /**
     * Parses date-strings like "12-Mar-2015 12:34:54.654+03:00"
     * defaults day->1, month->1, hour->0, minute->0, second->0, nanoseconds->0
     */
    public static  DateTimeFormatter DAY_MONTH_YEAR_PERMISSIVE_WITH_DEFAULTS = new DateTimeFormatterBuilder()
        .append(DAY_MONTH_YEAR_PERMISSIVE)
        .parseDefaulting(HOUR_OF_DAY, 0)
        .parseDefaulting(MINUTE_OF_HOUR, 0)
        .parseDefaulting(SECOND_OF_MINUTE, 0)
        .parseDefaulting(NANO_OF_SECOND, 0)
        .toFormatter();

    /** Parses {@link java.util.Date#toString()} */
    public static DateTimeFormatter UTIL_DATE_TO_STRING = new DateTimeFormatterBuilder()
        .parseLenient()
        .appendPattern("EEE")
        .appendLiteral(' ').appendPattern("MMM")
        .appendLiteral(' ').appendValue(DAY_OF_MONTH, 1, 2, SignStyle.NOT_NEGATIVE)
        .appendLiteral(' ').appendValue(HOUR_OF_DAY, 1, 2, SignStyle.NOT_NEGATIVE)
        .appendLiteral(':').appendPattern("mm")
        .optionalStart()
            .appendLiteral(':').appendPattern("ss")
            .optionalStart().appendFraction(NANO_OF_SECOND, 0, 9, true).optionalEnd()
        .optionalEnd()
        .optionalStart().appendLiteral(' ').appendPattern("zzz").optionalEnd()
        .appendLiteral(' ').appendPattern("yyyy")
        .toFormatter();

    /** Epoch milliseconds -> TemporalAccessor assuming system default zone id */
    static final Function<CharSequence, TemporalAccessor> EPOCH_MILLIS_PARSER =
        s -> LocalDateTime.ofInstant(Instant.ofEpochMilli(convert(s).intoLong()), ZoneId.systemDefault());


    /**
     * Tries all permissive parsers, taking the first success or throwing the first exception if all fail
     * ISO_PERMISSIVE -> DAY_MONTH_YEAR_PERMISSIVE -> UTIL_DATE_TO_STRING -> EPOCH_MILLIS_PARSER
     */
    public static final Function<CharSequence, TemporalAccessor> ALL_PARSER = orderedParseAttempter(
        ISO_PERMISSIVE::parse,
        DAY_MONTH_YEAR_PERMISSIVE::parse,
        UTIL_DATE_TO_STRING::parse,
        EPOCH_MILLIS_PARSER);

    /**
     * Tries all permissive parsers (with defaults where applicable), taking the first success or throwing the first
     * exception if all fail
     * ISO_PERMISSIVE -> DAY_MONTH_YEAR_PERMISSIVE -> UTIL_DATE_TO_STRING -> EPOCH_MILLIS_PARSER
     */
    public static final Function<CharSequence, TemporalAccessor> ALL_PARSER_WITH_DEFAULTS = orderedParseAttempter(
        ISO_PERMISSIVE_WITH_DEFAULTS::parse,
        DAY_MONTH_YEAR_PERMISSIVE_WITH_DEFAULTS::parse,
        UTIL_DATE_TO_STRING::parse,
        EPOCH_MILLIS_PARSER);

    /**
     * Returns an ordered functional blend of all input parsers. The attempter will try all functions until it succeeds.
     * If none succeed will re-throw the first exception
     * @param parsers ordered sequence of parsers to try to convert a CharSequence into a TemporalAccessor
     * @return ordered functional blend of all input parsers
     */
    @SafeVarargs
    public static Function<CharSequence, TemporalAccessor> orderedParseAttempter(Function<CharSequence, TemporalAccessor>... parsers) {
        return date -> {
            RuntimeException first = null;
            for (Function<CharSequence, TemporalAccessor> parser : parsers) {
                try { return parser.apply(date); }
                catch (RuntimeException ex) {
                    if (first == null) first = ex;
                }
            }
            if (first == null) throw new IllegalStateException("Empty parse attempter");
            throw first;
        };
    }

    /**
     * Convenience method for permissive parsing using the {@link ALL_PARSER}
     * @param dateChars date-string
     * @return parsed TemporalAccessor
     */
    public static TemporalAccessor parse(CharSequence dateChars) {
        return ALL_PARSER.apply(dateChars);
    }

    /**
     * Convenience method for permissive parsing using the {@link ALL_PARSER_WITH_DEFAULTS}
     * @param dateChars date-string
     * @return parsed TemporalAccessor
     */
    public static TemporalAccessor parseWithDefaults(CharSequence dateChars) {
        return ALL_PARSER_WITH_DEFAULTS.apply(dateChars);
    }

    private DynamicTimeFormats() {/* static */}
}
