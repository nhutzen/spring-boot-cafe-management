package vn.edu.fpt.cafemanagement.services;

import jakarta.servlet.http.HttpServletResponse;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import vn.edu.fpt.cafemanagement.entities.Order;

import java.io.IOException;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.List;
import java.util.Map;

@Service
public class ExcelExportService {

    public void generateSalesReport(HttpServletResponse response, LocalDate startDate, LocalDate endDate, Map<String, Object> salesSummary, List<Order> orders) throws IOException {
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Sales Report");

        // --- TẠO CÁC STYLE CHO BẢNG TÍNH ---

        // Style cho tiêu đề chính
        Font titleFont = workbook.createFont();
        titleFont.setBold(true);
        titleFont.setFontHeightInPoints((short) 16);
        titleFont.setColor(IndexedColors.DARK_BLUE.getIndex());
        CellStyle titleStyle = workbook.createCellStyle();
        titleStyle.setFont(titleFont);
        titleStyle.setAlignment(HorizontalAlignment.CENTER);

        // Style cho tiêu đề các mục (ví dụ: "Summary")
        Font sectionHeaderFont = workbook.createFont();
        sectionHeaderFont.setBold(true);
        sectionHeaderFont.setFontHeightInPoints((short) 12);
        CellStyle sectionHeaderStyle = workbook.createCellStyle();
        sectionHeaderStyle.setFont(sectionHeaderFont);
        sectionHeaderStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        sectionHeaderStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        sectionHeaderStyle.setBorderBottom(BorderStyle.THIN);

        // Style cho tiêu đề của bảng (ví dụ: "Order ID")
        Font tableHeaderFont = workbook.createFont();
        tableHeaderFont.setBold(true);
        tableHeaderFont.setColor(IndexedColors.WHITE.getIndex());
        CellStyle tableHeaderStyle = workbook.createCellStyle();
        tableHeaderStyle.setFont(tableHeaderFont);
        tableHeaderStyle.setFillForegroundColor(IndexedColors.ROYAL_BLUE.getIndex());
        tableHeaderStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        tableHeaderStyle.setAlignment(HorizontalAlignment.CENTER);
        tableHeaderStyle.setVerticalAlignment(VerticalAlignment.CENTER);
        tableHeaderStyle.setBorderTop(BorderStyle.THIN);
        tableHeaderStyle.setBorderBottom(BorderStyle.THIN);
        tableHeaderStyle.setBorderLeft(BorderStyle.THIN);
        tableHeaderStyle.setBorderRight(BorderStyle.THIN);

        // Style cơ bản cho ô dữ liệu (có viền)
        CellStyle baseDataCellStyle = createBorderedCellStyle(workbook);

        // Style cho các dòng xen kẽ (màu nền nhạt)
        CellStyle alternatingDataCellStyle = createBorderedCellStyle(workbook);
        alternatingDataCellStyle.setFillForegroundColor(IndexedColors.LIGHT_TURQUOISE.getIndex());
        alternatingDataCellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

        // Style cho ô tiền tệ
        DataFormat currencyFormat = workbook.createDataFormat();
        CellStyle currencyCellStyle = createBorderedCellStyle(workbook);
        currencyCellStyle.setDataFormat(currencyFormat.getFormat("\"VND\" #,##0"));

        CellStyle alternatingCurrencyCellStyle = createBorderedCellStyle(workbook);
        alternatingCurrencyCellStyle.cloneStyleFrom(alternatingDataCellStyle);
        alternatingCurrencyCellStyle.setDataFormat(currencyFormat.getFormat("\"VND\" #,##0"));

        // Style cho ô ngày tháng
        CellStyle dateCellStyle = createBorderedCellStyle(workbook);
        dateCellStyle.setDataFormat(workbook.getCreationHelper().createDataFormat().getFormat("dd-MM-yyyy HH:mm"));

        CellStyle alternatingDateCellStyle = createBorderedCellStyle(workbook);
        alternatingDateCellStyle.cloneStyleFrom(alternatingDataCellStyle);
        alternatingDateCellStyle.setDataFormat(workbook.getCreationHelper().createDataFormat().getFormat("dd-MM-yyyy HH:mm"));

        // Style cho các nhãn trong phần tóm tắt
        CellStyle summaryLabelStyle = workbook.createCellStyle();
        Font summaryFont = workbook.createFont();
        summaryFont.setBold(true);
        summaryLabelStyle.setFont(summaryFont);

        // --- TIÊU ĐỀ BÁO CÁO ---
        int rowNum = 0;
        Row titleRow = sheet.createRow(rowNum++);
        titleRow.setHeightInPoints(22);
        Cell titleCell = titleRow.createCell(0);
        titleCell.setCellValue("Sales Report");
        titleCell.setCellStyle(titleStyle);
        sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 6)); // Gộp 7 cột

        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        Row periodRow = sheet.createRow(rowNum++);
        periodRow.createCell(0).setCellValue("Period:");
        periodRow.createCell(1).setCellValue(startDate.format(dateFormatter) + " - " + endDate.format(dateFormatter));
        rowNum++; // Thêm một dòng trống

        // --- PHẦN TÓM TẮT (SUMMARY) ---
        Row summaryTitleRow = sheet.createRow(rowNum++);
        summaryTitleRow.setHeightInPoints(18);
        Cell summaryTitleCell = summaryTitleRow.createCell(0);
        summaryTitleCell.setCellValue("Summary");
        summaryTitleCell.setCellStyle(sectionHeaderStyle);
        sheet.addMergedRegion(new CellRangeAddress(rowNum - 1, rowNum - 1, 0, 6));

        createSummaryRow(sheet.createRow(rowNum++), "Total Revenue:", salesSummary.get("totalRevenue"), summaryLabelStyle, currencyCellStyle);
        createSummaryRow(sheet.createRow(rowNum++), "Total Orders:", salesSummary.get("totalOrders"), summaryLabelStyle, null);
        createSummaryRow(sheet.createRow(rowNum++), "Average Order Value:", salesSummary.get("averageOrderValue"), summaryLabelStyle, currencyCellStyle);
        createSummaryRow(sheet.createRow(rowNum++), "Vouchers Used:", salesSummary.get("vouchersUsed"), summaryLabelStyle, null);
        rowNum += 2; // Thêm khoảng trắng

        // --- TIÊU ĐỀ BẢNG GIAO DỊCH CHI TIẾT ---
        Row detailsTitleRow = sheet.createRow(rowNum++);
        detailsTitleRow.setHeightInPoints(18);
        Cell detailsTitleCell = detailsTitleRow.createCell(0);
        detailsTitleCell.setCellValue("Detailed Transactions");
        detailsTitleCell.setCellStyle(sectionHeaderStyle);
        sheet.addMergedRegion(new CellRangeAddress(rowNum - 1, rowNum - 1, 0, 6));

        Row detailsHeaderRow = sheet.createRow(rowNum++);
        detailsHeaderRow.setHeightInPoints(20);
        String[] columns = {"Order ID", "Date", "Customer", "Total Amount", "Points Used", "Voucher", "Status"};
        for (int i = 0; i < columns.length; i++) {
            Cell cell = detailsHeaderRow.createCell(i);
            cell.setCellValue(columns[i]);
            cell.setCellStyle(tableHeaderStyle);
        }

        // --- ĐỔ DỮ LIỆU VÀO BẢNG ---
        int dataStartRowNum = rowNum;
        for (Order order : orders) {
            Row row = sheet.createRow(rowNum++);
            boolean isEvenRow = (rowNum - dataStartRowNum) % 2 == 0;

            CellStyle currentDataStyle = isEvenRow ? alternatingDataCellStyle : baseDataCellStyle;
            CellStyle currentCurrencyStyle = isEvenRow ? alternatingCurrencyCellStyle : currencyCellStyle;
            CellStyle currentDateStyle = isEvenRow ? alternatingDateCellStyle : dateCellStyle;

            createCell(row, 0, "#" + order.getOrderId(), currentDataStyle);
            Date orderDate = Date.from(order.getCreatedAt().atZone(ZoneId.systemDefault()).toInstant());
            createCell(row, 1, orderDate, currentDateStyle);
            createCell(row, 2, order.getCustomer() != null ? order.getCustomer().getName() : "Guest", currentDataStyle);
            createCell(row, 3, order.getTotalPrice(), currentCurrencyStyle);
            createCell(row, 4, order.getPointsUsed(), currentDataStyle);
            createCell(row, 5, order.getVoucher() != null ? order.getVoucher().getVoucherName() : "-", currentDataStyle);
            createCell(row, 6, order.getStatus(), currentDataStyle);
        }

        // --- TỰ ĐỘNG ĐIỀU CHỈNH KÍCH THƯỚC CỘT ---
        for (int i = 0; i < columns.length; i++) {
            sheet.autoSizeColumn(i);
        }

        // --- GHI FILE VÀO RESPONSE ---
        workbook.write(response.getOutputStream());
        workbook.close();
    }

    // Hàm tiện ích để tạo style có viền
    private CellStyle createBorderedCellStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        return style;
    }

    // Hàm tiện ích để tạo một ô
    private void createCell(Row row, int column, Object value, CellStyle style) {
        Cell cell = row.createCell(column);
        if (value instanceof String) {
            cell.setCellValue((String) value);
        } else if (value instanceof Integer) {
            cell.setCellValue((Integer) value);
        } else if (value instanceof Double) {
            cell.setCellValue((Double) value);
        } else if (value instanceof Long) {
            cell.setCellValue((Long) value);
        } else if (value instanceof Date) {
            cell.setCellValue((Date) value);
        }
        cell.setCellStyle(style);
    }

    // Hàm tiện ích để tạo một dòng trong phần tóm tắt
    private void createSummaryRow(Row row, String label, Object value, CellStyle labelStyle, CellStyle valueStyle) {
        Cell labelCell = row.createCell(0);
        labelCell.setCellValue(label);
        labelCell.setCellStyle(labelStyle);

        if (valueStyle != null) {
            createCell(row, 1, value, valueStyle);
        } else {
            Cell valueCell = row.createCell(1);
            if (value instanceof Number) {
                valueCell.setCellValue(((Number) value).doubleValue());
            }
        }
    }
}
