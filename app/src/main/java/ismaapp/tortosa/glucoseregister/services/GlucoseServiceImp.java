package ismaapp.tortosa.glucoseregister.services;

import static android.content.ContentValues.TAG;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.util.Log;
import java.util.ArrayList;
import java.util.List;
import ismaapp.tortosa.glucoseregister.entities.GlucoseMeasurement;
import ismaapp.tortosa.glucoseregister.helpers.GlucoseDBHelper;
import ismaapp.tortosa.glucoseregister.repository.GlucoseRepository;
import ismaapp.tortosa.glucoseregister.utils.DateUtil;

public class GlucoseServiceImp implements IGlucoseService {
    private final GlucoseRepository glucoseRepository;
    private static final String ORDER_BY = " ORDER BY ";
    private static final String LOG_NAME = "GlucoseRepository";
    private static final String QUERY_LAST_MEASURE = "SELECT " + GlucoseDBHelper.COLUMN_ID +
            " FROM " + GlucoseDBHelper.TABLE_NAME +
            ORDER_BY + GlucoseDBHelper.COLUMN_DATE + " DESC, " +
            GlucoseDBHelper.COLUMN_ID + " DESC LIMIT 1";
    private static final String QUERY_LAST_VALUE = "SELECT " + GlucoseDBHelper.COLUMN_GLUCOSE_VALUE +
            " FROM " + GlucoseDBHelper.TABLE_NAME +
            ORDER_BY + GlucoseDBHelper.COLUMN_DATE + " DESC, " +
            GlucoseDBHelper.COLUMN_ID + " DESC LIMIT 1";

    private static final String QUERY_LAST_30_VALUES = "SELECT " + GlucoseDBHelper.COLUMN_GLUCOSE_VALUE +
            " FROM " + GlucoseDBHelper.TABLE_NAME +
            " ORDER BY " + GlucoseDBHelper.COLUMN_DATE + " DESC, " +
            GlucoseDBHelper.COLUMN_ID + " DESC LIMIT 10";

    private boolean actionSuccess = false;

    public GlucoseServiceImp(GlucoseRepository glucoseRepository) {
        this.glucoseRepository = glucoseRepository;
    }

    @Override
    public List<GlucoseMeasurement> getPaginatedGlucoseMeasurements(int pageNumber, int limit, boolean orderByLatest, boolean orderByHighestGlucose, String userSelection) {
        List<GlucoseMeasurement> glucoseMeasurements = new ArrayList<>();
        int offset = (pageNumber - 1) * limit;
        String order = buildOrderByClause(orderByLatest, orderByHighestGlucose, userSelection);
        String query = "SELECT * FROM " + GlucoseDBHelper.TABLE_NAME +
                order + " LIMIT " + limit + " OFFSET " + offset;
        try (Cursor cursor = executeQuery(query)) {
            glucoseMeasurements = extractGlucoseMeasurementsFromCursor(cursor);
        } catch (SQLiteException e) {
            logError("Error executing database query", e);
        }
        logDebug("Glucose Measurements: " + glucoseMeasurements);
        return glucoseMeasurements;
    }

    @Override
    public void insertGlucoseMeasurement(int glucoseValue) {
        try {
            if (glucoseValue != 0) {
                String date = DateUtil.getFormattedDate();
                ContentValues values = new ContentValues();
                values.put(GlucoseDBHelper.COLUMN_GLUCOSE_VALUE, glucoseValue);
                values.put(GlucoseDBHelper.COLUMN_DATE, date);
                long newRowId = glucoseRepository.getDatabase().insert(GlucoseDBHelper.TABLE_NAME, null, values);
                actionSuccess = newRowId != -1;
                if (actionSuccess) {
                    logDebug("Register inserted successfully, ID: " + newRowId + ", date: " + date);
                } else {
                    logError("Error inserting register into database", null);
                }
            } else {
                actionSuccess = false;
                logError("Cannot insert glucose measurement with value 0", null);
            }
        } catch (Exception e) {
            logError("Exception while inserting glucose measurement: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean isDeleteSuccess() {
        return actionSuccess;
    }

    @Override
    public void deleteAllGlucoseMeasurements() {
        try {
            int rowsAffected = glucoseRepository.getDatabase().delete(GlucoseDBHelper.TABLE_NAME, null, null);
            actionSuccess = rowsAffected > 0;
            if (actionSuccess) {
                Log.d(TAG, "All glucose measurements deleted successfully.");
            } else {
                Log.d(TAG, "No glucose measurements deleted.");
            }
        } catch (SQLiteException e) {
            Log.e(TAG, "Error deleting all glucose measurements", e);
        }
    }

    @Override
    public void deleteLastMeasure() {
        try {
            Cursor cursor = executeQuery(QUERY_LAST_MEASURE);

            if (cursor != null && cursor.moveToFirst()) {
                int columnIndex = cursor.getColumnIndex(GlucoseDBHelper.COLUMN_ID);
                if (columnIndex != -1) {
                    int lastEntryId = cursor.getInt(columnIndex);
                    String whereClause = GlucoseDBHelper.COLUMN_ID + " = ?";
                    String[] whereArgs = { String.valueOf(lastEntryId) };

                    int rowsAffected = glucoseRepository.getDatabase().delete(GlucoseDBHelper.TABLE_NAME, whereClause, whereArgs);

                    // Actualizar el estado de éxito de la operación
                    actionSuccess = rowsAffected > 0;
                    if (actionSuccess) {
                        Log.d(TAG, "Last entry successfully deleted");
                    } else {
                        Log.d(TAG, "Could not delete last entry");
                    }
                } else {
                    Log.e(TAG, "Column " + GlucoseDBHelper.COLUMN_ID + " not present in the cursor");
                }
            }

            if (cursor != null) {
                cursor.close();
            }
        } catch (Exception e) {
            Log.e(TAG, "Error when deleting last entry: " + e.getMessage(), e);
        }
    }

    @Override
    public int getLastGlucoseMeasurement() {
        int lastGlucoseMeasurement = 0;
        try {
            Cursor cursor = executeQuery(QUERY_LAST_VALUE);
            if (cursor != null && cursor.moveToFirst()) {
                int glucoseValueIndex = cursor.getColumnIndex(GlucoseDBHelper.COLUMN_GLUCOSE_VALUE);
                if (glucoseValueIndex != -1) {
                    lastGlucoseMeasurement = cursor.getInt(glucoseValueIndex);
                }
                cursor.close();
            }
        } catch (Exception e) {
            logError("Error retrieving last glucose measurement: " + e.getMessage(), e);
        }
        return lastGlucoseMeasurement;
    }

    @Override
    public List<GlucoseMeasurement> getLast30GlucoseMeasurement() {
        List<GlucoseMeasurement> last30GlucoseMeasurements = new ArrayList<>();
        try {
            Cursor cursor = executeQuery(QUERY_LAST_30_VALUES);
            if (cursor != null && cursor.moveToFirst()) {
                int glucoseValueIndex = cursor.getColumnIndex(GlucoseDBHelper.COLUMN_GLUCOSE_VALUE);

                while (!cursor.isAfterLast()) {
                    if (glucoseValueIndex != -1) {
                        int glucoseValue = cursor.getInt(glucoseValueIndex);
                        // Construir un objeto GlucoseMeasurement y agregarlo a la lista
                        GlucoseMeasurement measurement = new GlucoseMeasurement(0,glucoseValue, "");
                        last30GlucoseMeasurements.add(measurement);
                    }
                    cursor.moveToNext();
                }
                cursor.close();
            }
        } catch (Exception e) {
            logError("Error retrieving last 30 glucose measurements: " + e.getMessage(), e);
        }
        return last30GlucoseMeasurements;
    }


    @Override
    public boolean isDatabaseEmptyOrNull() {
        try {
            SQLiteDatabase database = glucoseRepository.getDatabase();
            if (database == null) {
                logError("Database is null", null);
                return true;
            } else {
                String query = "SELECT COUNT(*) FROM " + GlucoseDBHelper.TABLE_NAME;
                Cursor cursor = database.rawQuery(query, null);
                if (cursor != null && cursor.moveToFirst()) {
                    int count = cursor.getInt(0);
                    cursor.close();
                    return count == 0;
                } else {
                    logError("Cursor is null or empty", null);
                    return true;
                }
            }
        } catch (Exception e) {
            logError("Error checking if database is empty or null: " + e.getMessage(), e);
            return true;
        }
    }

    @Override
    public List<GlucoseMeasurement> getAllGlucoseMeasurements() {
        List<GlucoseMeasurement> glucoseMeasurements = new ArrayList<>();
        try (Cursor cursor = glucoseRepository.getDatabase().query(
                GlucoseDBHelper.TABLE_NAME,
                null,
                null,
                null,
                null,
                null,
                null
        )) {
            glucoseMeasurements = extractGlucoseMeasurementsFromCursor(cursor);
        } catch (SQLiteException e) {
            logError("Error executing database query", e);
        }
        return glucoseMeasurements;
    }

    //Methods Utils
    private Cursor executeQuery(String query) {
        Cursor cursor = null;
        try {
            cursor = glucoseRepository.getDatabase().rawQuery(query, null);
        } catch (Exception e) {
            Log.e(TAG, "Error executing query: " + e.getMessage(), e);
        }
        return cursor;
    }

    private String buildOrderByClause(boolean orderByLatest, boolean orderByHighestGlucose, String userSelection) {
        if (userSelection.equals("FECHA")) {
            return ORDER_BY + GlucoseDBHelper.COLUMN_DATE + (orderByLatest ? " DESC" : " ASC");
        } else if (userSelection.equals("REGISTRO")) {
            return ORDER_BY + GlucoseDBHelper.COLUMN_GLUCOSE_VALUE + (orderByHighestGlucose ? " DESC" : " ASC");
        }
        return ORDER_BY + GlucoseDBHelper.COLUMN_DATE + " ASC"; //Retorna por defecto.
    }

    private List<GlucoseMeasurement> extractGlucoseMeasurementsFromCursor(Cursor cursor) {
        List<GlucoseMeasurement> glucoseMeasurements = new ArrayList<>();
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
                    logError("Column not found at cursor", null);
                }
            } while (cursor.moveToNext());
        } else {
            logDebug("No rows found in cursor.");
        }
        return glucoseMeasurements;
    }

    private void logError(String message, Exception e) {
        Log.e(LOG_NAME, message, e);
    }

    private void logDebug(String message) {
        Log.d(LOG_NAME, message);
    }
}
