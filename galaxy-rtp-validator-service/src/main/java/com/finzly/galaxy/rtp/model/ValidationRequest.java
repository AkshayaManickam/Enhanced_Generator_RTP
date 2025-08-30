package com.finzly.galaxy.rtp.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ValidationRequest {
    private String xmlContent;
    private String messageType;
}
