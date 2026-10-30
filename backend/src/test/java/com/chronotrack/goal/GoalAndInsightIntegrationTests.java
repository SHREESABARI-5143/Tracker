package com.chronotrack.goal;

import com.chronotrack.dto.ActivityRequest;
import com.chronotrack.dto.GoalRequest;
import com.chronotrack.dto.RegisterRequest;
import com.chronotrack.dto.ReminderRequest;
import com.chronotrack.entity.Category;
import com.chronotrack.entity.GoalFrequency;
import com.chronotrack.repository.*;
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

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class GoalAndInsightIntegrationTests {

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

    @Autowired
    private GoalRepository goalRepository;

    @Autowired
    private ReminderRepository reminderRepository;

    private String token;
    private Long userId;
    private Long categoryId;

    @BeforeEach
    void setUp() throws Exception {
        reminderRepository.deleteAll();
        goalRepository.deleteAll();
        activityRepository.deleteAll();
        categoryRepository.deleteAll();
        userRepository.deleteAll();

        RegisterRequest register = new RegisterRequest("Grace Goal", "grace@example.com", "Password@123");
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
    @DisplayName("Should create goal and accurately calculate real-time completion progress from activities")
    void testCreateAndTrackGoalProgress() throws Exception {
        // Create Daily Goal: 120 minutes (2h)
        GoalRequest goalReq = new GoalRequest(
                categoryId, "Daily Focus Work", 120, GoalFrequency.DAILY, LocalDate.now(ZoneOffset.UTC), null
        );

        MvcResult createGoalRes = mockMvc.perform(post("/api/goals")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(goalReq)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.targetMinutes").value(120))
                .andExpect(jsonPath("$.data.actualMinutes").value(0))
                .andExpect(jsonPath("$.data.completed").value(false))
                .andReturn();

        Long goalId = objectMapper.readTree(createGoalRes.getResponse().getContentAsString()).path("data").path("id").asLong();

        // Log 90 minutes activity today
        ZonedDateTime now = ZonedDateTime.now(ZoneOffset.UTC);
        Instant actStart = now.minusMinutes(90).toInstant();
        Instant actEnd = now.toInstant();

        ActivityRequest actReq = new ActivityRequest(categoryId, "Session 1", null, actStart, actEnd, null);
        mockMvc.perform(post("/api/activities")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(actReq)))
                .andExpect(status().isCreated());

        // Verify goal progress is now 75.0%
        mockMvc.perform(get("/api/goals/" + goalId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.actualMinutes").value(90))
                .andExpect(jsonPath("$.data.progressPercentage").value(75.0))
                .andExpect(jsonPath("$.data.completed").value(false));

        // Log another 30 minutes activity
        Instant act2Start = now.minusMinutes(125).toInstant();
        Instant act2End = now.minusMinutes(95).toInstant();

        ActivityRequest act2Req = new ActivityRequest(categoryId, "Session 2", null, act2Start, act2End, null);
        mockMvc.perform(post("/api/activities")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(act2Req)))
                .andExpect(status().isCreated());

        // Verify goal is now 100% completed
        mockMvc.perform(get("/api/goals/" + goalId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.actualMinutes").value(120))
                .andExpect(jsonPath("$.data.progressPercentage").value(100.0))
                .andExpect(jsonPath("$.data.completed").value(true));
    }

    @Test
    @DisplayName("Should generate data-driven productivity insights from database activity records")
    void testGenerateInsightsFromDatabase() throws Exception {
        // Log an activity
        ZonedDateTime now = ZonedDateTime.now(ZoneOffset.UTC);
        ActivityRequest act = new ActivityRequest(categoryId, "Deep Study", null, now.minusHours(2).toInstant(), now.toInstant(), null);
        mockMvc.perform(post("/api/activities")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(act)))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/insights")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data[0].title").exists())
                .andExpect(jsonPath("$.data[0].message").exists());
    }

    @Test
    @DisplayName("Should create, retrieve, update and delete reminders")
    void testReminderCrud() throws Exception {
        ReminderRequest req = new ReminderRequest("Drink water & stretch", "14:30", true);

        MvcResult res = mockMvc.perform(post("/api/reminders")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.title").value("Drink water & stretch"))
                .andExpect(jsonPath("$.data.reminderTime").value("14:30"))
                .andReturn();

        Long reminderId = objectMapper.readTree(res.getResponse().getContentAsString()).path("data").path("id").asLong();

        // Update reminder
        ReminderRequest update = new ReminderRequest("Evening walk", "18:00", false);
        mockMvc.perform(put("/api/reminders/" + reminderId)
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(update)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.title").value("Evening walk"))
                .andExpect(jsonPath("$.data.enabled").value(false));

        // Delete reminder
        mockMvc.perform(delete("/api/reminders/" + reminderId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());

        assertThat(reminderRepository.findById(reminderId)).isEmpty();
    }
}
