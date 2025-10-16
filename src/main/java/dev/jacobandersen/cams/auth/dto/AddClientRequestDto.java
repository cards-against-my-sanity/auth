package dev.jacobandersen.cams.auth.dto;


import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public class AddClientRequestDto {
    @NotEmpty(message = "At least one client authentication method must be specified")
    private List<@NotBlank String> clientAuthenticationMethods;

    @NotEmpty(message = "At least one authorization grant type must be specified")
    private List<@NotBlank String> authorizationGrantTypes;

    @NotBlank(message = "At least one scope must be specified")
    private List<@NotBlank(message = "A scope must not be blank") String> scopes;

    @NotBlank(message = "At least one redirect URI must be specified")
    private List<@NotBlank(message = "A redirect URI must not be blank") String> redirectUris;

    @NotBlank(message = "At least one post-logout redirect URI must be specified")
    private List<@NotBlank(message = "A post-logout redirct URI must not be blank") String> postLogoutRedirectUris;

    private boolean requireProofKey;

    private boolean requireAuthorizationConsent;

    private boolean reuseRefreshTokens;

    @Min(value = 1L, message = "Access token time to live must be at least one second")
    private long accessTokenTimeToLiveSeconds;

    @Min(value = 1L, message = "Refresh token time to live must be at least one second")
    private long refreshTokenTimeToLiveSeconds;

    public List<String> getClientAuthenticationMethods() {
        return clientAuthenticationMethods;
    }

    public void setClientAuthenticationMethods(List<String> clientAuthenticationMethods) {
        this.clientAuthenticationMethods = clientAuthenticationMethods;
    }

    public List<String> getAuthorizationGrantTypes() {
        return authorizationGrantTypes;
    }

    public void setAuthorizationGrantTypes(List<String> authorizationGrantTypes) {
        this.authorizationGrantTypes = authorizationGrantTypes;
    }

    public List<String> getScopes() {
        return scopes;
    }

    public void setScopes(List<String> scopes) {
        this.scopes = scopes;
    }

    public List<String> getRedirectUris() {
        return redirectUris;
    }

    public void setRedirectUris(List<String> redirectUris) {
        this.redirectUris = redirectUris;
    }

    public List<String> getPostLogoutRedirectUris() {
        return postLogoutRedirectUris;
    }

    public void setPostLogoutRedirectUris(List<String> postLogoutRedirectUris) {
        this.postLogoutRedirectUris = postLogoutRedirectUris;
    }

    public boolean isRequireProofKey() {
        return requireProofKey;
    }

    public void setRequireProofKey(boolean requireProofKey) {
        this.requireProofKey = requireProofKey;
    }

    public boolean isRequireAuthorizationConsent() {
        return requireAuthorizationConsent;
    }

    public void setRequireAuthorizationConsent(boolean requireAuthorizationConsent) {
        this.requireAuthorizationConsent = requireAuthorizationConsent;
    }

    public boolean isReuseRefreshTokens() {
        return reuseRefreshTokens;
    }

    public void setReuseRefreshTokens(boolean reuseRefreshTokens) {
        this.reuseRefreshTokens = reuseRefreshTokens;
    }

    public long getAccessTokenTimeToLiveSeconds() {
        return accessTokenTimeToLiveSeconds;
    }

    public void setAccessTokenTimeToLiveSeconds(long accessTokenTimeToLiveSeconds) {
        this.accessTokenTimeToLiveSeconds = accessTokenTimeToLiveSeconds;
    }

    public long getRefreshTokenTimeToLiveSeconds() {
        return refreshTokenTimeToLiveSeconds;
    }

    public void setRefreshTokenTimeToLiveSeconds(long refreshTokenTimeToLiveSeconds) {
        this.refreshTokenTimeToLiveSeconds = refreshTokenTimeToLiveSeconds;
    }
}
