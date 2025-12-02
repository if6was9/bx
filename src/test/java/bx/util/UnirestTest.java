package bx.util;

import bx.unirest.UnirestJacksonObjectMapper;
import kong.unirest.core.Unirest;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.node.ObjectNode;

public class UnirestTest {

  @Test
  public void testIt() {

    Unirest.config().reset(true);
    var r =
        Unirest.get("https://accounts.google.com/.well-known/openid-configuration")
            .asObject(JsonNode.class);
    Assertions.assertThat(r.getBody()).isNull();

    UnirestJacksonObjectMapper.register();
    r =
        Unirest.get("https://accounts.google.com/.well-known/openid-configuration")
            .asObject(JsonNode.class);
    Assertions.assertThat(r.getBody().path("issuer").asString())
        .isEqualTo("https://accounts.google.com");

    r =
        Unirest.get("https://accounts.google.com/.well-known/openid-configuration")
            .asObject(ObjectNode.class);
    Assertions.assertThat(r.getBody().path("issuer").asString())
        .isEqualTo("https://accounts.google.com");

    var rs =
        Unirest.get("https://accounts.google.com/.well-known/openid-configuration")
            .asObject(String.class);

    var json = Json.readTree(rs.getBody());

    Assertions.assertThat(json.path("issuer").asString()).isEqualTo("https://accounts.google.com");
  }
}
