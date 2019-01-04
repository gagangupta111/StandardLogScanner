package com.loganalyzer.util;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

@Component
public class JsonDateSerializer extends JsonSerializer<Long> {

    @Value("${timestamp}")
    private String timestamp;

    @Override
    public void serialize(Long timestamp, JsonGenerator gen, SerializerProvider provider)
            throws IOException, JsonProcessingException {

        Instant instance = Instant.ofEpochMilli(timestamp);
        ZonedDateTime zonedDateTime = ZonedDateTime.ofInstant(instance,java.time.ZoneId.of("Asia/Kolkata"));

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MMM-dd EEE HH:mm:ss.SSS");
        String string = zonedDateTime.format(formatter);
        gen.writeString(string);

    }

}
