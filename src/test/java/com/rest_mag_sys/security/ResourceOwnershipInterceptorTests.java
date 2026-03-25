package com.rest_mag_sys.security;

import com.rest_mag_sys.common.JwtUtil;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class ResourceOwnershipInterceptorTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtUtil jwtUtil;

    @Test
    void customerCanAccessOwnOrderDetails() throws Exception {
        String customerToken = jwtUtil.generateToken(1000003L, "customer1", "customer");

        mockMvc.perform(get("/orders/details/1000001")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + customerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    void customerCannotAccessOthersOrderDetails() throws Exception {
        String customerToken = jwtUtil.generateToken(1000003L, "customer1", "customer");

        mockMvc.perform(get("/orders/details/1000002")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + customerToken))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value(403));
    }

    @Test
    void customerCanAccessOwnReviewDetails() throws Exception {
        String customerToken = jwtUtil.generateToken(1000003L, "customer1", "customer");

        mockMvc.perform(get("/review/1000001")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + customerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    void customerCannotAccessOthersReviewDetails() throws Exception {
        String customerToken = jwtUtil.generateToken(1000003L, "customer1", "customer");

        mockMvc.perform(get("/review/1000002")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + customerToken))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value(403));
    }

    @Test
    void adminCanAccessAnyOrderDetails() throws Exception {
        String adminToken = jwtUtil.generateToken(1000001L, "admin", "admin");

        mockMvc.perform(get("/orders/details/1000002")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }
}

