package ismaapp.tortosa.glucoseregister.services;

import java.util.List;

import ismaapp.tortosa.glucoseregister.entity.GlucoseMeasurement;

public interface IGlucoseServices {

    //Paginated query to the database and returns a list of objects.
    public List<GlucoseMeasurement> getPaginatedGlucoseMeasurements(int offset, int limit, boolean orderByLatest, boolean orderByHighestGlucose, String userSelection);
    //Insert a measurement into the database
    public void insertGlucoseMeasurement(int glucoseValue);
    //Delete all measurements from the database
    public void deleteAllGlucoseMeasurements();
    public boolean isInsertSuccess();
    public int getLastGlucoseMeasurement();

    boolean isDatabaseEmptyOrNull();
}
