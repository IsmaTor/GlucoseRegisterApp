package ismaapp.tortosa.glucoseregister.entity;

public class GlucoseMeasurement {
    private final long id;
    private final int glucoseValue;
    private final String date;

    public GlucoseMeasurement(long id, int glucoseValue, String date) {
        this.id = id;
        this.glucoseValue = glucoseValue;
        this.date = date;
    }

    public long getId() {
        return id;
    }

    public int getGlucoseValue() {
        return glucoseValue;
    }

    public String getDate() {
        return date;
    }
}
