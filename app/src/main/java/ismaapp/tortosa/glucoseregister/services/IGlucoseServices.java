package ismaapp.tortosa.glucoseregister.services;

import java.util.List;

import ismaapp.tortosa.glucoseregister.entity.GlucoseMeasurement;

public interface IGlucoseServices {

    //Paginated query to the database and returns a list of objects.
    public List<GlucoseMeasurement> getPaginatedGlucoseMeasurements(int offset, int limit, boolean orderByLatest);
    //Insert a measurement into the database
    public void insertGlucoseMeasurement(float glucoseValue);
    //Delete all measurements from the database
    public void deleteAllGlucoseMeasurements();
}
