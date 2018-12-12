package com.loganalyzer.util;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

/**
 * Used to serialize Java.util.Date, which is not a common JSON
 * type, so we have to create a custom serialize method;.
 *
 * @author Loiane Groner
 * http://loianegroner.com (English)
 * http://loiane.com (Portuguese)
 */

@Component
public class JsonDateSerializer extends JsonSerializer<Date> {

    private static SimpleDateFormat dateFormat;

    @Value("${timestamp}")
    private String timestamp;

    @PostConstruct
    public void initialize() {

        dateFormat = new SimpleDateFormat(timestamp);

    }

    @Override
    public void serialize(Date date, JsonGenerator gen, SerializerProvider provider)
            throws IOException, JsonProcessingException {


        String formattedDate = dateFormat.format(date);
        gen.writeString(formattedDate);
    }
}
