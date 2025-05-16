package dev.jacobandersen.cams.auth.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.jacobandersen.cams.auth.constant.CamsAuthConstant;
import dev.jacobandersen.cams.auth.constant.RefreshTokenTypeEnum;
import dev.jacobandersen.cams.auth.dto.in.LogInRequestDto;
import dev.jacobandersen.cams.auth.dto.in.RefreshTokenTypeRequestDto;
import dev.jacobandersen.cams.auth.exception.InvalidJwtPurposeException;
import dev.jacobandersen.cams.auth.exception.SessionMissingOrExpiredException;
import dev.jacobandersen.cams.auth.model.Session;
import dev.jacobandersen.cams.auth.model.User;
import dev.jacobandersen.cams.auth.security.JwtAuthenticator;
import dev.jacobandersen.cams.auth.service.AuthService;
import dev.jacobandersen.cams.auth.service.TokenService;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import jakarta.servlet.http.Cookie;
import org.apache.commons.lang3.RandomStringUtils;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.util.Collections;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(value = AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
class AuthControllerTest {
    private static User fakeValidUser;
    private static User fakeBannedUser;
    private static User fakeUnconfirmedUser;
    private static LogInRequestDto fakeLogInRequestDto;
    private static Session fakeValidSession;
    private static String generatedAccessToken;
    private static String generatedRefreshToken;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AuthenticationManager authenticationManager;

    @MockitoBean
    private AuthService authService;

    @MockitoBean
    private TokenService tokenService;

    @MockitoBean
    private JwtAuthenticator jwtAuthenticator;

    @BeforeAll
    static void prepareData() {
        fakeValidUser = new User();
        fakeValidUser.setConfirmed(true);

        fakeBannedUser = new User();
        fakeBannedUser.setBanned(true);

        fakeUnconfirmedUser = new User();
        fakeUnconfirmedUser.setConfirmed(false);

        fakeLogInRequestDto = new LogInRequestDto();
        fakeLogInRequestDto.setEmail("fake.valid.user@example.com");
        fakeLogInRequestDto.setPassword("password");

        fakeValidSession = new Session(fakeValidUser);

        generatedAccessToken = RandomStringUtils.insecure().nextAlphabetic(64);
        generatedRefreshToken = RandomStringUtils.insecure().nextAlphabetic(64);
    }

    @Test
    void testLogInSuccess() throws Exception {
        when(authenticationManager.authenticate(any())).thenReturn(UsernamePasswordAuthenticationToken.authenticated(
                fakeValidUser,
                null,
                Collections.emptyList()
        ));

        when(authService.createSession(eq(fakeValidUser), anyBoolean())).thenReturn(fakeValidSession);
        when(tokenService.createAccessToken(fakeValidUser)).thenReturn(generatedAccessToken);
        when(tokenService.createRefreshToken(fakeValidUser, fakeValidSession)).thenReturn(generatedRefreshToken);

        mockMvc.perform(post("/login")
                        .content(objectMapper.writeValueAsString(fakeLogInRequestDto))
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(cookie().exists(CamsAuthConstant.ACCESS_TOKEN_COOKIE_NAME))
                .andExpect(cookie().value(CamsAuthConstant.ACCESS_TOKEN_COOKIE_NAME, generatedAccessToken))
                .andExpect(cookie().exists(CamsAuthConstant.REFRESH_TOKEN_COOKIE_NAME))
                .andExpect(cookie().value(CamsAuthConstant.REFRESH_TOKEN_COOKIE_NAME, generatedRefreshToken));
    }

    @Test
    void testLogInWhenAlreadyLoggedIn() throws Exception {
        mockMvc.perform(post("/login")
                        .content(objectMapper.writeValueAsString(fakeLogInRequestDto))
                        .contentType(MediaType.APPLICATION_JSON)
                        .cookie(new Cookie(CamsAuthConstant.ACCESS_TOKEN_COOKIE_NAME, generatedAccessToken)))
                .andDo(print())
                .andExpect(status().isConflict());

        mockMvc.perform(post("/login")
                        .content(objectMapper.writeValueAsString(fakeLogInRequestDto))
                        .contentType(MediaType.APPLICATION_JSON)
                        .cookie(new Cookie(CamsAuthConstant.REFRESH_TOKEN_COOKIE_NAME, generatedRefreshToken)))
                .andDo(print())
                .andExpect(status().isConflict());

    }

    @Test
    void testLogInWhenInvalidCredentials() throws Exception {
        when(authenticationManager.authenticate(any())).thenThrow(new BadCredentialsException("AuthControllerTest: Bad credentials"));

        mockMvc.perform(post("/login")
                        .content(objectMapper.writeValueAsString(fakeLogInRequestDto))
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isUnauthorized());
    }

    @Test
    void testLogInWhenBanned() throws Exception {
        when(authenticationManager.authenticate(any())).thenReturn(UsernamePasswordAuthenticationToken.authenticated(
                fakeBannedUser,
                null,
                Collections.emptyList()
        ));

        mockMvc.perform(post("/login")
                        .content(objectMapper.writeValueAsString(fakeLogInRequestDto))
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isForbidden())
                .andExpect(MockMvcResultMatchers.content().string(Matchers.containsString("You are banned.")));
    }

    @Test
    void testLogInWhenNotConfirmed() throws Exception {
        when(authenticationManager.authenticate(any())).thenReturn(UsernamePasswordAuthenticationToken.authenticated(
                fakeUnconfirmedUser,
                null,
                Collections.emptyList()
        ));

        mockMvc.perform(post("/login")
                        .content(objectMapper.writeValueAsString(fakeLogInRequestDto))
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isForbidden())
                .andExpect(MockMvcResultMatchers.content().string(Matchers.containsString("Please confirm your account before logging in.")));
    }

    @Test
    void testLogOutSuccess() throws Exception {
        final UUID userId = UUID.randomUUID();
        final UUID sessionId = UUID.randomUUID();
        when(tokenService.validateToken(any(), any())).thenReturn(Jwts.claims().subject(userId.toString()).add("sid", sessionId.toString()).build());
        when(authService.endSession(userId, sessionId)).thenReturn(true);

        mockMvc.perform(delete("/logout")
                        .cookie(new Cookie(CamsAuthConstant.REFRESH_TOKEN_COOKIE_NAME, generatedRefreshToken)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(cookie().exists(CamsAuthConstant.REFRESH_TOKEN_COOKIE_NAME))
                .andExpect(cookie().maxAge(CamsAuthConstant.REFRESH_TOKEN_COOKIE_NAME, 0))
                .andExpect(cookie().exists(CamsAuthConstant.ACCESS_TOKEN_COOKIE_NAME))
                .andExpect(cookie().maxAge(CamsAuthConstant.ACCESS_TOKEN_COOKIE_NAME, 0));
    }

    @Test
    void testLogOutWhenInvalidToken() throws Exception {
        when(tokenService.validateToken(any(), any())).thenThrow(new JwtException("AuthControllerTest: Invalid token"));

        mockMvc.perform(delete("/logout")
                        .cookie(new Cookie(CamsAuthConstant.REFRESH_TOKEN_COOKIE_NAME, generatedRefreshToken)))
                .andDo(print())
                .andExpect(status().isUnauthorized());
    }

    @Test
    void testRefreshSuccess() throws Exception {
        final String newAccessToken = RandomStringUtils.insecure().nextAlphabetic(64);
        when(tokenService.refresh(any(), eq(RefreshTokenTypeEnum.ACCESS))).thenReturn(newAccessToken);


        mockMvc.perform(post("/refresh")
                        .cookie(new Cookie(CamsAuthConstant.REFRESH_TOKEN_COOKIE_NAME, generatedRefreshToken))
                        .content(objectMapper.writeValueAsString(new RefreshTokenTypeRequestDto().setType(RefreshTokenTypeEnum.ACCESS)))
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(cookie().exists(CamsAuthConstant.ACCESS_TOKEN_COOKIE_NAME))
                .andExpect(cookie().value(CamsAuthConstant.ACCESS_TOKEN_COOKIE_NAME, newAccessToken));

        final String newWebsocketToken = RandomStringUtils.insecure().nextAlphabetic(64);
        when(tokenService.refresh(any(), eq(RefreshTokenTypeEnum.WEBSOCKET))).thenReturn(newWebsocketToken);

        mockMvc.perform(post("/refresh")
                        .cookie(new Cookie(CamsAuthConstant.REFRESH_TOKEN_COOKIE_NAME, generatedRefreshToken))
                        .content(objectMapper.writeValueAsString(new RefreshTokenTypeRequestDto().setType(RefreshTokenTypeEnum.WEBSOCKET)))
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value(newWebsocketToken));
    }

    @Test
    void testRefreshInvalidTokenPurpose() throws Exception {
        when(tokenService.refresh(any(), any())).thenThrow(new InvalidJwtPurposeException("AuthControllerTest: Invalid JWT purpose"));

        mockMvc.perform(post("/refresh")
                        .cookie(new Cookie(CamsAuthConstant.REFRESH_TOKEN_COOKIE_NAME, generatedRefreshToken))
                        .content(objectMapper.writeValueAsString(new RefreshTokenTypeRequestDto().setType(RefreshTokenTypeEnum.ACCESS)))
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isBadRequest());

        mockMvc.perform(post("/refresh")
                        .cookie(new Cookie(CamsAuthConstant.REFRESH_TOKEN_COOKIE_NAME, generatedRefreshToken))
                        .content(objectMapper.writeValueAsString(new RefreshTokenTypeRequestDto().setType(RefreshTokenTypeEnum.WEBSOCKET)))
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    void testRefreshExpiredSession() throws Exception {
        when(tokenService.refresh(any(), any())).thenThrow(new SessionMissingOrExpiredException("AuthControllerTest: Session missing or expired"));

        mockMvc.perform(post("/refresh")
                        .cookie(new Cookie(CamsAuthConstant.REFRESH_TOKEN_COOKIE_NAME, generatedRefreshToken))
                        .content(objectMapper.writeValueAsString(new RefreshTokenTypeRequestDto().setType(RefreshTokenTypeEnum.ACCESS)))
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isUnauthorized());

        mockMvc.perform(post("/refresh")
                        .cookie(new Cookie(CamsAuthConstant.REFRESH_TOKEN_COOKIE_NAME, generatedRefreshToken))
                        .content(objectMapper.writeValueAsString(new RefreshTokenTypeRequestDto().setType(RefreshTokenTypeEnum.WEBSOCKET)))
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isUnauthorized());
    }
}
