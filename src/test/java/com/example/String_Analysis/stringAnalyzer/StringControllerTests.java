package com.example.String_Analysis;

import com.example.String_Analysis.dto.CreateStringRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.beans.factory.annotation.Autowired;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class StringControllerTests {

    @Autowired MockMvc mvc;
    @Autowired ObjectMapper objectMapper;

    @BeforeEach
    public void setup() {}

    @Test
    public void createAndGet() throws Exception {
        CreateStringRequest r = new CreateStringRequest("madam");
        mvc.perform(post("/strings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(r)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.value").value("madam"))
                .andExpect(jsonPath("$.properties.is_palindrome").value(true));

        mvc.perform(get("/strings/madam"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.value").value("madam"));
    }

    @Test
    public void listFilter() throws Exception {
        mvc.perform(post("/strings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new CreateStringRequest("aba"))))
                .andExpect(status().isCreated());

        mvc.perform(get("/strings")
                        .param("is_palindrome", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.count").isNumber());
    }
}
