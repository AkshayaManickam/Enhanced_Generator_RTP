package com.finzly.galaxy.rtp.validator.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentTemplate {
    private Long id;
    private String templateName;
    private String senderRoutingNumber;
    private String receiverRoutingNumber;
    private String senderAccountNumber;
    private String receiverAccountNumber;
    private String amount;
    private String currency;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

