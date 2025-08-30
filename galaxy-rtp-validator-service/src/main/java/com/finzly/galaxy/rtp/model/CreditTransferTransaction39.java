package com.finzly.galaxy.rtp.model;

import jakarta.xml.bind.annotation.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.util.List;

/**
 * Credit Transfer Transaction Information
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@XmlAccessorType(XmlAccessType.FIELD)
public class CreditTransferTransaction39 {
    
    @XmlElement(name = "PmtId",namespace = "urn:iso:std:iso:20022:tech:xsd:pacs.008.001.08", required = true)
    private PaymentIdentification7 pmtId;
    
    @XmlElement(name = "PmtTpInf",namespace = "urn:iso:std:iso:20022:tech:xsd:pacs.008.001.08", required = true)
    private PaymentTypeInformation28 pmtTpInf;
    
    @XmlElement(name = "IntrBkSttlmAmt",namespace = "urn:iso:std:iso:20022:tech:xsd:pacs.008.001.08", required = true)
    private ActiveCurrencyAndAmount intrBkSttlmAmt;
    
    @XmlElement(name = "ChrgBr",namespace = "urn:iso:std:iso:20022:tech:xsd:pacs.008.001.08", required = true)
    private String chrgBr;
    
    @XmlElement(name = "PrvsInstgAgt1",namespace = "urn:iso:std:iso:20022:tech:xsd:pacs.008.001.08")
    private BranchAndFinancialInstitutionIdentification6 prvsInstgAgt1;
    
    @XmlElement(name = "PrvsInstgAgt1Acct",namespace = "urn:iso:std:iso:20022:tech:xsd:pacs.008.001.08")
    private CashAccount38 prvsInstgAgt1Acct;
    
    @XmlElement(name = "PrvsInstgAgt2",namespace = "urn:iso:std:iso:20022:tech:xsd:pacs.008.001.08")
    private BranchAndFinancialInstitutionIdentification6 prvsInstgAgt2;
    
    @XmlElement(name = "PrvsInstgAgt2Acct",namespace = "urn:iso:std:iso:20022:tech:xsd:pacs.008.001.08")
    private CashAccount38 prvsInstgAgt2Acct;
    
    @XmlElement(name = "PrvsInstgAgt3",namespace = "urn:iso:std:iso:20022:tech:xsd:pacs.008.001.08")
    private BranchAndFinancialInstitutionIdentification6 prvsInstgAgt3;
    
    @XmlElement(name = "PrvsInstgAgt3Acct",namespace = "urn:iso:std:iso:20022:tech:xsd:pacs.008.001.08")
    private CashAccount38 prvsInstgAgt3Acct;
    
    @XmlElement(name = "InstgAgt",namespace = "urn:iso:std:iso:20022:tech:xsd:pacs.008.001.08", required = true)
    private BranchAndFinancialInstitutionIdentification6 instgAgt;
    
    @XmlElement(name = "InstdAgt",namespace = "urn:iso:std:iso:20022:tech:xsd:pacs.008.001.08", required = true)
    private BranchAndFinancialInstitutionIdentification6 instdAgt;
    
    @XmlElement(name = "IntrmyAgt1",namespace = "urn:iso:std:iso:20022:tech:xsd:pacs.008.001.08")
    private BranchAndFinancialInstitutionIdentification6 intrmyAgt1;
    
    @XmlElement(name = "IntrmyAgt1Acct",namespace = "urn:iso:std:iso:20022:tech:xsd:pacs.008.001.08")
    private CashAccount38 intrmyAgt1Acct;
    
    @XmlElement(name = "IntrmyAgt2",namespace = "urn:iso:std:iso:20022:tech:xsd:pacs.008.001.08")
    private BranchAndFinancialInstitutionIdentification6 intrmyAgt2;
    
    @XmlElement(name = "IntrmyAgt2Acct",namespace = "urn:iso:std:iso:20022:tech:xsd:pacs.008.001.08")
    private CashAccount38 intrmyAgt2Acct;
    
    @XmlElement(name = "IntrmyAgt3",namespace = "urn:iso:std:iso:20022:tech:xsd:pacs.008.001.08")
    private BranchAndFinancialInstitutionIdentification6 intrmyAgt3;
    
    @XmlElement(name = "IntrmyAgt3Acct",namespace = "urn:iso:std:iso:20022:tech:xsd:pacs.008.001.08")
    private CashAccount38 intrmyAgt3Acct;
    
    @XmlElement(name = "UltmtDbtr",namespace = "urn:iso:std:iso:20022:tech:xsd:pacs.008.001.08")
    private PartyIdentification135 ultmtDbtr;
    
    @XmlElement(name = "InitgPty",namespace = "urn:iso:std:iso:20022:tech:xsd:pacs.008.001.08")
    private PartyIdentification135 initgPty;
    
    @XmlElement(name = "Dbtr",namespace = "urn:iso:std:iso:20022:tech:xsd:pacs.008.001.08", required = true)
    private PartyIdentification135 dbtr;
    
    @XmlElement(name = "DbtrAcct",namespace = "urn:iso:std:iso:20022:tech:xsd:pacs.008.001.08", required = true)
    private CashAccount38 dbtrAcct;
    
    @XmlElement(name = "DbtrAgt",namespace = "urn:iso:std:iso:20022:tech:xsd:pacs.008.001.08", required = true)
    private BranchAndFinancialInstitutionIdentification6 dbtrAgt;
    
    @XmlElement(name = "DbtrAgtAcct",namespace = "urn:iso:std:iso:20022:tech:xsd:pacs.008.001.08")
    private CashAccount38 dbtrAgtAcct;
    
    @XmlElement(name = "CdtrAgt",namespace = "urn:iso:std:iso:20022:tech:xsd:pacs.008.001.08", required = true)
    private BranchAndFinancialInstitutionIdentification6 cdtrAgt;
    
    @XmlElement(name = "CdtrAgtAcct",namespace = "urn:iso:std:iso:20022:tech:xsd:pacs.008.001.08")
    private CashAccount38 cdtrAgtAcct;
    
    @XmlElement(name = "Cdtr",namespace = "urn:iso:std:iso:20022:tech:xsd:pacs.008.001.08", required = true)
    private PartyIdentification135 cdtr;
    
    @XmlElement(name = "CdtrAcct",namespace = "urn:iso:std:iso:20022:tech:xsd:pacs.008.001.08", required = true)
    private CashAccount38 cdtrAcct;
    
    @XmlElement(name = "UltmtCdtr",namespace = "urn:iso:std:iso:20022:tech:xsd:pacs.008.001.08")
    private PartyIdentification135 ultmtCdtr;
    
    @XmlElement(name = "InstrForCdtrAgt",namespace = "urn:iso:std:iso:20022:tech:xsd:pacs.008.001.08")
    private List<InstructionForCreditorAgent1> instrForCdtrAgt;
    
    @XmlElement(name = "Purp",namespace = "urn:iso:std:iso:20022:tech:xsd:pacs.008.001.08")
    private Purpose2Choice purp;
    
    @XmlElement(name = "RltdRmtInf",namespace = "urn:iso:std:iso:20022:tech:xsd:pacs.008.001.08")
    private RemittanceLocation7 rltdRmtInf;
    
    @XmlElement(name = "RmtInf",namespace = "urn:iso:std:iso:20022:tech:xsd:pacs.008.001.08")
    private RemittanceInformation16 rmtInf;
}
