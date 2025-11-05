package com.finzly.galaxy.rtp.validator.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentInfoRequest {
    private String senderRoutingNumber;
    private String receiverRoutingNumber;
    private String senderAccountNumber;
    private String receiverAccountNumber;
    private String amount;
    private String currency;
    private String templateName; // For saving as template
}

