package com.finzly.galaxy.rtp.util;

import jakarta.xml.bind.annotation.adapters.XmlAdapter;
import java.time.LocalDateTime;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class LocalDateAdapter extends XmlAdapter<String, LocalDate> {
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ISO_DATE;
    public LocalDate unmarshal(String v) { return LocalDate.parse(v, FORMATTER); }
    public String marshal(LocalDate v) { return v.format(FORMATTER); }
}
