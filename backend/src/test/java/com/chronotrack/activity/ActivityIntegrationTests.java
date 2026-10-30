package com.chronotrack.activity;

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

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class ActivityIntegrationTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ActivityRepository activityRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    private String userToken;
    private Long userId;
    private Long defaultCategoryId;

    @BeforeEach
    void setUp() throws Exception {
        activityRepository.deleteAll();
        categoryRepository.deleteAll();
        userRepository.deleteAll();

        // Register main user
        RegisterRequest register = new RegisterRequest("Alice Smith", "alice@example.com", "Password@123");
        MvcResult result = mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(register)))
                .andExpect(status().isCreated())
                .andReturn();

        JsonNode root = objectMapper.readTree(result.getResponse().getContentAsString());
        this.userToken = root.path("data").path("token").asText();
        this.userId = root.path("data").path("id").asLong();

        // Retrieve seeded category
        List<Category> categories = categoryRepository.findByUserIdOrderByNameAsc(this.userId);
        this.defaultCategoryId = categories.get(0).getId();
    }

    @Test
    @DisplayName("Should create activity successfully with auto-calculated duration")
    void testCreateActivitySuccess() throws Exception {
        Instant start = Instant.now().truncatedTo(ChronoUnit.SECONDS);
        Instant end = start.plus(90, ChronoUnit.MINUTES);

        ActivityRequest request = new ActivityRequest(
                defaultCategoryId, "Deep Work", "Implementing backend services", start, end, "Focus mode"
        );

        MvcResult result = mockMvc.perform(post("/api/activities")
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.title").value("Deep Work"))
                .andExpect(jsonPath("$.data.durationSeconds").value(5400))
                .andExpect(jsonPath("$.data.durationFormatted").value("1h 30m"))
                .andReturn();

        JsonNode data = objectMapper.readTree(result.getResponse().getContentAsString()).path("data");
        Long activityId = data.path("id").asLong();
        assertThat(activityRepository.findById(activityId)).isPresent();
    }

    @Test
    @DisplayName("Should strictly reject overlapping activities for the same user with 400 Bad Request")
    void testOverlappingActivitiesSameUserRejected() throws Exception {
        Instant base = Instant.now().truncatedTo(ChronoUnit.HOURS);
        Instant act1Start = base.plus(1, ChronoUnit.HOURS);
        Instant act1End = base.plus(3, ChronoUnit.HOURS); // 1:00 to 3:00

        ActivityRequest act1 = new ActivityRequest(defaultCategoryId, "Coding", "Java", act1Start, act1End, null);
        mockMvc.perform(post("/api/activities")
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(act1)))
                .andExpect(status().isCreated());

        // Overlapping activity: 2:00 to 4:00 (overlaps with 1:00 to 3:00)
        Instant act2Start = base.plus(2, ChronoUnit.HOURS);
        Instant act2End = base.plus(4, ChronoUnit.HOURS);
        ActivityRequest act2 = new ActivityRequest(defaultCategoryId, "Gym", "Workout", act2Start, act2End, null);

        mockMvc.perform(post("/api/activities")
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(act2)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("conflicts with an existing activity")));
    }

    @Test
    @DisplayName("Should allow different users to have activities at the exact same time")
    void testDifferentUsersSameTimeAllowed() throws Exception {
        Instant start = Instant.now().truncatedTo(ChronoUnit.HOURS);
        Instant end = start.plus(2, ChronoUnit.HOURS);

        // User A creates activity
        ActivityRequest actA = new ActivityRequest(defaultCategoryId, "User A Work", null, start, end, null);
        mockMvc.perform(post("/api/activities")
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(actA)))
                .andExpect(status().isCreated());

        // Register User B
        RegisterRequest registerB = new RegisterRequest("Bob", "bob@example.com", "Password@123");
        MvcResult resultB = mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerB)))
                .andExpect(status().isCreated())
                .andReturn();
        String tokenB = objectMapper.readTree(resultB.getResponse().getContentAsString()).path("data").path("token").asText();
        Long userBId = objectMapper.readTree(resultB.getResponse().getContentAsString()).path("data").path("id").asLong();
        Long userBCatId = categoryRepository.findByUserIdOrderByNameAsc(userBId).get(0).getId();

        // User B creates activity at exact same time -> MUST SUCCEED
        ActivityRequest actB = new ActivityRequest(userBCatId, "User B Study", null, start, end, null);
        mockMvc.perform(post("/api/activities")
                        .header("Authorization", "Bearer " + tokenB)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(actB)))
                .andExpect(status().isCreated());
    }

    @Test
    @DisplayName("Should reject activity where end time is before or equal to start time")
    void testEndTimeBeforeStartTimeRejected() throws Exception {
        Instant start = Instant.now();
        Instant end = start.minus(30, ChronoUnit.MINUTES);

        ActivityRequest invalid = new ActivityRequest(defaultCategoryId, "Invalid Time", null, start, end, null);
        mockMvc.perform(post("/api/activities")
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalid)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("End time must be strictly later than start time."));
    }

    @Test
    @DisplayName("Should update activity successfully without conflicting with self")
    void testUpdateActivityExcludingSelf() throws Exception {
        Instant start = Instant.now().truncatedTo(ChronoUnit.HOURS);
        Instant end = start.plus(1, ChronoUnit.HOURS);

        ActivityRequest act = new ActivityRequest(defaultCategoryId, "Initial Title", null, start, end, null);
        MvcResult res = mockMvc.perform(post("/api/activities")
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(act)))
                .andExpect(status().isCreated())
                .andReturn();

        Long id = objectMapper.readTree(res.getResponse().getContentAsString()).path("data").path("id").asLong();

        // Update title and keep same time range
        ActivityRequest updateReq = new ActivityRequest(defaultCategoryId, "Updated Title", "Added desc", start, end, "Notes");
        mockMvc.perform(put("/api/activities/" + id)
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.title").value("Updated Title"));
    }

    @Test
    @DisplayName("Should delete activity successfully and enforce user isolation")
    void testDeleteActivity() throws Exception {
        Instant start = Instant.now().truncatedTo(ChronoUnit.HOURS);
        Instant end = start.plus(1, ChronoUnit.HOURS);

        ActivityRequest act = new ActivityRequest(defaultCategoryId, "To Delete", null, start, end, null);
        MvcResult res = mockMvc.perform(post("/api/activities")
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(act)))
                .andExpect(status().isCreated())
                .andReturn();

        Long id = objectMapper.readTree(res.getResponse().getContentAsString()).path("data").path("id").asLong();

        // Delete activity
        mockMvc.perform(delete("/api/activities/" + id)
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isOk());

        assertThat(activityRepository.findById(id)).isEmpty();
    }
}
