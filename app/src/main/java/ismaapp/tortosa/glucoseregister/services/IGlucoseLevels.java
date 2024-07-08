package ismaapp.tortosa.glucoseregister.services;

import java.util.List;

import ismaapp.tortosa.glucoseregister.entities.GlucoseLevels;

public interface IGlucoseLevels {
    boolean isLevelSuccess();

    //Método para obtener los valores actuales de Levels.
    GlucoseLevels getLevels();

    //Método para obtener el nivel máximo de glucosa de la base de datos.
    int getLevelMaxDB();

    //Método para obtener el nivel mínimo de glucosa de la base de datos.
    int getLevelMinDB();

    // Método para actualizar los niveles de la tabla levels.
    void updateLevels(GlucoseLevels levels);

    //Método para recoger todos los levels.
    List<GlucoseLevels> getAllGlucoseLevels();
}
