package com.chronotrack.report;

import com.chronotrack.dto.ActivityRequest;
import com.chronotrack.dto.RegisterRequest;
import com.chronotrack.entity.Category;
import com.chronotrack.repository.ActivityRepository;
import com.chronotrack.repository.CategoryRepository;
import com.chronotrack.repository.UserRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class ReportIntegrationTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private ActivityRepository activityRepository;

    private String token;
    private Long userId;
    private Long categoryId;

    @BeforeEach
    void setUp() throws Exception {
        activityRepository.deleteAll();
        categoryRepository.deleteAll();
        userRepository.deleteAll();

        RegisterRequest register = new RegisterRequest("Report User", "report@example.com", "Password@123");
        register.setTimezone("UTC");
        register.setPreferredTimeFormat("24h");

        MvcResult res = mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(register)))
                .andExpect(status().isCreated())
                .andReturn();

        JsonNode data = objectMapper.readTree(res.getResponse().getContentAsString()).path("data");
        this.token = data.path("token").asText();
        this.userId = data.path("id").asLong();

        List<Category> categories = categoryRepository.findByUserIdOrderByNameAsc(this.userId);
        this.categoryId = categories.get(0).getId();
    }

    @Test
    @DisplayName("Should generate daily report with accurate tracked/untracked hours and category distribution")
    void testDailyReportCalculation() throws Exception {
        LocalDate date = LocalDate.of(2026, 11, 10);
        ZonedDateTime start = date.atTime(10, 0).atZone(ZoneOffset.UTC);
        ZonedDateTime end = date.atTime(13, 0).atZone(ZoneOffset.UTC); // 3 hours

        ActivityRequest req = new ActivityRequest(categoryId, "Team Meeting", null, start.toInstant(), end.toInstant(), null);
        mockMvc.perform(post("/api/activities")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/reports/daily?date=" + date)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.totalTrackedHours").value(3.0))
                .andExpect(jsonPath("$.data.totalUntrackedHours").value(21.0))
                .andExpect(jsonPath("$.data.totalActivities").value(1))
                .andExpect(jsonPath("$.data.categoryDistribution[0].hours").value(3.0))
                .andExpect(jsonPath("$.data.categoryDistribution[0].percentage").value(100.0));
    }

    @Test
    @DisplayName("Should generate weekly report strictly bounded Monday to Sunday with 7 daily breakdown items")
    void testWeeklyReportMondayToSunday() throws Exception {
        // Tuesday Nov 10, 2026 -> Monday is Nov 9, Sunday is Nov 15
        LocalDate tuesday = LocalDate.of(2026, 11, 10);
        ZonedDateTime start = tuesday.atTime(8, 0).atZone(ZoneOffset.UTC);
        ZonedDateTime end = tuesday.atTime(12, 0).atZone(ZoneOffset.UTC); // 4 hours

        ActivityRequest req = new ActivityRequest(categoryId, "Project work", null, start.toInstant(), end.toInstant(), null);
        mockMvc.perform(post("/api/activities")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated());

        MvcResult res = mockMvc.perform(get("/api/reports/weekly?startDate=" + tuesday)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.weekStart").value("2026-11-09")) // Monday
                .andExpect(jsonPath("$.data.weekEnd").value("2026-11-15"))   // Sunday
                .andExpect(jsonPath("$.data.totalTrackedHours").value(4.0))
                .andReturn();

        JsonNode breakdown = objectMapper.readTree(res.getResponse().getContentAsString()).path("data").path("dailyBreakdown");
        assertThat(breakdown.size()).isEqualTo(7);
        assertThat(breakdown.get(0).path("dayName").asText()).isEqualTo("Mon");
        assertThat(breakdown.get(1).path("dayName").asText()).isEqualTo("Tue");
        assertThat(breakdown.get(1).path("totalTrackedHours").asDouble()).isEqualTo(4.0);
        assertThat(breakdown.get(6).path("dayName").asText()).isEqualTo("Sun");
    }

    @Test
    @DisplayName("Should calculate week-over-week productivity percentage change correctly")
    void testWeekOverWeekComparison() throws Exception {
        // Week 1 (Monday Nov 2 to Sunday Nov 8, 2026): 10 hours
        LocalDate week1Monday = LocalDate.of(2026, 11, 2);
        ZonedDateTime w1Start = week1Monday.atTime(8, 0).atZone(ZoneOffset.UTC);
        ZonedDateTime w1End = week1Monday.atTime(18, 0).atZone(ZoneOffset.UTC); // 10 hours

        ActivityRequest w1Req = new ActivityRequest(categoryId, "Sprint 1", null, w1Start.toInstant(), w1End.toInstant(), null);
        mockMvc.perform(post("/api/activities")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(w1Req)))
                .andExpect(status().isCreated());

        // Week 2 (Monday Nov 9 to Sunday Nov 15, 2026): 12 hours (+20% change)
        LocalDate week2Monday = LocalDate.of(2026, 11, 9);
        ZonedDateTime w2Start = week2Monday.atTime(8, 0).atZone(ZoneOffset.UTC);
        ZonedDateTime w2End = week2Monday.atTime(20, 0).atZone(ZoneOffset.UTC); // 12 hours

        ActivityRequest w2Req = new ActivityRequest(categoryId, "Sprint 2", null, w2Start.toInstant(), w2End.toInstant(), null);
        mockMvc.perform(post("/api/activities")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(w2Req)))
                .andExpect(status().isCreated());

        // Query Week 2
        mockMvc.perform(get("/api/reports/weekly?startDate=" + week2Monday)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalTrackedHours").value(12.0))
                .andExpect(jsonPath("$.data.previousWeekHours").value(10.0))
                .andExpect(jsonPath("$.data.percentageChange").value(20.0));
    }

    @Test
    @DisplayName("Should generate monthly report with active days count and days in month")
    void testMonthlyReport() throws Exception {
        LocalDate nov15 = LocalDate.of(2026, 11, 15);
        ZonedDateTime start = nov15.atTime(10, 0).atZone(ZoneOffset.UTC);
        ZonedDateTime end = nov15.atTime(15, 0).atZone(ZoneOffset.UTC); // 5 hours

        ActivityRequest req = new ActivityRequest(categoryId, "Monthly Work", null, start.toInstant(), end.toInstant(), null);
        mockMvc.perform(post("/api/activities")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/reports/monthly?year=2026&month=11")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.year").value(2026))
                .andExpect(jsonPath("$.data.month").value(11))
                .andExpect(jsonPath("$.data.monthName").value("November"))
                .andExpect(jsonPath("$.data.daysInMonth").value(30))
                .andExpect(jsonPath("$.data.activeDaysCount").value(1))
                .andExpect(jsonPath("$.data.totalTrackedHours").value(5.0));
    }
}
