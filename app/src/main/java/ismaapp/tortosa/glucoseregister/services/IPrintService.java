package ismaapp.tortosa.glucoseregister.services;

import java.io.File;
import java.util.List;

import ismaapp.tortosa.glucoseregister.entities.GlucoseMeasurement;

public interface IPrintService extends IBaseService{
    //boolean isActionSuccess();

    File generatePDF(List<GlucoseMeasurement> glucoseMeasurements);
}
