package ismaapp.tortosa.glucoseregister.services;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteException;
import java.util.ArrayList;
import java.util.List;
import android.util.Log;
import ismaapp.tortosa.glucoseregister.entities.GlucoseLevels;
import ismaapp.tortosa.glucoseregister.helpers.GlucoseDBHelper;
import ismaapp.tortosa.glucoseregister.repository.GlucoseRepository;

public class GlucoseLevelsImp implements IGlucoseLevels {
    private final GlucoseRepository glucoseRepository;
    private boolean levelSuccess = false;

    public GlucoseLevelsImp(GlucoseRepository glucoseRepository) {
        this.glucoseRepository = glucoseRepository;
    }

    @Override
    public boolean isLevelSuccess() {
        return levelSuccess;
    }

    //Método para obtener los valores actuales de Levels
    @Override
    public GlucoseLevels getLevels() {
        String[] projection = {
                GlucoseDBHelper.LEVELS_COLUMN_ID,
                GlucoseDBHelper.LEVELS_COLUMN_LEVEL_MAX,
                GlucoseDBHelper.LEVELS_COLUMN_LEVEL_MIN
        };
        Cursor cursor = glucoseRepository.getDatabase().query(
                GlucoseDBHelper.LEVELS_TABLE_NAME,
                projection,
                null,
                null,
                null,
                null,
                null
        );
        if (cursor != null && cursor.moveToFirst()) {
            int idIndex = cursor.getColumnIndex(GlucoseDBHelper.LEVELS_COLUMN_ID);
            int levelMaxIndex = cursor.getColumnIndex(GlucoseDBHelper.LEVELS_COLUMN_LEVEL_MAX);
            int levelMinIndex = cursor.getColumnIndex(GlucoseDBHelper.LEVELS_COLUMN_LEVEL_MIN);

            if (idIndex != -1 && levelMaxIndex != -1 && levelMinIndex != -1) {
                int id = cursor.getInt(idIndex);
                int levelMax = cursor.getInt(levelMaxIndex);
                int levelMin = cursor.getInt(levelMinIndex);
                GlucoseLevels levels = new GlucoseLevels(levelMax, levelMin);
                levels.setId(id);
                cursor.close();
                return levels;
            } else {
                // Manejar el caso en que alguna columna no se encuentra
                cursor.close();
                return null;
            }
        }
        return null;
    }

    //Método para actualizar los niveles de la tabla levels.
    @Override
    public void updateLevels(GlucoseLevels levels) {
        ContentValues values = new ContentValues();
        values.put(GlucoseDBHelper.LEVELS_COLUMN_LEVEL_MAX, levels.getLevelMax());
        values.put(GlucoseDBHelper.LEVELS_COLUMN_LEVEL_MIN, levels.getLevelMin());

        String selection = GlucoseDBHelper.LEVELS_COLUMN_ID + " = ?";
        String[] selectionArgs = { String.valueOf(levels.getId()) };

        int count = glucoseRepository.getDatabase().update(
                GlucoseDBHelper.LEVELS_TABLE_NAME,
                values,
                selection,
                selectionArgs
        );

        if (count > 0) {
            levelSuccess = true;
            Log.d("GlucoseLevelsImp", "Niveles de glucosa actualizados correctamente");
        } else {
            levelSuccess = false;
            Log.e("GlucoseLevelsImp", "Error al actualizar los niveles de glucosa");
        }
    }

    @Override
    public List<GlucoseLevels> getAllGlucoseLevels() {
        List<GlucoseLevels> glucoseLevels = new ArrayList<>();
        try (Cursor cursor = glucoseRepository.getDatabase().query(
                GlucoseDBHelper.LEVELS_TABLE_NAME,
                null,
                null,
                null,
                null,
                null,
                null
        )) {
            glucoseLevels = extractGlucoseMeasurementsFromCursor(cursor);
        } catch (SQLiteException e) {
            Log.e("Error executing database query" + e.getMessage(), "");

        }
        return glucoseLevels;
    }

    private List<GlucoseLevels> extractGlucoseMeasurementsFromCursor(Cursor cursor) {
        List<GlucoseLevels> glucoseLevels = new ArrayList<>();
        if (cursor != null && cursor.moveToFirst()) {
            do {
                int idIndex = cursor.getColumnIndex(GlucoseDBHelper.LEVELS_COLUMN_ID);
                int glucoseLevelMax = cursor.getColumnIndex(GlucoseDBHelper.LEVELS_COLUMN_LEVEL_MAX);
                int glucoseLevelMin = cursor.getColumnIndex(GlucoseDBHelper.LEVELS_COLUMN_LEVEL_MIN);

                if (idIndex != -1 && glucoseLevelMax != -1 && glucoseLevelMin != -1) {
                    int levelMax = cursor.getInt(glucoseLevelMax);
                    int levelMin = cursor.getInt(glucoseLevelMin);

                    GlucoseLevels levels = new GlucoseLevels(levelMax, levelMin);
                    glucoseLevels.add(levels);
                } else {
                    Log.e("Column not found at cursor", "");
                }
            } while (cursor.moveToNext());
        } else {
            Log.d("No rows found in cursor.", "");
        }
        return glucoseLevels;
    }

}
