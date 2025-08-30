package com.finzly.galaxy.rtp.model;

import jakarta.xml.bind.annotation.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/**
 * Party Choice
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@XmlAccessorType(XmlAccessType.FIELD)
public class Party38Choice {
    
    @XmlElement(name = "OrgId",namespace = "urn:iso:std:iso:20022:tech:xsd:pacs.008.001.08")
    private OrganisationIdentification29 orgId;
    
    @XmlElement(name = "PrvtId",namespace = "urn:iso:std:iso:20022:tech:xsd:pacs.008.001.08")
    private PersonIdentification13 prvtId;
}
