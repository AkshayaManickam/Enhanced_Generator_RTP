package com.finzly.galaxy.rtp.service;

import com.finzly.galaxy.rtp.model.*;
import com.finzly.galaxy.rtp.util.XmlProcessor;
import com.finzly.galaxy.rtp.validator.ValidationResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
public class XmlGeneratorService {

    private static final List<String> CHRG_BR_VALUES = List.of("SLEV");
    private static final List<String> SVC_LVL_VALUES = List.of("SDVA");
    private static final List<String> CTGY_PURP_VALUES = List.of("BUSINESS", "CONSUMER");

    private Document createSampleDocument(int fileIndex) {
        // -------------------- Group Header --------------------
        GroupHeader93 groupHeader = new GroupHeader93();
        groupHeader.setMsgId("MSG" + System.currentTimeMillis() + "-" + fileIndex);
        groupHeader.setCreDtTm(LocalDateTime.now());
        groupHeader.setNbOfTxs("1");
        groupHeader.setTtlIntrBkSttlmAmt(new ActiveCurrencyAndAmount(new BigDecimal("1000.00"), "USD"));
        groupHeader.setIntrBkSttlmDt(LocalDate.now());

        // Settlement Info
        SettlementInstruction7 settlementInfo = new SettlementInstruction7();
        settlementInfo.setSttlmMtd("CLRG");
        ClearingSystemIdentification3Choice clearingSystem = new ClearingSystemIdentification3Choice();
        clearingSystem.setCd("TCH");
        settlementInfo.setClrSys(clearingSystem);
        groupHeader.setSttlmInf(settlementInfo);

        // -------------------- Payment Id --------------------
        PaymentIdentification7 paymentId = new PaymentIdentification7();
        paymentId.setInstrId("INSTR" + System.currentTimeMillis() + "-" + fileIndex);
        paymentId.setEndToEndId("E2E" + System.currentTimeMillis() + "-" + fileIndex);
        paymentId.setTxId("TX" + System.currentTimeMillis() + "-" + fileIndex);

        // -------------------- Payment Type Info --------------------
        PaymentTypeInformation28 paymentTypeInfo = new PaymentTypeInformation28();

        ServiceLevel8Choice serviceLevel = new ServiceLevel8Choice();
        serviceLevel.setCd(SVC_LVL_VALUES.get(fileIndex % SVC_LVL_VALUES.size()));
        paymentTypeInfo.setSvcLvl(serviceLevel);

        LocalInstrument2Choice localInstrument = new LocalInstrument2Choice();
        localInstrument.setPrtry("STANDARD");
        paymentTypeInfo.setLclInstrm(localInstrument);

        CategoryPurpose1Choice categoryPurpose = new CategoryPurpose1Choice();
        categoryPurpose.setPrtry(CTGY_PURP_VALUES.get(fileIndex % CTGY_PURP_VALUES.size()));
        paymentTypeInfo.setCtgyPurp(categoryPurpose);

        // -------------------- Credit Transfer --------------------
        CreditTransferTransaction39 creditTransfer = new CreditTransferTransaction39();
        creditTransfer.setPmtId(paymentId);
        creditTransfer.setPmtTpInf(paymentTypeInfo);
        creditTransfer.setIntrBkSttlmAmt(new ActiveCurrencyAndAmount(new BigDecimal("1000.00"), "USD"));
        creditTransfer.setChrgBr(CHRG_BR_VALUES.get(fileIndex % CHRG_BR_VALUES.size()));

        // -------------------- Agents / Banks --------------------
        BranchAndFinancialInstitutionIdentification6 instgAgt = new BranchAndFinancialInstitutionIdentification6();
        FinancialInstitutionIdentification18 finInstnId = new FinancialInstitutionIdentification18();
        finInstnId.setBicfi("BOFAUS3N");
        instgAgt.setFinInstnId(finInstnId);
        creditTransfer.setInstgAgt(instgAgt);

        BranchAndFinancialInstitutionIdentification6 instdAgt = new BranchAndFinancialInstitutionIdentification6();
        FinancialInstitutionIdentification18 finInstnId2 = new FinancialInstitutionIdentification18();
        finInstnId2.setBicfi("CHASUS33");
        instdAgt.setFinInstnId(finInstnId2);
        creditTransfer.setInstdAgt(instdAgt);

        // -------------------- Debtor / Sender --------------------
        PartyIdentification135 debtor = new PartyIdentification135();
        debtor.setNm("John Doe");
        PostalAddress24 debtorAddress = new PostalAddress24();
            debtorAddress.setStrtNm("123 Main St");
            debtorAddress.setTwnNm("New York");
            debtorAddress.setPstCd("10001");
            debtorAddress.setCtrySubDvsn("CA");
            debtorAddress.setCtry("US");
        debtor.setPstlAdr(debtorAddress);
        creditTransfer.setDbtr(debtor);

        CashAccount38 debtorAccount = new CashAccount38();
        AccountIdentification4Choice debtorAccountId = new AccountIdentification4Choice();
        debtorAccountId.setIban("US12345678901234567890");
        debtorAccount.setId(debtorAccountId);
        debtorAccount.setNm("John Doe Account");
        creditTransfer.setDbtrAcct(debtorAccount);
        creditTransfer.setDbtrAgt(instgAgt);

        // -------------------- Creditor / Receiver --------------------
        PartyIdentification135 creditor = new PartyIdentification135();
        creditor.setNm("Jane Smith");
        PostalAddress24 creditorAddress = new PostalAddress24();
            creditorAddress.setStrtNm("456 Market St");
            creditorAddress.setTwnNm("San Francisco");
            creditorAddress.setPstCd("94105");
            creditorAddress.setCtrySubDvsn("CA");
            creditorAddress.setCtry("US");
        creditor.setPstlAdr(creditorAddress);
        creditTransfer.setCdtr(creditor);

        CashAccount38 creditorAccount = new CashAccount38();
        AccountIdentification4Choice creditorAccountId = new AccountIdentification4Choice();
        creditorAccountId.setIban("US09876543210987654321");
        creditorAccount.setId(creditorAccountId);
        creditorAccount.setNm("Jane Smith Account");
        creditTransfer.setCdtrAcct(creditorAccount);
        creditTransfer.setCdtrAgt(instdAgt);

        // -------------------- Assemble Document --------------------
        FIToFICustomerCreditTransferV08 fiToFICustomerCreditTransfer = new FIToFICustomerCreditTransferV08();
        fiToFICustomerCreditTransfer.setGrpHdr(groupHeader);
        fiToFICustomerCreditTransfer.setCdtTrfTxInf(creditTransfer);

        Document document = new Document();
        document.setFiToFICstmrCdtTrf(fiToFICustomerCreditTransfer);

        return document;
    }


    public List<String> generateMultipleFiles(int numberOfFiles) throws Exception {
        XmlProcessor xmlProcessor = new XmlProcessor(
                getClass().getClassLoader().getResource("xsd/pacs008.xsd").toURI().getPath()
        );
        List<String> xmlContents = new ArrayList<>();
        for (int i = 1; i <= numberOfFiles; i++) {
            Document document = createSampleDocument(i);

            String xmlContent = xmlProcessor.marshalToXml(document);
            xmlContents.add(xmlContent);
            ValidationResult validationResult = xmlProcessor.validateXml(xmlContent);
            if (!validationResult.isValid()) {
                log.error("Validation failed for file {}: {}", i, validationResult.getErrorSummary());
                continue;
            }

            Path outputFile = Paths.get("output/pacs.008." + i + ".xml");
            Files.createDirectories(outputFile.getParent());
            Files.writeString(outputFile, xmlContent);

            log.info("Generated file: {}", outputFile.toAbsolutePath());
        }
        return xmlContents;
    }
}
