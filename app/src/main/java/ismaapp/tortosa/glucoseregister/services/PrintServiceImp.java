package ismaapp.tortosa.glucoseregister.services;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.Toast;

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

    public File generatePDF(Context context, List<GlucoseMeasurement> glucoseMeasurements) {
        List<GlucoseMeasurement> measurements = new ArrayList<>(glucoseMeasurements);

        // Obtener las mediciones de glucosa si la lista está vacía
        if (measurements.isEmpty()) {
            measurements = glucoseService.getAllGlucoseMeasurements();
        }

        // Crear un nombre de archivo único para el PDF
        String pdfFileName = "glucose_records.pdf";

        // Obtener el directorio de descargas usando MediaStore
        File downloadsDirectory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);

        // Crear un ContentValues para el nuevo archivo en MediaStore
        ContentValues contentValues = new ContentValues();
        contentValues.put(MediaStore.MediaColumns.DISPLAY_NAME, pdfFileName);
        contentValues.put(MediaStore.MediaColumns.MIME_TYPE, "application/pdf");
        contentValues.put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS);

        ContentResolver resolver = context.getContentResolver();
        Uri outputFileUri = resolver.insert(MediaStore.Files.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY), contentValues);

        if (outputFileUri != null) {
            try {
                OutputStream outputStream = resolver.openOutputStream(outputFileUri);
                if (outputStream != null) {
                    // Crear el documento PDF
                    Document document = new Document();
                    PdfWriter writer = PdfWriter.getInstance(document, outputStream);

                    // Encriptar el PDF con una contraseña
                    writer.setEncryption("11235DV".getBytes(), null,
                            PdfWriter.ALLOW_PRINTING, PdfWriter.ENCRYPTION_AES_128 | PdfWriter.STANDARD_ENCRYPTION_128);

                    document.open();

                    // Agregar contenido al documento PDF
                    addContentToPDF(document, measurements);

                    document.close();
                    outputStream.close();

                    // Verificar la existencia y tamaño del archivo PDF
                    File pdfFile = new File(downloadsDirectory, pdfFileName);
                    if (pdfFile.exists() && pdfFile.length() > 0) {
                        downloadSuccess = true;
                        Log.d(CLASS_NAME, "PDF creado exitosamente en: " + pdfFile.getAbsolutePath());
                        Toast.makeText(context, "PDF generado exitosamente", Toast.LENGTH_SHORT).show();
                        return pdfFile;
                    } else {
                        downloadSuccess = false;
                        Log.e(CLASS_NAME, "ERROR: El archivo PDF no se generó correctamente");
                        Toast.makeText(context, "Error al generar el PDF", Toast.LENGTH_SHORT).show();
                    }
                }
            } catch (IOException | DocumentException e) {
                Log.e(CLASS_NAME, "Error generando el PDF: " + e.getMessage(), e);
                Toast.makeText(context, "Error al generar el PDF", Toast.LENGTH_SHORT).show();
            }
        } else {
            Log.e(CLASS_NAME, "ERROR: No se pudo crear el archivo en MediaStore");
            Toast.makeText(context, "Error al crear el archivo en MediaStore", Toast.LENGTH_SHORT).show();
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
