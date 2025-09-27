package com.johnp.util;

import com.johnp.bean.MethodInfo;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class FileExportUtil {

    // ** Reference: Export Data to Excel in Java
    // *? https://www.codejava.net/coding/java-code-example-to-export-data-from-database-to-excel-file
    // */

    public static void xlsExport(String fileName, List<Map.Entry<String, MethodInfo>> dataList) throws IOException {
        try (XSSFWorkbook workbook = new XSSFWorkbook()) {
            XSSFSheet sheet = workbook.createSheet("caluclatedSuspicion");

            writeHeaderLine(sheet);
            writeDataLines(dataList, sheet);
            beautifyColumns(sheet);

            FileOutputStream outputStream = new FileOutputStream(fileName);
            workbook.write(outputStream);
        }
    }

    public static void csvExport(String fileName, List<Map.Entry<String, MethodInfo>> dataList) throws IOException {
        try (FileWriter writer = new FileWriter(fileName)) {
            // Write header
            writer.append("Method Name,Tarantula Suspicion,SBI Suspicion,Jaccard Suspicion,Ochai Suspicion\n");

            // Write data rows
            for (Map.Entry<String, MethodInfo> entry : dataList) {
                MethodInfo info = entry.getValue();
                writer.append(entry.getKey())
                        .append(',')
                        .append(String.valueOf(info.getSuspiciousnessTarantula()))
                        .append(',')
                        .append(String.valueOf(info.getSuspiciousnessSbi()))
                        .append(',')
                        .append(String.valueOf(info.getSuspiciousnessJaccard()))
                        .append(',')
                        .append(String.valueOf(info.getSuspiciousnessOchiai()))
                        .append('\n');
            }
        }
    }

    private static void beautifyColumns(XSSFSheet sheet) {

        // *? REF: https://stackoverflow.com/a/59718764
        // ** Set Fixed Width for Column
        sheet.setColumnWidth(0, 90 * 256);

        // *? REF: https://www.baeldung.com/java-apache-poi-expand-columns
        // ** Add Auto Width on All Columns
        sheet.autoSizeColumn(1);
        sheet.autoSizeColumn(2);
        sheet.autoSizeColumn(3);
        sheet.autoSizeColumn(4);
        sheet.createFreezePane(0, 1);

        // *? REF: https://stackoverflow.com/questions/77938769/how-to-add-filters-for-specific-columns-using-apache-poi
        sheet.setAutoFilter(CellRangeAddress.valueOf("A1:E1"));
    }

    private static void writeHeaderLine(XSSFSheet sheet) {

        Row headerRow = sheet.createRow(0);

        Cell headerCell = headerRow.createCell(0);
        headerCell.setCellValue("Method Name");

        headerCell = headerRow.createCell(1);
        headerCell.setCellValue("Tarantula Suspicion");

        headerCell = headerRow.createCell(2);
        headerCell.setCellValue("SBI Suspicion");

        headerCell = headerRow.createCell(3);
        headerCell.setCellValue("Jaccard Suspicion");

        headerCell = headerRow.createCell(4);
        headerCell.setCellValue("Ochai Suspicion");
    }

    private static void writeDataLines(List<Map.Entry<String, MethodInfo>> dataList,
                                       XSSFSheet sheet) {
        AtomicInteger rowCount = new AtomicInteger(1);

        dataList.forEach(entry -> {
            double tarantulaSuspicion = entry.getValue().getSuspiciousnessTarantula();
            double sbiSuspicion = entry.getValue().getSuspiciousnessSbi();
            double jaccardSuspicion = entry.getValue().getSuspiciousnessJaccard();
            double ochaiSuspicion = entry.getValue().getSuspiciousnessOchiai();

            Row row = sheet.createRow(rowCount.getAndIncrement());

            int columnCount = 0;
            Cell cell = row.createCell(columnCount++);
            cell.setCellValue(entry.getKey());

            cell = row.createCell(columnCount++);
            cell.setCellValue(tarantulaSuspicion);

            cell = row.createCell(columnCount++);
            cell.setCellValue(sbiSuspicion);

            cell = row.createCell(columnCount++);
            cell.setCellValue(jaccardSuspicion);

            cell = row.createCell(columnCount);
            cell.setCellValue(ochaiSuspicion);

        });

    }

}