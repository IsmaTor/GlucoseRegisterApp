package ismaapp.tortosa.glucoseregister.services;

import android.content.Context;

import java.io.File;
import java.util.List;

import ismaapp.tortosa.glucoseregister.entities.GlucoseMeasurement;

public interface IPrintService {
    boolean isDownloadSuccess();

    File generatePDF(Context context, List<GlucoseMeasurement> glucoseMeasurements);

    File generatePDF30Values(Context context, List<GlucoseMeasurement> glucoseMeasurements);
}
