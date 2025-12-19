package bx.util;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Supplier;
import org.slf4j.Logger;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.node.ObjectNode;
import tools.jackson.dataformat.yaml.YAMLMapper;

public class Config {

  static Logger logger = Slogger.forEnclosingClass();
  private YAMLMapper yamlMapper = new YAMLMapper();

  static Config instance;

  static {
    instance = new Config();
  }

  Map<String, String> overrides = Maps.newHashMap();
  Map<String, Map<String, String>> merged = Maps.newHashMap();

  List<Supplier<Map<String, String>>> suppliers = Lists.newArrayList();

  class CurrentDirSupplier implements Supplier<Map<String, String>> {

    @Override
    public Map<String, String> get() {

      File f = new File(".", "config.yml");
      if (f.exists()) {
        JsonNode n = yamlMapper.readTree(f);
        logger.atInfo().log("loading {}", f);
        return toMap(n);
      } else {
        logger.atInfo().log("not found: {}", f);
      }
      return Map.of();
    }
  }

  class HomeDirSupplier implements Supplier<Map<String, String>> {

    @Override
    public Map<String, String> get() {

      String app = getAppName();
      File dir = new File(System.getProperty("user.home"), String.format(".%s", app));
      File f = new File(dir, "config.yml");
      if (f.exists()) {
        JsonNode n = yamlMapper.readTree(f);
        logger.atInfo().log("loading {}", f);
        return toMap(n);
      } else {
        logger.atInfo().log("not found: {}", f);
      }
      return Map.of();
    }
  }

  public Config() {
    this(Map.of());
    setupSupplierChain();
  }

  public void setupSupplierChain() {
    suppliers = Lists.newArrayList();
    suppliers.add(
        () -> {
          return overrides;
        });
    suppliers.add(
        () -> {
          return System.getenv();
        });
    suppliers.add(new CurrentDirSupplier());
    suppliers.add(new HomeDirSupplier());
  }

  public Config(Map<String, String> props) {
    if (props == null) {
      props = Map.of();
    }
    this.overrides = props;
  }

  public static Config get() {
    return instance;
  }

  public Map<String, String> merge() {
    Map<String, String> merged = Maps.newHashMap();
    suppliers
        .reversed()
        .forEach(
            m -> {
              merged.putAll(m.get());
            });
    return Map.copyOf(merged);
  }

  public synchronized Map<String, String> getMergedProperties() {
    String name = getAppName();
    Map<String, String> merged = this.merged.get(name);
    if (merged == null) {
      merged = merge();
      this.merged.put(name, merged);
    }
    return merged;
  }

  public Set<String> getKeys() {

    return getMergedProperties().keySet();
  }

  public String getAppName() {
    String name = System.getProperty("app.name");
    if (S.isNotBlank(name)) {
      return name;
    }
    name = System.getenv("APP_NAME");
    if (S.isNotBlank(name)) {
      return name;
    }
    return "bx";
  }

  private Map<String, String> toMap(JsonNode n) {

    if (n == null) {
      return Map.of();
    }
    Map<String, String> m = Maps.newHashMap();
    if (!n.isObject()) {
      return Map.of();
    }
    ObjectNode on = (ObjectNode) n;
    on.propertyNames()
        .forEach(
            p -> {
              JsonNode val = on.path(p);
              if (val.isValueNode()) {
                m.put(p, val.asString(""));
              }
            });
    return m;
  }

  public Optional<String> get(String key) {

    if (key == null) {
      return Optional.empty();
    }

    return S.notBlank(getMergedProperties().get(key));
  }
}
