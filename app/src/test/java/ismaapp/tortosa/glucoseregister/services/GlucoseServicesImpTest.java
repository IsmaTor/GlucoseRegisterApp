package ismaapp.tortosa.glucoseregister.services;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import ismaapp.tortosa.glucoseregister.repository.GlucoseRepository;

@RunWith(MockitoJUnitRunner.class)
public class GlucoseServicesImpTest {

    @Mock
    GlucoseRepository glucoseRepository;
    @Mock
    SQLiteDatabase database;
    @Mock
    Cursor cursor;

    @Test
    public void testIsDatabaseEmptyOrNull() {
        GlucoseServicesImp glucoseServices = new GlucoseServicesImp(glucoseRepository);
        when(glucoseRepository.getDatabase()).thenReturn(database);
        when(database.rawQuery(Mockito.anyString(), Mockito.any())).thenReturn(cursor);
        when(cursor.moveToFirst()).thenReturn(true);
        when(cursor.getInt(0)).thenReturn(0);

        boolean result = glucoseServices.isDatabaseEmptyOrNull();

        assertTrue(result); // La base de datos debería estar vacía
    }
}
