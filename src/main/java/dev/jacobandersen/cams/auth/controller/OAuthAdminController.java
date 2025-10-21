package dev.jacobandersen.cams.auth.controller;

import dev.jacobandersen.cams.auth.dto.AddOrEditClientRequestDto;
import dev.jacobandersen.cams.auth.model.template.Alert;
import dev.jacobandersen.cams.auth.security.JpaRegisteredClientRepository;
import dev.jacobandersen.cams.auth.util.SetUtil;
import jakarta.validation.Valid;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
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
import java.util.stream.Collectors;

@Controller
@RequestMapping("admin/oauth")
public class OAuthAdminController extends BaseController {
    private final JpaRegisteredClientRepository jpaRegisteredClientRepository;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public OAuthAdminController(JpaRegisteredClientRepository jpaRegisteredClientRepository, PasswordEncoder passwordEncoder) {
        this.jpaRegisteredClientRepository = jpaRegisteredClientRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @GetMapping("clients")
    public String clients(Model model) {
        Set<RegisteredClient> registeredClients = this.jpaRegisteredClientRepository.findAll();
        model.addAttribute("registeredClients", registeredClients);
        return "admin/clients";
    }

    @GetMapping("clients/{id}")
    public String client(@PathVariable("id") String id, Model model, RedirectAttributes redirectAttributes) {
        return templateWithClientById("admin/client", id, model, redirectAttributes);
    }

    @GetMapping("clients/add")
    public String addClientForm(@ModelAttribute("addClientRequest") AddOrEditClientRequestDto dto, @ModelAttribute("alert") Alert alert, Model model) {
        return "admin/add_client";
    }

    @PostMapping("clients/add")
    public String addClient(@Valid @ModelAttribute("addClientRequest") AddOrEditClientRequestDto dto, BindingResult bindingResult, Model model, RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            return "admin/add_client";
        }

        final String clientId = UUID.randomUUID().toString();
        final String clientSecret = RandomStringUtils.secure().next(12, true, true);

        final RegisteredClient newClient = RegisteredClient.withId(UUID.randomUUID().toString())
                .clientId(clientId)
                .clientIdIssuedAt(Instant.now())
                .clientSecret(passwordEncoder.encode(clientSecret))
                .clientName(dto.getClientName())
                .clientAuthenticationMethods(methods -> methods.addAll(dto.getClientAuthenticationMethods().stream().map(ClientAuthenticationMethod::valueOf).toList()))
                .authorizationGrantTypes(types -> types.addAll(dto.getAuthorizationGrantTypes().stream().map(AuthorizationGrantType::new).toList()))
                .scopes(scopes -> scopes.addAll(dto.getScopes()))
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

        addFlashAlert(redirectAttributes, Alert.success(
                """
                        Created a new client successfully<br>
                        Client ID: %s<br>
                        Client Secret: %s<br>""".formatted(clientId, clientSecret)
        ));

        return "redirect:/admin/oauth/clients";
    }

    @GetMapping("clients/{id}/edit")
    public String editClientForm(@PathVariable("id") String id, Model model, RedirectAttributes redirectAttributes, @ModelAttribute("editClientRequest") AddOrEditClientRequestDto dto, @ModelAttribute("alert") Alert alert) {
        return templateWithClientById("admin/edit_client", id, model, redirectAttributes);
    }

    @PostMapping("clients/{id}/edit")
    public String editClient(@PathVariable("id") String id, @Valid @ModelAttribute("editClientRequest") AddOrEditClientRequestDto dto, BindingResult bindingResult, Model model, RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            return "redirect:/admin/oauth/clients/%s/edit".formatted(id);
        }

        final RegisteredClient client = jpaRegisteredClientRepository.findById(id);
        if (null == client) {
            addFlashAlert(redirectAttributes, Alert.error("Client with id " + id + " not found while processing edit."));
            return "redirect:/admin/oauth/clients";
        }

        final RegisteredClient updated = RegisteredClient.from(client)
                .clientName(dto.getClientName())
                .clientAuthenticationMethods(methods -> SetUtil.setAll(methods, dto.getClientAuthenticationMethods().stream().map(ClientAuthenticationMethod::valueOf).toList()))
                .authorizationGrantTypes(types -> SetUtil.setAll(types, dto.getAuthorizationGrantTypes().stream().map(AuthorizationGrantType::new).toList()))
                .scopes(scopes -> SetUtil.setAll(scopes, dto.getScopes()))
                .redirectUris(uris -> SetUtil.setAll(uris, dto.getRedirectUris()))
                .postLogoutRedirectUris(uris -> SetUtil.setAll(uris, dto.getPostLogoutRedirectUris()))
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

        jpaRegisteredClientRepository.save(updated);

        return "redirect:/admin/oauth/clients";
    }

    @GetMapping("clients/{id}/regenerate_secret")
    public String regenerateSecretConfirmationPage(@PathVariable("id") String id, Model model, RedirectAttributes redirectAttributes) {
        return templateWithClientById("admin/regenerate_secret", id, model, redirectAttributes);
    }

    @PostMapping("clients/{id}/regenerate_secret")
    public String regenerateSecret(@PathVariable("id") String id, RedirectAttributes redirectAttributes) {
        final RegisteredClient client = jpaRegisteredClientRepository.findById(id);
        if (null == client) {
            addFlashAlert(redirectAttributes, Alert.error("Client with id " + id + " not found while regenerating secret."));
            return "redirect:/admin/oauth/clients";
        }

        final String clientSecret = RandomStringUtils.secure().next(12, true, true);

        final RegisteredClient updated = RegisteredClient.from(client)
                .clientSecret(passwordEncoder.encode(clientSecret))
                .build();

        jpaRegisteredClientRepository.save(updated);

        addFlashAlert(redirectAttributes, Alert.success("""
                Successfully regenerate client secret<br>
                Client ID: %s<br>
                Client Secret: %s<br>""".formatted(updated.getClientId(), clientSecret)));

        return "redirect:/admin/oauth/clients";
    }

    @GetMapping("clients/{id}/delete")
    public String deleteClientConfirmationPage(@PathVariable("id") String id, Model model, RedirectAttributes redirectAttributes) {
        return templateWithClientById("admin/delete_client_confirmation", id, model, redirectAttributes);
    }

    @PostMapping("clients/{id}/delete")
    public String deleteClient(@PathVariable("id") String id, RedirectAttributes redirectAttributes) {
        jpaRegisteredClientRepository.deleteById(id);

        addFlashAlert(redirectAttributes, Alert.success("OAuth Client successfully deleted."));

        return "redirect:/admin/oauth/clients";
    }

    private String templateWithClientById(String template, String id, Model model, RedirectAttributes attributes) throws NullPointerException {
        RegisteredClient registeredClient = this.jpaRegisteredClientRepository.findById(id);
        if (null == registeredClient) {
            addFlashAlert(attributes, Alert.error("No client with that ID was found. Available clients are below."));
            return "redirect:admin/clients";
        }

        model.addAttribute("registeredClient", registeredClient);
        model.addAttribute("rcClientAuthMethodsCsv", registeredClient.getClientAuthenticationMethods().stream().map(ClientAuthenticationMethod::getValue).collect(Collectors.joining(",")));
        model.addAttribute("rcAuthGrantTypesCsv", registeredClient.getAuthorizationGrantTypes().stream().map(AuthorizationGrantType::getValue).collect(Collectors.joining(",")));
        model.addAttribute("rcScopesCsv", String.join(",", registeredClient.getScopes()));
        model.addAttribute("rcRedirectUrisCsv", String.join(",", registeredClient.getRedirectUris()));
        model.addAttribute("rcPostLogoutRedirectUrisCsv", String.join(",", registeredClient.getPostLogoutRedirectUris()));

        return template;
    }
}

