package com.finzly.galaxy.rtp.payment.rest;

import com.finzly.galaxy.rtp.payment.PaymentRequest;
import com.finzly.galaxy.rtp.payment.service.PaymentService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Slf4j
public class PaymentController {

    @Autowired
    private PaymentService paymentService;

    @PostMapping(value = "/payment", produces = "application/json", consumes = MediaType.TEXT_PLAIN_VALUE)
    @Operation(summary = "Process Incoming Rtp")
    public ResponseEntity<String> rtpInMessage(@RequestBody String paymentRequest, @RequestParam String env, @RequestParam String tenant) {
        paymentService.createInPayment(paymentRequest,env,tenant);
        return ResponseEntity.ok("SUCCESS");
    }
}
