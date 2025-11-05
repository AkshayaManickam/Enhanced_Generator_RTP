package com.finzly.galaxy.rtp.payment.adapter;

import com.finzly.galaxy.rtp.config.TenantEnvConfigService;
import com.finzly.galaxy.rtp.payment.PaymentRequest;
import com.swapstech.galaxy.common.tenant.model.TenantContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.Map;

@Slf4j
@Service
public class PaymentAdapter {

    @Autowired
    private TenantEnvConfigService tenantEnvConfigService;

    /**
     * Get OAuth2 access token from Keycloak
     */
    private String getAccessToken(String clientId, String secret, String authUrl) {
        log.info("=== Obtaining OAuth2 Access Token ===");
        log.info("Auth URL: {}", authUrl);
        log.info("Client ID: {}", clientId);
        
        try {
            WebClient authClient = WebClient.builder().build();
            
            MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
            formData.add("grant_type", "client_credentials");
            formData.add("client_id", clientId);
            formData.add("client_secret", secret);
            
            Map<String, Object> tokenResponse = authClient.post()
                    .uri(authUrl)
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .body(BodyInserters.fromFormData(formData))
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();
            
            if (tokenResponse != null && tokenResponse.containsKey("access_token")) {
                String token = (String) tokenResponse.get("access_token");
                log.info("✅ Access token obtained successfully");
                return token;
            } else {
                log.error("❌ Failed to obtain access token - no token in response");
                throw new RuntimeException("Failed to obtain OAuth2 access token");
            }
            
        } catch (Exception e) {
            log.error("❌ Error obtaining access token: {}", e.getMessage(), e);
            throw new RuntimeException("OAuth2 authentication failed: " + e.getMessage(), e);
        }
    }

    /**
     * Create WebClient with OAuth2 Bearer token
     */
    public WebClient createWebClient(String clientId, String secret, String authUrl, String tenant) {
        TenantContext.setCurrentTenant(tenant);
        log.info("Creating authenticated WebClient for tenant: {}", tenant);
        
        try {
            // Get OAuth2 access token
            String accessToken = getAccessToken(clientId, secret, authUrl);
            
            // Create WebClient with Bearer token
            return WebClient.builder()
                    .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                    .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.TEXT_PLAIN_VALUE)
                    .defaultHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                    .build();
                    
        } catch (Exception e) {
            log.error("Failed to create authenticated WebClient: {}", e.getMessage());
            throw new RuntimeException("WebClient creation failed: " + e.getMessage(), e);
        }
    }

    public String sendInPaymentRequest(PaymentRequest rtpMessage) {
        log.info("=== Send Payment Request START ===");
        log.info("Request details - Tenant: {}, Environment: {}", rtpMessage.getTenant(), rtpMessage.getEnv());
        
        String tenant = rtpMessage.getTenant();
        String env = rtpMessage.getEnv();

        try {
            // Load configuration from properties file
            String rtpAppUrl = tenantEnvConfigService.getProperty(tenant, env, "rtp.app.url");
            String clientId = tenantEnvConfigService.getProperty(tenant, env, "api.client.id");
            String secret = tenantEnvConfigService.getProperty(tenant, env, "api.secret");
            String authUrl = tenantEnvConfigService.getProperty(tenant, env, "api.auth.url");
            
            log.info("Configuration loaded:");
            log.info("  RTP App URL: {}", rtpAppUrl);
            log.info("  Client ID: {}", clientId);
            log.info("  Auth URL: {}", authUrl);
            
            // Create authenticated WebClient
            WebClient client = createWebClient(clientId, secret, authUrl, tenant);
            String sendUrl = rtpAppUrl + "/in";
            
            log.info("=== Sending payment to: {} ===", sendUrl);
            log.info("Message length: {} characters", rtpMessage.getBodyContent().length());
            
            try {
                String response = client.post()
                        .uri(sendUrl)
                        .contentType(MediaType.valueOf("text/plain"))
                        .accept(MediaType.APPLICATION_JSON)
                        .body(Mono.just(rtpMessage.getBodyContent()), String.class)
                        .retrieve()
                        .bodyToMono(String.class)
                        .block();
                        
                log.info("=== Payment sent successfully ===");
                log.info("Response: {}", response);
                return response;
                
            } catch (Exception e) {
                log.error("=== Exception while sending payment ===", e);
                log.error("Error details: {}", e.getMessage());
                
                // Check if it's an authentication error
                if (e.getMessage().contains("401") || e.getMessage().contains("Unauthorized")) {
                    throw new RuntimeException("Authentication failed - 401 Unauthorized. Please check credentials.", e);
                } else if (e.getMessage().contains("403") || e.getMessage().contains("Forbidden")) {
                    throw new RuntimeException("Access forbidden - 403. Please check permissions.", e);
                } else {
                    throw new RuntimeException("Failed to send payment: " + e.getMessage(), e);
                }
            }
            
        } catch (Exception e) {
            log.error("=== Exception in sendInPaymentRequest ===", e);
            throw new RuntimeException("Payment processing failed: " + e.getMessage(), e);
        }
    }
}
