package ismaapp.tortosa.glucoseregister.services;

import android.content.Context;

import java.io.File;
import java.util.List;

import ismaapp.tortosa.glucoseregister.entities.GlucoseMeasurement;

public interface IPrintService {
    boolean isDownloadSuccess();

    File pdfAllValues(Context context, List<GlucoseMeasurement> glucoseMeasurements);

    File pdf30Values(Context context, List<GlucoseMeasurement> glucoseMeasurements);
}
