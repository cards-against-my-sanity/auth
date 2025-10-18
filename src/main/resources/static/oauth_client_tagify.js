const originalInputValueFormat = valuesArr => valuesArr.map(item => item.value).join(",")

const clientAuthMethodSuggestions = [
    "client_secret_basic", "client_secret_post", "client_secret_jwt",
    "private_key_jwt", "none", "tls_client_auth", "self_signed_tls_client_auth"
]

const authGrantTypeSuggestions = [
    "authorization_code", "refresh_token", "client_credentials", "urn:ietf:params:oauth:grant-type:jwt-bearer",
    "urn:ietf:params:oauth:grant-type:device_code", "urn:ietf:params:oauth:grant-type:token-exchange"
]

const scopeSuggestions = [
    "openid", "profile", "email"
]

new Tagify(document.querySelector("#clientAuthenticationMethods"), {
    whitelist: clientAuthMethodSuggestions,
    enforceWhitelist: false,
    originalInputValueFormat
});

new Tagify(document.querySelector("#authorizationGrantTypes"), {
    whitelist: authGrantTypeSuggestions,
    enforceWhitelist: false,
    originalInputValueFormat
});

new Tagify(document.querySelector("#scopes"), {
    whitelist: scopeSuggestions,
    enforceWhitelist: false,
    originalInputValueFormat
});

new Tagify(document.querySelector("#redirectUris"), {
    originalInputValueFormat
});

new Tagify(document.querySelector("#postLogoutRedirectUris"), {
    originalInputValueFormat
});