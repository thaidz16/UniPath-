package com.thai.unipath.service;

import com.thai.unipath.model.Diem;
import com.thai.unipath.model.ThiSinh;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class ExcelServiceImpl implements IDataService {

    @Override
    public List<ThiSinh> readData(String filePath) {
        List<ThiSinh> list = new ArrayList<>();
        try (FileInputStream fis = new FileInputStream(new File(filePath));
             Workbook workbook = WorkbookFactory.create(fis)) {

            Sheet sheet = workbook.getSheetAt(0);
            Row headerRow = sheet.getRow(0);

            int offset = 0;
            if (headerRow != null && getCellValue(headerRow.getCell(0)).equalsIgnoreCase("STT")) {
                offset = 1;
            }

            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null || getCellValue(row.getCell(offset)).isEmpty()) continue;

                ThiSinh ts = new ThiSinh();

                // ĐỌC DỮ LIỆU DỰA TRÊN OFFSET (0 HOẶC 1)
                ts.setSbd(getCellValue(row.getCell(0 + offset)));      // Cột SBD
                ts.setHoTen(getCellValue(row.getCell(1 + offset)));    // Cột Họ Tên

                // Đọc Ngày sinh (Cột 2 + offset)
                try {
                    String dateStr = getCellValue(row.getCell(2 + offset)).trim();
                    if (!dateStr.isEmpty()) {
                        if (dateStr.contains("-")) {
                            ts.setNgaySinh(LocalDate.parse(dateStr));
                        } else if (dateStr.contains("/")) {
                            java.time.format.DateTimeFormatter formatter = java.time.format.DateTimeFormatter.ofPattern("d/M/yyyy");
                            ts.setNgaySinh(LocalDate.parse(dateStr, formatter));
                        } else {
                            ts.setNgaySinh(null);
                        }
                    }
                } catch (Exception e) {
                    ts.setNgaySinh(null);
                }

                // Đọc Quê quán (Cột 3 + offset)
                ts.setQueQuan(getCellValue(row.getCell(3 + offset)));

                // ĐỌC ĐIỂM SỐ (Bắt đầu từ cột 4 + offset)
                Diem d = new Diem();
                d.setToan(getNumericValue(row.getCell(4 + offset)));   // Toán
                d.setVan(getNumericValue(row.getCell(5 + offset)));    // Văn
                d.setAnh(getNumericValue(row.getCell(6 + offset)));    // Ngoại ngữ
                d.setSu(getNumericValue(row.getCell(7 + offset)));     // Sử
                d.setLy(getNumericValue(row.getCell(8 + offset)));     // Lý
                d.setHoa(getNumericValue(row.getCell(9 + offset)));    // Hóa
                d.setSinh(getNumericValue(row.getCell(10 + offset)));  // Sinh
                d.setDia(getNumericValue(row.getCell(11 + offset)));   // Địa
                d.setGdkt(getNumericValue(row.getCell(12 + offset)));  // GDKT
                d.setTin(getNumericValue(row.getCell(13 + offset)));   // Tin
                d.setCongNghe(getNumericValue(row.getCell(14 + offset))); // Công nghệ

                ts.setDiemThi(d);
                list.add(ts);
            }
        } catch (Exception e) {
            System.err.println("Lỗi đọc file Excel: " + e.getMessage());
        }
        return list;
    }

    @Override
    public void writeData(List<ThiSinh> list, String filePath) {
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Data");

            // LUÔN GHI CÓ CỘT STT CHO CHUYÊN NGHIỆP
            Row header = sheet.createRow(0);
            String[] cols = {"STT", "SBD", "Họ Tên", "Ngày Sinh", "Quê Quán", "Toán", "Ngữ văn", "Ngoại ngữ", "Lịch sử", "Vật lý", "Hóa học", "Sinh học", "Địa lý", "Giáo dục k", "Tin học", "Công nghệ"};

            for (int i = 0; i < cols.length; i++) {
                Cell cell = header.createCell(i);
                cell.setCellValue(cols[i]);
                CellStyle style = workbook.createCellStyle();
                Font font = workbook.createFont();
                font.setBold(true);
                style.setFont(font);
                cell.setCellStyle(style);
            }

            int rIdx = 1;
            for (ThiSinh ts : list) {
                Row r = sheet.createRow(rIdx);
                r.createCell(0).setCellValue(rIdx);
                r.createCell(1).setCellValue(ts.getSbd());
                r.createCell(2).setCellValue(ts.getHoTen());
                if (ts.getNgaySinh() != null) {
                    r.createCell(3).setCellValue(ts.getNgaySinh().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
                }
                r.createCell(4).setCellValue(ts.getQueQuan());

                Diem d = ts.getDiemThi();
                if (d != null) {
                    r.createCell(5).setCellValue(d.getToan() != null ? d.getToan() : 0);
                    r.createCell(6).setCellValue(d.getVan() != null ? d.getVan() : 0);
                    r.createCell(7).setCellValue(d.getAnh() != null ? d.getAnh() : 0);
                    r.createCell(8).setCellValue(d.getSu() != null ? d.getSu() : 0);
                    r.createCell(9).setCellValue(d.getLy() != null ? d.getLy() : 0);
                    r.createCell(10).setCellValue(d.getHoa() != null ? d.getHoa() : 0);
                    r.createCell(11).setCellValue(d.getSinh() != null ? d.getSinh() : 0);
                    r.createCell(12).setCellValue(d.getDia() != null ? d.getDia() : 0);
                    r.createCell(13).setCellValue(d.getGdkt() != null ? d.getGdkt() : 0);
                    r.createCell(14).setCellValue(d.getTin() != null ? d.getTin() : 0);
                    r.createCell(15).setCellValue(d.getCongNghe() != null ? d.getCongNghe() : 0);
                }
                rIdx++;
            }

            for (int i = 0; i < cols.length; i++) sheet.autoSizeColumn(i);

            try (FileOutputStream out = new FileOutputStream(filePath)) {
                workbook.write(out);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public ThiSinh searchBySBD(List<ThiSinh> list, String sbd) {
        if (list == null || sbd == null) return null;
        return list.stream().filter(ts -> sbd.equals(ts.getSbd())).findFirst().orElse(null);
    }

    private String getCellValue(Cell cell) {
        if (cell == null) return "";
        DataFormatter df = new DataFormatter();
        return df.formatCellValue(cell).trim();
    }

    private Double getNumericValue(Cell cell) {
        if (cell == null) return 0.0;
        try {
            return cell.getNumericCellValue();
        } catch (Exception e) {
            return 0.0;
        }
    }
}