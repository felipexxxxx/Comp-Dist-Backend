package com.healthsys.patient.patient;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.boot.test.mock.mockito.MockBean;
import com.healthsys.patient.messaging.PatientEventPublisher;

@SpringBootTest
@AutoConfigureMockMvc
class PatientControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PatientEventPublisher patientEventPublisher;

    @Test
    void shouldCreatePatientWhenAuthorized() throws Exception {
        mockMvc.perform(post("/api/patients")
                .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ADMIN"))
                    .jwt(jwt -> jwt.claim("roles", List.of("ADMIN"))))
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "name": "Maria Oliveira",
                      "birthDate": "1990-05-17",
                      "sex": "FEMALE",
                      "phone": "85999990000"
                    }
                    """))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.name").value("Maria Oliveira"))
            .andExpect(jsonPath("$.active").value(true));
    }

    @Test
    void shouldRequireAuthenticationForListingPatients() throws Exception {
        mockMvc.perform(get("/api/patients"))
            .andExpect(status().isUnauthorized());
    }
}
