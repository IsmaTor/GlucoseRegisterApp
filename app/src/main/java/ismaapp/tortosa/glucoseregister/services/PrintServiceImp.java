package ismaapp.tortosa.glucoseregister.services;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import ismaapp.tortosa.glucoseregister.entities.GlucoseMeasurement;

public class PrintServiceImp implements IPrintService {
    private final IGlucoseService glucoseService;
    private static final String CLASS_NAME = "PrintServiceImp";
    private boolean downloadSuccess = false;

    public PrintServiceImp (IGlucoseService glucoseService) {
        this.glucoseService = glucoseService;
    }

    @Override
    public boolean isDownloadSuccess() {
        return downloadSuccess;
    }

    @Override
    public File generatePDF(Context context, List<GlucoseMeasurement> glucoseMeasurements) {
        List<GlucoseMeasurement> measurements = new ArrayList<>(glucoseMeasurements);

        //Obtiene las mediciones de glucosa si la lista está vacía
        if (measurements.isEmpty()) {
            measurements = glucoseService.getAllGlucoseMeasurements();
        }

        String pdfFileName = "glucose_records.pdf"; //Nombre del archivo pdf

        //Obtener el archivo de descargas.
        File downloadsDirectory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);

        ContentValues contentValues = new ContentValues();
        contentValues.put(MediaStore.MediaColumns.DISPLAY_NAME, pdfFileName);
        contentValues.put(MediaStore.MediaColumns.MIME_TYPE, "application/pdf");
        contentValues.put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS);

        ContentResolver resolver = context.getContentResolver();
        Uri outputFileUri = resolver.insert(MediaStore.Files.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY), contentValues);

        if (outputFileUri != null) {
            OutputStream outputStream = null;
            try {
                outputStream = resolver.openOutputStream(outputFileUri);

                Document document = new Document(); //Crear el documento.

                PdfWriter.getInstance(document, outputStream); //Escribir el documento.

                document.open();

                addContentToPDF(document, measurements); //Agregar el contenido al documento.

                document.close();

                //Verificar la existencia del archivo pdf.
                File pdfFile = new File(downloadsDirectory, pdfFileName);
                if (pdfFile.exists() && pdfFile.length() > 0) {
                    downloadSuccess = true;
                    Log.d(CLASS_NAME, "PDF successfully created in: " + pdfFile.getAbsolutePath());
                    return pdfFile;
                } else {
                    downloadSuccess = false;
                    Log.e(CLASS_NAME, "ERROR: The PDF file was not generated correctly.");
                }
            } catch (IOException | DocumentException e) {
                Log.e(CLASS_NAME, "Error generating the PDF: " + e.getMessage(), e);
            } finally { //Garantiza la liberación de recursos.
                if (outputStream != null) {
                    try {
                        outputStream.close();
                    } catch (IOException e) {
                        Log.e(CLASS_NAME, "Error closing outputStream: " + e.getMessage(), e);
                    }
                }
            }
        } else {
            Log.e(CLASS_NAME, "ERROR: Could not create file in MediaStore.");
        }
        return null;
    }

    @Override
    public File generatePDF30Values(Context context, List<GlucoseMeasurement> glucoseMeasurements) {
        List<GlucoseMeasurement> measurements = new ArrayList<>(glucoseMeasurements);

        //Obtiene las mediciones de glucosa si la lista está vacía
        if (measurements.isEmpty()) {
            measurements = glucoseService.getLast30GlucoseMeasurement();
        }

        String pdfFileName = "glucose_records.pdf"; //Nombre del archivo pdf

        //Obtener el archivo de descargas.
        File downloadsDirectory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);

        ContentValues contentValues = new ContentValues();
        contentValues.put(MediaStore.MediaColumns.DISPLAY_NAME, pdfFileName);
        contentValues.put(MediaStore.MediaColumns.MIME_TYPE, "application/pdf");
        contentValues.put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS);

        ContentResolver resolver = context.getContentResolver();
        Uri outputFileUri = resolver.insert(MediaStore.Files.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY), contentValues);

        if (outputFileUri != null) {
            OutputStream outputStream = null;
            try {
                outputStream = resolver.openOutputStream(outputFileUri);

                Document document = new Document(); //Crear el documento.

                PdfWriter.getInstance(document, outputStream); //Escribir el documento.

                document.open();

                addContentToPDF(document, measurements); //Agregar el contenido al documento.

                document.close();

                //Verificar la existencia del archivo pdf.
                File pdfFile = new File(downloadsDirectory, pdfFileName);
                if (pdfFile.exists() && pdfFile.length() > 0) {
                    downloadSuccess = true;
                    Log.d(CLASS_NAME, "PDF successfully created in: " + pdfFile.getAbsolutePath());
                    return pdfFile;
                } else {
                    downloadSuccess = false;
                    Log.e(CLASS_NAME, "ERROR: The PDF file was not generated correctly.");
                }
            } catch (IOException | DocumentException e) {
                Log.e(CLASS_NAME, "Error generating the PDF: " + e.getMessage(), e);
            } finally { //Garantiza la liberación de recursos.
                if (outputStream != null) {
                    try {
                        outputStream.close();
                    } catch (IOException e) {
                        Log.e(CLASS_NAME, "Error closing outputStream: " + e.getMessage(), e);
                    }
                }
            }
        } else {
            Log.e(CLASS_NAME, "ERROR: Could not create file in MediaStore.");
        }
        return null;
    }

    //Creación de tabla para el documento pdf.
    private void addContentToPDF(Document document, List<GlucoseMeasurement> glucoseMeasurements) throws DocumentException {
        Paragraph title = new Paragraph("Glucose Measurement Records");
        title.setAlignment(Element.ALIGN_CENTER);
        document.add(title);

        PdfPTable table = new PdfPTable(2); //2 columnas
        table.setWidthPercentage(100);

        //Encabezados de tabla.
        table.addCell("Fecha");
        table.addCell("Valor de Glucosa");

        //Rellenar la tabla con datos de las mediciones de glucosa.
        for (GlucoseMeasurement measurement : glucoseMeasurements) {
            table.addCell(measurement.getDate()); // Agregar fecha
            table.addCell(String.valueOf(measurement.getGlucoseValue())); // Agregar valor de glucosa
        }
        document.add(table); //Agregar tabla al documento.
    }

}
