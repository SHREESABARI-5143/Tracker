package com.chronotrack.controller;

import com.chronotrack.dto.*;
import com.chronotrack.security.CustomUserDetails;
import com.chronotrack.service.ReportService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

/**
 * REST controller serving productivity reports and analytics.
 */
@RestController
@RequestMapping("/api/reports")
public class ReportController {

    private final ReportService reportService;

    public ReportController(ReportService reportService) {
        this.reportService = reportService;
    }

    /**
     * Get daily report for a specific date (defaults to today).
     */
    @GetMapping("/daily")
    public ResponseEntity<ApiResponse<DailyReportResponse>> getDailyReport(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        DailyReportResponse report = reportService.getDailyReport(userDetails.getId(), date);
        return ResponseEntity.ok(ApiResponse.success(report));
    }

    /**
     * Get weekly report adhering strictly to Monday to Sunday definition.
     */
    @GetMapping("/weekly")
    public ResponseEntity<ApiResponse<WeeklyReportResponse>> getWeeklyReport(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate) {
        WeeklyReportResponse report = reportService.getWeeklyReport(userDetails.getId(), startDate);
        return ResponseEntity.ok(ApiResponse.success(report));
    }

    /**
     * Get monthly productivity analytics and trends.
     */
    @GetMapping("/monthly")
    public ResponseEntity<ApiResponse<MonthlyReportResponse>> getMonthlyReport(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) Integer month) {
        MonthlyReportResponse report = reportService.getMonthlyReport(userDetails.getId(), year, month);
        return ResponseEntity.ok(ApiResponse.success(report));
    }

    /**
     * Get annual productivity summary.
     */
    @GetMapping("/yearly")
    public ResponseEntity<ApiResponse<YearlyReportResponse>> getYearlyReport(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam(required = false) Integer year) {
        YearlyReportResponse report = reportService.getYearlyReport(userDetails.getId(), year);
        return ResponseEntity.ok(ApiResponse.success(report));
    }

    /**
     * Export reports and activities to downloadable CSV format.
     */
    @GetMapping("/export/csv")
    public ResponseEntity<byte[]> exportCsv(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam(required = false, defaultValue = "weekly") String type,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        byte[] csvData = reportService.exportReportToCsv(userDetails.getId(), type, startDate, endDate);
        return ResponseEntity.ok()
                .header(org.springframework.http.HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"chronotrack_report.csv\"")
                .header(org.springframework.http.HttpHeaders.CONTENT_TYPE, "text/csv; charset=UTF-8")
                .body(csvData);
    }
}
