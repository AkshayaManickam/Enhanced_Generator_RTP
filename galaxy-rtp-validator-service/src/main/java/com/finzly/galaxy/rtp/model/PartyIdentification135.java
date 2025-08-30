package com.finzly.galaxy.rtp.model;

import jakarta.xml.bind.annotation.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/**
 * Party Identification
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@XmlAccessorType(XmlAccessType.FIELD)
public class PartyIdentification135 {
    
    @XmlElement(name = "Nm",namespace = "urn:iso:std:iso:20022:tech:xsd:pacs.008.001.08")
    private String nm;
    
    @XmlElement(name = "PstlAdr",namespace = "urn:iso:std:iso:20022:tech:xsd:pacs.008.001.08")
    private PostalAddress24 pstlAdr;
    
    @XmlElement(name = "Id",namespace = "urn:iso:std:iso:20022:tech:xsd:pacs.008.001.08")
    private Party38Choice id;
}
