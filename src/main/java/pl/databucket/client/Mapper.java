package pl.databucket.client;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import java.text.SimpleDateFormat;

public class Mapper {

    public static final String DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSSZ";

    public static ObjectMapper objectMapper = (new ObjectMapper())
            .registerModule(new JavaTimeModule())
            .setDateFormat(new SimpleDateFormat(DATE_FORMAT))
            .configure(DeserializationFeature.READ_UNKNOWN_ENUM_VALUES_AS_NULL, true)
            .setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY);

}
