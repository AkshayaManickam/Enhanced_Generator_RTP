package com.finzly.galaxy.rtp.mapper;

import com.finzly.galaxy.rtp.model.*;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

/**
 * Main mapper interface for PACS.008 message types
 */
@Mapper(componentModel = "spring")
public interface Pacs008Mapper {

    // FIToFI Customer Credit Transfer mappings
    @Mapping(target = "grpHdr", source = "grpHdr")
    @Mapping(target = "cdtTrfTxInf", source = "cdtTrfTxInf")
    FIToFICustomerCreditTransferV08 mapFIToFICustomerCreditTransfer(FIToFICustomerCreditTransferV08 source);

    // Group Header mappings
    @Mapping(target = "msgId", source = "msgId")
    @Mapping(target = "creDtTm", source = "creDtTm")
    @Mapping(target = "nbOfTxs", source = "nbOfTxs")
    @Mapping(target = "ttlIntrBkSttlmAmt", source = "ttlIntrBkSttlmAmt")
    @Mapping(target = "intrBkSttlmDt", source = "intrBkSttlmDt")
    @Mapping(target = "sttlmInf", source = "sttlmInf")
    GroupHeader93 mapGroupHeader(GroupHeader93 source);

    // Credit Transfer Transaction mappings
    @Mapping(target = "pmtId", source = "pmtId")
    @Mapping(target = "pmtTpInf", source = "pmtTpInf")
    @Mapping(target = "intrBkSttlmAmt", source = "intrBkSttlmAmt")
    @Mapping(target = "chrgBr", source = "chrgBr")
    @Mapping(target = "prvsInstgAgt1", source = "prvsInstgAgt1")
    @Mapping(target = "prvsInstgAgt1Acct", source = "prvsInstgAgt1Acct")
    @Mapping(target = "prvsInstgAgt2", source = "prvsInstgAgt2")
    @Mapping(target = "prvsInstgAgt2Acct", source = "prvsInstgAgt2Acct")
    @Mapping(target = "prvsInstgAgt3", source = "prvsInstgAgt3")
    @Mapping(target = "prvsInstgAgt3Acct", source = "prvsInstgAgt3Acct")
    @Mapping(target = "instgAgt", source = "instgAgt")
    @Mapping(target = "instdAgt", source = "instdAgt")
    @Mapping(target = "intrmyAgt1", source = "intrmyAgt1")
    @Mapping(target = "intrmyAgt1Acct", source = "intrmyAgt1Acct")
    @Mapping(target = "intrmyAgt2", source = "intrmyAgt2")
    @Mapping(target = "intrmyAgt2Acct", source = "intrmyAgt2Acct")
    @Mapping(target = "intrmyAgt3", source = "intrmyAgt3")
    @Mapping(target = "intrmyAgt3Acct", source = "intrmyAgt3Acct")
    @Mapping(target = "ultmtDbtr", source = "ultmtDbtr")
    @Mapping(target = "initgPty", source = "initgPty")
    @Mapping(target = "dbtr", source = "dbtr")
    @Mapping(target = "dbtrAcct", source = "dbtrAcct")
    @Mapping(target = "dbtrAgt", source = "dbtrAgt")
    @Mapping(target = "dbtrAgtAcct", source = "dbtrAgtAcct")
    @Mapping(target = "cdtrAgt", source = "cdtrAgt")
    @Mapping(target = "cdtrAgtAcct", source = "cdtrAgtAcct")
    @Mapping(target = "cdtr", source = "cdtr")
    @Mapping(target = "cdtrAcct", source = "cdtrAcct")
    @Mapping(target = "ultmtCdtr", source = "ultmtCdtr")
    @Mapping(target = "instrForCdtrAgt", source = "instrForCdtrAgt")
    @Mapping(target = "purp", source = "purp")
    @Mapping(target = "rltdRmtInf", source = "rltdRmtInf")
    @Mapping(target = "rmtInf", source = "rmtInf")
    CreditTransferTransaction39 mapCreditTransferTransaction(CreditTransferTransaction39 source);
}
