package ismaapp.tortosa.glucoseregister.services;

import android.content.ContentValues;
import android.database.Cursor;
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
    private static final String LOG_NAME = "GlucoseRepository";
    private boolean lastInsertSuccess = false;

    public GlucoseServicesImp(GlucoseRepository glucoseRepository) {
        this.glucoseRepository = glucoseRepository;
    }

    @Override
    public List<GlucoseMeasurement> getPaginatedGlucoseMeasurements(int offset, int limit, boolean orderByLatest) {
        List<GlucoseMeasurement> glucoseMeasurements = new ArrayList<>();
        String order = orderByLatest ? " DESC" : " ASC";
        String query = "SELECT * FROM " + GlucoseDBHelper.TABLE_NAME +
                " ORDER BY " + GlucoseDBHelper.COLUMN_DATE + order +
                " LIMIT " + limit + " OFFSET " + offset;

        try (Cursor cursor = glucoseRepository.getDatabase().rawQuery(query, null)) {

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
                        Log.e(LOG_NAME, "Column not found at cursor");
                    }

                } while (cursor.moveToNext());
            }
        } catch (SQLiteException e) {
            e.printStackTrace();
        }
        return glucoseMeasurements;
    }

    @Override
    public void insertGlucoseMeasurement(float glucoseValue) {
        try {
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
        }catch (Exception e) {
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

}
