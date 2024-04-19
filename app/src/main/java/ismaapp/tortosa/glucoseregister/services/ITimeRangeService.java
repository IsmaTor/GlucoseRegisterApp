package ismaapp.tortosa.glucoseregister.services;

import java.util.List;
import java.util.Map;

import ismaapp.tortosa.glucoseregister.entities.GlucoseMeasurement;

public interface ITimeRangeService {
    //Método para calcular el porcentaje de valores según intervalo de tiempo.
    Map<String, Double> calculateGlucosePercentageByCategory(int intervalHours);
    //Método para recoger los últimos valores.
    List<GlucoseMeasurement> getLastDays(int intervalHours);
}
