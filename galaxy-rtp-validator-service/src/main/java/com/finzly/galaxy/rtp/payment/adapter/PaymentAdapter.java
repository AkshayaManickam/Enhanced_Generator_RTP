package com.finzly.galaxy.rtp.payment.adapter;
import com.finzly.galaxy.rtp.config.TenantEnvConfigService;
import com.finzly.galaxy.rtp.payment.PaymentRequest;
import com.swapstech.galaxy.common.tenant.model.TenantContext;
import com.swapstech.galaxy.security.client.BankOSWebClient;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.io.IOException;

@Slf4j
@Service
public class PaymentAdapter {

    @Autowired
    private BankOSWebClient bankOSWebClient;

    @Autowired
    private TenantEnvConfigService tenantEnvConfigService;

    public WebClient webClient(String clientId,String secret,String authUrl,String tenant) {
        TenantContext.setCurrentTenant(tenant);
        log.info("In Oauth adapter - secret={}, clientId={}, uri={}", secret, clientId, authUrl);

        return bankOSWebClient.webClient(authUrl, clientId, secret);
    }

    public String sendInPaymentRequest(PaymentRequest rtpMessage) {
        log.info("Send Acknowledgement Request : {} ", rtpMessage);
        String tenant = rtpMessage.getTenant();
        String env = rtpMessage.getEnv();

        try {
            String rtpAppUrl = tenantEnvConfigService.getProperty(tenant, env, "rtp.app.url");
            String clientId = tenantEnvConfigService.getProperty(tenant, env, "api.client.id");
            String secret = tenantEnvConfigService.getProperty(tenant, env, "api.secret");
            String authUrl = tenantEnvConfigService.getProperty(tenant, env, "api.auth.url");
        WebClient client = webClient(clientId,secret,authUrl,rtpMessage.getTenant());
        String sendUrl = rtpAppUrl + "/in";
        log.info("Send acknowledgement URL {} ", sendUrl);
        try {
            String response = client.post().uri(sendUrl).contentType(MediaType.valueOf("text/plain"))
                    .accept(MediaType.APPLICATION_JSON)
                    .body(Mono.just(rtpMessage.getBodyContent()), String.class).retrieve().bodyToMono(
                            String.class).block();;
            return response;
        } catch (Exception e) {
            log.error("Exception while sending acknowledgement " , e);
        }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return null;
    }

}
