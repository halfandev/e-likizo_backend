
package com.abe.e_likizo.leave.service;

import com.abe.e_likizo.employee.entity.Employee;
import com.abe.e_likizo.leave.entity.LeaveApplication;
import com.abe.e_likizo.leave.entity.LeaveWorkflowHistory;
import com.abe.e_likizo.leave.enums.LeaveStatus;
import com.abe.e_likizo.leave.repository.LeaveApplicationRepository;
import com.abe.e_likizo.leave.repository.LeaveWorkflowHistoryRepository;

import com.lowagie.text.*;
import com.lowagie.text.pdf.PdfWriter;

import org.springframework.core.io.ClassPathResource;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class LeaveApprovalPdfService {

    private final LeaveApplicationRepository applicationRepository;
    private final LeaveWorkflowHistoryRepository historyRepository;

    public LeaveApprovalPdfService(
            LeaveApplicationRepository applicationRepository,
            LeaveWorkflowHistoryRepository historyRepository
    ) {
        this.applicationRepository = applicationRepository;
        this.historyRepository = historyRepository;
    }

    @Transactional(readOnly = true)
    public byte[] generateApprovalLetter(
            Long applicationId,
            Long employeeId
    ) {

        LeaveApplication application = applicationRepository
                .findById(applicationId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Leave application not found"
                        )
                );

        // Hakikisha ombi ni la Employee aliyeingia
        if (!application.getEmployee().getId().equals(employeeId)) {
            throw new AccessDeniedException(
                    "Huna ruhusa ya kupakua barua hii"
            );
        }

        // Barua inapatikana baada ya kibali cha mwisho tu
        if (application.getStatus() != LeaveStatus.COMPLETED) {
            throw new IllegalStateException(
                    "Leave application haijaidhinishwa kikamilifu"
            );
        }

        // Pata historia ya workflow
        List<LeaveWorkflowHistory> history =
                historyRepository
                        .findByLeaveApplication_IdOrderByActionAtAsc(
                                applicationId
                        );

        // Pata hatua ya mwisho iliyokamilisha uidhinishaji
        LeaveWorkflowHistory approval = history.stream()
                .filter(item ->
                        item.getNewStatus() == LeaveStatus.COMPLETED
                )
                .reduce((first, second) -> second)
                .orElseThrow(() ->
                        new IllegalStateException(
                                "Approval history haijapatikana"
                        )
                );

        Employee employee = application.getEmployee();

        try (ByteArrayOutputStream outputStream =
                     new ByteArrayOutputStream()) {

            Document document = new Document(
                    PageSize.A4, 55, 55, 35, 45
            );

            PdfWriter.getInstance(document, outputStream);

            document.open();

            // ==============================
            // 1. FONTS
            // ==============================

            Font normalFont = new Font(
                    Font.HELVETICA,
                    11,
                    Font.NORMAL
            );

            Font boldFont = new Font(
                    Font.HELVETICA,
                    11,
                    Font.BOLD
            );

            Font smallFont = new Font(
                    Font.HELVETICA,
                    9,
                    Font.NORMAL
            );

            Font subjectFont = new Font(
                    Font.HELVETICA,
                    11,
                    Font.BOLD
            );

            // ==============================
            // 2. OFFICIAL LETTERHEAD
            // ==============================

            ClassPathResource headerResource =
                    new ClassPathResource(
                            "images/police-letterhead.png"
                    );

            try (InputStream inputStream =
                         headerResource.getInputStream()) {

                Image header = Image.getInstance(
                        inputStream.readAllBytes()
                );

                header.scaleToFit(510, 105);
                header.setAlignment(Element.ALIGN_CENTER);

                document.add(header);
            }

            document.add(new Paragraph(" "));

            // ==============================
            // 3. OFFICE ADDRESS
            // ==============================

            Paragraph officeAddress = new Paragraph(
                    "Ofisi ya Mkuu wa Jeshi la Polisi,\n" +
                            "Makao Makuu ya Polisi,\n" +
                            "Mtaa wa Posta, S.L.P. 961,\n" +
                            "40483 DODOMA.",
                    smallFont
            );

            officeAddress.setAlignment(Element.ALIGN_RIGHT);
            document.add(officeAddress);

            document.add(new Paragraph(" "));

            // ==============================
            // 4. REFERENCE NUMBER AND DATE
            // ==============================

            DateTimeFormatter dateFormatter =
                    DateTimeFormatter.ofPattern("dd/MM/yyyy");

            LocalDate today = LocalDate.now();

            String referenceNumber =
                    "ELK/" + application.getId()
                            + "/" + today.getYear();

            Paragraph reference = new Paragraph(
                    "Kumb. Na: " + referenceNumber +
                            "\nTarehe: " + today.format(dateFormatter),
                    normalFont
            );

            reference.setAlignment(Element.ALIGN_RIGHT);
            document.add(reference);

            document.add(new Paragraph(" "));

            // ==============================
            // 5. EMPLOYEE INFORMATION
            // ==============================

            String fullName =
                    safe(employee.getFirstname()) + " " +
                            (employee.getMiddlename() != null
                                    ? employee.getMiddlename() + " "
                                    : "") +
                            safe(employee.getLastname());

            String rank = employee.getRank() != null
                    ? employee.getRank().name()
                    : "";

            Paragraph recipient = new Paragraph();

            recipient.add(new Phrase(
                    fullName.toUpperCase() + "\n",
                    boldFont
            ));

            recipient.add(new Phrase(
                    rank + "\n",
                    normalFont
            ));

            recipient.add(new Phrase(
                    "CHECK NUMBER: " +
                            safe(employee.getChecknumber()) + "\n",
                    normalFont
            ));

            recipient.add(new Phrase(
                    "FORCE NUMBER: " +
                            safe(employee.getForcenumber()) + "\n",
                    normalFont
            ));

            if (employee.getAdministrative() != null) {
                recipient.add(new Phrase(
                        employee.getAdministrative().getCode(),
                        normalFont
                ));
            }

            document.add(recipient);

            document.add(new Paragraph(" "));

            // ==============================
            // 6. SUBJECT
            // ==============================

            String subjectText =
                    "YAH: MAOMBI YA LIKIZO YA SIKU " +
                            application.getRequestedDays();

            Paragraph subject = new Paragraph(
                    subjectText,
                    subjectFont
            );

            document.add(subject);

            document.add(new Paragraph(" "));

            // ==============================
            // 7. LETTER BODY
            // ==============================

            Paragraph paragraph1 = new Paragraph(
                    "1. Rejea somo tajwa hapo juu.",
                    normalFont
            );

            paragraph1.setAlignment(Element.ALIGN_JUSTIFIED);
            paragraph1.setSpacingAfter(10);

            document.add(paragraph1);

            String leaveName =
                    application.getLeaveType().getName();

            String startDate =
                    application.getStartDate()
                            .format(dateFormatter);

            String endDate =
                    application.getEndDate()
                            .format(dateFormatter);

            Paragraph paragraph2 = new Paragraph(
                    "2. Umekubaliwa kwenda likizo ya aina ya " +
                            leaveName +
                            " ya siku " +
                            application.getRequestedDays() +
                            " kuanzia tarehe " +
                            startDate +
                            " hadi tarehe " +
                            endDate + ".",
                    normalFont
            );

            paragraph2.setAlignment(Element.ALIGN_JUSTIFIED);
            paragraph2.setSpacingAfter(10);

            document.add(paragraph2);

            Paragraph paragraph3 = new Paragraph(
                    "3. Likizo hii isomeke ni ya mwaka " +
                            application.getStartDate().getYear() + ".",
                    normalFont
            );

            paragraph3.setAlignment(Element.ALIGN_JUSTIFIED);
            paragraph3.setSpacingAfter(15);

            document.add(paragraph3);

            // ==============================
            // 8. APPROVAL RECORD
            // ==============================

            Paragraph approvalNote = new Paragraph(
                    "Kibali hiki kimerekodiwa na mfumo wa " +
                            "e-Likizo baada ya kukamilika kwa hatua zote " +
                            "za uidhinishaji.",
                    normalFont
            );

            approvalNote.setAlignment(Element.ALIGN_JUSTIFIED);
            approvalNote.setSpacingAfter(12);

            document.add(approvalNote);

            // ==============================
            // 9. SIGNATURE AREA
            // ==============================

            document.add(new Paragraph(" "));

            document.add(new Paragraph(
                    "........................................",
                    normalFont
            ));

            Paragraph signatureTitle = new Paragraph(
                    "SAHIHI YA AFISA MWENYE MAMLAKA",
                    boldFont
            );

            document.add(signatureTitle);

            document.add(new Paragraph(" "));

            // ==============================
            // 10. SYSTEM AUDIT INFORMATION
            // ==============================

            Paragraph audit = new Paragraph(
                    "Taarifa za mfumo: " +
                            "Aliyekamilisha hatua ya mwisho: " +
                            safe(approval.getActorEmail()) +
                            "\nHatua: " +
                            safe(approval.getAction()) +
                            "\nTarehe ya uidhinishaji: " +
                            (approval.getActionAt() != null
                                    ? approval.getActionAt().format(
                                    DateTimeFormatter.ofPattern(
                                            "dd/MM/yyyy HH:mm"
                                    )
                            )
                                    : ""),
                    smallFont
            );

            audit.setSpacingBefore(15);
            document.add(audit);

            // ==============================
            // 11. FOOTER
            // ==============================

            Paragraph footer = new Paragraph(
                    "\nHii ni barua iliyozalishwa na mfumo wa " +
                            "kielektroniki wa e-Likizo.",
                    smallFont
            );

            footer.setAlignment(Element.ALIGN_CENTER);

            document.add(footer);

            document.close();

            return outputStream.toByteArray();

        } catch (Exception exception) {

            throw new RuntimeException(
                    "Imeshindikana kutengeneza PDF ya kibali cha likizo",
                    exception
            );
        }
    }

    private String safe(Object value) {
        return value != null ? value.toString() : "";
    }
}
