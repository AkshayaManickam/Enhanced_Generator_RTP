package com.finzly.galaxy.rtp.validator.service;

import com.finzly.galaxy.rtp.validator.model.TagType;
import com.finzly.galaxy.rtp.validator.model.XmlTag;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class TagDataService {

    private List<XmlTag> rootTags = new ArrayList<>();
    private Map<String, XmlTag> tagIndex = new HashMap<>();

    @PostConstruct
    public void initializeTagData() {
        // Root level - FIToFICustomer Credit Transfer
        XmlTag root = createTag("1.0", "FIToFICstmrCdtTrf", "FITo FICustomer Credit Transfer V08", "[1..1]", null, TagType.MANDATORY, 0);
        
        // 1.x Group Header
        XmlTag grpHdr = createTag("1.0", "GrpHdr", "Group Header", "[1..1]", null, TagType.MANDATORY, 1);
        grpHdr.getChildren().add(createTag("1.1", "MsgId", "Message Identification", "[1..1]", 35, TagType.MANDATORY, 2));
        grpHdr.getChildren().add(createTag("1.2", "CreDtTm", "Creation Date Time", "[1..1]", 19, TagType.MANDATORY, 2));
        grpHdr.getChildren().add(createTag("1.4", "NbOfTxs", "Number Of Transactions", "[1..1]", 1, TagType.MANDATORY, 2));
        
        // 1.6 Total Interbank Settlement Amount with nested Currency
        XmlTag ttlIntrBkSttlmAmt = createTag("1.6", "TtlIntrBkSttlmAmt", "Total Interbank Settlement Amount", "[1..1]", 18, TagType.MANDATORY, 2);
        ttlIntrBkSttlmAmt.getChildren().add(createTag("1.7", "Ccy", "Currency", "[required]", 3, TagType.MANDATORY, 3));
        grpHdr.getChildren().add(ttlIntrBkSttlmAmt);
        
        grpHdr.getChildren().add(createTag("1.8", "IntrBkSttlmDt", "Interbank Settlement Date", "[1..1]", 10, TagType.MANDATORY, 2));
        
        // 1.9 Settlement Information with nested children
        XmlTag sttlmInf = createTag("1.9", "SttlmInf", "Settlement Information", "[1..1]", null, TagType.MANDATORY, 2);
        sttlmInf.getChildren().add(createTag("1.10", "SttlmMtd", "Settlement Method", "[1..1]", 4, TagType.MANDATORY, 3));
        
        // 1.30 Clearing System with nested Code
        XmlTag clrSys = createTag("1.30", "ClrSys", "Clearing System", "[1..1]", null, TagType.MANDATORY, 3);
        clrSys.getChildren().add(createTag("1.31", "Cd", "Code", "[1..1]", 3, TagType.MANDATORY, 4));
        sttlmInf.getChildren().add(clrSys);
        
        grpHdr.getChildren().add(sttlmInf);
        
        root.getChildren().add(grpHdr);
        
        // 2.x Credit Transfer Transaction Information
        XmlTag cdtTrfTxInf = createTag("2.0", "CdtTrfTxInf", "Credit Transfer Transaction Information", "[1..1]", null, TagType.MANDATORY, 1);
        
        // 2.1 Payment Identification
        XmlTag pmtId = createTag("2.1", "PmtId", "Payment Identification", "[1..1]", null, TagType.MANDATORY, 2);
        pmtId.getChildren().add(createTag("2.2", "InstrId", "Instruction Identification", "[1..1]", 35, TagType.MANDATORY, 3));
        pmtId.getChildren().add(createTag("2.3", "EndToEndId", "End To End Identification", "[1..1]", 35, TagType.MANDATORY, 3));
        pmtId.getChildren().add(createTag("2.4", "TxId", "Transaction Identification", "[1..1]", 35, TagType.MANDATORY, 3));
        pmtId.getChildren().add(createTag("2.5", "UETR", "UETR", "[0..1]", 36, TagType.OPTIONAL, 3));
        pmtId.getChildren().add(createTag("2.6", "ClrSysRef", "Clearing System Reference", "[0..1]", 35, TagType.OPTIONAL, 3));
        cdtTrfTxInf.getChildren().add(pmtId);
        
        // 2.7 Payment Type Information
        XmlTag pmtTpInf = createTag("2.7", "PmtTpInf", "Payment Type Information", "[1..1]", null, TagType.MANDATORY, 2);
        XmlTag svcLvl = createTag("2.10", "SvcLvl", "Service Level", "[1..1]", null, TagType.MANDATORY, 3);
        svcLvl.getChildren().add(createTag("2.11", "Cd", "Code", "[1..1]", 4, TagType.MANDATORY, 4));
        pmtTpInf.getChildren().add(svcLvl);
        
        XmlTag lclInstrm = createTag("2.13", "LclInstrm", "Local Instrument", "[1..1]", null, TagType.MANDATORY, 3);
        lclInstrm.getChildren().add(createTag("2.15", "Prtry", "Proprietary", "[1..1]", 35, TagType.MANDATORY, 4));
        pmtTpInf.getChildren().add(lclInstrm);
        
        XmlTag ctgyPurp = createTag("2.16", "CtgyPurp", "Category Purpose", "[1..1]", null, TagType.MANDATORY, 3);
        ctgyPurp.getChildren().add(createTag("2.18", "Prtry", "Proprietary", "[1..1]", 35, TagType.MANDATORY, 4));
        pmtTpInf.getChildren().add(ctgyPurp);
        cdtTrfTxInf.getChildren().add(pmtTpInf);
        
        // 2.19 Interbank Settlement Amount
        XmlTag intrBkSttlmAmt = createTag("2.19", "IntrBkSttlmAmt", "Interbank Settlement Amount", "[1..1]", 18, TagType.MANDATORY, 2);
        intrBkSttlmAmt.getChildren().add(createTag("2.20", "Ccy", "Currency", "[required]", 3, TagType.MANDATORY, 3));
        cdtTrfTxInf.getChildren().add(intrBkSttlmAmt);
        
        XmlTag chrgBr = createTag("2.36", "ChrgBr", "Charge Bearer", "[1..1]", 4, TagType.MANDATORY, 2);
        cdtTrfTxInf.getChildren().add(chrgBr);
        
        // Previous Instructing Agents (All Optional)
        cdtTrfTxInf.getChildren().add(createPreviousInstructingAgent("2.104", "PrvsInstgAgt1", "Previous Instructing Agent 1", TagType.OPTIONAL, 2));
        cdtTrfTxInf.getChildren().add(createAccountTag("2.168", "PrvsInstgAgt1Acct", "Previous Instructing Agent 1Account", TagType.OPTIONAL, 2));
        cdtTrfTxInf.getChildren().add(createPreviousInstructingAgent("2.187", "PrvsInstgAgt2", "Previous Instructing Agent 2", TagType.OPTIONAL, 2));
        cdtTrfTxInf.getChildren().add(createAccountTag("2.251", "PrvsInstgAgt2Acct", "Previous Instructing Agent 2Account", TagType.OPTIONAL, 2));
        cdtTrfTxInf.getChildren().add(createPreviousInstructingAgent("2.270", "PrvsInstgAgt3", "Previous Instructing Agent 3", TagType.OPTIONAL, 2));
        cdtTrfTxInf.getChildren().add(createAccountTag("2.334", "PrvsInstgAgt3Acct", "Previous Instructing Agent 3Account", TagType.OPTIONAL, 2));
        
        // Instructing/Instructed Agents (Mandatory)
        cdtTrfTxInf.getChildren().add(createAgentTag("2.353", "InstgAgt", "Instructing Agent", TagType.MANDATORY, 2));
        cdtTrfTxInf.getChildren().add(createAgentTag("2.417", "InstdAgt", "Instructed Agent", TagType.MANDATORY, 2));
        
        // Intermediary Agents (All Optional)
        cdtTrfTxInf.getChildren().add(createIntermediaryAgent("2.481", "IntrmyAgt1", "Intermediary Agent 1", TagType.OPTIONAL, 2));
        cdtTrfTxInf.getChildren().add(createAccountTag("2.545", "IntrmyAgt1Acct", "Intermediary Agent 1Account", TagType.OPTIONAL, 2));
        cdtTrfTxInf.getChildren().add(createIntermediaryAgent("2.564", "IntrmyAgt2", "Intermediary Agent 2", TagType.OPTIONAL, 2));
        cdtTrfTxInf.getChildren().add(createAccountTag("2.628", "IntrmyAgt2Acct", "Intermediary Agent 2Account", TagType.OPTIONAL, 2));
        cdtTrfTxInf.getChildren().add(createIntermediaryAgent("2.647", "IntrmyAgt3", "Intermediary Agent 3", TagType.OPTIONAL, 2));
        cdtTrfTxInf.getChildren().add(createAccountTag("2.711", "IntrmyAgt3Acct", "Intermediary Agent 3Account", TagType.OPTIONAL, 2));
        
        // Ultimate Debtor (Optional)
        cdtTrfTxInf.getChildren().add(createUltimateDebtorTag("2.730", "UltmtDbtr", "Ultimate Debtor", TagType.OPTIONAL, 2));
        
        // Initiating Party (Conditional)
        cdtTrfTxInf.getChildren().add(createInitiatingPartyTag("2.792", "InitgPty", "Initiating Party", TagType.CONDITIONAL, 2));
        
        // Debtor (Mandatory)
        cdtTrfTxInf.getChildren().add(createDebtorTag("2.854", "Dbtr", "Debtor", TagType.MANDATORY, 2));
        
        // Debtor Account (Mandatory)
        cdtTrfTxInf.getChildren().add(createDebtorAccountTag("2.916", "DbtrAcct", "Debtor Account", TagType.MANDATORY, 2));
        
        // Debtor Agent (Mandatory)
        cdtTrfTxInf.getChildren().add(createDebtorCreditorAgentTag("2.935", "DbtrAgt", "Debtor Agent", TagType.MANDATORY, 2));
        
        // Debtor Agent Account (Optional)
        cdtTrfTxInf.getChildren().add(createAccountTag("2.999", "DbtrAgtAcct", "Debtor Agent Account", TagType.OPTIONAL, 2));
        
        // Creditor Agent (Mandatory)
        cdtTrfTxInf.getChildren().add(createDebtorCreditorAgentTag("2.1018", "CdtrAgt", "Creditor Agent", TagType.MANDATORY, 2));
        
        // Creditor Agent Account (Optional)
        cdtTrfTxInf.getChildren().add(createAccountTag("2.1082", "CdtrAgtAcct", "Creditor Agent Account", TagType.OPTIONAL, 2));
        
        // Creditor (Mandatory)
        cdtTrfTxInf.getChildren().add(createCreditorTag("2.1101", "Cdtr", "Creditor", TagType.MANDATORY, 2));
        
        // Creditor Account (Mandatory)
        cdtTrfTxInf.getChildren().add(createCreditorAccountTag("2.1163", "CdtrAcct", "Creditor Account", TagType.MANDATORY, 2));
        
        // Ultimate Creditor (Optional)
        cdtTrfTxInf.getChildren().add(createUltimateCreditorTag("2.1182", "UltmtCdtr", "Ultimate Creditor", TagType.OPTIONAL, 2));
        
        // Instruction For Creditor Agent (Conditional)
        cdtTrfTxInf.getChildren().add(createInstructionForCreditorAgentTag("2.1244", "InstrForCdtrAgt", "Instruction For Creditor Agent", TagType.CONDITIONAL, 2));
        
        // Purpose (Optional)
        cdtTrfTxInf.getChildren().add(createPurposeTag("2.1250", "Purp", "Purpose", TagType.OPTIONAL, 2));
        
        // Related Remittance Information (Conditional)
        cdtTrfTxInf.getChildren().add(createRelatedRemittanceInfoTag("2.1316", "RltdRmtInf", "Related Remittance Information", TagType.CONDITIONAL, 2));
        
        // Remittance Information (Optional)
        cdtTrfTxInf.getChildren().add(createRemittanceInfoTag("2.1345", "RmtInf", "Remittance Information", TagType.OPTIONAL, 2));
        
        root.getChildren().add(cdtTrfTxInf);
        
        rootTags.add(root);
        indexTags(root);
    }

    private XmlTag createTag(String index, String xmlTag, String elementName, String occurrence, 
                              Integer length, TagType type, int level) {
        XmlTag tag = XmlTag.builder()
                .index(index)
                .xmlTag(xmlTag)
                .elementName(elementName)
                .occurrence(occurrence)
                .length(length)
                .type(type)
                .level(level)
                .selected(type == TagType.MANDATORY)
                .children(new ArrayList<>())
                .build();
        return tag;
    }

    private XmlTag createPreviousInstructingAgent(String index, String xmlTag, String elementName, 
                                                    TagType type, int level) {
        XmlTag agent = createTag(index, xmlTag, elementName, "[0..1]", null, type, level);
        
        // FinInstnId
        XmlTag finInstnId = createTag(index + ".1", "FinInstnId", "Financial Institution Identification", "[1..1]", null, TagType.MANDATORY, level + 1);
        finInstnId.setOrCondition(true); // Mark as OR condition
        
        // BICFI (Conditional - OR option 1)
        finInstnId.getChildren().add(createTag(index + ".2", "BICFI", "BICFI", "[0..1]", 11, TagType.CONDITIONAL, level + 2));
        
        // ClrSysMmbId (Conditional - OR option 2) with nested MmbId
        XmlTag clrSysMmbId = createTag(index + ".3", "ClrSysMmbId", "Clearing System Member Identification", "[0..1]", null, TagType.CONDITIONAL, level + 2);
        clrSysMmbId.getChildren().add(createTag(index + ".7", "MmbId", "Member Identification", "[1..1]", 9, TagType.MANDATORY, level + 3));
        finInstnId.getChildren().add(clrSysMmbId);
        
        agent.getChildren().add(finInstnId);
        return agent;
    }

    private XmlTag createIntermediaryAgent(String index, String xmlTag, String elementName, 
                                           TagType type, int level) {
        XmlTag agent = createTag(index, xmlTag, elementName, "[0..1]", null, type, level);
        
        // FinInstnId
        XmlTag finInstnId = createTag(index + ".1", "FinInstnId", "Financial Institution Identification", "[1..1]", null, TagType.MANDATORY, level + 1);
        finInstnId.setOrCondition(true); // Mark as OR condition
        
        // BICFI (Conditional - OR option 1)
        finInstnId.getChildren().add(createTag(index + ".2", "BICFI", "BICFI", "[0..1]", 11, TagType.CONDITIONAL, level + 2));
        
        // ClrSysMmbId (Conditional - OR option 2) with nested MmbId
        XmlTag clrSysMmbId = createTag(index + ".3", "ClrSysMmbId", "Clearing System Member Identification", "[0..1]", null, TagType.CONDITIONAL, level + 2);
        clrSysMmbId.getChildren().add(createTag(index + ".7", "MmbId", "Member Identification", "[1..1]", 9, TagType.MANDATORY, level + 3));
        finInstnId.getChildren().add(clrSysMmbId);
        
        agent.getChildren().add(finInstnId);
        return agent;
    }

    private XmlTag createAgentTag(String index, String xmlTag, String elementName, TagType type, int level) {
        XmlTag agent = createTag(index, xmlTag, elementName, "[1..1]", null, type, level);
        
        // FinInstnId - [1..1] - MANDATORY
        XmlTag finInstnId = createTag(index + ".1", "FinInstnId", "Financial Institution Identification", "[1..1]", null, TagType.MANDATORY, level + 1);
        
        // ClrSysMmbId - [1..1] - MANDATORY (No OR condition for Instructing/Instructed Agents)
        XmlTag clrSysMmbId = createTag(index + ".3", "ClrSysMmbId", "Clearing System Member Identification", "[1..1]", null, TagType.MANDATORY, level + 2);
        clrSysMmbId.getChildren().add(createTag(index + ".7", "MmbId", "Member Identification", "[1..1]", 9, TagType.MANDATORY, level + 3));
        finInstnId.getChildren().add(clrSysMmbId);
        
        agent.getChildren().add(finInstnId);
        return agent;
    }

    private XmlTag createDebtorCreditorAgentTag(String index, String xmlTag, String elementName, TagType type, int level) {
        XmlTag agent = createTag(index, xmlTag, elementName, "[1..1]", null, type, level);
        
        // FinInstnId - [1..1] - MANDATORY
        XmlTag finInstnId = createTag(index + ".1", "FinInstnId", "Financial Institution Identification", "[1..1]", null, TagType.MANDATORY, level + 1);
        finInstnId.setOrCondition(true); // Mark as OR condition for Debtor/Creditor Agents
        
        // BICFI - [0..1] - CONDITIONAL (OR option 1)
        finInstnId.getChildren().add(createTag(index + ".2", "BICFI", "BICFI", "[0..1]", 11, TagType.CONDITIONAL, level + 2));
        
        // ClrSysMmbId - [0..1] - CONDITIONAL (OR option 2)
        XmlTag clrSysMmbId = createTag(index + ".3", "ClrSysMmbId", "Clearing System Member Identification", "[0..1]", null, TagType.CONDITIONAL, level + 2);
        clrSysMmbId.getChildren().add(createTag(index + ".7", "MmbId", "Member Identification", "[1..1]", 9, TagType.MANDATORY, level + 3));
        finInstnId.getChildren().add(clrSysMmbId);
        
        agent.getChildren().add(finInstnId);
        return agent;
    }

    private XmlTag createAccountTag(String index, String xmlTag, String elementName, TagType type, int level) {
        XmlTag account = createTag(index, xmlTag, elementName, type == TagType.MANDATORY ? "[1..1]" : "[0..1]", null, type, level);
        
        // Id (Identification) - [1..1] - MANDATORY
        XmlTag id = createTag(index + ".1", "Id", "Identification", "[1..1]", null, TagType.MANDATORY, level + 1);
        id.setOrCondition(true); // Mark as OR condition for child elements
        
        // IBAN - [1..1] - CONDITIONAL (OR option 1)
        id.getChildren().add(createTag(index + ".2", "IBAN", "IBAN", "[1..1]", 34, TagType.CONDITIONAL, level + 2));
        
        // Othr (Other) - [1..1] - CONDITIONAL (OR option 2)
        XmlTag othr = createTag(index + ".3", "Othr", "Other", "[1..1]", null, TagType.CONDITIONAL, level + 2);
        othr.getChildren().add(createTag(index + ".4", "Id", "Identification", "[1..1]", 34, TagType.MANDATORY, level + 3));
        id.getChildren().add(othr);
        
        account.getChildren().add(id);
        return account;
    }

    private XmlTag createDebtorAccountTag(String index, String xmlTag, String elementName, TagType type, int level) {
        XmlTag account = createTag(index, xmlTag, elementName, "[1..1]", null, type, level);
        
        // Id (Identification) - [1..1] - MANDATORY
        XmlTag id = createTag(index + ".1", "Id", "Identification", "[1..1]", null, TagType.MANDATORY, level + 1);
        id.setOrCondition(true); // Mark as OR condition for child elements
        
        // IBAN - [1..1] - CONDITIONAL (OR option 1)
        id.getChildren().add(createTag(index + ".2", "IBAN", "IBAN", "[1..1]", 34, TagType.CONDITIONAL, level + 2));
        
        // Othr (Other) - [1..1] - CONDITIONAL (OR option 2)
        XmlTag othr = createTag(index + ".3", "Othr", "Other", "[1..1]", null, TagType.CONDITIONAL, level + 2);
        othr.getChildren().add(createTag(index + ".4", "Id", "Identification", "[1..1]", 34, TagType.MANDATORY, level + 3));
        id.getChildren().add(othr);
        
        account.getChildren().add(id);
        
        // Nm (Name) - [0..1] - OPTIONAL - specific to Debtor Account
        account.getChildren().add(createTag(index + ".13", "Nm", "Name", "[0..1]", 70, TagType.OPTIONAL, level + 1));
        
        return account;
    }

    private XmlTag createCreditorAccountTag(String index, String xmlTag, String elementName, TagType type, int level) {
        XmlTag account = createTag(index, xmlTag, elementName, "[1..1]", null, type, level);
        
        // Id (Identification) - [1..1] - MANDATORY
        XmlTag id = createTag(index + ".1", "Id", "Identification", "[1..1]", null, TagType.MANDATORY, level + 1);
        id.setOrCondition(true); // Mark as OR condition for child elements
        
        // IBAN - [1..1] - CONDITIONAL (OR option 1)
        id.getChildren().add(createTag(index + ".2", "IBAN", "IBAN", "[1..1]", 34, TagType.CONDITIONAL, level + 2));
        
        // Othr (Other) - [1..1] - CONDITIONAL (OR option 2)
        XmlTag othr = createTag(index + ".3", "Othr", "Other", "[1..1]", null, TagType.CONDITIONAL, level + 2);
        othr.getChildren().add(createTag(index + ".4", "Id", "Identification", "[1..1]", 34, TagType.MANDATORY, level + 3));
        id.getChildren().add(othr);
        
        account.getChildren().add(id);
        
        // Nm (Name) - [0..1] - OPTIONAL - specific to Creditor Account
        account.getChildren().add(createTag(index + ".13", "Nm", "Name", "[0..1]", 70, TagType.OPTIONAL, level + 1));
        
        // Prxy (Proxy) - [0..1] - OPTIONAL - specific to Creditor Account
        XmlTag prxy = createTag(index + ".14", "Prxy", "Proxy", "[0..1]", null, TagType.OPTIONAL, level + 1);
        prxy.getChildren().add(createTag(index + ".18", "Id", "Identification", "[1..1]", 256, TagType.MANDATORY, level + 2));
        account.getChildren().add(prxy);
        
        return account;
    }

    private XmlTag createUltimateDebtorTag(String index, String xmlTag, String elementName, TagType type, int level) {
        XmlTag party = createTag(index, xmlTag, elementName, "[0..1]", null, type, level);
        
        // Nm (Name) - [1..1] - MANDATORY
        party.getChildren().add(createTag(index + ".1", "Nm", "Name", "[1..1]", 140, TagType.MANDATORY, level + 1));
        
        // PstlAdr (Postal Address) - [0..1] - OPTIONAL
        party.getChildren().add(createPostalAddressTag(index + ".2", "PstlAdr", "Postal Address", TagType.OPTIONAL, level + 1));
        
        // Id (Identification) - [0..1] - CONDITIONAL with OR condition
        XmlTag id = createTag(index + ".54", "Id", "Identification", "[0..1]", null, TagType.CONDITIONAL, level + 1);
        id.setOrCondition(true);
        
        // OrgId (Organisation Identification) - [1..1] - CONDITIONAL
        XmlTag orgId = createTag(index + ".55", "OrgId", "Organisation Identification", "[1..1]", null, TagType.CONDITIONAL, level + 2);
        orgId.getChildren().add(createTag(index + ".56", "AnyBIC", "Any BIC", "[0..1]", 11, TagType.OPTIONAL, level + 3));
        orgId.getChildren().add(createTag(index + ".57", "LEI", "LEI", "[0..1]", 20, TagType.CONDITIONAL, level + 3));
        
        // Othr (Other) - [0..5] - OPTIONAL
        XmlTag othrOrg = createTag(index + ".58", "Othr", "Other", "[0..5]", null, TagType.OPTIONAL, level + 3);
        othrOrg.getChildren().add(createTag(index + ".59", "Id", "Identification", "[1..1]", 35, TagType.MANDATORY, level + 4));
        XmlTag schmeNmOrg = createTag(index + ".60", "SchmeNm", "Scheme Name", "[0..1]", null, TagType.OPTIONAL, level + 4);
        schmeNmOrg.getChildren().add(createTag(index + ".62", "Prtry", "Proprietary", "[1..1]", 35, TagType.MANDATORY, level + 5));
        othrOrg.getChildren().add(schmeNmOrg);
        orgId.getChildren().add(othrOrg);
        id.getChildren().add(orgId);
        
        // PrvtId (Private Identification) - [1..1] - CONDITIONAL
        XmlTag prvtId = createTag(index + ".64", "PrvtId", "Private Identification", "[1..1]", null, TagType.CONDITIONAL, level + 2);
        
        // DtAndPlcOfBirth (Date And Place Of Birth) - [0..1] - OPTIONAL
        XmlTag dtAndPlc = createTag(index + ".65", "DtAndPlcOfBirth", "Date And Place Of Birth", "[0..1]", null, TagType.OPTIONAL, level + 3);
        dtAndPlc.getChildren().add(createTag(index + ".66", "BirthDt", "Birth Date", "[1..1]", 10, TagType.MANDATORY, level + 4));
        dtAndPlc.getChildren().add(createTag(index + ".68", "CityOfBirth", "City Of Birth", "[1..1]", 35, TagType.MANDATORY, level + 4));
        dtAndPlc.getChildren().add(createTag(index + ".69", "CtryOfBirth", "Country Of Birth", "[1..1]", 2, TagType.MANDATORY, level + 4));
        prvtId.getChildren().add(dtAndPlc);
        
        // Othr (Other) - [0..5] - OPTIONAL for PrvtId
        XmlTag othrPrvt = createTag(index + ".70", "Othr", "Other", "[0..5]", null, TagType.OPTIONAL, level + 3);
        othrPrvt.getChildren().add(createTag(index + ".71", "Id", "Identification", "[1..1]", 35, TagType.MANDATORY, level + 4));
        XmlTag schmeNmPrvt = createTag(index + ".72", "SchmeNm", "Scheme Name", "[0..1]", null, TagType.OPTIONAL, level + 4);
        schmeNmPrvt.getChildren().add(createTag(index + ".74", "Prtry", "Proprietary", "[1..1]", 35, TagType.MANDATORY, level + 5));
        othrPrvt.getChildren().add(schmeNmPrvt);
        prvtId.getChildren().add(othrPrvt);
        
        id.getChildren().add(prvtId);
        party.getChildren().add(id);
        
        return party;
    }

    private XmlTag createPartyTag(String index, String xmlTag, String elementName, TagType type, int level) {
        XmlTag party = createTag(index, xmlTag, elementName, "[0..1]", null, type, level);
        party.getChildren().add(createTag(index + ".1", "Nm", "Name", "[1..1]", 140, TagType.MANDATORY, level + 1));
        party.getChildren().add(createPostalAddressTag(index + ".2", "PstlAdr", "Postal Address", TagType.OPTIONAL, level + 1));
        party.getChildren().add(createTag(index + ".54", "Id", "Identification", "[0..1]", null, TagType.CONDITIONAL, level + 1));
        
        return party;
    }

    private XmlTag createInitiatingPartyTag(String index, String xmlTag, String elementName, TagType type, int level) {
        XmlTag party = createTag(index, xmlTag, elementName, "[0..1]", null, type, level);
        party.getChildren().add(createTag(index + ".1", "Nm", "Name", "[1..1]", 140, TagType.MANDATORY, level + 1));
        party.getChildren().add(createTag(index + ".24", "Id", "Identification", "[0..1]", null, TagType.OPTIONAL, level + 1));
        
        XmlTag id = party.getChildren().get(1);
        id.getChildren().add(createTag(index + ".25", "OrgId", "Organisation Identification", "[1..1]", null, TagType.CONDITIONAL, level + 2));
        XmlTag orgId = id.getChildren().get(0);
        orgId.getChildren().add(createTag(index + ".28", "Othr", "Other", "[1..5]", null, TagType.MANDATORY, level + 3));
        XmlTag othr = orgId.getChildren().get(0);
        othr.getChildren().add(createTag(index + ".29", "Id", "Identification", "[1..1]", 35, TagType.MANDATORY, level + 4));
        othr.getChildren().add(createTag(index + ".30", "SchmeNm", "Scheme Name", "[0..1]", null, TagType.OPTIONAL, level + 4));
        XmlTag schmeNm = othr.getChildren().get(1);
        schmeNm.getChildren().add(createTag(index + ".31", "Prtry", "Proprietary", "[1..1]", 35, TagType.MANDATORY, level + 5));
        
        return party;
    }

    private XmlTag createDebtorTag(String index, String xmlTag, String elementName, TagType type, int level) {
        XmlTag debtor = createTag(index, xmlTag, elementName, "[1..1]", null, type, level);
        debtor.getChildren().add(createTag(index + ".1", "Nm", "Name", "[1..1]", 140, TagType.MANDATORY, level + 1));
        debtor.getChildren().add(createPostalAddressTag(index + ".2", "PstlAdr", "Postal Address", TagType.CONDITIONAL, level + 1));
        debtor.getChildren().add(createTag(index + ".24", "Id", "Identification", "[0..1]", null, TagType.CONDITIONAL, level + 1));
        
        XmlTag id = debtor.getChildren().get(2);
        id.getChildren().add(createTag(index + ".25", "OrgId", "Organisation Identification", "[1..1]", null, TagType.CONDITIONAL, level + 2));
        id.setOrCondition(true);
        XmlTag orgId = id.getChildren().get(0);
        orgId.getChildren().add(createTag(index + ".28", "LEI", "LEI", "[1..1]", 20, TagType.MANDATORY, level + 3));
        
        id.getChildren().add(createTag(index + ".34", "PrvtId", "Private Identification", "[1..1]", null, TagType.CONDITIONAL, level + 2));
        XmlTag prvtId = id.getChildren().get(1);
        prvtId.getChildren().add(createTag(index + ".35", "DtAndPlcOfBirth", "Date And Place Of Birth", "[1..1]", null, TagType.MANDATORY, level + 3));
        XmlTag dtAndPlc = prvtId.getChildren().get(0);
        dtAndPlc.getChildren().add(createTag(index + ".36", "BirthDt", "Birth Date", "[1..1]", 10, TagType.MANDATORY, level + 4));
        dtAndPlc.getChildren().add(createTag(index + ".38", "CityOfBirth", "City Of Birth", "[1..1]", 35, TagType.MANDATORY, level + 4));
        dtAndPlc.getChildren().add(createTag(index + ".39", "CtryOfBirth", "Country Of Birth", "[1..1]", 2, TagType.MANDATORY, level + 4));
        
        return debtor;
    }

    private XmlTag createCreditorTag(String index, String xmlTag, String elementName, TagType type, int level) {
        XmlTag creditor = createTag(index, xmlTag, elementName, "[1..1]", null, type, level);
        creditor.getChildren().add(createTag(index + ".1", "Nm", "Name", "[1..1]", 140, TagType.MANDATORY, level + 1));
        creditor.getChildren().add(createPostalAddressTag(index + ".2", "PstlAdr", "Postal Address", TagType.OPTIONAL, level + 1));
        creditor.getChildren().add(createTag(index + ".62", "Id", "Identification", "[0..1]", null, TagType.CONDITIONAL, level + 1));
        
        XmlTag id = creditor.getChildren().get(2);
        id.getChildren().add(createTag(index + ".63", "OrgId", "Organisation Identification", "[1..1]", null, TagType.CONDITIONAL, level + 2));
        id.setOrCondition(true);
        XmlTag orgId = id.getChildren().get(0);
        orgId.getChildren().add(createTag(index + ".66", "LEI", "LEI", "[1..1]", 20, TagType.MANDATORY, level + 3));
        
        id.getChildren().add(createTag(index + ".72", "PrvtId", "Private Identification", "[1..1]", null, TagType.CONDITIONAL, level + 2));
        XmlTag prvtId = id.getChildren().get(1);
        prvtId.getChildren().add(createTag(index + ".73", "DtAndPlcOfBirth", "Date And Place Of Birth", "[1..1]", null, TagType.MANDATORY, level + 3));
        XmlTag dtAndPlc = prvtId.getChildren().get(0);
        dtAndPlc.getChildren().add(createTag(index + ".74", "BirthDt", "Birth Date", "[1..1]", 10, TagType.MANDATORY, level + 4));
        dtAndPlc.getChildren().add(createTag(index + ".76", "CityOfBirth", "City Of Birth", "[1..1]", 35, TagType.MANDATORY, level + 4));
        dtAndPlc.getChildren().add(createTag(index + ".77", "CtryOfBirth", "Country Of Birth", "[1..1]", 2, TagType.MANDATORY, level + 4));
        
        return creditor;
    }

    private XmlTag createUltimateCreditorTag(String index, String xmlTag, String elementName, TagType type, int level) {
        XmlTag party = createTag(index, xmlTag, elementName, "[0..1]", null, type, level);
        
        // Nm (Name) - [1..1] - MANDATORY
        party.getChildren().add(createTag(index + ".1", "Nm", "Name", "[1..1]", 140, TagType.MANDATORY, level + 1));
        
        // PstlAdr (Postal Address) - [0..1] - OPTIONAL
        party.getChildren().add(createPostalAddressTag(index + ".2", "PstlAdr", "Postal Address", TagType.OPTIONAL, level + 1));
        
        // Id (Identification) - [0..1] - CONDITIONAL with OR condition
        XmlTag id = createTag(index + ".24", "Id", "Identification", "[0..1]", null, TagType.CONDITIONAL, level + 1);
        id.setOrCondition(true);
        
        // OrgId (Organisation Identification) - [1..1] - CONDITIONAL
        XmlTag orgId = createTag(index + ".25", "OrgId", "Organisation Identification", "[1..1]", null, TagType.CONDITIONAL, level + 2);
        orgId.getChildren().add(createTag(index + ".26", "AnyBIC", "Any BIC", "[0..1]", 11, TagType.OPTIONAL, level + 3));
        orgId.getChildren().add(createTag(index + ".27", "LEI", "LEI", "[0..1]", 20, TagType.CONDITIONAL, level + 3));
        
        // Othr (Other) - [0..5] - OPTIONAL
        XmlTag othrOrg = createTag(index + ".28", "Othr", "Other", "[0..5]", null, TagType.OPTIONAL, level + 3);
        othrOrg.getChildren().add(createTag(index + ".29", "Id", "Identification", "[1..1]", 35, TagType.MANDATORY, level + 4));
        XmlTag schmeNmOrg = createTag(index + ".30", "SchmeNm", "Scheme Name", "[0..1]", null, TagType.OPTIONAL, level + 4);
        schmeNmOrg.getChildren().add(createTag(index + ".32", "Prtry", "Proprietary", "[1..1]", 35, TagType.MANDATORY, level + 5));
        othrOrg.getChildren().add(schmeNmOrg);
        orgId.getChildren().add(othrOrg);
        id.getChildren().add(orgId);
        
        // PrvtId (Private Identification) - [1..1] - CONDITIONAL
        XmlTag prvtId = createTag(index + ".34", "PrvtId", "Private Identification", "[1..1]", null, TagType.CONDITIONAL, level + 2);
        
        // DtAndPlcOfBirth (Date And Place Of Birth) - [0..1] - OPTIONAL
        XmlTag dtAndPlc = createTag(index + ".35", "DtAndPlcOfBirth", "Date And Place Of Birth", "[0..1]", null, TagType.OPTIONAL, level + 3);
        dtAndPlc.getChildren().add(createTag(index + ".36", "BirthDt", "Birth Date", "[1..1]", 10, TagType.MANDATORY, level + 4));
        dtAndPlc.getChildren().add(createTag(index + ".38", "CityOfBirth", "City Of Birth", "[1..1]", 35, TagType.MANDATORY, level + 4));
        dtAndPlc.getChildren().add(createTag(index + ".39", "CtryOfBirth", "Country Of Birth", "[1..1]", 2, TagType.MANDATORY, level + 4));
        prvtId.getChildren().add(dtAndPlc);
        
        // Othr (Other) - [0..5] - OPTIONAL for PrvtId
        XmlTag othrPrvt = createTag(index + ".40", "Othr", "Other", "[0..5]", null, TagType.OPTIONAL, level + 3);
        othrPrvt.getChildren().add(createTag(index + ".41", "Id", "Identification", "[1..1]", 35, TagType.MANDATORY, level + 4));
        XmlTag schmeNmPrvt = createTag(index + ".42", "SchmeNm", "Scheme Name", "[0..1]", null, TagType.OPTIONAL, level + 4);
        schmeNmPrvt.getChildren().add(createTag(index + ".44", "Prtry", "Proprietary", "[1..1]", 35, TagType.MANDATORY, level + 5));
        othrPrvt.getChildren().add(schmeNmPrvt);
        prvtId.getChildren().add(othrPrvt);
        
        id.getChildren().add(prvtId);
        party.getChildren().add(id);
        
        return party;
    }

    private XmlTag createPostalAddressTag(String index, String xmlTag, String elementName, TagType type, int level) {
        XmlTag address = createTag(index, xmlTag, elementName, "[0..1]", null, type, level);
        address.getChildren().add(createTag(index + ".9", "StrtNm", "Street Name", "[1..1]", 70, TagType.MANDATORY, level + 1));
        address.getChildren().add(createTag(index + ".10", "BldgNb", "Building Number", "[0..1]", 16, TagType.OPTIONAL, level + 1));
        address.getChildren().add(createTag(index + ".16", "PstCd", "Post Code", "[1..1]", 16, TagType.MANDATORY, level + 1));
        address.getChildren().add(createTag(index + ".17", "TwnNm", "Town Name", "[1..1]", 35, TagType.MANDATORY, level + 1));
        address.getChildren().add(createTag(index + ".19", "CtrySubDvsn", "Country Sub Division", "[1..1]", 35, TagType.MANDATORY, level + 1));
        address.getChildren().add(createTag(index + ".20", "Ctry", "Country", "[1..1]", 2, TagType.MANDATORY, level + 1));
        address.getChildren().add(createTag(index + ".21", "AdrLine", "Address Line", "[0..1]", 70, TagType.OPTIONAL, level + 1));
        
        return address;
    }

    private XmlTag createInstructionForCreditorAgentTag(String index, String xmlTag, String elementName, TagType type, int level) {
        XmlTag tag = createTag(index, xmlTag, elementName, "[0..5]", null, type, level);
        tag.getChildren().add(createTag(index + ".1", "Cd", "Code", "[1..1]", 4, TagType.MANDATORY, level + 1));
        tag.getChildren().add(createTag(index + ".2", "InstrInf", "Instruction Information", "[1..1]", 140, TagType.MANDATORY, level + 1));
        return tag;
    }

    private XmlTag createPurposeTag(String index, String xmlTag, String elementName, TagType type, int level) {
        XmlTag tag = createTag(index, xmlTag, elementName, "[0..1]", null, type, level);
        tag.getChildren().add(createTag(index + ".1", "Cd", "Code", "[1..1]", 4, TagType.CONDITIONAL, level + 1));
        tag.setOrCondition(true);
        tag.getChildren().add(createTag(index + ".2", "Prtry", "Proprietary", "[1..1]", 35, TagType.CONDITIONAL, level + 1));
        return tag;
    }

    private XmlTag createRelatedRemittanceInfoTag(String index, String xmlTag, String elementName, TagType type, int level) {
        XmlTag tag = createTag(index, xmlTag, elementName, "[0..1]", null, type, level);
        tag.getChildren().add(createTag(index + ".1", "RmtId", "Remittance Identification", "[0..1]", 35, TagType.CONDITIONAL, level + 1));
        tag.getChildren().add(createTag(index + ".2", "RmtLctnDtls", "Remittance Location Details", "[0..1]", null, TagType.OPTIONAL, level + 1));
        tag.getChildren().add(createTag(index + ".7", "Mtd", "Method", "[1..1]", 4, TagType.MANDATORY, level + 1));
        tag.getChildren().add(createTag(index + ".8", "ElctrnccAdr", "Electronic Address", "[0..1]", 2048, TagType.CONDITIONAL, level + 1));
        return tag;
    }

    private XmlTag createRemittanceInfoTag(String index, String xmlTag, String elementName, TagType type, int level) {
        XmlTag tag = createTag(index, xmlTag, elementName, "[0..1]", null, type, level);
        
        // Ustrd (Unstructured) - [0..1] - OPTIONAL
        tag.getChildren().add(createTag(index + ".1", "Ustrd", "Unstructured", "[0..1]", 140, TagType.OPTIONAL, level + 1));
        
        // Strd (Structured) - [0..1] - OPTIONAL
        XmlTag strd = createTag(index + ".2", "Strd", "Structured", "[0..1]", null, TagType.OPTIONAL, level + 1);
        
        // RfrdDocInf (Referred Document Information) - [0..5] - CONDITIONAL
        XmlTag rfrdDocInf = createTag(index + ".3", "RfrdDocInf", "Referred Document Information", "[0..5]", null, TagType.CONDITIONAL, level + 2);
        
        // Tp (Type) - [0..1] - OPTIONAL
        XmlTag tp = createTag(index + ".4", "Tp", "Type", "[0..1]", null, TagType.OPTIONAL, level + 3);
        XmlTag cdOrPrtry = createTag(index + ".5", "CdOrPrtry", "Code Or Proprietary", "[1..1]", null, TagType.MANDATORY, level + 4);
        cdOrPrtry.setOrCondition(true);
        cdOrPrtry.getChildren().add(createTag(index + ".6", "Cd", "Code", "[1..1]", 4, TagType.CONDITIONAL, level + 5));
        cdOrPrtry.getChildren().add(createTag(index + ".7", "Prtry", "Proprietary", "[1..1]", 35, TagType.CONDITIONAL, level + 5));
        tp.getChildren().add(cdOrPrtry);
        rfrdDocInf.getChildren().add(tp);
        
        // Nb (Number) - [1..1] - MANDATORY
        rfrdDocInf.getChildren().add(createTag(index + ".9", "Nb", "Number", "[1..1]", 35, TagType.MANDATORY, level + 3));
        
        // RltdDt (Related Date) - [0..1] - CONDITIONAL
        rfrdDocInf.getChildren().add(createTag(index + ".10", "RltdDt", "Related Date", "[0..1]", 10, TagType.CONDITIONAL, level + 3));
        
        strd.getChildren().add(rfrdDocInf);
        
        // RfrdDocAmt (Referred Document Amount) - [0..1] - OPTIONAL
        XmlTag rfrdDocAmt = createTag(index + ".46", "RfrdDocAmt", "Referred Document Amount", "[0..1]", null, TagType.OPTIONAL, level + 2);
        
        // DscntApldAmt (Discount Applied Amount) - [0..2] - OPTIONAL
        XmlTag dscntApldAmt = createTag(index + ".49", "DscntApldAmt", "Discount Applied Amount", "[0..2]", null, TagType.OPTIONAL, level + 3);
        XmlTag tpDiscount = createTag(index + ".50", "Tp", "Type", "[1..1]", null, TagType.MANDATORY, level + 4);
        tpDiscount.getChildren().add(createTag(index + ".52", "Prtry", "Proprietary", "[1..1]", 4, TagType.MANDATORY, level + 5));
        dscntApldAmt.getChildren().add(tpDiscount);
        
        XmlTag amt = createTag(index + ".53", "Amt", "Amount", "[1..1]", 18, TagType.MANDATORY, level + 4);
        amt.getChildren().add(createTag(index + ".54", "Ccy", "Currency", "[required]", 3, TagType.MANDATORY, level + 5));
        dscntApldAmt.getChildren().add(amt);
        rfrdDocAmt.getChildren().add(dscntApldAmt);
        
        strd.getChildren().add(rfrdDocAmt);
        
        // CdtrRefInf (Creditor Reference Information) - [0..1] - OPTIONAL
        XmlTag cdtrRefInf = createTag(index + ".78", "CdtrRefInf", "Creditor Reference Information", "[0..1]", null, TagType.OPTIONAL, level + 2);
        XmlTag cdtrRefId = createTag(index + ".102", "Id", "Identification", "[1..1]", null, TagType.MANDATORY, level + 3);
        XmlTag cdtrOrgId = createTag(index + ".103", "OrgId", "Organisation Identification", "[1..1]", null, TagType.MANDATORY, level + 4);
        XmlTag cdtrOthr = createTag(index + ".106", "Othr", "Other", "[1..1]", null, TagType.MANDATORY, level + 5);
        cdtrOthr.getChildren().add(createTag(index + ".107", "Id", "Identification", "[1..1]", 35, TagType.MANDATORY, level + 6));
        cdtrOrgId.getChildren().add(cdtrOthr);
        cdtrRefId.getChildren().add(cdtrOrgId);
        cdtrRefInf.getChildren().add(cdtrRefId);
        strd.getChildren().add(cdtrRefInf);
        
        // Invcee (Invoicee) - [0..1] - OPTIONAL
        XmlTag invcee = createTag(index + ".140", "Invcee", "Invoicee", "[0..1]", null, TagType.OPTIONAL, level + 2);
        XmlTag invceeId = createTag(index + ".164", "Id", "Identification", "[1..1]", null, TagType.MANDATORY, level + 3);
        XmlTag invceeOrgId = createTag(index + ".165", "OrgId", "Organisation Identification", "[1..1]", null, TagType.MANDATORY, level + 4);
        XmlTag invceeOthr = createTag(index + ".168", "Othr", "Other", "[1..1]", null, TagType.MANDATORY, level + 5);
        invceeOthr.getChildren().add(createTag(index + ".169", "Id", "Identification", "[1..1]", 35, TagType.MANDATORY, level + 6));
        invceeOrgId.getChildren().add(invceeOthr);
        invceeId.getChildren().add(invceeOrgId);
        invcee.getChildren().add(invceeId);
        strd.getChildren().add(invcee);
        
        tag.getChildren().add(strd);
        
        return tag;
    }

    private void indexTags(XmlTag tag) {
        tagIndex.put(tag.getIndex(), tag);
        for (XmlTag child : tag.getChildren()) {
            indexTags(child);
        }
    }

    public List<XmlTag> getAllTags() {
        return rootTags;
    }

    public XmlTag getTagByIndex(String index) {
        return tagIndex.get(index);
    }

    public Map<String, XmlTag> getTagIndex() {
        return tagIndex;
    }
}

