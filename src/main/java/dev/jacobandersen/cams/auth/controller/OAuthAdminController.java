package dev.jacobandersen.cams.auth.controller;

import dev.jacobandersen.cams.auth.dto.AddClientRequestDto;
import dev.jacobandersen.cams.auth.model.template.Alert;
import dev.jacobandersen.cams.auth.security.JpaRegisteredClientRepository;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.settings.ClientSettings;
import org.springframework.security.oauth2.server.authorization.settings.TokenSettings;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Set;
import java.util.UUID;

@Controller
@RequestMapping("admin/oauth")
public class OAuthAdminController extends BaseController {
    private final JpaRegisteredClientRepository jpaRegisteredClientRepository;

    @Autowired
    public OAuthAdminController(JpaRegisteredClientRepository jpaRegisteredClientRepository) {
        this.jpaRegisteredClientRepository = jpaRegisteredClientRepository;
    }

    @GetMapping("clients")
    public String clients(Model model) {
        Set<RegisteredClient> registeredClients = this.jpaRegisteredClientRepository.findAll();
        model.addAttribute("registeredClients", registeredClients);
        return "clients";
    }

    @GetMapping("clients/{id}")
    public String client(@PathVariable("id") String id, Model model) {
        RegisteredClient registeredClient = this.jpaRegisteredClientRepository.findByClientId(id);
        model.addAttribute("registeredClient", registeredClient);
        return "client";
    }

    @GetMapping("clients/add")
    public String addClientForm(@ModelAttribute("addClientRequest") AddClientRequestDto dto, @ModelAttribute("alert") Alert alert, Model model) {
        model.addAttribute("clientAuthenticationMethodSuggestions", Set.of(
                "client_secret_basic", "client_secret_post", "client_secret_jwt",
                "private_key_jwt", "none", "tls_client_auth", "self_signed_tls_client_auth"
        ));

        model.addAttribute("authorizationGrantTypeSuggestions", Set.of(
                "authorization_code", "refresh_token", "client_credentials", "urn:ietf:params:oauth:grant-type:jwt-bearer",
                "urn:ietf:params:oauth:grant-type:device_code", "urn:ietf:params:oauth:grant-type:token-exchange"
        ));

        return "add_client";
    }

    @PostMapping("client/add")
    public String addClient(@Valid @ModelAttribute("addClientRequest") AddClientRequestDto dto, BindingResult bindingResult, Model model, RedirectAttributes redirectAttributes) {
        final RegisteredClient newClient = RegisteredClient.withId(UUID.randomUUID().toString())
                .clientAuthenticationMethods(methods -> methods.addAll(dto.getClientAuthenticationMethods().stream().map(ClientAuthenticationMethod::valueOf).toList()))
                .authorizationGrantTypes(types -> types.addAll(dto.getAuthorizationGrantTypes().stream().map(AuthorizationGrantType::new).toList()))
                .scopes(scopes -> scopes.addAll(dto.getScopes()))
                .clientIdIssuedAt(Instant.now())
                .redirectUris(uris -> uris.addAll(dto.getRedirectUris()))
                .postLogoutRedirectUris(uris -> uris.addAll(dto.getPostLogoutRedirectUris()))
                .clientSettings(ClientSettings.builder()
                        .requireProofKey(dto.isRequireProofKey())
                        .requireAuthorizationConsent(dto.isRequireAuthorizationConsent())
                        .build())
                .tokenSettings(TokenSettings.builder()
                        .reuseRefreshTokens(dto.isReuseRefreshTokens())
                        .accessTokenTimeToLive(Duration.of(dto.getAccessTokenTimeToLiveSeconds(), ChronoUnit.SECONDS))
                        .refreshTokenTimeToLive(Duration.of(dto.getRefreshTokenTimeToLiveSeconds(), ChronoUnit.SECONDS))
                        .build())
                .build();

        jpaRegisteredClientRepository.save(newClient);

        addFlashAlert(redirectAttributes, Alert.success("Created new client successfully!"));
        return "redirect:/clients";
    }

    @GetMapping("clients/edit")
    public String editClientForm(Model model) {
        return "edit_client";
    }

    @PutMapping("clients/edit")
    public String editClient() {
        return "redirect:/clients/edit";
    }

    @DeleteMapping("clients/delete")
    public String deleteClient() {
        return "redirect:/clients";
    }
}

