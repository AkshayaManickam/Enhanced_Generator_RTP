package com.finzly.galaxy.rtp.util;
import jakarta.xml.bind.annotation.adapters.XmlAdapter;
import java.time.LocalDateTime;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;


    public class LocalDateTimeAdapter extends XmlAdapter<String, LocalDateTime> {
        private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ISO_DATE_TIME;
        public LocalDateTime unmarshal(String v) { return LocalDateTime.parse(v, FORMATTER); }
        public String marshal(LocalDateTime v) { return v.format(FORMATTER); }

    }


