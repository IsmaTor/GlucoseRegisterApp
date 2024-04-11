package ismaapp.tortosa.glucoseregister.services;

import java.util.Map;

public interface ITimeRangeService {
    //Método para calcular el porcentaje de valores según intervalo de tiempo.
    Map<String, Double> calculateGlucosePercentageByCategory(int intervalHours);
}
