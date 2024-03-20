package ismaapp.tortosa.glucoseregister.services;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import ismaapp.tortosa.glucoseregister.entity.GlucoseMeasurement;
import ismaapp.tortosa.glucoseregister.helpers.GlucoseDBHelper;
import ismaapp.tortosa.glucoseregister.repository.GlucoseRepository;
import ismaapp.tortosa.glucoseregister.utils.DateUtils;

public class GlucoseServicesImp implements IGlucoseServices{

    private final GlucoseRepository glucoseRepository;
    private static final String ORDER_BY = " ORDER BY ";
    private static final String LOG_NAME = "GlucoseRepository";
    private boolean lastInsertSuccess = false;

    public GlucoseServicesImp(GlucoseRepository glucoseRepository) {
        this.glucoseRepository = glucoseRepository;
    }

    @Override
    public List<GlucoseMeasurement> getPaginatedGlucoseMeasurements(int pageNumber, int limit, boolean orderByLatest, boolean orderByHighestGlucose, String userSelection) {
        List<GlucoseMeasurement> glucoseMeasurements = new ArrayList<>();
        int offset = (pageNumber - 1) * limit;
        String order = ORDER_BY + GlucoseDBHelper.COLUMN_DATE + " ASC";

        if (userSelection.equals("FECHA")) {
            order = ORDER_BY + GlucoseDBHelper.COLUMN_DATE + ((orderByLatest) ? " DESC" : " ASC");
        } else if (userSelection.equals("REGISTRO")) {
            order = ORDER_BY + GlucoseDBHelper.COLUMN_GLUCOSE_VALUE + ((orderByHighestGlucose) ? " DESC" : " ASC");
        }

        String query = "SELECT * FROM " + GlucoseDBHelper.TABLE_NAME +
                order + " LIMIT " + limit + " OFFSET " + offset;

        try (Cursor cursor = glucoseRepository.getDatabase().rawQuery(query, null)) {
            if (cursor != null && cursor.moveToFirst()) {
                do {
                    int idIndex = cursor.getColumnIndex(GlucoseDBHelper.COLUMN_ID);
                    int glucoseValueIndex = cursor.getColumnIndex(GlucoseDBHelper.COLUMN_GLUCOSE_VALUE);
                    int dateIndex = cursor.getColumnIndex(GlucoseDBHelper.COLUMN_DATE);

                    if (idIndex != -1 && glucoseValueIndex != -1 && dateIndex != -1) {
                        long id = cursor.getLong(idIndex);
                        int glucoseValue = cursor.getInt(glucoseValueIndex);
                        String date = cursor.getString(dateIndex);

                        GlucoseMeasurement measurement = new GlucoseMeasurement(id, glucoseValue, date);
                        glucoseMeasurements.add(measurement);
                    } else {
                        Log.e(LOG_NAME, "Column not found at cursor");
                    }
                } while (cursor.moveToNext());
            } else {
                Log.d(LOG_NAME, "No rows found in cursor."); // Registro de depuración para cuando no se encuentran filas en el cursor
            }
        } catch (SQLiteException e) {
            Log.e(LOG_NAME, "Error executing database query", e);
        }

        Log.d("Glucose Measurements", glucoseMeasurements.toString()); // Registro de depuración para las mediciones de glucosa obtenidas
        return glucoseMeasurements;
    }

    @Override
    public void insertGlucoseMeasurement(int glucoseValue) {
        try {
            if (glucoseValue != 0) {
                Log.d(LOG_NAME, "Inserting glucose measurement: " + glucoseValue);
                String date = DateUtils.getFormattedDate();
                ContentValues values = new ContentValues();
                values.put(GlucoseDBHelper.COLUMN_GLUCOSE_VALUE, glucoseValue);
                values.put(GlucoseDBHelper.COLUMN_DATE, date);

                long newRowId = glucoseRepository.getDatabase().insert(GlucoseDBHelper.TABLE_NAME, null, values);

                lastInsertSuccess = newRowId != -1;

                if (lastInsertSuccess) {
                    Log.d(LOG_NAME, "Register inserted successfully, ID: " + newRowId + "date: " + date);
                } else {
                    Log.e(LOG_NAME, "Error inserting register into database.");
                }
            } else {
                lastInsertSuccess = false;
                Log.e(LOG_NAME, "Cannot insert glucose measurement with value 0.");
            }
        } catch (Exception e) {
            Log.e(LOG_NAME, "Exception while inserting glucose measurement: " + e.getMessage());
        }
    }


    @Override
    public boolean isInsertSuccess() {
        return lastInsertSuccess;
    }

    @Override
    public void deleteAllGlucoseMeasurements() {
        try {
            glucoseRepository.getDatabase().delete(GlucoseDBHelper.TABLE_NAME, null, null);
            Log.d(LOG_NAME, "All glucose measurements deleted successfully.");
        } catch (SQLiteException e) {
            Log.e(LOG_NAME, "Error deleting all glucose measurements.", e);
        }
    }

    @Override
    public int getLastGlucoseMeasurement() {
        int lastGlucoseMeasurement = 0;
        try {
            String query = "SELECT " + GlucoseDBHelper.COLUMN_GLUCOSE_VALUE +
                    " FROM " + GlucoseDBHelper.TABLE_NAME +
                    ORDER_BY + GlucoseDBHelper.COLUMN_DATE + " DESC, " +
                    GlucoseDBHelper.COLUMN_ID + " DESC LIMIT 1";

            Cursor cursor = glucoseRepository.getDatabase().rawQuery(query, null);

            if (cursor != null && cursor.moveToFirst()) {
                int glucoseValueIndex = cursor.getColumnIndex(GlucoseDBHelper.COLUMN_GLUCOSE_VALUE);
                if (glucoseValueIndex != -1) {
                    lastGlucoseMeasurement = (int) cursor.getFloat(glucoseValueIndex);
                }
                cursor.close();
            }
        } catch (Exception e) {
            Log.e(LOG_NAME, "Error retrieving last glucose measurement: " + e.getMessage());
        }
        return lastGlucoseMeasurement;
    }

    @Override
    public boolean isDatabaseEmptyOrNull() {
        try {
            SQLiteDatabase database = glucoseRepository.getDatabase();
            if (database == null) {
                Log.e(LOG_NAME, "Database is null.");
                return true;
            } else {
                String query = "SELECT COUNT(*) FROM " + GlucoseDBHelper.TABLE_NAME;
                Cursor cursor = database.rawQuery(query, null);
                if (cursor != null && cursor.moveToFirst()) {
                    int count = cursor.getInt(0);
                    cursor.close();
                    return count == 0;
                } else {
                    Log.e(LOG_NAME, "Cursor is null or empty.");
                    return true;
                }
            }
        } catch (Exception e) {
            Log.e(LOG_NAME, "Error checking if database is empty or null: " + e.getMessage());
            return true;
        }
    }

}
