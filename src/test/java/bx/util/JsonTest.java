package bx.util;

import com.google.common.hash.Hashing;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicInteger;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.node.ArrayNode;
import tools.jackson.databind.node.MissingNode;
import tools.jackson.databind.node.NullNode;
import tools.jackson.databind.node.ObjectNode;
import tools.jackson.databind.node.StringNode;

public class JsonTest {

  @Test
  public void testArrayStream() {

    var arr = Json.createArrayNode();

    for (int i = 0; i < 100; i++) {
      arr.add(i);
    }

    AtomicInteger last = new AtomicInteger(-1);
    Json.asStream(arr)
        .forEach(
            it -> {
              Assertions.assertThat(it.asInt()).isGreaterThan(last.intValue());
              last.set(it.asInt());
            });
  }

  @Test
  public void testEmptyStreams() {

    Assertions.assertThat(Json.asStream(NullNode.instance).count()).isEqualTo(0);
    Assertions.assertThat(Json.asStream(MissingNode.getInstance()).count()).isEqualTo(0);
    Assertions.assertThat(Json.asStream(null).count()).isEqualTo(0);
  }

  @Test
  public void testHash() {

    JsonNode n = Json.readTree("null");

    Assertions.assertThat(Json.hash(n))
        .isEqualTo("74234e98afe7498fb5daf1f36ac2d78acc339464f950703b8c019892f982b90b");
    Assertions.assertThat(Json.hash(NullNode.getInstance()))
        .isEqualTo("74234e98afe7498fb5daf1f36ac2d78acc339464f950703b8c019892f982b90b");
    Assertions.assertThat(Json.hash(Json.readTree("")))
        .isEqualTo("74234e98afe7498fb5daf1f36ac2d78acc339464f950703b8c019892f982b90b");
    Assertions.assertThat(Json.hash(Json.readTree("123")))
        .isEqualTo("a665a45920422f9d417e4867efdc4fb8a04a1f3fff1fa07e998e86f7f7a27ae3");

    Assertions.assertThat(Json.hash(Json.readTree("\"hello\"")))
        .isEqualTo("5aa762ae383fbb727af3c7a36d4940a5b8c40a989452d2304fc958ff3f354e7a");

    Assertions.assertThat(Json.hash(Json.readTree("true")))
        .isEqualTo("b5bea41b6c623f7c09f1bf24dcae58ebab3c0cdd90ad966bc43a45b44867e12b");

    String s = "{\"foo\":\"bar\"}";

    String val = Hashing.sha256().hashString(s, StandardCharsets.UTF_8).toString();

    Assertions.assertThat(val)
        .isEqualTo("7a38bf81f383f69433ad6e900d35b3e2385593f76a7b7ab5d4355b8ba41ee24b");

    ObjectNode on = Json.createObjectNode();
    on.put("foo", "bar");

    Assertions.assertThat(Json.hash(on))
        .isEqualTo("7a38bf81f383f69433ad6e900d35b3e2385593f76a7b7ab5d4355b8ba41ee24b");

    tools.jackson.databind.node.ArrayNode an = Json.createArrayNode();

    Assertions.assertThat(Json.hash(an))
        .isEqualTo("4f53cda18c2baa0c0354bb5f9a3ecbe5ed12ab4d8e11ba873c2f11161202b945");

    an.add("foo");
    an.add((String) null);
    an.add(Json.createObjectNode());
    an.add(Json.createArrayNode());
    an.add(123);
    an.add(123.4);
    an.add("bar");

    Assertions.assertThat(Json.hash(an))
        .isEqualTo("2caeec3b3bacb4586be07bc06936bece287d217ec5ac4a41520d936991f30b41");

    var o1 = Json.createObjectNode();
    var o2 = Json.createObjectNode();

    o1.put("a", 1);
    o1.put("b", 2);

    o2.put("b", 2);
    o2.put("a", 1);

    Assertions.assertThat(Json.hash(o1)).isEqualTo(Json.hash(o2));
  }

  @Test
  public void testIt() {
    ObjectNode n = Json.createObjectNode();

    n.put("a", 1);
    n.put("b", 2);
    ObjectNode n2 = Json.createObjectNode();
    n2.put("c", 3);
    n.set("obj", n2);

    Json.traverse(
        n,
        c -> {
          System.out.println(c);
        });
  }

  @Test
  public void testAsObjectNode() {
    ObjectNode n = Json.createObjectNode();
    n.put("a", 1);

    Assertions.assertThat(Json.asObjectNode(n)).isPresent();
    Assertions.assertThat(Json.asObjectNode(n).get()).isSameAs(n);
    Assertions.assertThat(Json.asObjectNode(null)).isEmpty();
    Assertions.assertThat(Json.asObjectNode(MissingNode.getInstance())).isEmpty();
    Assertions.assertThat(Json.asObjectNode(NullNode.getInstance())).isEmpty();
    Assertions.assertThat(Json.asObjectNode(StringNode.valueOf("hello"))).isEmpty();
  }

  @Test
  public void testAsArrayNode() {
    ArrayNode n = Json.createArrayNode();
    n.add("hello");

    Assertions.assertThat(Json.asArrayNode(n)).isPresent();
    Assertions.assertThat(Json.asArrayNode(n).get()).isSameAs(n);
    Assertions.assertThat(Json.asArrayNode(null)).isEmpty();
    Assertions.assertThat(Json.asArrayNode(MissingNode.getInstance())).isEmpty();
    Assertions.assertThat(Json.asArrayNode(NullNode.getInstance())).isEmpty();
    Assertions.assertThat(Json.asArrayNode(StringNode.valueOf("hello"))).isEmpty();
  }

  @Test
  public void testStream() {
    ObjectNode n = Json.createObjectNode();
    n.put("foo", "bar");

    Assertions.assertThat(Json.asStream(n).toList()).hasSize(1);
    Assertions.assertThat(Json.asStream(n).toList()).containsExactly(n);

    Assertions.assertThat(Json.asStream(null).count()).isEqualTo(0);
    Assertions.assertThat(Json.asStream(NullNode.instance).count()).isEqualTo(0);
    Assertions.assertThat(Json.asStream(MissingNode.getInstance()).count()).isEqualTo(0);

    Assertions.assertThat(Json.asStream(StringNode.valueOf("hello")).toList().getFirst().asString())
        .isEqualTo("hello");

    var json =
        """
        [{"n":0},{"n":1}]
        """;

    Assertions.assertThat(Json.asStream(Json.readTree(json)).findFirst().get().path("n").asInt())
        .isEqualTo(0);
    Assertions.assertThat(Json.asStream(Json.readTree(json)).toList().get(1).path("n").asInt())
        .isEqualTo(1);
  }
}
