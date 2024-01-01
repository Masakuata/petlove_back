package xatal.sharedz.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public abstract class Util {
    public static final String DATE_FORMAT = "dd-MM-yyyy";

    public static boolean containsAnyCase(String s1, String s2) {
        return s1.toLowerCase().contains(s2.toLowerCase());
    }

    public static boolean compareDates(Date date1, Date date2) {
        Calendar c1 = Calendar.getInstance();
        c1.setTime(date1);
        Calendar c2 = Calendar.getInstance();
        c2.setTime(date2);
        return c1.get(Calendar.YEAR) == c2.get(Calendar.YEAR)
                && c1.get(Calendar.MONTH) == c2.get(Calendar.MONTH)
                && c1.get(Calendar.DAY_OF_MONTH) == c2.get(Calendar.DAY_OF_MONTH);
    }

    public static Date dateFromString(String date) throws ParseException {
        return Util.dateFromString(date, Util.DATE_FORMAT);
    }

    public static Date dateFromString(String date, String pattern) throws ParseException {
        return new SimpleDateFormat(pattern, Locale.ENGLISH).parse(date);
    }

    public static String dateToString(Date date) {
        return Util.dateToString(date, Util.DATE_FORMAT);
    }

    public static String dateToString(Date date, String pattern) {
        return new SimpleDateFormat(pattern).format(date);
    }
}
