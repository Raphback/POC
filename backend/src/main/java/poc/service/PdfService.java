package poc.service;

import com.lowagie.text.*;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import org.springframework.stereotype.Service;
import poc.model.Affectation;
import poc.model.Etudiant;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class PdfService {

    public ByteArrayInputStream generateTickets(List<Affectation> affectations) {
        Document document = new Document();
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        try {
            PdfWriter.getInstance(document, out);
            document.open();

            // Group by Student
            Map<Etudiant, List<Affectation>> byStudent = affectations.stream()
                    .collect(Collectors.groupingBy(Affectation::getEtudiant));

            for (Map.Entry<Etudiant, List<Affectation>> entry : byStudent.entrySet()) {
                Etudiant etudiant = entry.getKey();
                List<Affectation> studentAffectations = entry.getValue();

                addStudentTicket(document, etudiant, studentAffectations);
                document.newPage();
            }

            document.close();

        } catch (DocumentException e) {
            e.printStackTrace();
        }

        return new ByteArrayInputStream(out.toByteArray());
    }

    private void addStudentTicket(Document document, Etudiant etudiant, List<Affectation> affectations) throws DocumentException {
        Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18);
        Font normalFont = FontFactory.getFont(FontFactory.HELVETICA, 12);

        Paragraph title = new Paragraph("FESUP 2026 - Convocation", titleFont);
        title.setAlignment(Element.ALIGN_CENTER);
        document.add(title);

        document.add(new Paragraph("\n"));

        document.add(new Paragraph("Nom : " + etudiant.getNom().toUpperCase() + " " + etudiant.getPrenom(), normalFont));
        document.add(new Paragraph("Matricule : " + etudiant.getMatriculeCsv(), normalFont));
        if (etudiant.getLycee() != null) {
            document.add(new Paragraph("Lycée : " + etudiant.getLycee().getNom(), normalFont));
        }

        document.add(new Paragraph("\nVotre parcours :", normalFont));
        document.add(new Paragraph("\n"));

        PdfPTable table = new PdfPTable(3);
        table.setWidthPercentage(100);
        table.setWidths(new int[]{1, 3, 1});

        addTableHeader(table, "Type");
        addTableHeader(table, "Activité");
        addTableHeader(table, "Salle");

        for (Affectation aff : affectations) {
            table.addCell(aff.getActivite().getType().toString());
            table.addCell(aff.getActivite().getTitre());
            table.addCell("Salle X"); // Placeholder for Room
        }

        document.add(table);
    }

    private void addTableHeader(PdfPTable table, String headerTitle) {
        PdfPCell header = new PdfPCell();
        header.setBackgroundColor(java.awt.Color.LIGHT_GRAY);
        header.setPhrase(new Phrase(headerTitle));
        table.addCell(header);
    }
}
