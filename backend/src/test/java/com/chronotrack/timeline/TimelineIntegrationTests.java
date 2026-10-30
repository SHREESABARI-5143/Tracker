package com.chronotrack.timeline;

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
class TimelineIntegrationTests {

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

        // Register user with UTC timezone
        RegisterRequest register = new RegisterRequest("Timeline User", "timeline@example.com", "Password@123");
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
    @DisplayName("Should return full 24-hour untracked segment when no activities exist")
    void testEmptyDayProducesSingle24HourUntrackedSegment() throws Exception {
        LocalDate date = LocalDate.of(2026, 11, 1);

        MvcResult res = mockMvc.perform(get("/api/timeline/daily?date=" + date)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.totalTrackedSeconds").value(0))
                .andExpect(jsonPath("$.data.totalUntrackedSeconds").value(86400))
                .andExpect(jsonPath("$.data.totalActivities").value(0))
                .andReturn();

        JsonNode segments = objectMapper.readTree(res.getResponse().getContentAsString()).path("data").path("segments");
        assertThat(segments.size()).isEqualTo(1);
        assertThat(segments.get(0).path("type").asText()).isEqualTo("UNTRACKED");
        assertThat(segments.get(0).path("durationSeconds").asLong()).isEqualTo(86400);
        assertThat(segments.get(0).path("startHour").asDouble()).isEqualTo(0.0);
        assertThat(segments.get(0).path("endHour").asDouble()).isEqualTo(24.0);
    }

    @Test
    @DisplayName("Should accurately assemble activities and untracked intervals totaling exactly 24 hours")
    void testTimelineWithActivitiesAndGaps() throws Exception {
        LocalDate date = LocalDate.of(2026, 11, 2);

        // Activity 1: 09:00 to 11:00 UTC (2 hours)
        ZonedDateTime act1Start = date.atTime(9, 0).atZone(ZoneOffset.UTC);
        ZonedDateTime act1End = date.atTime(11, 0).atZone(ZoneOffset.UTC);
        ActivityRequest req1 = new ActivityRequest(categoryId, "Morning Work", null, act1Start.toInstant(), act1End.toInstant(), null);
        mockMvc.perform(post("/api/activities")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req1)))
                .andExpect(status().isCreated());

        // Activity 2: 14:00 to 16:00 UTC (2 hours)
        ZonedDateTime act2Start = date.atTime(14, 0).atZone(ZoneOffset.UTC);
        ZonedDateTime act2End = date.atTime(16, 0).atZone(ZoneOffset.UTC);
        ActivityRequest req2 = new ActivityRequest(categoryId, "Afternoon Study", null, act2Start.toInstant(), act2End.toInstant(), null);
        mockMvc.perform(post("/api/activities")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req2)))
                .andExpect(status().isCreated());

        MvcResult res = mockMvc.perform(get("/api/timeline/daily?date=" + date)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalTrackedSeconds").value(14400)) // 4 hours
                .andExpect(jsonPath("$.data.totalUntrackedSeconds").value(72000)) // 20 hours
                .andExpect(jsonPath("$.data.totalActivities").value(2))
                .andReturn();

        JsonNode data = objectMapper.readTree(res.getResponse().getContentAsString()).path("data");
        JsonNode segments = data.path("segments");

        // Exactly 5 continuous segments: Untracked, Activity 1, Untracked, Activity 2, Untracked
        assertThat(segments.size()).isEqualTo(5);
        assertThat(segments.get(0).path("type").asText()).isEqualTo("UNTRACKED");
        assertThat(segments.get(0).path("durationSeconds").asLong()).isEqualTo(9 * 3600); // 00:00 to 09:00

        assertThat(segments.get(1).path("type").asText()).isEqualTo("ACTIVITY");
        assertThat(segments.get(1).path("durationSeconds").asLong()).isEqualTo(2 * 3600); // 09:00 to 11:00

        assertThat(segments.get(2).path("type").asText()).isEqualTo("UNTRACKED");
        assertThat(segments.get(2).path("durationSeconds").asLong()).isEqualTo(3 * 3600); // 11:00 to 14:00

        assertThat(segments.get(3).path("type").asText()).isEqualTo("ACTIVITY");
        assertThat(segments.get(3).path("durationSeconds").asLong()).isEqualTo(2 * 3600); // 14:00 to 16:00

        assertThat(segments.get(4).path("type").asText()).isEqualTo("UNTRACKED");
        assertThat(segments.get(4).path("durationSeconds").asLong()).isEqualTo(8 * 3600); // 16:00 to 24:00

        // Total of all 5 segments must equal 86400 (24h)
        long sumSeconds = 0;
        for (JsonNode seg : segments) {
            sumSeconds += seg.path("durationSeconds").asLong();
        }
        assertThat(sumSeconds).isEqualTo(86400);
    }

    @Test
    @DisplayName("Should correctly divide activities crossing midnight across consecutive calendar days")
    void testActivityCrossingMidnightClippedCorrectly() throws Exception {
        LocalDate day1 = LocalDate.of(2026, 11, 3);
        LocalDate day2 = LocalDate.of(2026, 11, 4);

        // Sleep: 10:00 PM (22:00) on Day 1 to 6:00 AM (06:00) on Day 2 -> Total 8 hours (28800s)
        ZonedDateTime start = day1.atTime(22, 0).atZone(ZoneOffset.UTC);
        ZonedDateTime end = day2.atTime(6, 0).atZone(ZoneOffset.UTC);

        ActivityRequest sleep = new ActivityRequest(categoryId, "Night Sleep", null, start.toInstant(), end.toInstant(), null);
        mockMvc.perform(post("/api/activities")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sleep)))
                .andExpect(status().isCreated());

        // Inspect Day 1: should have 2 hours of sleep (22:00 to 24:00)
        MvcResult resDay1 = mockMvc.perform(get("/api/timeline/daily?date=" + day1)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalTrackedSeconds").value(7200)) // 2 hours
                .andExpect(jsonPath("$.data.totalUntrackedSeconds").value(79200)) // 22 hours
                .andReturn();

        // Inspect Day 2: should have 6 hours of sleep (00:00 to 06:00)
        MvcResult resDay2 = mockMvc.perform(get("/api/timeline/daily?date=" + day2)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalTrackedSeconds").value(21600)) // 6 hours
                .andExpect(jsonPath("$.data.totalUntrackedSeconds").value(64800)) // 18 hours
                .andReturn();

        // 7200 (Day 1) + 21600 (Day 2) = 28800 total hours of sleep
        assertThat(7200 + 21600).isEqualTo(28800);
    }
}
