package ismaapp.tortosa.glucoseregister.services;

import static org.hamcrest.CoreMatchers.any;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import android.database.Cursor;
import android.database.MatrixCursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;

import ismaapp.tortosa.glucoseregister.helpers.GlucoseDBHelper;
import ismaapp.tortosa.glucoseregister.repository.GlucoseRepository;

@RunWith(MockitoJUnitRunner.class)
public class GlucoseServicesImpTest {
    private static final String LOG_NAME = "GlucoseRepositoryTest";

    @Mock
    GlucoseRepository glucoseRepository;
    @InjectMocks
    GlucoseServicesImp glucoseServicesImp;
    @Mock
    SQLiteDatabase database;
    @Mock
    Cursor cursor;
    private AutoCloseable closeable;

    @Before
    public void setUp() {
        closeable = MockitoAnnotations.openMocks(this);
    }

    @After
    public void setClose() throws Exception{
        closeable.close();
    }

    @Test
    public void testSumAndSubtract() {
        int result = glucoseServicesImp.sumAndSubtract(5, 3);
        assertEquals(10, result);
    }


    @Test
    public void testGetLastGlucoseMeasurement_NoResults() {


        // Calling the method under test
        int result = glucoseServicesImp.getLastGlucoseMeasurement();

        // Verifying that correct value is returned when no results are found
        assertEquals(0, result); // Expecting 0 when no results are found
    }


    @Test
    public void testIsDatabaseEmptyOrNull() {
        glucoseServicesImp = new GlucoseServicesImp(glucoseRepository);
        when(glucoseRepository.getDatabase()).thenReturn(database);
        when(database.rawQuery(anyString(), Mockito.any())).thenReturn(cursor);
        when(cursor.moveToFirst()).thenReturn(true);
        when(cursor.getInt(0)).thenReturn(0);

        boolean result = glucoseServicesImp.isDatabaseEmptyOrNull();

        assertTrue(result);
    }
}
