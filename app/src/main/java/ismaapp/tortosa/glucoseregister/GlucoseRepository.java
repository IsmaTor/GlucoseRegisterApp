package ismaapp.tortosa.glucoseregister;

import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

public class GlucoseRepository {
    private final SQLiteDatabase database;
    private static final String LOG_NAME = "GlucoseRepository";

    public GlucoseRepository(SQLiteDatabase database) {
        this.database = database;
    }

    public void insertGlucoseMeasurement(float glucoseValue) {
        Log.d(LOG_NAME, "Inserting glucose measurement: " + glucoseValue);
        String date = DateUtils.getFormattedDate();

        ContentValues values = new ContentValues();
        values.put(GlucoseDBHelper.COLUMN_GLUCOSE_VALUE, glucoseValue);
        values.put(GlucoseDBHelper.COLUMN_DATE, date);

        long newRowId = database.insert(GlucoseDBHelper.TABLE_NAME, null, values);

        if (newRowId != -1) {
            Log.d(LOG_NAME, "Registro insertado correctamente, ID: " + newRowId);
        } else {
            Log.e(LOG_NAME, "Error al insertar el registro en la base de datos.");
        }
    }
}

