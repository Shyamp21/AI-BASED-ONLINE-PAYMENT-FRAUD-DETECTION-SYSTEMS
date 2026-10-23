package com.frauddetection;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.frauddetection.dto.request.LoginRequest;
import com.frauddetection.dto.request.RegisterRequest;
import com.frauddetection.dto.request.TransactionPredictRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.math.BigDecimal;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class FraudDetectionApplicationTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("Context loads successfully")
    void contextLoads() {
    }

    @Test
    @DisplayName("Authentication: Register new analyst and login")
    void testAuthWorkflow() throws Exception {
        String testUser = "analyst_test_" + System.currentTimeMillis();
        RegisterRequest registerReq = new RegisterRequest();
        registerReq.setUsername(testUser);
        registerReq.setEmail(testUser + "@fraudguard.ai");
        registerReq.setPassword("Password@123");
        registerReq.setFullName("Test Risk Analyst");
        registerReq.setRole("ROLE_USER");

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerReq)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.token").exists())
                .andExpect(jsonPath("$.username").value(testUser));

        LoginRequest loginReq = new LoginRequest();
        loginReq.setUsername(testUser);
        loginReq.setPassword("Password@123");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").exists())
                .andExpect(jsonPath("$.type").value("Bearer"));
    }

    @Test
    @DisplayName("Transaction: Predict fraud and evaluate probabilistic output")
    void testTransactionPredict() throws Exception {
        // First authenticate
        LoginRequest loginReq = new LoginRequest();
        loginReq.setUsername("admin");
        loginReq.setPassword("Admin@123");

        MvcResult loginResult = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginReq)))
                .andExpect(status().isOk())
                .andReturn();

        String responseBody = loginResult.getResponse().getContentAsString();
        String token = objectMapper.readTree(responseBody).get("token").asText();

        TransactionPredictRequest predictReq = new TransactionPredictRequest();
        predictReq.setExternalTransactionId("TXN-TEST-" + System.currentTimeMillis());
        predictReq.setAmount(new BigDecimal("85000.00"));
        predictReq.setType("TRANSFER");
        predictReq.setStep(15);
        predictReq.setSenderId("C1928374650");
        predictReq.setOldbalanceOrg(new BigDecimal("85000.00"));
        predictReq.setNewbalanceOrig(BigDecimal.ZERO);
        predictReq.setReceiverId("C9918273645");
        predictReq.setOldbalanceDest(BigDecimal.ZERO);
        predictReq.setNewbalanceDest(BigDecimal.ZERO);

        mockMvc.perform(post("/api/transactions/predict")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(predictReq)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.fraudProbability").exists())
                .andExpect(jsonPath("$.predictedClass").exists())
                .andExpect(jsonPath("$.riskLevel").exists());
    }

    @Test
    @DisplayName("Dashboard: Fetch summary metrics")
    void testDashboardSummary() throws Exception {
        LoginRequest loginReq = new LoginRequest();
        loginReq.setUsername("admin");
        loginReq.setPassword("Admin@123");

        MvcResult loginResult = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginReq)))
                .andReturn();

        String token = objectMapper.readTree(loginResult.getResponse().getContentAsString()).get("token").asText();

        mockMvc.perform(get("/api/dashboard/summary")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalTransactions").isNumber())
                .andExpect(jsonPath("$.fraudPercentage").isNumber());
    }
}
