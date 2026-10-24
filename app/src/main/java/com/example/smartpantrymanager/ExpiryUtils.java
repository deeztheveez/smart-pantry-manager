package com.example.smartpantrymanager;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/**
 * Small helper methods for working with expiry dates.
 * Dates are stored in the database as text in the form yyyy-MM-dd.
 */
public class ExpiryUtils {

    // An item counts as "expiring soon" if it expires within this many days
    public static final int EXPIRING_SOON_DAYS = 3;

    private static final String ISO_FORMAT = "yyyy-MM-dd";
    private static final String DISPLAY_FORMAT = "dd MMM yyyy";

    /**
     * Returns how many days from today until the date.
     * Negative = already expired. Long.MAX_VALUE = no date set.
     */
    public static long daysUntil(String isoDate) {
        if (isoDate == null || isoDate.isEmpty()) {
            return Long.MAX_VALUE;
        }
        try {
            Date expiry = new SimpleDateFormat(ISO_FORMAT, Locale.UK).parse(isoDate);

            // "Today" at midnight, so that we compare whole days
            Calendar today = Calendar.getInstance();
            today.set(Calendar.HOUR_OF_DAY, 0);
            today.set(Calendar.MINUTE, 0);
            today.set(Calendar.SECOND, 0);
            today.set(Calendar.MILLISECOND, 0);

            long difference = expiry.getTime() - today.getTimeInMillis();
            return Math.round(difference / 86400000.0); // milliseconds in a day
        } catch (ParseException e) {
            return Long.MAX_VALUE;
        }
    }

    /** Turns "2026-11-05" into "05 Nov 2026" for showing on screen. */
    public static String formatForDisplay(String isoDate) {
        try {
            Date date = new SimpleDateFormat(ISO_FORMAT, Locale.UK).parse(isoDate);
            return new SimpleDateFormat(DISPLAY_FORMAT, Locale.UK).format(date);
        } catch (Exception e) {
            return isoDate;
        }
    }

    /** Returns today's date plus a number of days, as yyyy-MM-dd (used for the sample pantry). */
    public static String isoFromNowPlusDays(int days) {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_YEAR, days);
        return new SimpleDateFormat(ISO_FORMAT, Locale.UK).format(calendar.getTime());
    }
}