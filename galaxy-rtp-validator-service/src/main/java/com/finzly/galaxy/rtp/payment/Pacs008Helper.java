package com.finzly.galaxy.rtp.payment;

import org.springframework.stereotype.Component;

import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Random;
import java.util.UUID;

@Component
public class Pacs008Helper {

    public String pacs008Message(String message){
        String bizMsgIdr = generateBizMsgIdr("990000001T1");
        String creDt = getCurrentCreDt();
        String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<Message\n" +
                "    xmlns:ct=\"urn:iso:std:iso:20022:tech:xsd:pacs.008.001.08\"\n" +
                "    xmlns:head=\"urn:iso:std:iso:20022:tech:xsd:head.001.001.01\" p3:schemaLocation=\"urn:tch messages.xsd\"\n" +
                "    xmlns:p3=\"http://www.w3.org/2001/XMLSchema-instance\"\n" +
                "    xmlns=\"urn:tch\">\n" +
                "    <AppHdr>\n" +
                "        <head:Fr>\n" +
                "            <head:FIId>\n" +
                "                <head:FinInstnId>\n" +
                "                    <head:ClrSysMmbId>\n" +
                "                        <head:MmbId>990000001T1</head:MmbId>\n" +//replace
                "                    </head:ClrSysMmbId>\n" +
                "                </head:FinInstnId>\n" +
                "                <head:BrnchId>\n" +
                "                    <head:Id>101010101MT</head:Id>\n" +//replace
                "                </head:BrnchId>\n" +
                "            </head:FIId>\n" +
                "        </head:Fr>\n" +
                "        <head:To>\n" +
                "            <head:FIId>\n" +
                "                <head:FinInstnId>\n" +
                "                    <head:ClrSysMmbId>\n" +
                "                        <head:MmbId>653060183A1</head:MmbId>\n" +//replace
                "                    </head:ClrSysMmbId>\n" +
                "                </head:FinInstnId>\n" +
                "            </head:FIId>\n" +
                "        </head:To>\n" +
                "        <head:BizMsgIdr>"+bizMsgIdr+"</head:BizMsgIdr>\n" +//replace
                "        <head:MsgDefIdr>pacs.008.001.08</head:MsgDefIdr>\n" +
                "        <head:CreDt>"+creDt+"</head:CreDt>\n" +//replace
                "        <head:Sgntr>\n" +
                "            <ds:Signature\n" +
                "                xmlns:ds=\"http://www.w3.org/2000/09/xmldsig#\">\n" +
                "                <ds:SignedInfo>\n" +
                "                    <ds:CanonicalizationMethod Algorithm=\"http://www.w3.org/TR/2001/REC-xml-c14n-20010315\" />\n" +
                "                    <ds:SignatureMethod Algorithm=\"http://www.w3.org/2001/04/xmldsig-more#rsa-sha256\" />\n" +
                "                    <ds:Reference URI=\"\">\n" +
                "                        <ds:Transforms>\n" +
                "                            <ds:Transform Algorithm=\"http://www.w3.org/2000/09/xmldsig#enveloped-signature\" />\n" +
                "                            <ds:Transform Algorithm=\"http://www.w3.org/2006/12/xml-c14n11\" />\n" +
                "                        </ds:Transforms>\n" +
                "                        <ds:DigestMethod Algorithm=\"http://www.w3.org/2001/04/xmlenc#sha256\" />\n" +
                "                        <ds:DigestValue>ViuJ00r+gBUcp3vEb0PDsNqfhkCygNlwoCEujPrXw7U=</ds:DigestValue>\n" +
                "                    </ds:Reference>\n" +
                "                </ds:SignedInfo>\n" +
                "                <ds:SignatureValue>uWwt/zWJvYWud6s6wmgEOm4LIGnQhuTJmXA4MxVe0r30t9YoJbmdrPt3GV8WXEMlkqukS07kzUn0ya2hiW6OARU/ZfE/PJW5w/2jeGH2ypDEoJuIQ5Eh1QaqOvyOy5RapA9liH/Q2QVTO1uyEDqm/n0g4eA7nzxjKS/8UKHq9FiNZJ06Jqml7Zvv/3VF3Ls/Qymo1ybtSzrfGfUwGtp4j7RH8MBF1eA/H6gGzyp7VK+zP4jysWHug2SAudDregmaGNONzjPzYO5srpHzHd7DK4T1eKYz5mm4tdcpfh7bdEF2wAeKV4T5dI+lOfr969rzcPjUVcLrL5FMQHEnpPHMfQ==</ds:SignatureValue>\n" +
                "                <ds:KeyInfo>\n" +
                "                    <ds:X509Data>\n" +
                "                        <ds:X509SubjectName>CN=Open Test Solutions,OU=OTS,O=FIS,L=Diegem,ST=Vlaams-Brabant,C=BE</ds:X509SubjectName>\n" +
                "                        <ds:X509IssuerSerial>\n" +
                "                            <ds:X509IssuerName>CN=Open Test Solutions, OU=OTS, O=FIS, L=Diegem, ST=Vlaams-Brabant, C=BE</ds:X509IssuerName>\n" +
                "                            <ds:X509SerialNumber>610326338160951572</ds:X509SerialNumber>\n" +//replace
                "                        </ds:X509IssuerSerial>\n" +
                "                    </ds:X509Data>\n" +
                "                </ds:KeyInfo>\n" +
                "            </ds:Signature>\n" +
                "        </head:Sgntr>\n" +
                "    </AppHdr>\n" +
                "    <CreditTransfer>";
        return xml + message +"    </CreditTransfer>\n" + "</Message>";
    }

    public static String getCurrentCreDt() {
        // ISO 8601 format with UTC timezone
        DateTimeFormatter formatter = DateTimeFormatter.ISO_OFFSET_DATE_TIME;
        return ZonedDateTime.now(java.time.ZoneOffset.UTC).format(formatter);
    }
    public static String generateBizMsgIdr(String senderId) {
        String systemRef = "HOTS" + String.format("%011d", new Random().nextInt(999999999));

        String date = LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE); // YYYYMMDD
        if (systemRef == null || systemRef.isEmpty()) {
            // fallback: short UUID
            systemRef = UUID.randomUUID().toString().replace("-", "").substring(0, 15).toUpperCase();
        }
        return "B" + date + senderId + systemRef;
    }
}
