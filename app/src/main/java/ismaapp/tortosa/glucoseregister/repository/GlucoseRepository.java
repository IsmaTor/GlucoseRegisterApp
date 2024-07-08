package ismaapp.tortosa.glucoseregister.repository;

import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;

import ismaapp.tortosa.glucoseregister.entities.GlucoseLevels;
import ismaapp.tortosa.glucoseregister.helpers.GlucoseDBHelper;

public class GlucoseRepository {
    private final SQLiteDatabase database;

    public GlucoseRepository(SQLiteDatabase database) {
        this.database = database;
    }

    public SQLiteDatabase getDatabase() {
        return database;
    }

    //Método para insertar los valores iniciales de Levels
    public long insertInitialLevels(GlucoseLevels levels) {
        ContentValues values = new ContentValues();
        values.put(GlucoseDBHelper.LEVELS_COLUMN_LEVEL_MAX, levels.getLevelMax());
        values.put(GlucoseDBHelper.LEVELS_COLUMN_LEVEL_MIN, levels.getLevelMin());
        return database.insert(GlucoseDBHelper.LEVELS_TABLE_NAME, null, values);
    }

    //Método para actualizar los valores de Levels
    public int updateLevels(GlucoseLevels levels) {
        ContentValues values = new ContentValues();
        values.put(GlucoseDBHelper.LEVELS_COLUMN_LEVEL_MAX, levels.getLevelMax());
        values.put(GlucoseDBHelper.LEVELS_COLUMN_LEVEL_MIN, levels.getLevelMin());
        return database.update(GlucoseDBHelper.LEVELS_TABLE_NAME, values, GlucoseDBHelper.LEVELS_COLUMN_ID + " = ?", new String[]{String.valueOf(levels.getId())});
    }

}

