package ismaapp.tortosa.glucoseregister;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class DateUtils {

    private DateUtils() {
        // No-op: This class will not be instantiated to ensure consistency of results.
    }

    public static Date getCurrentDate() {
        return new Date();
    }

    public static String getFormattedDate() {
        Date date = new Date();
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());
        return dateFormat.format(date);
    }
}
