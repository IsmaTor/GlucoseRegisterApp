package ismaapp.tortosa.glucoseregister.repository;

import android.database.sqlite.SQLiteDatabase;

public class GlucoseRepository {
    private final SQLiteDatabase database;

    public GlucoseRepository(SQLiteDatabase database) {
        this.database = database;
    }

    public SQLiteDatabase getDatabase() {
        return database;
    }

}

