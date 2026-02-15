package com.brick.billing.controller;

import java.io.ByteArrayOutputStream;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;

import javax.sql.DataSource;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.brick.billing.repository.BookingRepository;
import com.brick.billing.repository.ReportRepository;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final DataSource dataSource;
    private final BookingRepository bookingRepo;
    private final ReportRepository reportRepo;

    // -------------------------------
    // 1. DATABASE USAGE
    // -------------------------------
    @GetMapping("/db-usage")
    public Map<String, Object> getDbUsage() throws Exception {

        try (Connection conn = dataSource.getConnection();
             Statement st = conn.createStatement()) {

            // Neon/Postgres DB size
            ResultSet rs = st.executeQuery(
                "SELECT pg_database_size(current_database())"
            );
            rs.next();
            long sizeBytes = rs.getLong(1);

            double sizeMB = sizeBytes / (1024.0 * 1024.0);

            // Assume 512 MB logical cap (adjust if needed)
            double maxMB = 512;

            double percent = (sizeMB / maxMB) * 100;

            Map<String, Object> map = new HashMap<>();
            map.put("usedPercent", percent);
            map.put("dbSizeMB", sizeMB);
            map.put("maxMB", maxMB);

            return map;
        }
    }

    // -------------------------------
    // 2. EXPORT TO EXCEL
    // -------------------------------
    @GetMapping("/export")
    public ResponseEntity<byte[]> exportExcel() throws Exception {

        Workbook workbook = new XSSFWorkbook();

        // ---------- BOOKINGS SHEET ----------
        Sheet bookingSheet = workbook.createSheet("Bookings");

        Row header = bookingSheet.createRow(0);
        header.createCell(0).setCellValue("ID");
        header.createCell(1).setCellValue("Customer");
        header.createCell(2).setCellValue("Mobile");
        header.createCell(3).setCellValue("Quantity");
        header.createCell(4).setCellValue("Status");
        header.createCell(5).setCellValue("Created");

        int rowIdx = 1;

        try (Connection conn = dataSource.getConnection();
            Statement st = conn.createStatement()) {

            ResultSet rs = st.executeQuery("""
                SELECT b.id,
                    c.name,
                    c.mobile,
                    b.quantity,
                    b.status,
                    b.created_at
                FROM booking b
                JOIN customer c ON c.id = b.customer_id
                ORDER BY b.created_at DESC
            """);

            while (rs.next()) {
                Row r = bookingSheet.createRow(rowIdx++);
                r.createCell(0).setCellValue(rs.getLong(1));
                r.createCell(1).setCellValue(rs.getString(2));
                r.createCell(2).setCellValue(rs.getString(3));
                r.createCell(3).setCellValue(rs.getInt(4));
                r.createCell(4).setCellValue(rs.getString(5));
                r.createCell(5).setCellValue(rs.getTimestamp(6).toString());
            }
        }

        // ---------- REPORT SHEET ----------
        Sheet reportSheet = workbook.createSheet("Reports");

        Row rh = reportSheet.createRow(0);
        rh.createCell(0).setCellValue("Report ID");
        rh.createCell(1).setCellValue("Booking ID");
        rh.createCell(2).setCellValue("Status");
        rh.createCell(3).setCellValue("Final Total");

        int rr = 1;

        try (Connection conn = dataSource.getConnection();
            Statement st = conn.createStatement()) {

            ResultSet rs = st.executeQuery("""
                SELECT id, booking_id, status, final_total
                FROM report
                ORDER BY id
            """);

            while (rs.next()) {
                Row row = reportSheet.createRow(rr++);
                row.createCell(0).setCellValue(rs.getLong(1));
                row.createCell(1).setCellValue(rs.getLong(2));
                row.createCell(2).setCellValue(rs.getString(3));
                row.createCell(3).setCellValue(rs.getDouble(4));
            }
        }

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        workbook.write(out);
        workbook.close();

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=backup.xlsx")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(out.toByteArray());
    }


    // -------------------------------
    // 3. TRUNCATE DATA
    // -------------------------------
    @DeleteMapping("/truncate")
    public void truncateAll() throws Exception {

        try (Connection conn = dataSource.getConnection();
             Statement st = conn.createStatement()) {

            // Order matters (FK constraints)
            // Child tables first
            st.execute("TRUNCATE TABLE report_item RESTART IDENTITY CASCADE");

            // Then reports
            st.execute("TRUNCATE TABLE report RESTART IDENTITY CASCADE");

            // Then bookings
            st.execute("TRUNCATE TABLE booking RESTART IDENTITY CASCADE");

            // Finally customers (no one depends on it now)
            st.execute("TRUNCATE TABLE customer RESTART IDENTITY CASCADE");
        }
    }
}
