package ismaapp.tortosa.glucoseregister.helpers;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

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


    public GlucoseDBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);

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

}
