package ismaapp.tortosa.glucoseregister.services;

import java.util.List;

import ismaapp.tortosa.glucoseregister.entities.GlucoseLevels;

public interface IGlucoseLevels {
    //Método para obtener los valores actuales de Levels.
    GlucoseLevels getLevels();
    //Método para recoger todos los levels.
    List<GlucoseLevels> getAllGlucoseLevels();
}
