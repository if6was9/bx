package bx.unirest;

import bx.util.BxException;
import bx.util.Json;
import kong.unirest.core.GenericType;
import kong.unirest.core.JsonNode;
import kong.unirest.core.ObjectMapper;
import kong.unirest.core.Unirest;
import kong.unirest.core.UnirestInstance;

public class UnirestJacksonObjectMapper implements ObjectMapper {

  tools.jackson.databind.ObjectMapper mapper = Json.mapper();

  @Override
  public <T> T readValue(String value, Class<T> valueType) {

    if (tools.jackson.databind.JsonNode.class.isAssignableFrom(valueType)) {
      return (T) mapper.readTree(value);
    } else if (String.class.isAssignableFrom(valueType)) {
      return (T) value;
    }
    throw new BxException("unsupported type: " + valueType);
  }

  @Override
  public <T> T readValue(String value, GenericType<T> genericType) {

    if (JsonNode.class.isAssignableFrom(genericType.getTypeClass())) {
      return (T) mapper.readTree(value);
    } else if (String.class.isAssignableFrom(genericType.getTypeClass())) {
      return (T) value;
    }
    throw new BxException("unsupported type: " + genericType);
  }

  @Override
  public String writeValue(Object value) {

    if (value instanceof tools.jackson.databind.JsonNode) {
      return mapper.writeValueAsString(value);
    }
    throw new BxException("unsupported type: " + value.getClass());
  }

  public static void resiter(UnirestInstance instance) {
    instance.config().setObjectMapper(new UnirestJacksonObjectMapper());
  }

  public static void register() {
    Unirest.config().setObjectMapper(new UnirestJacksonObjectMapper());
  }
}
