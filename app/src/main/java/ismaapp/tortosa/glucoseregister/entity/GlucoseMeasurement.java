package ismaapp.tortosa.glucoseregister.entity;

public class GlucoseMeasurement {
    private final long id;
    private final double glucoseValue;
    private final String date;

    public GlucoseMeasurement(long id, double glucoseValue, String date) {
        this.id = id;
        this.glucoseValue = glucoseValue;
        this.date = date;
    }

    public long getId() {
        return id;
    }

    public double getGlucoseValue() {
        return glucoseValue;
    }

    public String getDate() {
        return date;
    }
}
