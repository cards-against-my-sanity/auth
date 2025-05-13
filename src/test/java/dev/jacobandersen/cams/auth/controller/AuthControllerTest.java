package dev.jacobandersen.cams.auth.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.jacobandersen.cams.auth.constant.CamsAuthConstant;
import dev.jacobandersen.cams.auth.dto.in.LogInRequestDto;
import dev.jacobandersen.cams.auth.model.Session;
import dev.jacobandersen.cams.auth.model.User;
import dev.jacobandersen.cams.auth.service.AuthService;
import dev.jacobandersen.cams.auth.service.TokenService;
import dev.jacobandersen.cams.auth.service.UserService;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.shaded.org.apache.commons.lang3.RandomStringUtils;

import java.util.Collections;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.cookie;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(value = AuthController.class)
@AutoConfigureMockMvc(addFilters = false
)
class AuthControllerTest {
    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AuthenticationManager authenticationManager;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private AuthService authService;

    @MockitoBean
    private TokenService tokenService;

    private static User fakeValidUser;
    private static UserDetails fakeValidUserDetails;
    private static Session fakeValidSession;
    private static String generatedAccessToken;
    private static String generatedRefreshToken;

    @BeforeAll
    static void setup() {
        fakeValidUser = new User();
        fakeValidUser.setEmail("fake.valid.user@example.com");
        fakeValidUser.setConfirmed(true);

        fakeValidUserDetails = org.springframework.security.core.userdetails.User
                .withUsername(fakeValidUser.getEmail()).password("").build();

        fakeValidSession = new Session(fakeValidUser);

        generatedAccessToken = RandomStringUtils.randomAlphanumeric(64);
        generatedRefreshToken = RandomStringUtils.randomAlphanumeric(64);
    }

    @Test
    void testSuccessfulLogin() throws Exception {
        when(authenticationManager.authenticate(any())).thenReturn(UsernamePasswordAuthenticationToken.authenticated(
                fakeValidUserDetails, null, Collections.emptyList()));
        when(userService.findUserByEmail(anyString())).thenReturn(Optional.of(fakeValidUser));
        when(authService.createSession(eq(fakeValidUser), anyBoolean())).thenReturn(fakeValidSession);
        when(tokenService.createAccessToken(fakeValidUser)).thenReturn(generatedAccessToken);
        when(tokenService.createRefreshToken(fakeValidUser, fakeValidSession)).thenReturn(generatedRefreshToken);

        final LogInRequestDto dto = new LogInRequestDto();
        dto.setEmail(fakeValidUser.getEmail());
        dto.setPassword("password");
        dto.setRememberMe(true);

        mockMvc.perform(post("/login").content(objectMapper.writeValueAsString(dto)).contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(cookie().exists(CamsAuthConstant.ACCESS_TOKEN_COOKIE_NAME))
                .andExpect(cookie().value(CamsAuthConstant.ACCESS_TOKEN_COOKIE_NAME, generatedAccessToken))
                .andExpect(cookie().exists(CamsAuthConstant.REFRESH_TOKEN_COOKIE_NAME))
                .andExpect(cookie().value(CamsAuthConstant.REFRESH_TOKEN_COOKIE_NAME, generatedRefreshToken));
    }
}
