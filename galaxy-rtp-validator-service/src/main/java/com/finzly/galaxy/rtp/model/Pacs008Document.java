package com.finzly.galaxy.rtp.model;

import com.fasterxml.jackson.dataformat.xml.annotation.*;
import java.util.List;

@JacksonXmlRootElement(localName = "Document", namespace = Pacs008Document.ISO_NS)
public class Pacs008Document {

    public static final String ISO_NS = "urn:iso:std:iso:20022:tech:xsd:pacs.008.001.08";

    @JacksonXmlProperty(localName = "FIToFICstmrCdtTrf", namespace = ISO_NS)
    public FIToFICstmrCdtTrf fiToFICstmrCdtTrf;

    public static class FIToFICstmrCdtTrf {
        @JacksonXmlProperty(localName = "GrpHdr", namespace = ISO_NS)
        public GrpHdr grpHdr;

        @JacksonXmlElementWrapper(useWrapping = false)
        @JacksonXmlProperty(localName = "CdtTrfTxInf", namespace = ISO_NS)
        public List<CdtTrfTxInf> cdtTrfTxInf;
    }

    public static class GrpHdr {
        @JacksonXmlProperty(localName = "MsgId", namespace = ISO_NS)
        public String MsgId;

        @JacksonXmlProperty(localName = "CreDtTm", namespace = ISO_NS)
        public String CreDtTm;

        @JacksonXmlProperty(localName = "NbOfTxs", namespace = ISO_NS)
        public String NbOfTxs;

        @JacksonXmlProperty(localName = "TtlIntrBkSttlmAmt", namespace = ISO_NS)
        public ActiveCurrencyAndAmount TtlIntrBkSttlmAmt;

        @JacksonXmlProperty(localName = "IntrBkSttlmDt", namespace = ISO_NS)
        public String IntrBkSttlmDt;

        @JacksonXmlProperty(localName = "SttlmInf", namespace = ISO_NS)
        public SttlmInf SttlmInf;

        @JacksonXmlProperty(localName = "InstgAgt", namespace = ISO_NS)
        public Agent InstgAgt; // contains FinInstnId

        @JacksonXmlProperty(localName = "InstdAgt", namespace = ISO_NS)
        public Agent InstdAgt; // contains FinInstnId
    }

    public static class SttlmInf {
        @JacksonXmlProperty(localName = "SttlmMtd", namespace = ISO_NS)
        public String SttlmMtd;

        @JacksonXmlProperty(localName = "ClrSysRef", namespace = ISO_NS)
        public String ClrSysRef;
    }

    public static class CdtTrfTxInf {
        @JacksonXmlProperty(localName = "PmtId", namespace = ISO_NS)
        public PmtId PmtId;

        @JacksonXmlProperty(localName = "IntrBkSttlmAmt", namespace = ISO_NS)
        public ActiveCurrencyAndAmount IntrBkSttlmAmt;

        @JacksonXmlProperty(localName = "ChrgBr", namespace = ISO_NS)
        public String ChrgBr;

        @JacksonXmlProperty(localName = "InstgAgt", namespace = ISO_NS)
        public Agent InstgAgt;

        @JacksonXmlProperty(localName = "InstdAgt", namespace = ISO_NS)
        public Agent InstdAgt;

        @JacksonXmlProperty(localName = "Dbtr", namespace = ISO_NS)
        public Party Dbtr;

        @JacksonXmlProperty(localName = "DbtrAcct", namespace = ISO_NS)
        public Account DbtrAcct;

        @JacksonXmlProperty(localName = "UltmtDbtr", namespace = ISO_NS)
        public Party UltmtDbtr;

        @JacksonXmlProperty(localName = "Cdtr", namespace = ISO_NS)
        public Party Cdtr;

        @JacksonXmlProperty(localName = "CdtrAcct", namespace = ISO_NS)
        public Account CdtrAcct;

        @JacksonXmlProperty(localName = "UltmtCdtr", namespace = ISO_NS)
        public Party UltmtCdtr;

        @JacksonXmlProperty(localName = "RmtInf", namespace = ISO_NS)
        public RmtInf RmtInf;

        @JacksonXmlProperty(localName = "Purp", namespace = ISO_NS)
        public Purp Purp;

        @JacksonXmlProperty(localName = "ChrgsInf", namespace = ISO_NS)
        @JacksonXmlElementWrapper(useWrapping = false)
        public List<ChrgsInf> ChrgsInf;

        @JacksonXmlProperty(localName = "InstrForCdtrAgt", namespace = ISO_NS)
        public InstrForCdtrAgt InstrForCdtrAgt;

        @JacksonXmlProperty(localName = "AddtlInf", namespace = ISO_NS)
        public String AddtlInf;
    }


    // ---------------- Common Amount Type ----------------
    public static class ActiveCurrencyAndAmount {
        @JacksonXmlProperty(isAttribute = true, localName = "Ccy")
        public String Ccy;

        @JacksonXmlText
        public String value;
    }

    // ---------------- Payment Identification ----------------
    public static class PmtId {
        @JacksonXmlProperty(localName = "InstrId", namespace = ISO_NS)
        public String InstrId;

        @JacksonXmlProperty(localName = "EndToEndId", namespace = ISO_NS)
        public String EndToEndId;

        @JacksonXmlProperty(localName = "TxId", namespace = ISO_NS)
        public String TxId;

        @JacksonXmlProperty(localName = "ClrSysRef", namespace = ISO_NS)
        public String ClrSysRef;
    }

    public static class Agent {
        @JacksonXmlProperty(localName = "FinInstnId", namespace = ISO_NS)
        public FinInstnId FinInstnId;
    }

    public static class FinInstnId {
        @JacksonXmlProperty(localName = "BIC", namespace = ISO_NS)
        public String BIC;           // Mandatory

        @JacksonXmlProperty(localName = "Nm", namespace = ISO_NS)
        public String Nm;            // Optional

        @JacksonXmlProperty(localName = "ClrSysMmbId", namespace = ISO_NS)
        public String ClrSysMmbId;   // Optional
    }

    public static class Party {
        @JacksonXmlProperty(localName = "Nm", namespace = ISO_NS)
        public String Nm;

        @JacksonXmlProperty(localName = "PstlAdr", namespace = ISO_NS)
        public PstlAdr PstlAdr;

        @JacksonXmlProperty(localName = "Id", namespace = ISO_NS)
        public PartyId Id;
    }

    public static class PartyId {
        @JacksonXmlProperty(localName = "OrgId", namespace = ISO_NS)
        public String OrgId;

        @JacksonXmlProperty(localName = "PrvtId", namespace = ISO_NS)
        public String PrvtId;
    }

    public static class PstlAdr {
        @JacksonXmlProperty(localName = "Ctry", namespace = ISO_NS)
        public String Ctry;

        @JacksonXmlProperty(localName = "AdrLine", namespace = ISO_NS)
        @JacksonXmlElementWrapper(useWrapping = false)
        public List<String> AdrLine;
    }

    public static class Account {
        @JacksonXmlProperty(localName = "Id", namespace = ISO_NS)
        public AccountId Id;

        @JacksonXmlProperty(localName = "Ccy", namespace = ISO_NS)
        public String Ccy;
    }

    public static class AccountId {
        @JacksonXmlProperty(localName = "IBAN", namespace = ISO_NS)
        public String IBAN;

        @JacksonXmlProperty(localName = "OthrId", namespace = ISO_NS)
        public String OthrId;
    }

    public static class RmtInf {
        @JacksonXmlProperty(localName = "Ustrd", namespace = ISO_NS)
        @JacksonXmlElementWrapper(useWrapping = false)
        public List<String> Ustrd;

        @JacksonXmlProperty(localName = "Strd", namespace = ISO_NS)
        @JacksonXmlElementWrapper(useWrapping = false)
        public List<StructuredRemittance> Strd;
    }

    public static class StructuredRemittance {
        @JacksonXmlProperty(localName = "RmtId", namespace = ISO_NS)
        public String RmtId;

        @JacksonXmlProperty(localName = "Tp", namespace = ISO_NS)
        public String Tp;

        @JacksonXmlProperty(localName = "Issr", namespace = ISO_NS)
        public String Issr;

        @JacksonXmlProperty(localName = "RmtAmt", namespace = ISO_NS)
        public ActiveCurrencyAndAmount RmtAmt;
    }

    public static class ChrgsInf {
        @JacksonXmlProperty(localName = "Amt", namespace = ISO_NS)
        public ActiveCurrencyAndAmount Amt;

        @JacksonXmlProperty(localName = "CdtDbtInd", namespace = ISO_NS)
        public String CdtDbtInd;

        @JacksonXmlProperty(localName = "ChrgBr", namespace = ISO_NS)
        public String ChrgBr;

        @JacksonXmlProperty(localName = "Tp", namespace = ISO_NS)
        public String Tp;
    }

    public static class InstrForCdtrAgt {
        @JacksonXmlProperty(localName = "Cd", namespace = ISO_NS)
        @JacksonXmlElementWrapper(useWrapping = false)
        public List<String> Cd;
    }

    public static class Purp {
        @JacksonXmlProperty(localName = "Cd", namespace = ISO_NS)
        public String Cd;
    }



}
