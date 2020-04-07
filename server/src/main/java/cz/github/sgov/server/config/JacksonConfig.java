package cz.github.sgov.server.config;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.module.SimpleModule;
import java.io.IOException;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.topbraid.shacl.validation.ValidationReport;

@Configuration
public class JacksonConfig {

  @Bean
  public ObjectMapper objectMapper() {
    ObjectMapper mapper = new ObjectMapper();
    mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    mapper.configure(MapperFeature.DEFAULT_VIEW_INCLUSION, true);
    SimpleModule module = new SimpleModule();
    module.addSerializer(ValidationReport.class, new JsonSerializer<ValidationReport>() {
      @Override
      public void serialize(ValidationReport value, JsonGenerator gen,
                            SerializerProvider serializers) throws IOException {
        gen.writeStartObject();
        gen.writeBooleanField("conforms", value.conforms());
        gen.writeFieldName("results");
        gen.writeStartArray();
        value.results().forEach(r -> {
          try {
            gen.writeStartObject();
            gen.writeStringField("severity", r.getSeverity().getURI());
            gen.writeStringField("message", r.getMessage());
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
    });
    mapper.registerModule(module);
    return mapper;
  }
}