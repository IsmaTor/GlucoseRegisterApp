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
import java.io.FileOutputStream;
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
        if (glucoseMeasurements.isEmpty()) {
            // Si la lista de mediciones está vacía, obtén las mediciones del servicio
            glucoseMeasurements = glucoseService.getAllGlucoseMeasurements();
        }

        String pdfFileName = "glucose_records.pdf";

        // Obtener el directorio específico de la aplicación para almacenamiento externo
        File externalFilesDir = context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS);

        if (externalFilesDir != null) {
            File pdfFile = new File(externalFilesDir, pdfFileName);

            try {
                FileOutputStream outputStream = new FileOutputStream(pdfFile);
                Document document = new Document();
                PdfWriter writer = PdfWriter.getInstance(document, outputStream);

                // Encriptar el PDF con una contraseña
                writer.setEncryption("11235DV".getBytes(), null,
                        PdfWriter.ALLOW_PRINTING, PdfWriter.ENCRYPTION_AES_128 | PdfWriter.STANDARD_ENCRYPTION_128);

                document.open();

                // Agregar contenido al documento PDF
                addContentToPDF(document, glucoseMeasurements);

                document.close();
                outputStream.close();

                Log.d(CLASS_NAME, "PDF creado exitosamente en: " + pdfFile.getAbsolutePath());
                Toast.makeText(context, "PDF generado exitosamente", Toast.LENGTH_SHORT).show();

                return pdfFile; // Devuelve el archivo PDF generado
            } catch (IOException | DocumentException e) {
                Log.e(CLASS_NAME, "Error generando el PDF: " + e.getMessage(), e);
                Toast.makeText(context, "Error al generar el PDF", Toast.LENGTH_SHORT).show();
            }
        } else {
            Log.e(CLASS_NAME, "Directorio externo no disponible para almacenamiento");
            Toast.makeText(context, "Error: Directorio externo no disponible", Toast.LENGTH_SHORT).show();
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
