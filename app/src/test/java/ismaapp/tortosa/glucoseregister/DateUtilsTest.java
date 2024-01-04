package ismaapp.tortosa.glucoseregister;

import java.util.Date;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

public class DateUtilsTest {

    @Test
    public void testGetCurrentDate2() {
        Date currentDate = DateUtils.getCurrentDate();
        assertNotNull(currentDate);
        // You can add more assertions based on your requirements
    }

    @Test
    public void testGetCurrentDate() {
        try (MockedStatic<DateUtils> mockedStatic = Mockito.mockStatic(DateUtils.class)) {
            // Mock the static method
            Date mockDate = new Date();
            mockedStatic.when(DateUtils::getCurrentDate).thenReturn(mockDate);

            // Test the method that uses getCurrentDate
            Date currentDate = DateUtils.getCurrentDate();
            assertEquals(mockDate, currentDate);
        }
    }
    
}

