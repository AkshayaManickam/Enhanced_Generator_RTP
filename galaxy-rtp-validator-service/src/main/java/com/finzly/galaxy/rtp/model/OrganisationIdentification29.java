package com.finzly.galaxy.rtp.model;

import jakarta.xml.bind.annotation.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.util.List;

/**
 * Organisation Identification
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@XmlAccessorType(XmlAccessType.FIELD)
public class OrganisationIdentification29 {
    
    @XmlElement(name = "AnyBIC",namespace = "urn:iso:std:iso:20022:tech:xsd:pacs.008.001.08")
    private String anyBIC;
    
    @XmlElement(name = "LEI",namespace = "urn:iso:std:iso:20022:tech:xsd:pacs.008.001.08")
    private String lei;
    
    @XmlElement(name = "Othr",namespace = "urn:iso:std:iso:20022:tech:xsd:pacs.008.001.08")
    private List<GenericOrganisationIdentification1> othr;
}
