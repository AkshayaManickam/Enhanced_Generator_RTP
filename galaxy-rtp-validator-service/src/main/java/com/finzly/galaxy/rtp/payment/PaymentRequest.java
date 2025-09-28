package com.finzly.galaxy.rtp.payment;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.stereotype.Service;

@Getter
@Setter
@ToString
public class PaymentRequest {

    private String tenant;

    private String env;

    private String bodyContent;

}
