package ismaapp.tortosa.glucoseregister.services;

import java.io.File;
import java.util.List;

import ismaapp.tortosa.glucoseregister.entities.GlucoseMeasurement;

public interface IPrintService {
    boolean isDownloadSuccess();

    File generatePDF(List<GlucoseMeasurement> glucoseMeasurements);
}
