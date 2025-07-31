package inu.codin.codinticketingapi.domain.admin.service;

import inu.codin.codinticketingapi.domain.admin.dto.response.ExcelResponse;
import inu.codin.codinticketingapi.domain.admin.entity.Event;
import inu.codin.codinticketingapi.domain.admin.exception.ExcelErrorCode;
import inu.codin.codinticketingapi.domain.admin.exception.ExcelException;
import inu.codin.codinticketingapi.domain.ticketing.entity.Participation;
import inu.codin.codinticketingapi.domain.ticketing.exception.TicketingErrorCode;
import inu.codin.codinticketingapi.domain.ticketing.exception.TicketingException;
import inu.codin.codinticketingapi.domain.ticketing.repository.EventRepository;
import inu.codin.codinticketingapi.domain.ticketing.repository.ParticipationRepository;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ExcelService {
    private final EventRepository eventRepository;
    private final ParticipationRepository participationRepository;

    private final static int ROW_START = 1;
    private final static String SHEET_NAME_SUFFIX = "_참가자";
    private final static String[] HEADERS = {"사용자 ID", "이름", "학과", "학번", "경품 수령 상태", "교환권 번호"};

    @Transactional(readOnly = true)
    public ExcelResponse getExcel(Long eventId) {
        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            String fileName = createSheet(workbook, eventId);
            workbook.write(out);

            return ExcelResponse.of(out.toByteArray(), fileName);
        } catch (IOException e) {
            throw new ExcelException(ExcelErrorCode.EXCEL_GENERATION_FAILED);
        }
    }

    private String createSheet(Workbook workbook, Long eventId) {
        Event event = getEvent(eventId);
        String fileName = event.getTitle() + SHEET_NAME_SUFFIX;
        Sheet sheet = workbook.createSheet(fileName);

        createHeaderRow(sheet);

        List<Participation> participationList = getParticipation(event.getId());
        populateDataRows(sheet, participationList);

        autoSizeAllColumns(sheet);

        return encodeFileName(fileName);
    }

    private Event getEvent(Long eventId) {
        return eventRepository.findById(eventId).orElseThrow(() -> new TicketingException(TicketingErrorCode.EVENT_NOT_FOUND));
    }

    private String encodeFileName(String filename) {

        return URLEncoder.encode(filename, StandardCharsets.UTF_8).replaceAll("\\+", "%20");
    }

    private void createHeaderRow(Sheet sheet) {
        Row headerRow = sheet.createRow(0);

        for (int i = 0; i < HEADERS.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(HEADERS[i]);
        }
    }

    private List<Participation> getParticipation(Long eventId) {
        List<Participation> participationList = participationRepository.findAllByEvent_Id(eventId);

        if (participationList.isEmpty()) {
            throw new TicketingException(TicketingErrorCode.PARTICIPATION_NOT_FOUND);
        }

        return participationList;
    }

    private void populateDataRows(Sheet sheet, List<Participation> participationList) {
        int rowNum = ROW_START;

        for (Participation participation : participationList) {
            Row row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue(participation.getUserId());
            row.createCell(1).setCellValue(participation.getName());
            row.createCell(2).setCellValue(participation.getDepartment().name());
            row.createCell(3).setCellValue(participation.getStudentId());
            row.createCell(4).setCellValue(participation.getStatus().name());
            row.createCell(5).setCellValue(participation.getTicketNumber());
        }
    }

    private void autoSizeAllColumns(Sheet sheet) {
        for (int i = 0; i < HEADERS.length; i++) {
            sheet.autoSizeColumn(i);
        }
    }
}
