package com.example.Proyecto_IoT.util;

import com.example.Proyecto_IoT.dto.medicion.TelemetryDTO;
import com.example.Proyecto_IoT.model.Device;
import com.example.Proyecto_IoT.model.User;
import com.lowagie.text.Document;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileOutputStream;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class PdfGenerator {
    public String generar(List<TelemetryDTO> datos, User user, Device device) {
        String carpeta = "mediciones_pdf";
        File dir = new File(carpeta);
        if (!dir.exists()) dir.mkdirs();

        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH-mm-ss");
        String currentDate = LocalDateTime.now().format(dateFormatter);
        String currentTime = LocalDateTime.now().format(timeFormatter);

        String ruta = carpeta + "/" + "medicion_D"+ currentDate+"_T"+ currentTime +"_usuario"+ user.getId() + ".pdf";
        Document document = new Document();
        try (FileOutputStream fos = new FileOutputStream(ruta)) {
            PdfWriter.getInstance(document, fos);
            document.open();

            document.add(new Paragraph("Fecha: " + currentDate));
            document.add(new Paragraph("Hora: " + currentTime));
            document.add(new Paragraph("Usuario: " + user.getEmail()));
            document.add(new Paragraph("Dispositivo: " + device.getName()));
            document.add(new Paragraph(" "));

            if (!datos.isEmpty()) {
                // Definir los encabezados según los atributos de TelemetryDTO
                String[] headers = {"tiempo", "temperatura", "humedad", "presion"};
                PdfPTable table = new PdfPTable(headers.length);
                // Encabezados
                for (String header : headers) {
                    table.addCell(new Phrase(header));
                }
                // Filas
                for (TelemetryDTO muestra : datos) {
                    // Conversión segura del timestamp a HH:mm:ss
                    String hora = "";
                    if (muestra.getTimeStamp() != null) {
                        try {
                            long ts = Long.parseLong(muestra.getTimeStamp());
                            LocalDateTime ldt = Instant.ofEpochMilli(ts).atZone(ZoneId.systemDefault()).toLocalDateTime();
                            hora = ldt.format(DateTimeFormatter.ofPattern("HH:mm:ss"));
                        } catch (Exception e) {
                            hora = muestra.getTimeStamp(); // fallback: mostrar el valor original si falla la conversión
                        }
                    }
                    table.addCell(hora);
                    table.addCell(muestra.getTemperatura() != null ? muestra.getTemperatura() : "");
                    table.addCell(muestra.getHumedad() != null ? muestra.getHumedad() : "");
                    table.addCell(muestra.getPresion() != null ? muestra.getPresion() : "");
                }
                document.add(table);
            } else {
                document.add(new Paragraph("No se recolectaron datos."));
            }

            document.close();
        } catch (Exception e) {
            throw new RuntimeException("Error al generar el PDF", e);
        }
        return ruta;
    }
}
