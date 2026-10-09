package fly2us.tn.dossier.service;

import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.TextAlignment;
import fly2us.tn.dossier.model.Dossier;
import fly2us.tn.dossier.model.DossierDocument;

import java.io.ByteArrayOutputStream;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class PdfGenerator {

    public static byte[] generateDossierPdf(Dossier dossier, List<DossierDocument> documents) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        try {
            PdfWriter writer = new PdfWriter(baos);
            PdfDocument pdf = new PdfDocument(writer);
            Document document = new Document(pdf);

            // Titre
            Paragraph title = new Paragraph("DOSSIER " + dossier.getDossierNumber())
                    .setFontSize(20)
                    .setBold()
                    .setTextAlignment(TextAlignment.CENTER);
            document.add(title);

            document.add(new Paragraph("\n"));

            // Informations du dossier
            document.add(new Paragraph("Informations du Dossier").setBold().setFontSize(14));
            document.add(new Paragraph("Numéro : " + dossier.getDossierNumber()));
            document.add(new Paragraph("Type : " + dossier.getType()));
            document.add(new Paragraph("Pays : " + dossier.getCountry()));
            document.add(new Paragraph("Statut : " + dossier.getStatus()));
            document.add(new Paragraph("Date de création : " +
                    dossier.getCreatedAt().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))));

            if (dossier.getSubmittedAt() != null) {
                document.add(new Paragraph("Date de dépôt : " +
                        dossier.getSubmittedAt().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))));
            }

            document.add(new Paragraph("\n"));

            // Liste des documents
            document.add(new Paragraph("Documents du Dossier").setBold().setFontSize(14));

            Table table = new Table(4);
            table.addHeaderCell("Type");
            table.addHeaderCell("Fichier");
            table.addHeaderCell("Statut");
            table.addHeaderCell("Date");

            for (DossierDocument doc : documents) {
                table.addCell(doc.getDocumentType());
                table.addCell(doc.getFileName());
                table.addCell(doc.getStatus());
                table.addCell(doc.getUploadedAt().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
            }

            document.add(table);

            document.close();
        } catch (Exception e) {
            throw new RuntimeException("Erreur lors de la génération du PDF", e);
        }

        return baos.toByteArray();
    }
}