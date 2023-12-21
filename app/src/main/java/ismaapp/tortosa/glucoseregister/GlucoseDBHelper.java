package ismaapp.tortosa.glucoseregister;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

public class GlucoseDBHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "glucosedb.db";
    private static final int DATABASE_VERSION = 1;

    public static final String TABLE_NAME = "glucose_measurements";
    public static final String COLUMN_ID = "id";
    public static final String COLUMN_GLUCOSE_VALUE = "glucose_value";
    public static final String COLUMN_DATE = "date";

    private static final String SQL_CREATE_ENTRIES =
            "CREATE TABLE " + TABLE_NAME + " (" +
                    COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                    COLUMN_GLUCOSE_VALUE + " REAL," +
                    COLUMN_DATE + " TEXT)";

    private static final String SQL_DELETE_ENTRIES =
            "DROP TABLE IF EXISTS " + TABLE_NAME;

    private final Context context;

    public GlucoseDBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        this.context = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_ENTRIES);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL(SQL_DELETE_ENTRIES);
        onCreate(db);
    }

    public List<GlucoseMeasurement> getPaginatedGlucoseMeasurements(int offset, int limit) {
        List<GlucoseMeasurement> glucoseMeasurements = new ArrayList<>();
        String query = "SELECT * FROM " + TABLE_NAME +
                " ORDER BY " + COLUMN_DATE + " DESC" +
                " LIMIT " + limit + " OFFSET " + offset;

        try (SQLiteDatabase db = this.getReadableDatabase();
             Cursor cursor = db.rawQuery(query, null)) {

            if (cursor != null && cursor.moveToFirst()) {
                do {
                    // Verificar la existencia de las columnas antes de intentar obtenerlas
                    int idIndex = cursor.getColumnIndex(COLUMN_ID);
                    int glucoseValueIndex = cursor.getColumnIndex(COLUMN_GLUCOSE_VALUE);
                    int dateIndex = cursor.getColumnIndex(COLUMN_DATE);

                    if (idIndex != -1 && glucoseValueIndex != -1 && dateIndex != -1) {
                        long id = cursor.getLong(idIndex);
                        double glucoseValue = cursor.getDouble(glucoseValueIndex);
                        String date = cursor.getString(dateIndex);

                        GlucoseMeasurement measurement = new GlucoseMeasurement(id, glucoseValue, date);
                        glucoseMeasurements.add(measurement);
                    } else {
                        // Manejar el caso en que alguna columna no esté presente
                        Log.e("GlucoseDBHelper", "Columna no encontrada en el cursor");
                    }

                } while (cursor.moveToNext());
            }
        } catch (SQLiteException e) {
            e.printStackTrace();
        }

        return glucoseMeasurements;
    }

}

