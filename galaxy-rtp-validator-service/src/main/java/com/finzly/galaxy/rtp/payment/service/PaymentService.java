package com.finzly.galaxy.rtp.payment.service;

import com.finzly.galaxy.rtp.mapper.Pacs008Mapper;
import com.finzly.galaxy.rtp.payment.Pacs008Helper;
import com.finzly.galaxy.rtp.payment.PaymentRequest;
import com.finzly.galaxy.rtp.payment.adapter.PaymentAdapter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class PaymentService {

    @Autowired
    private PaymentAdapter paymentAdapter;

    @Autowired
    private Pacs008Helper pacs008Helper;
    public String createInPayment(String paymentRequest,String env,String tenant){
        try {
            PaymentRequest request = new PaymentRequest();
            request.setEnv(env);
            request.setTenant(tenant);
            String finalMessage = pacs008Helper.pacs008Message(paymentRequest);
            request.setBodyContent(finalMessage);
            paymentAdapter.sendInPaymentRequest(request);
        } catch (Exception e) {
            log.error("Exception while createInPayment {}",e);
            return "Failed";
        }
        return "Success";
    }


}
