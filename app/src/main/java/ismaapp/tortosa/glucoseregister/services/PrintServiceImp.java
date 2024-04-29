package ismaapp.tortosa.glucoseregister.services;

import android.os.Environment;
import android.util.Log;

import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
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
    public File generatePDF(List<GlucoseMeasurement> glucoseMeasurements) {
        glucoseMeasurements = glucoseService.getAllGlucoseMeasurements();

        File downloadsDirectory = getDownloadsDirectory(); //Ruta del directorio de descargas.

        String pdfFilePath = downloadsDirectory.getPath() + "/glucose_records.pdf"; //Ruta de guardado.
        File pdfFile = new File(pdfFilePath);

        try (FileOutputStream outputStream = new FileOutputStream(pdfFile)) {
            Document document = new Document(); //Creación del documento.
            PdfWriter.getInstance(document, outputStream);
            document.open();

            addContentToPDF(document, glucoseMeasurements); //Agragar contenido al documento.

            document.close();

            // Verificar la existencia del archivo PDF después de cerrar el documento.
            if (pdfFile.exists() && pdfFile.length() > 0) {
                downloadSuccess = true;
                Log.d(CLASS_NAME, "PDF creado con éxito en: " + pdfFilePath);
            } else {
                downloadSuccess = false;
                Log.e(CLASS_NAME, "ERROR: El archivo PDF no se ha generado correctamente.");
            }

        } catch (DocumentException | IOException e) {
            Log.e(CLASS_NAME, "Error generando PDF: " + e.getMessage(), e);
        }

        return pdfFile;
    }

    //Creación de tabla para el documento pdf.
    private void addContentToPDF(Document document, List<GlucoseMeasurement> glucoseMeasurements) throws DocumentException {
        Paragraph title = new Paragraph("Glucose Measurement Records");
        title.setAlignment(Element.ALIGN_CENTER);
        document.add(title);

        PdfPTable table = new PdfPTable(3); // 3 columnas para fecha, valor de glucosa y nota
        table.setWidthPercentage(100);

        //Encabezados de tabla.
        table.addCell("ID");
        table.addCell("Valor de Glucosa");
        table.addCell("Fecha");

        //Rellenar la tabla con datos de las mediciones de glucosa.
        for (GlucoseMeasurement measurement : glucoseMeasurements) {
            table.addCell(String.valueOf(measurement.getId())); // Agregar ID
            table.addCell(String.valueOf(measurement.getGlucoseValue())); // Agregar valor de glucosa
            table.addCell(measurement.getDate()); // Agregar fecha
        }

        document.add(table); //Agregar tabla al documento.
    }

    private File getDownloadsDirectory() {
        //Guardar el archivo en una carpeta predeterminada.
        File downloadsDirectory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);

        if (!downloadsDirectory.exists()) {
            downloadsDirectory.mkdirs(); // Crear el directorio si no existe
        }

        return downloadsDirectory;
    }

}
