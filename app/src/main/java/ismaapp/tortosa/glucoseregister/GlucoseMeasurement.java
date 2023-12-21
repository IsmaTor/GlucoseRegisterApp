package ismaapp.tortosa.glucoseregister;

public class GlucoseMeasurement {
    private long id;
    private double glucoseValue;
    private String date;

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
