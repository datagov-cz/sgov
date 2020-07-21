package com.github.sgov.server.controller.util;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import java.io.IOException;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.topbraid.shacl.validation.ValidationReport;

/**
 * Serializes the SHACL validation report to JSON.
 */
public class ValidationReportSerializer extends JsonSerializer<ValidationReport> {

    public ValidationReportSerializer() {
    }

    private String getLang() {
        return ((ServletRequestAttributes) RequestContextHolder
            .getRequestAttributes()).getRequest().getLocale().toLanguageTag();
    }

    @Override
    public void serialize(ValidationReport value, JsonGenerator gen,
                          SerializerProvider serializers) throws IOException {
        gen.writeStartObject();
        gen.writeBooleanField("conforms", value.conforms());
        gen.writeFieldName("results");
        gen.writeStartArray();
        value.results().forEach(r -> {
            try {
                final StringBuilder sb = new StringBuilder();
                r.getMessages().forEach(n -> {
                    if (getLang().startsWith(n.asLiteral().getLanguage())) {
                        sb.append(n);
                    }
                });
                gen.writeStartObject();
                gen.writeStringField("severity", r.getSeverity().getURI());
                gen.writeStringField("message", sb.toString());
                gen.writeStringField("focusNode", r.getFocusNode().toString());
                //                        gen.writeStringField("value", r.getValue()
                //                        .toString());
                gen.writeEndObject();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        gen.writeEndArray();
        gen.writeEndObject();
    }
}

