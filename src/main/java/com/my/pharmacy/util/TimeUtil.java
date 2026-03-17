package com.my.pharmacy.util;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.TimeZone;

/**
 * TimeUtil — Single source of truth for all date/time operations in PharmDesk.
 *
 * Root cause of time mismatch:
 *   SQLite stores CURRENT_TIMESTAMP in UTC. Java reads it as UTC.
 *   But the PC clock is local time (e.g. PKT = UTC+5).
 *   So a sale made at 3:00 PM PKT is stored as 10:00 AM UTC.
 *   Without conversion, it displays as 10:00 AM instead of 3:00 PM.
 *
 * Fix:
 *   - All timestamps are stored as UTC in DB (SQLite default — do not change)
 *   - All display formatting converts UTC → local via systemDefault() zone
 *   - All SQL date comparisons use 'localtime' modifier
 *   - Java code uses nowTimestamp() for new records (correct UTC storage)
 *   - Java code uses toLocalDate() for date comparisons
 *
 * Usage:
 *   new Timestamp(System.currentTimeMillis())  →  TimeUtil.nowTimestamp()
 *   LocalDate.now()                            →  TimeUtil.today()
 *   new SimpleDateFormat("dd-MMM-yyyy HH:mm")  →  TimeUtil.DISPLAY_FORMAT / DISPLAY_FORMAT_SHORT
 *   sdf.format(rs.getTimestamp(...))           →  TimeUtil.format(rs.getTimestamp(...))
 */
public class TimeUtil {

    /** Local timezone — all display conversions use this */
    public static final ZoneId LOCAL_ZONE = ZoneId.systemDefault();

    /** Display format: "15-Mar-2025 14:30" */
    public static final String PATTERN_FULL  = "dd-MMM-yyyy HH:mm";

    /** Display format: "15-Mar-2025" */
    public static final String PATTERN_DATE  = "dd-MMM-yyyy";

    /** Display format for backup filenames: "2025-03-15_14-30-00" */
    public static final String PATTERN_FILE  = "yyyy-MM-dd_HH-mm-ss";

    /** Display format for invoice PDF: "15 Mar 2025" */
    public static final String PATTERN_INVOICE = "dd MMM yyyy";

    // ── Current time ──────────────────────────────────────────────────────────

    /** Returns current UTC timestamp for storing new records in DB */
    public static Timestamp nowTimestamp() {
        return new Timestamp(System.currentTimeMillis());
    }

    /** Returns today's local date for date comparisons and UI defaults */
    public static LocalDate today() {
        return LocalDate.now(LOCAL_ZONE);
    }

    /** Returns current local datetime for filenames, backup labels etc. */
    public static LocalDateTime nowLocal() {
        return LocalDateTime.now(LOCAL_ZONE);
    }

    // ── Formatting (UTC Timestamp → local display string) ─────────────────────

    /**
     * Format a DB timestamp (stored as UTC) for display in local time.
     * Use this everywhere instead of new SimpleDateFormat().format(ts).
     */
    public static String format(Timestamp utcTimestamp) {
        if (utcTimestamp == null) return "";
        return format(utcTimestamp, PATTERN_FULL);
    }

    public static String format(Timestamp utcTimestamp, String pattern) {
        if (utcTimestamp == null) return "";
        SimpleDateFormat sdf = new SimpleDateFormat(pattern);
        sdf.setTimeZone(TimeZone.getTimeZone(LOCAL_ZONE));
        return sdf.format(utcTimestamp);
    }

    /** Format a java.util.Date (e.g. file.lastModified()) in local time */
    public static String format(Date date, String pattern) {
        if (date == null) return "";
        SimpleDateFormat sdf = new SimpleDateFormat(pattern);
        sdf.setTimeZone(TimeZone.getTimeZone(LOCAL_ZONE));
        return sdf.format(date);
    }

    /** Format LocalDateTime for backup filenames etc. */
    public static String format(LocalDateTime ldt, String pattern) {
        if (ldt == null) return "";
        return ldt.format(DateTimeFormatter.ofPattern(pattern));
    }

    // ── SQL helpers ───────────────────────────────────────────────────────────

    /**
     * SQL fragment for comparing a UTC timestamp column to today's local date.
     * Usage: "WHERE " + TimeUtil.sqlToday("sale_date")
     *   →  "WHERE date(sale_date, 'localtime') = date('now', 'localtime')"
     */
    public static String sqlToday(String column) {
        return "date(" + column + ", 'localtime') = date('now', 'localtime')";
    }

    /**
     * SQL fragment for comparing a UTC timestamp column to a specific local date.
     * Usage: "WHERE " + TimeUtil.sqlDateEquals("sale_date") + " param = localDate.toString()"
     *   →  "WHERE date(sale_date, 'localtime') = date(?)"
     */
    public static String sqlDateEquals(String column) {
        return "date(" + column + ", 'localtime') = date(?)";
    }

    /**
     * SQL fragment for start-of-month comparison.
     * Usage: "WHERE " + TimeUtil.sqlThisMonth("sale_date")
     */
    public static String sqlThisMonth(String column) {
        return "date(" + column + ", 'localtime') >= date('now', 'start of month', 'localtime')";
    }
}
