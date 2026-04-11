package com.healthsys.identity.user;

import static org.hamcrest.Matchers.hasItem;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.healthsys.identity.messaging.IdentityEventPublisher;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class UserControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private IdentityEventPublisher identityEventPublisher;

    @Test
    void shouldCreateUserWhenAdminAuthorized() throws Exception {
        mockMvc.perform(post("/api/users")
                .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ADMIN"))
                    .jwt(jwt -> jwt.claim("roles", List.of("ADMIN"))))
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "name": "Novo Recepcionista",
                      "email": "recepcao@healthsys.local",
                      "password": "Recepcao@123",
                      "role": "RECEPTIONIST"
                    }
                    """))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.email").value("recepcao@healthsys.local"))
            .andExpect(jsonPath("$.role").value("RECEPTIONIST"))
            .andExpect(jsonPath("$.active").value(true));
    }

    @Test
    void shouldListUsersWhenAdminAuthorized() throws Exception {
        mockMvc.perform(get("/api/users")
                .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ADMIN"))
                    .jwt(jwt -> jwt.claim("roles", List.of("ADMIN")))))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[*].email", hasItem("admin@healthsys.local")));
    }

    @Test
    void shouldRejectUserCreationForNonAdmin() throws Exception {
        mockMvc.perform(post("/api/users")
                .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_RECEPTIONIST"))
                    .jwt(jwt -> jwt.claim("roles", List.of("RECEPTIONIST"))))
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "name": "Nao Autorizado",
                      "email": "blocked@healthsys.local",
                      "password": "Recepcao@123",
                      "role": "RECEPTIONIST"
                    }
                    """))
            .andExpect(status().isForbidden());
    }
}
