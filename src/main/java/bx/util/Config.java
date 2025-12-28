package bx.util;

import com.google.common.base.Suppliers;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;
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

  AtomicReference<Map<String, String>> mergedRef = new AtomicReference<Map<String, String>>();

  ThreadLocal<Boolean> reenter = new ThreadLocal<Boolean>();
  List<Supplier<Map<String, String>>> suppliers = Lists.newArrayList();
  Supplier<String> appNameSupplier = Suppliers.memoize(this::findAppName);

  class CurrentDirSupplier implements Supplier<Map<String, String>> {

    @Override
    public Map<String, String> get() {

      File f = new File(".", "config.yml");
      if (f.exists()) {
        JsonNode n = yamlMapper.readTree(f);
        logger.atTrace().log("loading {}", f);
        return toMap(n);
      } else {
        logger.atTrace().log("not found: {}", f);
      }
      return Map.of();
    }
  }

  class EnvVarSupplier implements Supplier<Map<String, String>> {

    public Map<String, String> get() {
      return System.getenv();
    }
  }

  class SysPropSupplier implements Supplier<Map<String, String>> {

    public Map<String, String> get() {
      Map<String, String> m = Maps.newHashMap();
      Properties sp = System.getProperties();
      sp.forEach(
          (k, v) -> {
            m.put((String) k, (String) v);
          });
      return m;
    }
  }

  class HomeDirSupplier implements Supplier<Map<String, String>> {

    @Override
    public Map<String, String> get() {

      String app = findAppName();
      File dir = new File(System.getProperty("user.home"), String.format(".%s", app));
      File f = new File(dir, "config.yml");
      if (f.exists()) {
        JsonNode n = yamlMapper.readTree(f);
        logger.atTrace().log("loading {}", f);
        return toMap(n);
      } else {
        logger.atDebug().log("not found: {}", f);
      }
      return Map.of();
    }
  }

  public Config() {
    this(new HashMap<String, String>());
    reset();
  }

  public void reset() {
    suppliers.clear();
    this.reenter.set(null);
    this.mergedRef.set(null);

    this.appNameSupplier = Suppliers.memoize(this::findAppName);
    suppliers = Lists.newArrayList();
    suppliers.add(
        () -> {
          return overrides;
        });
    suppliers.add(new EnvVarSupplier());
    suppliers.add(new SysPropSupplier());
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

  private Map<String, String> merge() {
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

    Map<String, String> merged = mergedRef.get();
    if (merged != null) {
      return merged;
    }

    merged = merge();
    this.mergedRef.set(merged);

    return merged;
  }

  public Set<String> getKeys() {

    return getMergedProperties().keySet();
  }

  public String getAppName() {

    return appNameSupplier.get();
  }

  private String findAppName() {

    Boolean b = reenter.get();

    if (b == null || b == false) {
      reenter.set(true);
      try {
        for (Supplier<Map<String, String>> s : suppliers) {
          Map<String, String> m = s.get();
          if (m != null) {
            String appName = m.get("app.name");

            if (S.isNotBlank(appName)) {
              return appName;
            }
            appName = m.get("APP_NAME");
            if (S.isNotBlank(appName)) {
              return appName;
            }
          }
        }
      } finally {
        reenter.set(null);
      }
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

  public void override(String name, String val, boolean reset) {

    this.overrides.put(name, val);
    if (reset) {
      reset();
    }
  }
}
