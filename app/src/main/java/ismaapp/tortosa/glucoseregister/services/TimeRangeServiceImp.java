package ismaapp.tortosa.glucoseregister.services;

import android.util.Log;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import ismaapp.tortosa.glucoseregister.entities.GlucoseMeasurement;

public class TimeRangeServiceImp implements ITimeRangeService{
    private static final String LOG_NAME = "GlucoseRepository";
    private final IGlucoseService glucoseService;

    public TimeRangeServiceImp(IGlucoseService glucoseServices) {
        this.glucoseService = glucoseServices;
    }

    //Método para calcular el porcentaje de valores según intervalo de tiempo.
    @Override
    public Map<String, Double> calculateGlucosePercentageByCategory(int intervalHours) {
        Map<String, Double> glucosePercentages = new HashMap<>();

        try {
            List<GlucoseMeasurement> glucoseMeasurements = glucoseService.getAllGlucoseMeasurements();

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

    private Map<LocalDateTime, List<GlucoseMeasurement>> groupGlucoseByTimeInterval(
            List<GlucoseMeasurement> measurements, int intervalHours, LocalDateTime now) {

        Map<LocalDateTime, List<GlucoseMeasurement>> groupedMeasurements = new HashMap<>();

        // Calcular el tiempo de inicio del intervalo más reciente.
        LocalDateTime intervalStartTime = now.minusHours(intervalHours);

        for (GlucoseMeasurement measurement : measurements) {
            String dbDate = measurement.getDate();
            LocalDateTime dateTime = LocalDateTime.parse(dbDate, DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm"));

            // Verificar si la medición está dentro del intervalo deseado.
            if (isWithinInterval(dateTime, intervalStartTime, now)) {
                // Redondear el tiempo de la medición al inicio del intervalo actual.
                LocalDateTime intervalKey = roundDownToNearestInterval(dateTime, intervalHours);

                // Agrupar la medición en el mapa usando el tiempo de intervalo como clave.
                groupedMeasurements.computeIfAbsent(intervalKey, k -> new ArrayList<>()).add(measurement);
            }
        }

        return groupedMeasurements;
    }

    private boolean isWithinInterval(LocalDateTime dateTime, LocalDateTime intervalStartTime, LocalDateTime now) {
        return !dateTime.isBefore(intervalStartTime) && !dateTime.isAfter(now);
    }

    private LocalDateTime roundDownToNearestInterval(LocalDateTime dateTime, int intervalHours) {
        int hour = dateTime.getHour();
        int remainder = hour % intervalHours;
        int roundedHour = hour - remainder;

        return dateTime.withHour(roundedHour).withMinute(0).withSecond(0).withNano(0);
    }

}
