package ismaapp.tortosa.glucoseregister.utils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

public class DateUtil {

    private static final String DATE_FORMAT = "dd-MM-yyyy HH:mm";

    private DateUtil() {
        // No-op: This class will not be instantiated to ensure consistency of results.
    }

    private static String getFormattedDateNow() {
        LocalDateTime localDate = LocalDateTime.now();
        DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern(DATE_FORMAT, Locale.getDefault());
        return localDate.format(dateFormat);
    }

    public static String getFormattedDate() {
            return getFormattedDateNow();
    }
}
