package com.loganalyzer.util;

import java.io.IOException;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class JsonDateSerializer extends JsonSerializer<Timestamp> {

    @Value("${timestamp}")
    private String timestamp;

    @Override
    public void serialize(Timestamp timestamp, JsonGenerator gen, SerializerProvider provider)
            throws IOException, JsonProcessingException {

        Instant instance = Instant.ofEpochMilli(timestamp.getTime());
        ZonedDateTime zonedDateTime = ZonedDateTime.ofInstant(instance,java.time.ZoneId.of("Asia/Kolkata"));

        DateTimeFormatter formatter = java.time.format.DateTimeFormatter.ofPattern("yyyy-MMM-dd EEE HH:mm:ss.SSS");
        String string = zonedDateTime.format(formatter);
        gen.writeString(string);

    }
}
