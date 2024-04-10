package ismaapp.tortosa.glucoseregister.services;

import android.database.Cursor;
import android.database.sqlite.SQLiteException;
import android.util.Log;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import ismaapp.tortosa.glucoseregister.entity.GlucoseMeasurement;
import ismaapp.tortosa.glucoseregister.helpers.GlucoseDBHelper;
import ismaapp.tortosa.glucoseregister.repository.GlucoseRepository;

public class GlucosePercentageCalculator {

    private static final String LOG_NAME = "GlucoseRepository";
    private final GlucoseRepository glucoseRepository;

    public GlucosePercentageCalculator(GlucoseRepository glucoseRepository) {
        this.glucoseRepository = glucoseRepository;
    }

    public Map<String, Double> calculateGlucosePercentageByCategory(int intervalHours) {
        Map<String, Double> glucosePercentages = new HashMap<>();

        try {
            List<GlucoseMeasurement> glucoseMeasurements = getAllGlucoseMeasurements();

            LocalDateTime now = LocalDateTime.now(); //Fecha y hora actual

            Map<LocalDateTime, List<GlucoseMeasurement>> groupedMeasurements = groupGlucoseByTimeInterval(glucoseMeasurements, intervalHours, now);

            int lowCount = 0;
            int normalCount = 0;
            int highCount = 0;

            for (List<GlucoseMeasurement> measurementsInInterval : groupedMeasurements.values()) {
                if (!measurementsInInterval.isEmpty()) {
                    for (GlucoseMeasurement measurement : measurementsInInterval) {
                        int glucoseValue = measurement.getGlucoseValue();
                        if (glucoseValue < 80) {
                            lowCount++;
                        } else if (glucoseValue <= 130) {
                            normalCount++;
                        } else {
                            highCount++;
                        }
                    }
                }
            }

            double totalCategories = (double) lowCount + normalCount + highCount;

            if (totalCategories > 0) {
                glucosePercentages.put("Bajo (<80)", (lowCount / totalCategories) * 100.0);
                glucosePercentages.put("Normal (80-130)", (normalCount / totalCategories) * 100.0);
                glucosePercentages.put("Alto (>130)", (highCount / totalCategories) * 100.0);

            } else {
                Log.w(LOG_NAME, "No valid glucose measurements found to calculate percentages.");
            }

        } catch (Exception e) {
            Log.e(LOG_NAME, "Error calculating glucose percentages by time interval: " + e.getMessage());
        }

        return glucosePercentages;
    }

    public List<GlucoseMeasurement> getAllGlucoseMeasurements() {
        List<GlucoseMeasurement> glucoseMeasurements = new ArrayList<>();

        try (Cursor cursor = glucoseRepository.getDatabase().query(
                GlucoseDBHelper.TABLE_NAME,
                null,
                null,
                null,
                null,
                null,
                null
        )) {
            if (cursor != null && cursor.moveToFirst()) {
                do {
                    int idIndex = cursor.getColumnIndex(GlucoseDBHelper.COLUMN_ID);
                    int glucoseValueIndex = cursor.getColumnIndex(GlucoseDBHelper.COLUMN_GLUCOSE_VALUE);
                    int dateIndex = cursor.getColumnIndex(GlucoseDBHelper.COLUMN_DATE);

                    if (idIndex != -1 && glucoseValueIndex != -1 && dateIndex != -1) {
                        long id = cursor.getLong(idIndex);
                        int glucoseValue = cursor.getInt(glucoseValueIndex);
                        String date = cursor.getString(dateIndex);

                        GlucoseMeasurement measurement = new GlucoseMeasurement(id, glucoseValue, date);
                        glucoseMeasurements.add(measurement);
                    } else {
                        Log.e(LOG_NAME, "Column not found at cursor");
                    }
                } while (cursor.moveToNext());

            } else {
                Log.d(LOG_NAME, "No rows found in cursor.");
            }
        } catch (SQLiteException e) {
            Log.e(LOG_NAME, "Error executing database query", e);
        }

        return glucoseMeasurements;
    }

    private Map<LocalDateTime, List<GlucoseMeasurement>> groupGlucoseByTimeInterval(List<GlucoseMeasurement> measurements, int intervalHours, LocalDateTime now) {
        Map<LocalDateTime, List<GlucoseMeasurement>> groupedMeasurements = new HashMap<>();

        for (GlucoseMeasurement measurement : measurements) {
            String dbDate = measurement.getDate();

            LocalDateTime dateTime = LocalDateTime.parse(dbDate, DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm")); //Formateo de fecha y hora.

            LocalDateTime intervalStartTime = now.minusHours(intervalHours).truncatedTo(ChronoUnit.HOURS); //Cálulo de intervalos desde que el usuario clicka al botón.

            // Comprobación de que la medición esté dentro del intervalo deseado.
            if (dateTime.isAfter(intervalStartTime) && dateTime.isBefore(now)) {
                groupedMeasurements.computeIfAbsent(intervalStartTime, k -> new ArrayList<>()).add(measurement);
            }
        }
        return groupedMeasurements;
    }

}
