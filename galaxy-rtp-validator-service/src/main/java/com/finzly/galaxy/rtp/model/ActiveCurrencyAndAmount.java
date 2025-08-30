package com.finzly.galaxy.rtp.model;

import jakarta.xml.bind.annotation.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.math.BigDecimal;

/**
 * Active Currency and Amount with currency code attribute
 */
@Data
@NoArgsConstructor
@XmlAccessorType(XmlAccessType.FIELD)
public class ActiveCurrencyAndAmount {
    
    @XmlValue
    private BigDecimal value;
    
    @XmlAttribute(name = "Ccy",namespace = "", required = true)
    private String ccy;
    
    public ActiveCurrencyAndAmount(BigDecimal value, String ccy) {
        this.value = value;
        this.ccy = ccy;
    }
}
