package com.bookcorner.auth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.bookcorner.auth.user.User;
import com.bookcorner.auth.user.UserRepository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;

/** Exercises the real HTTP layer: security, validation, JSON, error format, and the database. */
@IntegrationTest
@Transactional
class RegistrationApiTest {

    private static final String PASSWORD = "a-long-enough-password";

    @Autowired
    private MockMvc mvc;

    @Autowired
    private UserRepository users;

    private ResultActions register(String email, String password, String fullName) throws Exception {
        String json = """
                {"email":"%s","password":"%s","fullName":"%s"}
                """.formatted(email, password, fullName);
        return postJson(json);
    }

    private ResultActions postJson(String json) throws Exception {
        return mvc.perform(post("/api/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json));
    }

    @Test
    void registersANewUser() throws Exception {
        register("New.Reader@Example.com", PASSWORD, "New Reader")
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNotEmpty())
                .andExpect(jsonPath("$.email").value("new.reader@example.com"))
                .andExpect(jsonPath("$.fullName").value("New Reader"))
                .andExpect(jsonPath("$.roles[0]").value("USER"))
                .andExpect(jsonPath("$.emailVerified").value(false))
                .andExpect(jsonPath("$.password").doesNotExist())
                .andExpect(jsonPath("$.passwordHash").doesNotExist());
    }

    @Test
    void storesOnlyAHashOfThePassword() throws Exception {
        register("hash.check@example.com", PASSWORD, "Hash Check").andExpect(status().isCreated());

        User saved = users.findByEmail("hash.check@example.com").orElseThrow();

        assertThat(saved.getPasswordHash()).startsWith("{bcrypt}").doesNotContain(PASSWORD);
    }

    @Test
    void rejectsTheSameEmailInADifferentCase() throws Exception {
        register("dupe@example.com", PASSWORD, "First").andExpect(status().isCreated());

        register("DUPE@Example.com", PASSWORD, "Second")
                .andExpect(status().isConflict())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.code").value("EMAIL_ALREADY_REGISTERED"));
    }

    @Test
    void reportsEveryInvalidFieldWithoutEchoingThePassword() throws Exception {
        String badPassword = "p@ss-w0rd";   // 9 characters: too short

        String body = register("not-an-email", badPassword, "   ")
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.code").value("VALIDATION_FAILED"))
                .andExpect(jsonPath("$.errors[?(@.field=='email')]").exists())
                .andExpect(jsonPath("$.errors[?(@.field=='password')]").exists())
                .andExpect(jsonPath("$.errors[?(@.field=='fullName')]").exists())
                .andReturn().getResponse().getContentAsString();

        assertThat(body).doesNotContain(badPassword);
    }

    @Test
    void acceptsAPasswordOfExactly72Bytes() throws Exception {
        register("max.password@example.com", "a".repeat(72), "Max Password").andExpect(status().isCreated());
    }

    @Test
    void rejectsAPasswordOver72Bytes() throws Exception {
        register("too.long@example.com", "a".repeat(73), "Too Long")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors[?(@.field=='password')]").exists());
    }

    @Test
    void countsPasswordLengthInBytesNotCharacters() throws Exception {
        // 40 characters, but each "é" is 2 bytes in UTF-8, so 80 bytes: over the bcrypt limit
        register("multibyte@example.com", "é".repeat(40), "Multi Byte")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors[?(@.field=='password')]").exists());
    }

    @Test
    void rejectsMalformedJsonAsAProblem() throws Exception {
        postJson("{this is not json")
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON));
    }

    @Test
    void protectedEndpointsRejectAnonymousRequests() throws Exception {
        mvc.perform(get("/api/v1/users/me")).andExpect(status().isUnauthorized());
    }
}
