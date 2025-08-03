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
    private final static String NO_PARTICIPANTS = "현재 이벤트에 참가자가 존재하지 않습니다.";
    private final static String SHEET_NAME_SUFFIX = "_참가자";
    private final static String UNKNOWN = "UNKNOWN";
    private final static String[] HEADERS = {"사용자 ID", "이름", "학과", "학번", "경품 수령 상태", "교환권 번호", "서명"};

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

    /**
     * 주어진 파일명을 URL 또는 HTTP 헤더에서 안전하게 사용할 수 있도록 인코딩합니다.
     * <p>
     * URL 인코딩은 파일명에 포함된 공백과 같은 특수 문자들이
     * URL 또는 HTTP 헤더에서 사용될 때 발생할 수 있는 문제를 방지하기 위해 올바르게 이스케이프되도록 합니다.
     * 기본적으로 URLEncoder는 공백을 '+'로 인코딩하지만, 이 메서드는 '+'를 '%20'으로 대체하여
     * 파일명에 대한 표준 URL 인코딩 규칙을 따르도록 합니다.
     *
     * @param filename 인코딩할 원본 파일명.
     * @return 공백이 '%20'으로 대체된 URL 인코딩된 파일명.
     */
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


        return participationRepository.findAllByEvent_Id(eventId);
    }

    private void populateDataRows(Sheet sheet, List<Participation> participationList) {
        int rowNum = ROW_START;

        if (participationList.isEmpty()) {
            Row row = sheet.createRow(rowNum);
            row.createCell(0).setCellValue(NO_PARTICIPANTS);

            return;
        }

        for (Participation participation : participationList) {
            Row row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue(participation.getUserId());
            row.createCell(1).setCellValue(participation.getName());
            row.createCell(2).setCellValue(participation.getDepartment() != null ? participation.getDepartment().name() : UNKNOWN);
            row.createCell(3).setCellValue(participation.getStudentId());
            row.createCell(4).setCellValue(participation.getStatus() != null ? participation.getStatus().name() : UNKNOWN);
            row.createCell(5).setCellValue(participation.getTicketNumber());
        }
    }

    private void autoSizeAllColumns(Sheet sheet) {
        for (int i = 0; i < HEADERS.length; i++) {
            sheet.autoSizeColumn(i);
        }
    }
}
