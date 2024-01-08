package ismaapp.tortosa.glucoseregister;

import android.os.Build;

import androidx.annotation.RequiresApi;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.Locale;

public class DateUtils {

    private static final String DATE_FORMAT = "dd-MM-yyyy HH:mm";

    private DateUtils() {
        // No-op: This class will not be instantiated to ensure consistency of results.
    }

    public static Date getCurrentDate() {
        return new Date();
    }

    private static String getFormattedDateNowLegacy() {
        Date date = new Date();
        SimpleDateFormat dateFormat = new SimpleDateFormat(DATE_FORMAT, Locale.getDefault());
        return dateFormat.format(date);
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private static String getFormattedDateNow() {
        LocalDateTime localDate = LocalDateTime.now();
        DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern(DATE_FORMAT, Locale.getDefault());
        return localDate.format(dateFormat);
    }

    public static String getFormattedDate() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            return getFormattedDateNow();
        } else {
            return getFormattedDateNowLegacy();
        }
    }
}
