package com.chronotrack.timer;

import com.chronotrack.dto.RegisterRequest;
import com.chronotrack.dto.TimerStartRequest;
import com.chronotrack.entity.Category;
import com.chronotrack.repository.ActivityRepository;
import com.chronotrack.repository.CategoryRepository;
import com.chronotrack.repository.TimerSessionRepository;
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

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class TimerIntegrationTests {

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
    private TimerSessionRepository timerSessionRepository;

    private String token;
    private Long userId;
    private Long categoryId;

    @BeforeEach
    void setUp() throws Exception {
        activityRepository.deleteAll();
        timerSessionRepository.deleteAll();
        categoryRepository.deleteAll();
        userRepository.deleteAll();

        // Register test user
        RegisterRequest register = new RegisterRequest("David Timer", "david@example.com", "Password@123");
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
    @DisplayName("Should start timer, persist to database, and return RUNNING status")
    void testStartTimer() throws Exception {
        TimerStartRequest request = new TimerStartRequest(categoryId, "Writing Documentation", "Timer test", null);

        mockMvc.perform(post("/api/timer/start")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.title").value("Writing Documentation"))
                .andExpect(jsonPath("$.data.status").value("RUNNING"))
                .andExpect(jsonPath("$.data.running").value(true));

        assertThat(timerSessionRepository.findByUserId(userId)).isPresent();
    }

    @Test
    @DisplayName("Should pause and resume active timer")
    void testPauseAndResumeTimer() throws Exception {
        TimerStartRequest request = new TimerStartRequest(categoryId, "Coding sprint", null, null);
        mockMvc.perform(post("/api/timer/start")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());

        // Pause
        mockMvc.perform(post("/api/timer/pause")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("PAUSED"))
                .andExpect(jsonPath("$.data.running").value(false));

        // Resume
        mockMvc.perform(post("/api/timer/resume")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("RUNNING"))
                .andExpect(jsonPath("$.data.running").value(true));
    }

    @Test
    @DisplayName("Should restore active timer state via GET /api/timer/current")
    void testGetCurrentTimer() throws Exception {
        TimerStartRequest request = new TimerStartRequest(categoryId, "Study Session", null, null);
        mockMvc.perform(post("/api/timer/start")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/timer/current")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.title").value("Study Session"))
                .andExpect(jsonPath("$.data.elapsedFormatted").isNotEmpty());
    }

    @Test
    @DisplayName("Should stop timer, create completed Activity, and delete TimerSession")
    void testStopTimerCreatesActivity() throws Exception {
        TimerStartRequest request = new TimerStartRequest(categoryId, "Reading Book", "Chapter 4", "Finished");
        mockMvc.perform(post("/api/timer/start")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());

        MvcResult stopResult = mockMvc.perform(post("/api/timer/stop")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.title").value("Reading Book"))
                .andExpect(jsonPath("$.data.durationSeconds").value(org.hamcrest.Matchers.greaterThanOrEqualTo(1)))
                .andReturn();

        // Verify timer session was removed
        assertThat(timerSessionRepository.findByUserId(userId)).isEmpty();

        // Verify activity was created
        assertThat(activityRepository.findByUserIdOrderByStartTimeDesc(userId)).hasSize(1);
    }

    @Test
    @DisplayName("Should reject starting second timer when one is already active")
    void testPreventMultipleTimers() throws Exception {
        TimerStartRequest req1 = new TimerStartRequest(categoryId, "First Task", null, null);
        mockMvc.perform(post("/api/timer/start")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req1)))
                .andExpect(status().isCreated());

        TimerStartRequest req2 = new TimerStartRequest(categoryId, "Second Task", null, null);
        mockMvc.perform(post("/api/timer/start")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req2)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("already active")));
    }
}
