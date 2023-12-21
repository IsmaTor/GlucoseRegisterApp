package ismaapp.tortosa.glucoseregister;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

public class GlucoseRepository {
    private final SQLiteDatabase database;
    private static final String LOG_NAME = "GlucoseRepository";

    public GlucoseRepository(SQLiteDatabase database) {
        this.database = database;
    }

    public List<GlucoseMeasurement> getPaginatedGlucoseMeasurements(int offset, int limit) {
        List<GlucoseMeasurement> glucoseMeasurements = new ArrayList<>();
        String query = "SELECT * FROM " + GlucoseDBHelper.TABLE_NAME +
                " ORDER BY " + GlucoseDBHelper.COLUMN_DATE + " DESC" +
                " LIMIT " + limit + " OFFSET " + offset;

        Cursor cursor = null;

        try {
            cursor = database.rawQuery(query, null);

            if (cursor != null && cursor.moveToFirst()) {
                do {
                    int idIndex = cursor.getColumnIndex(GlucoseDBHelper.COLUMN_ID);
                    int glucoseValueIndex = cursor.getColumnIndex(GlucoseDBHelper.COLUMN_GLUCOSE_VALUE);
                    int dateIndex = cursor.getColumnIndex(GlucoseDBHelper.COLUMN_DATE);

                    if (idIndex != -1 && glucoseValueIndex != -1 && dateIndex != -1) {
                        long id = cursor.getLong(idIndex);
                        double glucoseValue = cursor.getDouble(glucoseValueIndex);
                        String date = cursor.getString(dateIndex);

                        GlucoseMeasurement measurement = new GlucoseMeasurement(id, glucoseValue, date);
                        glucoseMeasurements.add(measurement);
                    } else {
                        Log.e(LOG_NAME, "Columna no encontrada en el cursor");
                    }

                } while (cursor.moveToNext());
            }
        } catch (SQLiteException e) {
            e.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

        return glucoseMeasurements;
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

