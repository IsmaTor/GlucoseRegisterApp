package ismaapp.tortosa.glucoseregister.services;

import android.util.Log;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ismaapp.tortosa.glucoseregister.entities.GlucoseLevels;
import ismaapp.tortosa.glucoseregister.entities.GlucoseMeasurement;

public class TimeRangeServiceImp implements ITimeRangeService{
    private static final String LOG_NAME = "GlucoseRepository";
    private final IGlucoseService glucoseService;
    private final GlucoseLevels glucoseLevels;

    public TimeRangeServiceImp(IGlucoseService glucoseServices, GlucoseLevels glucoseLevels) {
        this.glucoseService = glucoseServices;
        this.glucoseLevels = glucoseLevels;
    }

    //Método para calcular el porcentaje de valores según intervalo de tiempo.
    @Override
    public Map<String, Double> calculateGlucosePercentageByCategory(int intervalHours) {
        Map<String, Double> glucosePercentages = new HashMap<>();

        try {
            List<GlucoseMeasurement> glucoseMeasurements = glucoseService.getAllGlucoseMeasurements();

            LocalDateTime now = LocalDateTime.now(); //Fecha y hora actual.

            Map<LocalDateTime, List<GlucoseMeasurement>> groupedMeasurements = groupGlucoseByTimeInterval(glucoseMeasurements, intervalHours, now);

            int lowCount = 0;
            int normalCount = 0;
            int highCount = 0;

            for (List<GlucoseMeasurement> measurementsInInterval : groupedMeasurements.values()) {
                if (!measurementsInInterval.isEmpty()) {
                    for (GlucoseMeasurement measurement : measurementsInInterval) {
                        int glucoseValue = measurement.getGlucoseValue();
                        if (glucoseValue < glucoseLevels.getLevelMin()) {
                            lowCount++;
                        } else if (glucoseValue <= glucoseLevels.getLevelMax()) {
                            normalCount++;
                        } else {
                            highCount++;
                        }
                    }
                }
            }

            double totalCategories = (double) lowCount + normalCount + highCount;

            if (totalCategories > 0) {
                glucosePercentages.put("Bajo (<" + glucoseLevels.getLevelMin() + ")", (lowCount / totalCategories) * 100.0);
                glucosePercentages.put("Normal (" + glucoseLevels.getLevelMin() + "-" + glucoseLevels.getLevelMax() + ")", (normalCount / totalCategories) * 100.0);
                glucosePercentages.put("Alto (>" + glucoseLevels.getLevelMax() + ")", (highCount / totalCategories) * 100.0);
            } else {
                Log.w(LOG_NAME, "No valid glucose measurements found to calculate percentages.");
            }

        } catch (Exception e) {
            Log.e(LOG_NAME, "Error calculating glucose percentages by time interval: " + e.getMessage());
        }

        return glucosePercentages;
    }

    @Override
    public List<GlucoseMeasurement> getLastDays(int intervalHours) {
        List<GlucoseMeasurement> glucoseMeasurementsWithoutPercentage = new ArrayList<>();

        try {
            List<GlucoseMeasurement> glucoseMeasurements = glucoseService.getAllGlucoseMeasurements();

            LocalDateTime now = LocalDateTime.now(); // Fecha y hora actual.

            Map<LocalDateTime, List<GlucoseMeasurement>> groupedMeasurements = groupGlucoseByTimeInterval(glucoseMeasurements, intervalHours, now);

            for (List<GlucoseMeasurement> measurementsInInterval : groupedMeasurements.values()) {
                if (!measurementsInInterval.isEmpty()) {
                    glucoseMeasurementsWithoutPercentage.addAll(measurementsInInterval);
                }
            }

        } catch (Exception e) {
            Log.e(LOG_NAME, "Error retrieving glucose measurements without percentages: " + e.getMessage());
        }

        return glucoseMeasurementsWithoutPercentage;
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
